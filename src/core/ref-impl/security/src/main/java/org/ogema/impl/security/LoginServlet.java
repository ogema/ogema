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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.SessionAuth;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.slf4j.Logger;

public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1l;

	protected static final String LOGIN_PATH = "/web/login.html";
	protected static final String LOGIN_SERVLET_PATH = "/ogema/login";
	protected static final String OLDREQ_ATTR_NAME = "requestBeforeLogin";

	private static final String LOGIN_FAILED_MSG = "Login failed: Wrong Username/Password";
	private static final String MAX_LOGIN_TRIES_EXCEEDED_MSG = "Max number of tries for login exceeded."
			+ " Login is blocked for %s.";

	private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	private PermissionManager permissionManager;
	private UserAdmin ua;

	// used to prevent brute force attacks by blocking IPs if more than a predefined number of logins
	// failed. 
	private LoginFailureInspector failureInspector = new LoginFailureInspector();

	public LoginServlet(PermissionManager permissionManager, UserAdmin ua) {
		this.permissionManager = permissionManager;
		this.ua = ua;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		failureInspector.cleanUp();
		HttpSession session = req.getSession();
		if (session.getAttribute(SessionAuth.AUTH_ATTRIBUTE_NAME) != null) {
			resp.sendRedirect("/ogema/index.html");
			return;
		}

		InputStream is;
		OutputStream bout;
		int len = 0;
		byte[] buf = new byte[512];
		try {
			is = getClass().getResource(LOGIN_PATH).openStream();
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

		logger.info(req.toString());
		String usr = req.getParameter("usr");
		String pwd = req.getParameter("pwd");
		if (Configuration.DEBUG) {
			logger.info("Login request for user: " + usr);
		}
		if (usr == null || usr.isEmpty() || pwd == null || pwd.isEmpty()) {
			logger.info("Invalid user or password");
			resp.getWriter().write(LOGIN_FAILED_MSG);
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		// Check if the user authentication is valid
		boolean auth = permissionManager.getAccessManager().authenticate(usr, pwd, true);
		if (auth) {
			User user = (User) ua.getRole(usr);
			Authorization author = ua.getAuthorization(user);
			HttpSession session = req.getSession();
			SessionAuth sauth = new SessionAuth(author,permissionManager.getAccessManager(), session);
			// check if we had an old req to redirect to the originally requested URL before invalidating
			String newLocation = "/ogema/index.html";
			if (session.getAttribute(OLDREQ_ATTR_NAME) != null) {
				newLocation = session.getAttribute(OLDREQ_ATTR_NAME).toString();
			}

			// invalidate old session to prevent session hijacking:
			req.getSession(false).invalidate();
			session = req.getSession(true);
			session.setAttribute(SessionAuth.AUTH_ATTRIBUTE_NAME, sauth);
			session.setAttribute(SessionAuth.USER_CREDENTIAL, pwd);

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
		}
		resp.setContentType("text/html");
		resp.flushBuffer();
	}
}
