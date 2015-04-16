/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.impl.security;

import static org.ogema.impl.security.WebAccessManagerImpl.OLDREQ_ATTR_NAME;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.SessionAuth;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.logging.LoggerFactory;
import org.osgi.service.http.HttpContext;
import org.osgi.service.useradmin.User;
import org.slf4j.Logger;

/**
 * Each application an instance of this class is registered. The permissions
 * for the access to the web resources of an application is checked in handleSecurity().
 * All of the active sessions information are corresponding to this context are managed by this class.
 * For each request http service calls
 * handleSecurity, where the authorization for calling web resources of a
 * particular app will be checked.
 * 
 */

/**
 * Parts of the URI can be reached via HttpServletRequest by calling the following methods:
 * /applicationID/directory/file.ext/info getRequestURI() /applicationID getContextPath() /directory/file.ext
 * getServletPath() /info getPathInfo()
 * 
 */
/**
 * @author Zekeriya Mansuroglu
 *
 */
public class OgemaHttpContext implements HttpContext {

	private WebAccessManagerImpl wam;
	private PermissionManager pm;
	boolean securemode = true;
	AppID owner;

	LoggerFactory factory;

	// alias vs. app id
	LinkedHashMap<String, String> resources;
	// alias vs. app id
	HashMap<String, AppID> servlets;

	ConcurrentHashMap<String, String> sessionsOtpqueue;

