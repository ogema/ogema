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
package org.ogema.frameworkadministration.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.frameworkadministration.controller.UserController;
import org.ogema.frameworkadministration.utils.Utils;

/**
 *
 * @author tgries
 */
public class FAServletUser extends HttpServlet {

	private static final long serialVersionUID = -5558921230241725532L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		@SuppressWarnings("unused")
		String pathInfo = req.getPathInfo();

		String action = req.getParameter("action");

		if ("logout".equals(action)) {
			req.getSession().invalidate();
			return;
		}

		if ("getUserData".equals(action)) {
			String user = req.getParameter("usr");
			String result = UserController.getInstance().getUserInformation(user);
			resp.getWriter().write(result);
			resp.setStatus(200);
		}
		else if ("getUserPolicies".equals(action)) {
			String user = req.getParameter("usr");
			//String result = UserController.getInstance().getPolicies(user);
			String result = UserController.getInstance().getPoliciesMachineUser(user);
			resp.getWriter().write(result);
			resp.setStatus(200);
		}
		else if ("getUserPermittedApps".equals(action)) {
			String user = req.getParameter("usr");
			//String result = UserController.getInstance().getPermittedApps(user);
			String result = UserController.getInstance().getAppsNaturalUser(user);
			resp.getWriter().write(result);
			resp.setStatus(200);
		}
		else if ("grantAdminRights".equals(action)) {
			String user = req.getParameter("usr");
			boolean success = UserController.getInstance().grantAdminRights(user);
			if (success) {
				String message = Utils.createMessage("OK", "admin rights granted");
				resp.getWriter().write(message);
				resp.setStatus(200);
			}
			else {
				String message = Utils.createMessage("ERROR", "could not set admin rights for user");
				resp.getWriter().write(message);
			}
		}
		else if ("revokeAdminRights".equals(action)) {
			String user = req.getParameter("usr");
			boolean success = UserController.getInstance().revokeAdminRights(user);
			if (success) {
				String message = Utils.createMessage("OK", "admin rights revoked");
				resp.getWriter().write(message);
				resp.setStatus(200);
			}
			else {
				String message = Utils.createMessage("ERROR", "could not revoke admin rights for user");
				resp.getWriter().write(message);
			}
		}
		else {
			resp.getWriter().write(UserController.getInstance().getAllUsersJSON());
			resp.setStatus(200);
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String pathInfo = req.getPathInfo();

		switch (pathInfo) {
		case "/createUser": {

			String jsonMessage = Utils.readJsonFromRequest(req);
			boolean success = UserController.getInstance().createUser(jsonMessage);
			if (success) {
				String message = Utils.createMessage("OK", "user successfully created");
				resp.getWriter().write(message);
				resp.setStatus(200);
			}
			else {
				String message = Utils.createMessage("ERROR", "user could not be created because user already exists");
				resp.getWriter().write(message);
			}
			break;
		}
		case "/deleteUser": {

			String jsonMessage = Utils.readJsonFromRequest(req);
			boolean success = UserController.getInstance().deleteUser(jsonMessage);
			if (success) {
				String message = Utils.createMessage("OK", "user removed");
				resp.getWriter().write(message);
				resp.setStatus(200);
			}
			else {
				String message = Utils.createMessage("ERROR", "user does not exist");
				resp.getWriter().write(message);
			}
			break;
		}
		case "/copyUser": {

			String jsonMessage = Utils.readJsonFromRequest(req);
			boolean success = UserController.getInstance().copyUser(jsonMessage);
			if (success) {
				String message = Utils.createMessage("OK", "user was copied");
				resp.getWriter().write(message);
				resp.setStatus(200);
			}
			else {
				String message = Utils.createMessage("ERROR", "user could not be copied");
				resp.getWriter().write(message);
			}
			break;
		}
		case "/changePassword": {

			String jsonMessage = Utils.readJsonFromRequest(req);
			boolean success = UserController.getInstance().changePassword(jsonMessage);
			if (success) {
				String message = Utils.createMessage("OK", "password for user changed");
				resp.getWriter().write(message);
				resp.setStatus(200);
			}
			else {
				String message = Utils.createMessage("ERROR", "could not change password for user");
				resp.getWriter().write(message);
			}
			break;
		}
		case "/setPermittedApps": {
			String jsonMessage = Utils.readJsonFromRequest(req);
			boolean success = UserController.getInstance().setAppsNaturalUser(jsonMessage);
			if (success) {
				String message = Utils.createMessage("OK", "permitted Apps for user set");
				resp.getWriter().write(message);
				resp.setStatus(200);
			}
			else {
				String message = Utils.createMessage("ERROR", "could not set permitted Apps for user");
				resp.getWriter().write(message);
			}
			break;
		}
		case "/setPolicies": {
			String jsonMessage = Utils.readJsonFromRequest(req);
			boolean success = UserController.getInstance().setPoliciesMachineUser(jsonMessage);
			if (success) {
				String message = Utils.createMessage("OK", "policies for user set");
				resp.getWriter().write(message);
				resp.setStatus(200);
			}
			else {
				String message = Utils.createMessage("ERROR", "could not set policies for user");
				resp.getWriter().write(message);
			}
			break;
		}
		default:
			break;
		}

	}

}
