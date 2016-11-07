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
package org.ogema.webresourcemanager.impl.internal;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.security.WebAccessManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.http.HttpService;
import org.osgi.service.useradmin.UserAdmin;

@Component(specVersion = "1.2")
@Service(Application.class)
public class MyWebResourceManager implements Application {

    protected OgemaLogger logger;
    @Reference
    private AdministrationManager administrationManager;
    @Reference
    private PermissionManager permissionManager;
    @Reference
    HttpService httpService;
    @Reference
    UserAdmin ua;

    private static ApplicationManager appManager;
    private static MyWebResourceManager instance;
    private WebAccessManager webManager;
    private BundleContext bundleContext;
    private MyHttpService myHttpService;

    @Override
    public void start(ApplicationManager appManager) {
        try {
            logger = appManager.getLogger();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (MyWebResourceManager.appManager == null) {
            MyWebResourceManager.appManager = appManager;
        }
        MyWebResourceManager.instance = this;
        webManager = appManager.getWebAccessManager();
        bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        myHttpService = new MyHttpService(httpService);
        myHttpService.registerCustomCSS();
        appManager.getWebAccessManager().registerStartUrl("/wicket");
        
        // TODO register wicket pages with normal WebAccessManager
    }

    @Override
    public void stop(AppStopReason stop) {
        logger.info("Bye OGEMA!");
        logger.debug("{} stopped", getClass().getName());
        MyWebResourceManager.appManager = null;
    }

    public static ApplicationManager getAppManager() {
        return MyWebResourceManager.appManager;
    }

    public static MyWebResourceManager getInstance() {
        return MyWebResourceManager.instance;
    }

    public OgemaLogger getLogger() {
        return logger;
    }

    public WebAccessManager getWebManager() {
        return webManager;
    }

    public AdministrationManager getAdministrationManager() {
        return administrationManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public UserAdmin getUa() {
        return ua;
    }
    
    

}
