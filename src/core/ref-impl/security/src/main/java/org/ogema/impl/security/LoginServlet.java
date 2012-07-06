/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
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

import static org.ogema.impl.security.WebAccessManagerImpl.OLDREQ_ATTR_NAME;

public class LoginServlet extends HttpServlet {
	protected static final String LOGIN_PATH = "/web/login.html";
	protected static final String LOGIN_SERVLET_PATH = "/ogema/login";

	private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	private PermissionManager permissionManager;
	private UserAdmin ua;

	public LoginServlet(PermissionManager permissionManager, UserAdmin ua) {
		this.permissionManager = permissionManager;
		this.ua = ua;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//		if(req.getSession().getAttribute(name))
		//		req.getRequestDispatcher(LOGIN_PATH).forward(req, resp);
		if (req.getSession().getAttribute(SessionAuth.AUTH_ATTRIBUTE_NAME) != null) {
			//			req.getRequestDispatcher("/apps/ogema/framework/gui").forward(req, resp);
			resp.sendRedirect("/ogema/index.html");
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
		logger.info(req.toString());
		String usr = req.getParameter("usr");
		String pwd = req.getParameter("pwd");
		if (Configuration.DEBUG) {
			logger.info("Login request for: usr/pwd");
			logger.info(usr + "/" + pwd);
		}
		if (usr == null || usr.isEmpty() || pwd == null || pwd.isEmpty()) {
			logger.info("Invalid user or password");
			resp.getWriter().write("");
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		// Check if the user authentication is valid
		boolean auth = permissionManager.getAccessManager().authenticate(usr, pwd, true);
		HttpSession ses = req.getSession();
		if (auth) {
			User user = (User) ua.getRole(usr);
			Authorization author = ua.getAuthorization(user);
			SessionAuth sauth = new SessionAuth(author, user, ses);
			// check if we had an old req to redirect to the originally requested URL before invalidating 
			HttpServletRequest oldReq = (HttpServletRequest) ses.getAttribute(OLDREQ_ATTR_NAME);

			// invalidate old session to prevent session hijacking:
			req.getSession(false).invalidate();
			ses = req.getSession(true);
			ses.setAttribute(SessionAuth.AUTH_ATTRIBUTE_NAME, sauth);

			/*
			 * Handle Request which is received before login was sent. This request is responded with the login page,
			 * therefore the login request is responded with the stalled request received before.
			 */
			String newLocation = "/ogema/index.html";
			resp.setContentType("text/html");
			if (oldReq != null) {
				resp.setStatus(HttpServletResponse.SC_OK);
				StringBuffer requestURL = oldReq.getRequestURL();
				if (requestURL != null) {
					newLocation = requestURL.toString();
				}
				ses.setAttribute(OLDREQ_ATTR_NAME, null);
			}
			else {
				resp.setStatus(HttpServletResponse.SC_OK);
			}

			resp.getWriter().write(newLocation);
		}
		else {
			resp.getWriter().write("");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.flushBuffer();
		}
	}
}
