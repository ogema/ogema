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

/**
 * Each application an instance of this class is registered. The permissions
 * for the access to the web resources of an application is checked in handleSecurity().
 * All of the active sessions information are corresponding to this context are managed by this class.
 * For each request http service calls
 * handleSecurity, where the authorization for calling web resources of a
 * particular app will be checked.
 *
 */
import static org.ogema.accesscontrol.Constants.OTPNAME;
import static org.ogema.accesscontrol.Constants.OTUNAME;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.SessionAuth;
import org.ogema.accesscontrol.Util;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.logging.LoggerFactory;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;

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

	private final PermissionManager pm;
	static final Boolean httpEnable;
	private static final boolean xservletEnable;

	AppID owner;

	LoggerFactory factory;

	// note: the following three maps are actually concurrent hash maps, but there is a strange incompatibility with
	// Java 7 when building this in a Java 8 environment, if one declares the maps to be ConcurrentHashMap (here: error on shutdown)
	// see https://stackoverflow.com/questions/32954041/concurrenthashmap-crashing-application-compiled-with-jdk-8-but-targeting-jre-7

	// alias vs. resource path
	final Map<String, String> resources;
	// alias vs. app id
	final Map<String, String> servlets;

	final Map<String, String> sessionsOtpqueue;

	private final static Logger logger = org.slf4j.LoggerFactory.getLogger(OgemaHttpContext.class.getName());
	private final AccessManager accessMngr;

	// loopback addresses and host names to determine if a request comes from the loopback interface
	private static final Set<String> loopbackAddresses = new HashSet<>();

	static {
		loopbackAddresses.add("127.0.0.1");
		loopbackAddresses.add("0:0:0:0:0:0:0:1");
		try {
			for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if (nif.isLoopback()) {
					for (InetAddress ia : Collections.list(nif.getInetAddresses())) {
						loopbackAddresses.add(ia.getHostAddress());
						loopbackAddresses.add(ia.getHostName());
					}
				}
			}
		} catch (SocketException se) {
			logger.error("could not determine loopback addresses", se);
		}
	}

	public OgemaHttpContext(PermissionManager pm, AppID app) {
		Objects.requireNonNull(pm);
		Objects.requireNonNull(app);
		this.resources = new ConcurrentHashMap<>();
		this.servlets = new ConcurrentHashMap<>();
		this.sessionsOtpqueue = new ConcurrentHashMap<>();
		this.pm = pm;
		this.accessMngr = pm.getAccessManager();
		this.owner = app;
	}

	public void close() {
		// note: this does not prevent the alias to be unregisterd from the http service, which is taken care
		// of by ApplicationWebAccessManager and ApplicationTracker
		resources.clear();
		servlets.clear();
		sessionsOtpqueue.clear();
	}

	public final ThreadLocal<HttpSession> requestThreadLocale = new ThreadLocal<>();

	static boolean isLoopbackAddress(String address) {
		return loopbackAddresses.contains(address);
	}

	/*
	 * handleSecurity is called by httpService in both cases if the request is targeting a web resource or a servlet.
	 */
	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String currenturi = request.getRequestURI();
		final String info = request.getPathInfo();
		if (Configuration.DEBUG && logger.isDebugEnabled()) {
			StringBuffer url = request.getRequestURL();
			String servletpath = request.getServletPath();
			String query = request.getQueryString();
			String trans = request.getPathTranslated();
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
		// System.out.println("Testing on non-secure allow: httpEnable:"+httpEnable+"
		// remoteAddr:"+request.getRemoteAddr()+" scheme:"+scheme);
		if (!httpEnable && (!isLoopbackAddress(request.getRemoteAddr()) && !scheme.equals("https"))) {
			logger.error("\tSecure connection is required.");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getOutputStream().write("\tSecure connection is required.".getBytes());
			response.flushBuffer();
			return false;
		}

		HttpSession httpses = request.getSession();
		SessionAuth sesAuth;

		logger.debug("SessionID: {}", httpses.getId());
		logger.debug("HTTP-Referer: {}", request.getHeader("Referer"));
		if ((sesAuth = (SessionAuth) httpses.getAttribute(SessionAuth.AUTH_ATTRIBUTE_NAME)) == null) {
			// Store the request, so that it could be responded after successful login.
			if (!request.getRequestURL().toString().endsWith("favicon.ico")) {
				if (request.getRequestURI().equals("/ogema") || request.getRequestURI().equals("/ogema/")) {
					httpses.setAttribute(LoginServlet.OLDREQ_ATTR_NAME, "/ogema/index.html");
					logger.debug("Saved old request URI -> default page");
				}
				else {
					/*
					 * for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements(); ){ String header =
					 * e.nextElement(); System.out.printf("%s: %s%n", header, request.getHeader(header)); }
					 */
					StringBuilder requestPathAndQuery = new StringBuilder(request.getContextPath());
					requestPathAndQuery.append(request.getServletPath());
					if (request.getPathInfo() != null) {
						requestPathAndQuery.append(request.getPathInfo());
					}
					if (request.getQueryString() != null) {
						requestPathAndQuery.append("?").append(request.getQueryString());
					}
					String oldReq = requestPathAndQuery.toString();
					httpses.setAttribute(LoginServlet.OLDREQ_ATTR_NAME, oldReq);
					logger.debug("Saved old request URI -> {}", oldReq);
				}
			}

			try {
				if (Configuration.DEBUG)
					logger.debug("New Session is forwarded to Login page.");
				request.getRequestDispatcher(LoginServlet.LOGIN_SERVLET_PATH).forward(request, response);
			} catch (ServletException e) {
				logger.error(this.getClass().getSimpleName(), e);
			} catch (IOException e) {
				logger.error(this.getClass().getSimpleName(), e);
			} catch (IllegalStateException e) {
				logger.debug(
						"HttpRequestForwarding caused an IllegalStateException. It could be caused by the bad handling of the OutputStream status");
			}
			return false;
		}
		else {
			if (Configuration.DEBUG)
				logger.debug("Known Session detected.");
		}

		/*
		 * Satisfaction of APP-SEC 16
		 */
		if (xservletEnable) {
			// check if its a servlet request. In this case only the one time password is to be checked.
			/*
			 * Get The authentication information
			 */
			String usr = request.getParameter(OTUNAME);
			String pwd = request.getParameter(OTPNAME);

			String key = servlets.get(currenturi);
			if (key == null)
				key = Util.startsWithAnyKey(servlets, currenturi);
			if (key != null) {
				// a web page may be generated dynamically in a servlet
				if (currenturi.endsWith(".html") && request.getMethod().equals("GET"))
					return true;
				if (usr == null)
					return false;
				return pm.getWebAccess().authenticate(httpses, usr, pwd);
			}
		}

		/*
		 * Set HttpOnly and secure flags which helps mitigate the client side XSS attacks accessing the session cookie.
		 */
		String sessionid = httpses.getId();
		response.addHeader("SET-COOKIE", "JSESSIONID=" + sessionid + ";HttpOnly");

		// Look for access right of the user to the app sites according this http context.
		String usrName = sesAuth.getName();
		boolean permitted = false;
		try {
			permitted = accessMngr.isAppPermitted(usrName, owner);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (!permitted) {
			String message = "User " + usrName + " is not permitted to access to " + request.getPathInfo();
			// request.getSession().invalidate(); // why invalidate the session, because the access is not authorized only.
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);

			if (Configuration.DEBUG)
				logger.debug(message);
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

	/*
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

		String key = Util.startsWithAnyValue(resources, name);

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
		String otp = registerOTP(owner, sesid);
		if (otp == null)
			return null;

		try {
			url = new URL("ogema", owner.getIDString(), 0, name, new RedirectionURLHandler(owner, name, otp));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return url;
	}

	private String registerOTP(AppID app, HttpSession ses) {
		SessionAuth auth = (SessionAuth) ses.getAttribute(SessionAuth.AUTH_ATTRIBUTE_NAME);
		if (auth == null)
			return null;
		String otp = auth.registerAppOtp(app);
		return otp;
	}

	@Override
	public String getMimeType(String name) {
		// MimeType of the default HttpContext will be used.
		return null;
	}

	static {
		httpEnable = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

			@Override
			public Boolean run() {
				return Boolean.getBoolean("org.ogema.non-secure.http.enable");
			}
		});
		/*
		 * Switch to enable/disable servlet access restrictions after APP-SEC 16: Access to a web resource
		 * out of a previously downloaded web page shall only be granted, if the requested web resource is a
		 * static content or the dynamic content (servlet) is registered by the same app as the source of
		 * the web page.
		 */
		xservletEnable = System.getSecurityManager() != null && AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

			@Override
			public Boolean run() {
				return Boolean.getBoolean("org.ogema.xservletacces.enable");
			}
		});
	}
}
