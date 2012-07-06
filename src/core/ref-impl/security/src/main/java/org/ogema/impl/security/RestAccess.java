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
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.AppDomainCombiner;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.ResourcePermission;
import org.ogema.accesscontrol.SessionAuth;
import org.ogema.accesscontrol.UserRightsProxy;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdministrationManager;
import org.slf4j.Logger;

public class RestAccess extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3258753513240214977L;
	static final String OTPNAME = "param2";

	static final String OTUNAME = "param1";

	/*
	 * Handle Get requests to the rest server. Each request has to contain the parameter otpwd (one time password) and
	 * otusr (one time user) (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	private AppDomainCombiner domainCombiner;
	private AccessManager accMan;

	private PermissionManager permMan;
	private AdministrationManager adminMan;

	public RestAccess(PermissionManager permMan, AdministrationManager adminMan) {
		this.domainCombiner = new AppDomainCombiner();
		this.accMan = permMan.getAccessManager();
		this.permMan = permMan;
		this.adminMan = adminMan;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pi = req.getPathInfo();
		if (Configuration.DEBUG) {
			logger.info("Rest Servlet doGet: path URI is " + pi);
		}
		if (pi == null) {
			logger.info("Rest Servlet doGet: no path URI specified");
			return;
		}
		if (!checkAccess(req))
			return;
		resp.setContentType("text/script");
	}

	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pi = req.getPathInfo();
		if (pi == null) {
			logger.info("Rest Servlet doGet: no path URI specified");
			return;
		}
		if (!checkAccess(req))
			return;
		resp.setContentType("text/script");
		logger.info("Rest Servlet doGet: path URI is " + pi);
	}

	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pi = req.getPathInfo();
		if (pi == null) {
			logger.info("Rest Servlet doGet: no path URI specified");
			return;
		}
		if (!checkAccess(req))
			return;
		resp.setContentType("text/script");
		logger.info("Rest Servlet doGet: path URI is " + pi);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pi = req.getPathInfo();
		if (pi == null) {
			logger.info("Rest Servlet doGet: no path URI specified");
			return;
		}
		if (!checkAccess(req))
			return;
		resp.setContentType("text/script");
		logger.info("Rest Servlet doGet: path URI is " + pi);
	}

	/* @formatter:off */
	/*
	 * Check the authorization to access to the rest interface. Access is granted if:
	 * 1 user/password passed to the request as parameter and they match a registered one time user authorization, or
	 * 2 user/password passed to the request as parameter and they match a non-natural (machine) user authorization, or
	 * 3 user/password are not passed to the request as parameter and the current session belongs to a registered non-natural (machine) user.
	 */
	/* @formatter:on */
	boolean checkAccess(HttpServletRequest req) {
		/*
		 * Check access permission trough the AccessControlContext of the App which is involved in this request.
		 */
		String path = req.getPathInfo();

		HttpSession ses = req.getSession();
		/*
		 * Get The authentication information
		 */
		String pwd = req.getParameter(OTPNAME);
		String usr = req.getParameter(OTUNAME);

		// case 1
		if (usr != null && pwd != null && check1TimePW(ses, usr, pwd)) {
			// Get the AccessControlContex of the involved app
			AdminApplication aaa = adminMan.getAppById(usr);// ctx.wam.admin.getAppById(usr);
			if (aaa == null)
				return false;
			ProtectionDomain pda[] = new ProtectionDomain[1];
			pda[0] = aaa.getID().getApplication().getClass().getProtectionDomain();
			if (checkAppAccess(pda, req.getMethod(), path)) {
				if (Configuration.DEBUG) {
					logger.info("RestAccess permitted for 1Time-PW authenticated app " + usr);
				}
				return true;
			}
		}

		// case 2
		if (usr != null && pwd != null && checkM2MUserPW(usr, pwd)) {
			UserRightsProxy urp = accMan.getUrp(usr);
			if (urp == null) {
				if (Configuration.DEBUG)
					logger.info("RestAccess denied for external authenticated user " + usr);
				return false;
			}
			ProtectionDomain pda[] = new ProtectionDomain[1];
			pda[0] = urp.getClass().getProtectionDomain();
			if (checkAppAccess(pda, req.getMethod(), path)) {
				if (Configuration.DEBUG) {
					logger.info("RestAccess permitted for external authenticated user " + usr);
				}
				return true;
			}
		}

		// case 3
		SessionAuth sauth = (SessionAuth) ses.getAttribute(SessionAuth.AUTH_ATTRIBUTE_NAME);
		String loggedUser = sauth.getUsr().getName();
		if (!accMan.isNatural(loggedUser)) {
			if (Configuration.DEBUG) {
				logger.info("RestAccess granted.");
			}
			return true;
		}
		if (Configuration.DEBUG) {
			logger.info("RestAccess denied.");
		}
		return false;
	}

	private boolean checkAppAccess(ProtectionDomain pda[], String method, String path) {
		AccessControlContext acc = new AccessControlContext(new AccessControlContext(pda), domainCombiner);
		// The rest method says which kind of access is desired to the resource
		String action;
		switch (method) {
		case "GET":
			action = "READ";
			break;
		case "POST":
			action = "CREATE";
			break;
		case "PUT":
			action = "WRITE";
			break;
		case "DELETE":
			action = "DELETE";
			break;
		default:
			action = null;
			break;
		}
		// Create permission
		final ResourcePermission perm = new ResourcePermission("path=" + path, action);

		boolean result = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
			public Boolean run() {
				try {
					System.getSecurityManager().checkPermission(perm);
				} catch (Throwable t) {
					return false;
				}
				return true;
			}
		}, acc);

		return result;
	}

	/*
	 * Access to the rest server could be performed by web resources including dynamic content or by any external entity
	 * that is registered as an authorized user. In case of a web resource one time password and user are checked to be
	 * known in the current session. In case of an external entity the AccessManager is asked for the authorization of
	 * the user. In both cases the requested URL has to contain the pair of parameter OTPNAME and OTUNAME.
	 */
	boolean check1TimePW(HttpSession ses, String usr, String pwd) {
		/*
		 * If the app is already registered with this one time password the access is permitted.
		 */
		return permMan.getWebAccess().authenticate(ses, usr, pwd);
	}

	private boolean checkM2MUserPW(String usr, String pwd) {
		/*
		 * Is there an user registered with the credentials attached to the request?
		 */
		return accMan.authenticate(usr, pwd, false);
		// return ctx.wam.permMan.accessMan.authenticate(usr, pwd);

	}
}
