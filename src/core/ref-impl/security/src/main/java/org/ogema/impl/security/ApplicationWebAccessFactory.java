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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.SessionAuth;
import org.ogema.core.application.AppID;
import org.ogema.core.security.WebAccessManager;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.useradmin.UserAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class ApplicationWebAccessFactory implements WebAccessManager {

    final PermissionManager pm;

    final HttpService http;
    final Map<String, String> baseUrls = new ConcurrentHashMap<>();
    final Map<AppID, ApplicationWebAccessManager> appWAMs = new ConcurrentHashMap<>();
    final Logger logger = LoggerFactory.getLogger(getClass());
    final RestHttpContext restContext;
    final M2MLogin m2mLogin;

    public ApplicationWebAccessFactory(PermissionManager pm, HttpService http, UserAdmin userAdmin) {
		this.http = http;
        this.pm = pm;

		this.restContext = new RestHttpContext();
		//new RestAccess(permMan, admin);
		this.m2mLogin = new M2MLogin(pm, userAdmin);
		try {
			this.http.registerResources("/login", "/web", null);
			LoginServlet loginServlet = new LoginServlet(pm, userAdmin);
			this.http.registerServlet(LoginServlet.LOGIN_SERVLET_PATH, loginServlet, null, null);
			this.http.registerServlet("/m2mLogin", this.m2mLogin, null, restContext);
		} catch (NamespaceException | ServletException e) {
			logger.error("registration failed", e);
		}
		/**
		 * DOS Filter for default context
		 */
		try {
			registerFilter(null);
		} catch (ServletException e) {
			logger.error("filter registration failed", e);
		}
	}

    /*
     * Other registrations are removed by the ApplicationTracker -> we need to keep a reference to the HttpService for this
     * (also of the ApplicationWebAccessManagers?)
     */
    public void close() {
    	try {
    		http.unregister("/login");
    	} catch (Exception e) { /* ignore */ }
    	try {
    		http.unregister(LoginServlet.LOGIN_SERVLET_PATH);
    	} catch (Exception e) { /* ignore */ }
    	try {
    		http.unregister("/m2mLogin");
    	} catch (Exception e) { /* ignore */ }
    	Iterator<ApplicationWebAccessManager> it = appWAMs.values().iterator();
    	while (it.hasNext()) {
    		ApplicationWebAccessManager wam = it.next();
    		wam.close();
    		it.remove();
    	}
    	baseUrls.clear();
    }

    private void registerFilter(HttpContext ctx) throws ServletException {
        //filter registrations moved to DS component definition in OSGI-INF/
	}

    /*
     * Creates a new WebAccessManager for the given app, or returns an existing
     * instance.
     */
    public synchronized WebAccessManager createApplicationWebAccessManager(AppID app) {
        if (appWAMs.containsKey(app)){
            return appWAMs.get(app);
        }
        ApplicationWebAccessManager aw = new ApplicationWebAccessManager(app, this);
        appWAMs.put(app, aw);
        return aw;
    }

    ApplicationWebAccessManager getAppWAM(AppID appId) {
        return appWAMs.get(appId);
    }

    @Override
    public boolean authenticate(HttpSession ses, String usr, String pwd) {

        /*
         * Is there any authorization object on the current session?
         */
        SessionAuth sa = (SessionAuth) ses.getAttribute("ogemaAuth");
        if (sa != null) {
            String tmpotp = sa.getOtpList().get(usr);
            /*
             * If the app is already registered with this one time password the access is permitted.
             */
            if (tmpotp != null && tmpotp.equals(pwd)) {
                return true;
            }
        }
        return false;
    }

    String[] registerStaticResource(HttpServlet servlet, HttpServletRequest req, AppID appId) {
    	SessionAuth sauth = (SessionAuth) req.getSession().getAttribute("ogemaAuth");
    	if (sauth == null)
    		return null;
    	String otp = sauth.registerAppOtp(appId);
		return new String[]{appId.getIDString(), otp};
    }

    @Override
    public void unregisterWebResource(String alias) {
        throw new UnsupportedOperationException("Not supported by global WebAccessManager, requires Application-specific WebAccessManager");
    }

    @Override
    public String registerWebResource(String alias, String name) {
        throw new UnsupportedOperationException("Not supported by global WebAccessManager, requires Application-specific WebAccessManager");
    }

    @Override
    public String registerWebResource(String alias, Servlet servlet) {
        throw new UnsupportedOperationException("Not supported by global WebAccessManager, requires Application-specific WebAccessManager");
    }

    @Override
    @Deprecated
    public Map<String, String> getRegisteredResources(AppID appid) {
        ApplicationWebAccessManager appWAM = getAppWAM(appid);
        if (appWAM == null || appWAM.ctx == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(appWAM.ctx.resources);
        }
    }

	@Override
    @Deprecated
	public Set<String> getRegisteredServlets(AppID appid) {
		ApplicationWebAccessManager appWAM = getAppWAM(appid);
        if (appWAM == null || appWAM.ctx == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(appWAM.ctx.servlets.keySet());
        }
	}

    @Override
    public Map<String, String> getRegisteredResources() {
        //no web resources are registered with the global web access manager
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getRegisteredServlets() {
        //no web resources are registered with the global web access manager
        return Collections.emptySet();
    }

    public void registerStartUrl(AppID appId, String url) {
        baseUrls.put(appId.getIDString(), url);
    }

    public String getStartUrl(AppID appid) {
        ApplicationWebAccessManager aw = getAppWAM(appid);
        return aw != null ? aw.getStartUrl() : null;
    }

    @Override
    public boolean unregisterWebResourcePath(String alias) {
        throw new UnsupportedOperationException("Not supported by global WebAccessManager, requires Application-specific WebAccessManager");
    }

    @Override
    public String registerWebResourcePath(String alias, Servlet servlet) {
        throw new UnsupportedOperationException("Not supported by global WebAccessManager, requires Application-specific WebAccessManager");
    }

    @Override
    public String registerWebResourcePath(String alias, String name) {
        throw new UnsupportedOperationException("Not supported by global WebAccessManager, requires Application-specific WebAccessManager");
    }

    @Override
    public void registerStartUrl(String url) {
        throw new UnsupportedOperationException("Not supported by global WebAccessManager, requires Application-specific WebAccessManager");
    }

	@Override
	public String getStartUrl() {
		throw new UnsupportedOperationException("Not supported by global WebAccessManager, requires Application-specific WebAccessManager");
	}



}
