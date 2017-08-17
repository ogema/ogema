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
package org.ogema.rest.servlet;

import static org.ogema.accesscontrol.Constants.OTPNAME;
import static org.ogema.accesscontrol.Constants.OTUNAME;

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.ProtectionDomain;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.AppDomainCombiner;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.UserRightsProxy;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdministrationManager;
import org.slf4j.Logger;

/**
 * @author Zekeriya Mansuroglu
 *
 */
public class RestAccess {

	/*
	 * Handle Get requests to the rest server. Each request has to contain the parameter otpwd (one time password) and
	 * otusr (one time user) (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	private final AppDomainCombiner domainCombiner;
	private final AccessManager accMan;

	private final PermissionManager permMan;
	private final AdministrationManager adminMan;

	public RestAccess(PermissionManager permMan, AdministrationManager adminMan) {
		Objects.requireNonNull(permMan);
		Objects.requireNonNull(adminMan);
		this.permMan = permMan;
		this.adminMan = adminMan;
		this.domainCombiner = new AppDomainCombiner();
		this.accMan = permMan.getAccessManager();
	}

	// boolean checkAccess(HttpServletRequest req) {
	// return checkAccess(req, req.getPathInfo());
	// }

	boolean checkAccess(HttpServletRequest req, String resourcePath) {
		/*
		 * Check access permission trough the AccessControlContext of the App which is involved in this request.
		 */

		HttpSession ses = req.getSession();
		/*
		 * Get The authentication information
		 */
		String usr = req.getParameter(OTUNAME);
		String pwd = req.getParameter(OTPNAME);

		if (usr == null) {
			logger.debug("REST access to {} without user name", resourcePath);
			return false;
		}
		if (pwd == null) {
			logger.debug("REST access to {} without user password", resourcePath);
			return false;
		}

		if (check1TimePW(ses, usr, pwd)) {
			// Get the AccessControlContex of the involved app
			AdminApplication aaa = adminMan.getAppById(usr);// ctx.wam.admin.getAppById(usr);
			if (aaa == null)
				return false;
			ProtectionDomain pda[] = new ProtectionDomain[1];
			pda[0] = aaa.getID().getApplication().getClass().getProtectionDomain();
			if (setProtectionDomain(pda)) {
				if (logger.isDebugEnabled()) {
					logger.debug("RestAccess permitted for 1Time-PW authenticated app {}", usr);
				}
				return true;
			}
		}

		if (checkM2MUserPW(usr, pwd)) {
			UserRightsProxy urp = accMan.getUrp(usr);
			if (urp == null) {
				logger.info("RestAccess denied for external authenticated user {}", usr);
				return false;
			}
			ProtectionDomain pda[] = new ProtectionDomain[1];
			pda[0] = urp.getClass().getProtectionDomain();
			if (setProtectionDomain(pda)) {
				if (logger.isDebugEnabled()) {
					logger.debug("RestAccess permitted for external authenticated user {}", usr);
				}
				return true;
			}
		}
		logger.info("RestAccess denied.");
		return false;
	}

	private boolean setProtectionDomain(ProtectionDomain pda[]) {
		final AccessControlContext acc = new AccessControlContext(new AccessControlContext(pda), domainCombiner);
		permMan.setAccessContext(acc);
		return true;
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
		return permMan.getAccessManager().authenticate(usr, pwd, false);

	}

	protected boolean setAccessContext(HttpServletRequest req, HttpServletResponse resp, boolean securityEnabled)
			throws ServletException, IOException {
		if (!checkAccess(req, req.getPathInfo())) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return false;
		}
		else {
			return true;
		}
	}
}
