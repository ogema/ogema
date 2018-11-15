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

public class M2MLogin extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6087165576060518072L;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	final PermissionManager permMan;
	final UserAdmin userAdmin;

	public M2MLogin(PermissionManager permMan, UserAdmin userAdmin) {
		this.permMan = permMan;
		this.userAdmin = userAdmin;
	}

	/*
	 * Handle the login request. If the authentication is successful then register a new SessionAuth object for the
	 * corresponding session. (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	synchronized protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		UserAdmin admin = userAdmin;
		logger.info(req.toString());
		String usr = req.getParameter(Constants.OTUNAME);
		String pwd = req.getParameter(Constants.OTPNAME);
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
			SessionAuth sauth = new SessionAuth(author, permMan.getAccessManager(), ses);
			ses.setAttribute(Constants.AUTH_ATTRIBUTE_NAME, sauth);

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
