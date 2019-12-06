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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.Constants;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.SessionAuth;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.slf4j.Logger;

// would be preferable to create a separate component for this 
// and leverage its own configuration class...
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1l;

	private static final String PROPERTY_USE_CDN = "org.ogema.gui.usecdn";
	private static final String PROPERTY_START_PAGE = "org.ogema.gui.startpage";
	
	protected static final String LOGIN_PATH;
	protected static final String LOGIN_PATH_LOCAL = "/web/login.html";
	protected static final String LOGIN_PATH_REMOTE = "/web/login2.html";
	protected static final String LOGIN_SERVLET_PATH = "/ogema/login";
	protected static final String OLDREQ_ATTR_NAME = "requestBeforeLogin";
	
	private static final String START_PAGE;

	private static final String LOGIN_FAILED_MSG = "Login failed: Username and/or Password wrong";
	private static final String MAX_LOGIN_TRIES_EXCEEDED_MSG = "Max number of tries for login exceeded."
			+ " Login is blocked for %s.";
	
	static {
		final boolean useCdn = Boolean.getBoolean(PROPERTY_USE_CDN);
		LOGIN_PATH = useCdn ? LOGIN_PATH_REMOTE : LOGIN_PATH_LOCAL;
		final String startProp = System.getProperty(PROPERTY_START_PAGE);
		START_PAGE = startProp != null ? startProp : useCdn ? "/ogema/index2.html" : "/ogema/index.html";
	}
	
	private volatile String ICON;
	private volatile String ICON_TYPE;
	private volatile String STYLE;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	private final PermissionManager permissionManager;
	private final UserAdmin ua;

	// used to prevent brute force attacks by blocking IPs if more than a predefined number of logins
	// failed. 
	private final LoginFailureInspector failureInspector = new LoginFailureInspector();

	public LoginServlet(PermissionManager permissionManager, UserAdmin ua, Map<String, Object> config) {
		this.permissionManager = permissionManager;
		this.ua = ua;
		configUpdate(config);
	}
	
	final void configUpdate(final Map<String, Object> config) {
		// we may need to read system properties, and who knows what else is on the call stack...
		AccessController.doPrivileged(new PrivilegedAction<Void>() {

			@Override
			public Void run() {
				Object iconObj = config.get(ConfigurationConstants.LOGIN_ICON_CONFIG);
				final String icon = iconObj instanceof String ? (String) iconObj
						: System.getProperty(ConfigurationConstants.DEFAULT_LOGIN_ICON_PROPERTY, "ogema.svg");
				URL test = LoginServlet.class.getResource("/web/" + icon);
				if (test == null)
					ICON = "/web/ogema.svg";
				else
					ICON = "/web/" + icon;
				final String[] components = ICON.split("\\.");
				final String type;
				if (components.length > 1) {
					final String ending = components[components.length-1];
					switch (ending.toLowerCase()) {
					case "svg":
						type = "svg+xml";
						break;
					case "jpg":
					case "jpeg":
						type = "jpeg";
						break;
					default:
						type = ending.toLowerCase();
					}
				} else
					type = "png"; // XXX
				ICON_TYPE = type;
				Object styleObj = config.get(ConfigurationConstants.STYLE_CONFIG);
				final String style;
				if (styleObj instanceof String)
					style = (String) styleObj;
				else {
					style = System.getProperty(ConfigurationConstants.DEFAULT_STYLE_PROPERTY, "primary");
				}
				STYLE = style;
				return null;
			}
		});
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		failureInspector.cleanUp();
		HttpSession session = req.getSession();
		if (session.getAttribute(Constants.AUTH_ATTRIBUTE_NAME) != null) {
			resp.sendRedirect(START_PAGE);
			return;
		}
		final String style = req.getParameter("style");
		if (style != null) {
			resp.setCharacterEncoding("UTF-8");
			resp.setContentType("text/plain");
			resp.getWriter().write(STYLE);
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		}
		final URL resource;
		final String icon = req.getParameter("icon");
		if (icon != null) {
			resource = getClass().getResource(ICON);
			resp.setContentType("image/" + ICON_TYPE);
		} else {
			resource = getClass().getResource("false".equalsIgnoreCase(req.getParameter("usecdn")) ? LOGIN_PATH_LOCAL : LOGIN_PATH);
		}
		InputStream is;
		OutputStream bout;
		int len = 0;
		byte[] buf = new byte[512];
		try {
			is = resource.openStream();
			bout = resp.getOutputStream();
			do {
				len = is.read(buf);
				if (len == -1) // This check is needed due to jetty's own OutputStream implementation.
					break;
				bout.write(buf, 0, len);
				// NOTE: Jetty server has its own OutputStream class its write method doesn't throw this
				// IndexOutOfBoundsException
			} while (true);
			resp.setStatus(HttpServletResponse.SC_OK);
			//			resp.flushBuffer();
		} catch (IOException e1) {
			e1.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * (non-Javadoc) Handle Request which is received before login was sent. This request is responded with the login
	 * page, therefore the login request is responded with the stalled request received before.
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */

	/*
	 * Handle the login request. If the authentication is successful then register a new SessionAuth object for the
	 * corresponding session. (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		failureInspector.cleanUp();
		String remoteAddress = req.getRemoteAddr();
		if (failureInspector.isUserBlocked(remoteAddress)) {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			resp.getWriter().write(
					String.format(MAX_LOGIN_TRIES_EXCEEDED_MSG, failureInspector.getRemainingBlockTime(remoteAddress)));
			return;
		}

//		logger.info(req.toString()); // do not log sensitive info like pw
		// Check if the user authentication is valid
		final String usr = permissionManager.getAccessManager().authenticate(req, true);
		if (usr != null) {
			User user = (User) ua.getRole(usr);
			Authorization author = ua.getAuthorization(user);
			HttpSession session = req.getSession();
			SessionAuth sauth = new SessionAuth(author,permissionManager.getAccessManager(), session);
			// check if we had an old req to redirect to the originally requested URL before invalidating
			String newLocation = START_PAGE;
			if (session.getAttribute(OLDREQ_ATTR_NAME) != null) {
				newLocation = session.getAttribute(OLDREQ_ATTR_NAME).toString();
			}

			// invalidate old session to prevent session hijacking:
			req.getSession(false).invalidate();
			session = req.getSession(true);
			session.setAttribute(Constants.AUTH_ATTRIBUTE_NAME, sauth);
			// only applicable in case of pw-base access
			final String pwd = req.getParameter(Constants.OTPNAME);
			if (pwd != null)
				session.setAttribute(Constants.USER_CREDENTIAL, pwd);
			logger.info("User log in: {}", usr);
			/*
			 * Handle Request which is received before login was sent. This request is responded with the login page,
			 * therefore the login request is responded with the stalled request received before.
			 */
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(newLocation);
		}
		else {
			failureInspector.loginFailed(remoteAddress);
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

			if (failureInspector.isUserBlocked(remoteAddress)) {
				resp.getWriter().write(
						String.format(MAX_LOGIN_TRIES_EXCEEDED_MSG, failureInspector
								.getRemainingBlockTime(remoteAddress)));
			}
			else {
				resp.getWriter().write(LOGIN_FAILED_MSG);
			}
			logger.info("Failed log-in attempt: {}", remoteAddress);
		}
		resp.setContentType("text/html");
		resp.flushBuffer();
	}
}
