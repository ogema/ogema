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
