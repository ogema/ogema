/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.Constants;
import org.ogema.accesscontrol.HttpConfig;
import org.ogema.accesscontrol.HttpConfigManagement;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.SessionAuth;
import org.ogema.applicationregistry.ApplicationRegistry;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.security.WebAccessManager;
import org.ogema.util.Util;
import org.ogema.webadmin.AdminWebAccessManager;
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

	private static final String SESSION_COOKIE_PROP = "org.eclipse.jetty.servlet.SesionCookie";
	static final Boolean httpEnable;
	// private static final boolean xservletEnable;
	
	private final String cookie;
	
	final AppID owner;

	// LoggerFactory factory;

	// note: the following four maps are actually concurrent hash maps, but there is a strange incompatibility with
	// Java 7 when building this in a Java 8 environment, if one declares the maps to be ConcurrentHashMap (here: error
	// on shutdown)
	// see
	// https://stackoverflow.com/questions/32954041/concurrenthashmap-crashing-application-compiled-with-jdk-8-but-targeting-jre-7

	// alias vs. resource path
	final Map<String, String> resources;
	// alias vs. app id
	final Map<String, String> servlets;

	final Map<String, String> sessionsOtpqueue;

	// alias vs registration; synchronized on this; use {@link #getStaticRegistrations()} to create the map
	volatile Map<String, AdminWebAccessManager.StaticRegistration> staticRegistrations;
	// aliases; subset of resourcse key set; synchronized on resources
	volatile Collection<String> nonOtpResources;

	private final static Logger logger = org.slf4j.LoggerFactory.getLogger(OgemaHttpContext.class.getName());
	private final AccessManager accessMngr;
	private final ApplicationRegistry appReg;

	private final PermissionManager permMan;
	private final AtomicReference<HttpConfigManagement> httpConfigRef;

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

	public OgemaHttpContext(final PermissionManager pm, final AppID app, 
			AtomicReference<HttpConfigManagement> httpConfigRef) {
		Objects.requireNonNull(pm);
		Objects.requireNonNull(app);
		this.resources = new ConcurrentHashMap<>(3);
		this.servlets = new ConcurrentHashMap<>(3);
		this.sessionsOtpqueue = new ConcurrentHashMap<>();
		this.appReg = pm.getApplicationRegistry();
		this.accessMngr = pm.getAccessManager();
		this.owner = app;
		this.permMan = pm;
		this.httpConfigRef = httpConfigRef;
		this.cookie = getProperty(app, SESSION_COOKIE_PROP, "JSESSIONID");
	}
	
	private static String getProperty(final AppID app, final String property, final String defaultValue) {
		final String take1 = AccessController.doPrivileged(new PrivilegedAction<String>() {

			@Override
			public String run() {
				return app.getBundle().getBundleContext().getProperty(property);
			}
		});
		return take1 != null ? take1 : defaultValue;
	}

	public void close() {
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
		if (Configuration.DEBUG && logger.isTraceEnabled()) {
			StringBuffer url = request.getRequestURL();
			String servletpath = request.getServletPath();
			String query = request.getQueryString();
			String trans = request.getPathTranslated();
			logger.trace("Current URI: " + currenturi);
			logger.trace("Current URL: " + url);
			logger.trace("Servlet path: " + servletpath);
			logger.trace("Path info: " + info);
			logger.trace("Query String: " + query);
			logger.trace("Path Translated: " + trans);
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
		if ((sesAuth = (SessionAuth) httpses.getAttribute(Constants.AUTH_ATTRIBUTE_NAME)) == null) {
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
				// if we did not check for the method, then open browser tabs that periodically POST data would lead to invalid login requests
				// and the user getting blocked...
				if ("GET".equals(request.getMethod())) {
					if (Configuration.DEBUG)
						logger.debug("New Session is forwarded to Login page.");
					request.getRequestDispatcher(LoginServlet.LOGIN_SERVLET_PATH).forward(request, response);
				}
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
		final HttpConfigManagement httpConfigs = httpConfigRef.get();
		final HttpConfig httpConfig = httpConfigs == null ? null : httpConfigs.getConfig(owner.getBundle());
		if (httpConfig != null) {
			final String allowedOrigin = httpConfig.getAllowedOrigin(request);
			if (allowedOrigin != null) {
				String clientOrigin = request.getHeader("origin");
				if (allowedOrigin.equals("*")) {
					if (clientOrigin != null)
						response.addHeader("Access-Control-Allow-Origin", clientOrigin);
				} 
				else
					response.addHeader("Access-Control-Allow-Origin", allowedOrigin);
				if (httpConfig.isAllowCredentials(request))
					response.addHeader("Access-Control-Allow-Credentials", "true");
				final String allowedHeaders = httpConfig.getAllowedHeaders(request);
				if (allowedHeaders != null)
					response.addHeader("Access-Control-Allow-Headers", allowedHeaders);
			}
			if (httpConfig.getCustomHeaders(request) != null) {
				for (Map.Entry<String, String> h: httpConfig.getCustomHeaders(request).entrySet()) {
					response.addHeader(h.getKey(), h.getValue());
				}
			}
		}
		/*
		 * Satisfaction of APP-SEC 16
		 */
		// 1. Check if it is a servlet query
		/*
		String key = servlets.get(currenturi);
		if (key == null)
			key = Util.startsWithAnyKey(servlets, currenturi); // FIXME safe? Case of overlapping servlet/static registrations?
			*/
		// 1.1 If not skip further checks related to OTP
		boolean result = true;
		if (isServletAndNonStatic(currenturi, resources, servlets, staticRegistrations)) {
			// 2. Determine the App that owns the servlet (it's the field value owner)
			AppID servletOwner = owner;
			// 3. Get the app that owns the referrer of the servlet
			String usr = request.getParameter(OTUNAME);
			String pwd = request.getParameter(OTPNAME);
			// 3.1 if its a servlet request and no one time credentials are present, than no access is permitted
			AppID urlOwner = null;
			if (usr != null) {
				// 3.2 the OTP should match, if not we assume that urlOwner doesn't have the permission for this servlet
				// access
				AdminApplication app = appReg.getAppById(usr);
				final WebAccessManager wam = app != null ? permMan.getWebAccess(app.getID()) : null;
				if (wam == null) { // happens when the bundle gets updated -> need to refresh the session 
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					return false;
				}
				result = wam.authenticate(httpses, usr, pwd);
				urlOwner = app.getID();
			}
			else
				result = false;
			// 4. Compare both apps, if they don't match the urlOwner app needs WebAccessPermission to the app that owns
			// the servlet.
			if (result && !servletOwner.equals(urlOwner)) {
				result = permMan.checkWebAccess(urlOwner, servletOwner);
			}
		}
		if (!result) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"Servlet access unauthorized. Check your one time credentials.");
			return false;
		}

		/*
		 * Set HttpOnly and secure flags which helps mitigate the client side XSS attacks accessing the session cookie.
		 */
		String sessionid = httpses.getId();
		response.addHeader("SET-COOKIE", cookie + "=" + sessionid + ";HttpOnly");

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
			accessMngr.setCurrentUser(usrName);
			if (Configuration.DEBUG)
				logger.debug("User authorization successful.");
			String id = httpses.getId();
			// If a resource and not a servlet is requested
			// the session infos are to be hashed to register an otp session tupel.
			String info0 = null;
			if (currenturi != null) {
				final Map<String,String> staticUriRedirects = httpConfig == null ? null : httpConfig.getUriRedirects();
				if (staticUriRedirects != null) {
					for (String k : staticUriRedirects.keySet()) { 
						if (currenturi.startsWith(k)) {
							info0 = staticUriRedirects.get(k);
							break;
						}
					}
				}
			}
			final boolean isRedirectTarget = info0 != null;
			info0 = isRedirectTarget ? info0 : info;
			if (info0 != null) {
				int mimeIdx = info0.lastIndexOf('.');
				if (mimeIdx != -1) {
					String mime = info0.substring(mimeIdx + 1);
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
	
	private static Map.Entry<String, String> getBestMatchingEntry(final String uri, final Map<String, String> map) {
		Map.Entry<String, String> result = null;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (uri.startsWith(entry.getKey() + "/")) {
				if (result == null || result.getKey().length() < entry.getKey().length())
					result = entry;
			}
		}
		return result;
	}
	
	/**
	 * @param uri
	 * @param resources
	 * @param servlets
	 * @param staticRegistrations
	 * 		may be null
	 * @return
	 */
	private static boolean isServletAndNonStatic(final String uri, final Map<String, String> resources, 
				final Map<String, String> servlets, final Map<String, AdminWebAccessManager.StaticRegistration> staticRegistrations) {
		final String value0 = servlets.get(uri);
		if (value0 != null) 
			return staticRegistrations == null || !staticRegistrations.containsKey(uri);
		if (resources.containsKey(uri))
			return false;
		final Map.Entry<String, String> bestMatchingServlet = getBestMatchingEntry(uri, servlets);
		if (bestMatchingServlet == null)
			return false;
		final Map.Entry<String, String> bestMatchingResource = getBestMatchingEntry(uri, resources);
		if (bestMatchingResource == null || bestMatchingResource.getKey().length() < bestMatchingServlet.getKey().length()) {
			final boolean isStatic = staticRegistrations != null && staticRegistrations.containsKey(bestMatchingServlet.getKey());
			return !isStatic;
		}
		return false;
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
			logger.debug("getResource: {}", name);
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
			if (!isNonOtpResource(key))
				sesid = requestThreadLocale.get();
		}
		boolean isRedirect = false;
		final HttpConfigManagement httpConfigs = httpConfigRef.get();
		final Map<String,String> staticRedirects = httpConfigs == null ? null : httpConfigs.getConfig(owner.getBundle()).getRedirects();
		if (staticRedirects != null) {
			final URL url0 = app.getClass().getResource(name);
			for (String k : staticRedirects.keySet()) { 
				if (name.startsWith(k)) {  			// k ends with "/"
					if (url0 == null)
						name = staticRedirects.get(k);
					isRedirect = true;
					break;
				}
			}
		}

		// this should only return the bundles jar resources
		// permissions are checked inside by OSGi FW
		if (sesid == null || (isRedirect && !name.toLowerCase().endsWith(".html") && !name.toLowerCase().equals(".htm"))) {
			return app.getClass().getResource(name);
		}
		
		url = owner.getOneTimePasswordInjector(name, sesid);
		return url;
	}

	private String registerOTP(AppID app, HttpSession ses) {
		SessionAuth auth = (SessionAuth) ses.getAttribute(Constants.AUTH_ATTRIBUTE_NAME);
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

	private synchronized Map<String, AdminWebAccessManager.StaticRegistration> getStaticRegistrations() {
		if (staticRegistrations == null)
			staticRegistrations = new ConcurrentHashMap<>(3);
		return staticRegistrations;
	}

	synchronized AdminWebAccessManager.StaticRegistration addStaticRegistration(final String path,
			final Servlet servlet, final ApplicationWebAccessManager wam) {
		final AdminWebAccessManager.StaticRegistration staticReg = new StaticRegistrationImpl(servlet, path, wam);
		getStaticRegistrations().put(staticReg.getPath(), staticReg);
		return staticReg;
	}

	private final boolean isNonOtpResource(final String alias) {
		final Collection<String> nonOtps = this.nonOtpResources;
		if (nonOtps == null)
			return false;
		return nonOtps.contains(alias);
	}

	void addBasicResourceAlias(String path) {
		synchronized (resources) {
			if (nonOtpResources == null)
				nonOtpResources = new HashSet<>();
			nonOtpResources.add(path);
		}
	}

	void unregisterResource(String alias) {
		servlets.remove(alias);
		if (resources.remove(alias) == null)
			return;
		final Collection<String> nonOtps = this.nonOtpResources;
		if (nonOtps != null)
			nonOtps.remove(alias);
	}

	static {
		httpEnable = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

			@Override
			public Boolean run() {
				return Boolean.getBoolean(ConfigurationConstants.HTTP_ENABLE_PROP);
			}
		});
		/*
		 * Switch to enable/disable servlet access restrictions after APP-SEC 16: Access to a web resource out of a
		 * previously downloaded web page shall only be granted, if the requested web resource is a static content or
		 * the dynamic content (servlet) is registered by the same app as the source of the web page.
		 */
		// xservletEnable = System.getSecurityManager() != null && AccessController.doPrivileged(new
		// PrivilegedAction<Boolean>() {
		//
		// @Override
		// public Boolean run() {
		// return Boolean.getBoolean("org.ogema.xservletaccess.enable");
		// }
		// });
	}

	private final static class StaticRegistrationImpl implements AdminWebAccessManager.StaticRegistration {

		// private final Servlet servlet;
		private final String path;
		private final ApplicationWebAccessManager wam;
		private volatile boolean unregistered = false;

		public StaticRegistrationImpl(Servlet servlet, String path, ApplicationWebAccessManager wam) {
			// this.servlet = Objects.requireNonNull(servlet);
			Objects.requireNonNull(servlet);
			this.path = Objects.requireNonNull(path);
			this.wam = Objects.requireNonNull(wam);
		}

		@Override
		public String getPath() {
			return path;
		}

		// TODO clean up; restrict # of otp per user?
		@Override
		public String[] generateOneTimePwd(HttpServletRequest req) {
			if (unregistered)
				throw new IllegalStateException("Servlet has been unregistered: " + path);
			final OgemaHttpContext ctx = wam.ctx;
			if (ctx == null)
				return null;
			final HttpSession session = req.getSession();
			if (session == null)
				return null;
			final String otp = ctx.registerOTP(ctx.owner, req.getSession());
			if (otp == null)
				return null;
			return new String[] { ctx.owner.getIDString(), otp };
		}

		@Override
		public void unregister() {
			synchronized (this) {
				if (unregistered)
					return;
				unregistered = true;
			}
			final OgemaHttpContext ctx = wam.ctx;
			if (ctx != null) {
				synchronized (ctx) {
					final Map<String, AdminWebAccessManager.StaticRegistration> statics = ctx.staticRegistrations;
					if (statics != null) {
						statics.remove(path);
						if (statics.isEmpty())
							ctx.staticRegistrations = null;
					}
				}
			}
			wam.unregisterWebResource(path);
		}

	}

}
