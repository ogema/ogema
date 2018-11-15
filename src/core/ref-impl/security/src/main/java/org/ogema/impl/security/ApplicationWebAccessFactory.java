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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.HttpConfigManagement;
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
    private final LoginServlet loginServlet;
    private final AtomicReference<HttpConfigManagement> headerManagement;

    public ApplicationWebAccessFactory(PermissionManager pm, HttpService http, UserAdmin userAdmin, 
    		AtomicReference<HttpConfigManagement> headerManagement, Map<String, Object> config) {
		this.http = http;
        this.pm = pm;
        this.headerManagement = headerManagement; 

		this.restContext = new RestHttpContext();
		//new RestAccess(permMan, admin);
		this.m2mLogin = new M2MLogin(pm, userAdmin);
		this.loginServlet = new LoginServlet(pm, userAdmin, config);
		try {
			this.http.registerResources("/login", "/web", null);
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
    
    void configChanged(Map<String, Object> config) {
    	loginServlet.configUpdate(config);
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
        ApplicationWebAccessManager aw = new ApplicationWebAccessManager(app, this, headerManagement);
        appWAMs.put(app, aw);
        return aw;
    }
    
    synchronized boolean closeWebAccess(AppID app) {
    	final ApplicationWebAccessManager wam = appWAMs.remove(app);
    	if (wam == null)
    		return false;
    	wam.close();
    	return true;
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

    @Deprecated
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
