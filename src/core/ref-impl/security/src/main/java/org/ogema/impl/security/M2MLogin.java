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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.SessionAuth;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.slf4j.Logger;

public class M2MLogin extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6087165576060518072L;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	PermissionManagerImpl permMan;

	public M2MLogin(PermissionManagerImpl permMan) {
		this.permMan = permMan;
	}

	/*
	 * Handle the login request. If the authentication is successful then register a new SessionAuth object for the
	 * corresponding session. (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	synchronized protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		UserAdmin admin = permMan.accessMan.usrAdmin;
		logger.info(req.toString());
		String usr = req.getParameter("usr");
		String pwd = req.getParameter("pwd");
		if (Configuration.DEBUG) {
			logger.info("Login request for M2M user: ");
			logger.info(usr + "/" + pwd);
		}
		if (usr.length() == 0) {
			logger.info("Invalid user or password");
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			notify();
			return;
		}
		// Check if the user authentication is valid
		boolean auth = permMan.getAccessManager().authenticate(usr, pwd, false);
		HttpSession ses = req.getSession();
		if (auth) {
			User user = (User) admin.getRole(usr);
			Authorization author = admin.getAuthorization(user);
			SessionAuth sauth = new SessionAuth(author, user, ses);
			ses.setAttribute(SessionAuth.AUTH_ATTRIBUTE_NAME, sauth);

			/*
			 * Handle Request which is received before login was sent. This request is responded with the login page,
			 * therefore the login request is responded with the stalled request received before.
			 */
			// Get the request and response before the login redirection and and handle them.
			// HttpServletRequest oldReq = (HttpServletRequest) ses.getAttribute(OLDREQ_ATTR_NAME);
			// if (oldReq != null) {
			// String requested = oldReq.getRequestURI();
			// resp.setStatus(resp.SC_FOUND);
			// resp.setHeader("Location", oldReq.getRequestURL().toString());
			// resp.setContentType("text/html");
			// ses.setAttribute(OLDREQ_ATTR_NAME, null);
			// }
		}
		else {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			resp.flushBuffer();
		}
		notify();
	}
}
