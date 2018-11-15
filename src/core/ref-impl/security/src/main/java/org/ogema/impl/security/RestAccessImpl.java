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

import static org.ogema.accesscontrol.Constants.OTPNAME;
import static org.ogema.accesscontrol.Constants.OTUNAME;

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.ProtectionDomain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.AppDomainCombiner;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.RestAccess;
import org.ogema.accesscontrol.UserRightsProxy;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.slf4j.Logger;

/**
 * @author Zekeriya Mansuroglu
 */
@Service({RestAccess.class, Application.class})
@Component
public class RestAccessImpl implements RestAccess, Application {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	private final AppDomainCombiner domainCombiner = new AppDomainCombiner();

	@Reference
	private PermissionManager permMan;
	
	private volatile ApplicationManager appMan;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
	}
	
	@Override
	public void stop(AppStopReason reason) {
		this.appMan = null;
	}

	/*
	 * Handle Get requests to the rest server. Each request has to contain the parameter "user" 
	 * (Javascript variable "otpwd" - one time password) and "pw" (Javascript variable "otusr" - one time user) (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	private ApplicationManager getAppManagerInternal(final HttpServletRequest req, final String resourcePath) {
		final ApplicationManager appManInternal = this.appMan;
		if (appManInternal == null) // framework not running or service not started yet
			return null;
		/*
		 * Check access permission trough the AccessControlContext of the App which is involved in this request.
		 */

		final HttpSession ses = req.getSession();
		/*
		 * Get The authentication information
		 */
		final String usr = req.getParameter(OTUNAME);
		final String pwd = req.getParameter(OTPNAME);
		final AccessManager accMan = permMan.getAccessManager();
		if (usr != null && pwd != null && check1TimePW(ses, usr, pwd)) {
			// Get the AccessControlContex of the involved app
			AdminApplication aaa = appManInternal.getAdministrationManager().getAppById(usr);// ctx.wam.admin.getAppById(usr);
			if (aaa == null)
				return null;
			ProtectionDomain pda[] = new ProtectionDomain[1];
			pda[0] = aaa.getID().getApplication().getClass().getProtectionDomain();
			if (setProtectionDomain(pda)) {
				if (logger.isDebugEnabled()) {
					logger.debug("RestAccess permitted for 1Time-PW authenticated app {}", usr);
				}
				accMan.setCurrentUser(accMan.getLoggedInUser(req));
				return aaa.getAppManager();
			}
		}
		final String user = accMan.authenticate(req, false);
		if (user != null) {
			UserRightsProxy urp = accMan.getUrp(user);
			if (urp == null) {
				logger.info("RestAccess denied for external authenticated user {}", user);
				return null;
			}
			accMan.setCurrentUser(user);
			return appManInternal;
		}
		logger.info("RestAccess denied.");
		return null;
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

	/*
	private boolean checkM2MUserPW(String usr, String pwd) {
		
		//Is there an user registered with the credentials attached to the request?
		return permMan.getAccessManager().authenticate(usr, pwd, false);

	}
	*/

	@Override
	public ApplicationManager authenticate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final ApplicationManager appMan = getAppManagerInternal(req, req.getPathInfo());
		if (appMan == null)
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		return appMan;
	}
	
	@Override
	public AccessControlContext getAccessContext(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final ApplicationManager appManInternal = this.appMan;
		if (appManInternal == null) // framework not running or service not started yet
			return null;
		final HttpSession ses = req.getSession();
		/*
		 * Get The authentication information
		 */
		final String usr = req.getParameter(OTUNAME);
		final String pwd = req.getParameter(OTPNAME);
		final AccessManager accMan = permMan.getAccessManager();
		if (usr != null && pwd != null && check1TimePW(ses, usr, pwd)) {
			// Get the AccessControlContex of the involved app
			AdminApplication aaa = appManInternal.getAdministrationManager().getAppById(usr);// ctx.wam.admin.getAppById(usr);
			if (aaa == null)
				return null;
			final String user = accMan.getLoggedInUser(req);
			final UserRightsProxy urp = user == null ? null : accMan.getUrp(user);
			accMan.setCurrentUser(urp == null ? null : user);
			final ProtectionDomain pd = aaa.getID().getApplication().getClass().getProtectionDomain();
			if (urp == null)
				return new AccessControlContext(new ProtectionDomain[] {pd});
			else
				return new AccessControlContext(new ProtectionDomain[] {pd, urp.getClass().getProtectionDomain()});
		}
		final String user = accMan.authenticate(req, false);
		if (user != null) {
			UserRightsProxy urp = accMan.getUrp(user);
			if (urp == null) {
				logger.info("RestAccess denied for external authenticated user {}", user);
				return null;
			}
			accMan.setCurrentUser(user);
			return new AccessControlContext(new ProtectionDomain[] {urp.getClass().getProtectionDomain()});
		}
		logger.info("RestAccess denied.");
		return null;
	}
	
}