	private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	public OgemaHttpContext(PermissionManager pm, AppID app) {
		this.resources = new LinkedHashMap<>();
		this.servlets = new HashMap<>();
		this.sessionsOtpqueue = new ConcurrentHashMap<>();

		this.wam = (WebAccessManagerImpl) pm.getWebAccess();
		this.pm = pm;
		this.owner = app;
		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				// For development purposes secure mode could be disabled
				if (System.getProperty("org.ogema.secure.httpcontex", "true").equals("true"))
					securemode = true;
				else
					securemode = false;
				return null;
			}
		});
	}

	public final ThreadLocal<HttpSession> requestThreadLocale = new ThreadLocal<>();

	/**
	 * handleSecurity is called by httpService in both cases if the request is targeting a web resource or a servlet.
	 */
	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String currenturi = request.getRequestURI();
		StringBuffer url = request.getRequestURL();
		String servletpath = request.getServletPath();
		String info = request.getPathInfo();
		String query = request.getQueryString();
		String trans = request.getPathTranslated();

		if (Configuration.DEBUG) {
			logger.debug("Current URI: " + currenturi);
			logger.debug("Current URL: " + url);
			logger.debug("Servlet path: " + servletpath);
			logger.debug("Path info: " + info);
			logger.debug("Query String: " + query);
			logger.debug("Path Translated: " + trans);
		}
		/*
		 * If the request requires a secure connection and the getScheme method in the request does not return 'https'
		 * or some other acceptable secure protocol, then this method should set the status in the response object to
		 * Forbidden(403) and return false.
		 */
		String scheme = request.getScheme();
		if (!scheme.equals("https")) {
			// FIXME: why not redirecting to https?
			logger.error("\tSecure connection is required.");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getOutputStream().write("\tSecure connection is required.".getBytes());
			response.flushBuffer();
			return false;
		}

		HttpSession httpses = request.getSession();
		SessionAuth ses;
		if ((ses = (SessionAuth) httpses.getAttribute(SessionAuth.AUTH_ATTRIBUTE_NAME)) == null) {
			
			 // Store the request, so that it could be responded after successful login.
            if (!request.getRequestURL().toString().endsWith("favicon.ico")
            		&& !request.getRequestURI().toString().equals("/ogema")) {
                httpses.setAttribute(OLDREQ_ATTR_NAME, request.getRequestURL().toString());
            }

			try {
				request.getRequestDispatcher(LoginServlet.LOGIN_SERVLET_PATH).forward(request, response);
			} catch (ServletException e) {
				logger.error(this.getClass().getSimpleName(), e);
				return false;
			}
		}
		// If we get a session object the user was successfully authenticated.
		if (ses == null) {
			if (Configuration.DEBUG)
				logger.debug("User authentication failed.");
			return false;
		}
		else {
			if (Configuration.DEBUG)
				logger.debug("User authentication successful.");
		}

		// Look for access right of the user to the app sites according this http context.
		User usr = ses.getUsr();
		boolean permitted = false;
		try {
			permitted = pm.getAccessManager().isAppPermitted(usr, owner);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (!permitted) {
                        
                        request.getSession().invalidate();
                        response.sendRedirect("/ogema/login");
                        
			if (Configuration.DEBUG)
				logger.debug("User authorization failed.");
			return false;
		}
		else {
			if (Configuration.DEBUG)
				logger.debug("User authorization successful.");
			String id = httpses.getId();
			// If a resource and not a servlet is requested
			// the session infos are to be hashed to register an otp session tupel.
			if (info != null) {
				int mimeIdx = info.lastIndexOf('.');
				if (mimeIdx != -1) {
					String mime = info.substring(mimeIdx + 1);
					switch (mime) {
					case "html":
					case "htm":
					case "mhtm":
					case "mhtml":
						sessionsOtpqueue.put(currenturi, id);
						requestThreadLocale.set(httpses);
						break;
					default:
						requestThreadLocale.set(null);
						break;
					}
				}
			}
			return true;
		}
	}

	/**
	 * Called by the Http Service to map a resource name to a URL. For servlet contextRegs, Http Service will call this
	 * method to support the ServletContext methods getResource and getResourceAsStream. For resource contextRegs, Http
	 * Service will call this method to locate the named resource. The context can control from where resources come.
	 * For example, the resource can be mapped to a file in the bundle's persistent storage area via
	 * bundleContext.getDataFile(name).toURL() or to a resource in the context's bundle via getClass().getResource(name)
	 * 
	 * Each web page potentially contains AJAX requests to the rest server. Therefore the link to the requested web page
	 * will be embedded in to a html snippet that contains one time authentication information. This information are
	 * valid until the page is reloaded or the session is expired. To distinguish between the first request and the
	 * request out of the redirected html snippet the parameter named OGEMAREDIR is added to the redirected link. The
	 * session handling is already done at this point.
	 */
	@Override
	public URL getResource(String name) {
		// HACK Note that the jetty server calls this method with an relative path that doesn't start with '/'. For this
		// case we add it as prefix before creating an URL.
		if (name.charAt(0) != '/')
			name = "/" + name;
		if (Configuration.DEBUG)
			logger.debug("getResource: " + name);
		Application app = owner.getApplication();
		URL url = null;
		String key = null, value = null;

		Set<Entry<String, String>> entries = resources.entrySet();
		for (Entry<String, String> e : entries) {
			value = e.getValue();
			if (name.startsWith(value)) { // FIXME name has to start with value+"/"
				key = e.getKey();
				break;
			}
		}

		HttpSession sesid = null;
		if (key != null) {
			// Check if there request for this page registered as which is a candidate for an otp.
			// sesid = sessionsOtpqueue.get(info);
			// If this method is called before the first call to handlesecurity no session was registered yet. In this
			// case
			// we return just a dummy URL.
			sesid = requestThreadLocale.get();
		}

		// this should only return the bundles jar resources
		// permissions are checked inside by OSGi FW
		if (sesid == null)
			return app.getClass().getResource(name);

		// Get the corresponding session authorization
		String otp = wam.registerOTP(owner, sesid);
		if (otp == null)
			return null;

		try {
			url = new URL("ogema", owner.getIDString(), 0, name, new RedirectionURLHandler(owner, name, otp));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	}

	@Override
	public String getMimeType(String name) {
		// MimeType of the default HttpContext will be used.
		return null;
	}
}
