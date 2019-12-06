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
package org.ogema.application.manager.impl;

import org.ogema.timer.TimerScheduler;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.security.WebAccessManager;
import org.ogema.persistence.ResourceDB;
import org.ogema.recordeddata.DataRecorder;
import org.ogema.resourcemanager.impl.ResourceDBManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;

/* Collects services required for ApplicationManager via DS, and then starts
 tracking Application registrations in activate(). */
@Component(specVersion = "1.2")
// @Reference(referenceInterface = Application.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy =
// ReferencePolicy.DYNAMIC, bind = "addApplication", unbind = "removeApplication")
public class ApplicationTracker {

    public static final String WORKQUEUE_DRAIN_INTERVAL = "ogema.apps.workqueuedrain";
    final long drain_interval = Long.getLong(WORKQUEUE_DRAIN_INTERVAL, 20000);

    @Reference
    protected TimerScheduler timerScheduler;
    // for calling ApplicationManagerImpl.drainWorkQueue periodically.
    private Timer drainTimer;

    @Reference
    protected FrameworkClock clock;

    private final Map<Application, ApplicationManagerImpl> apps = new ConcurrentHashMap<>();

    private final Map<Application, AppAdminAccessImpl> appAdmins = new HashMap<>();

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    @Reference(bind = "setPermissionManager")
    private PermissionManager permissionManager;
    private ServiceTracker<Application, Application> tracker;

    @Reference(bind = "setResourceDB")
    protected ResourceDB resDB;
    protected ResourceDBManager resDBManager;

    @Reference(bind = "setChannelAccess")
    protected ChannelAccess channelAccess;

    @Reference(bind = "setHardwareManager")
    protected HardwareManager hardwareManager;

    @Reference
    protected DataRecorder recordedData;

    @Reference
    protected AdministrationManager administration;

    protected BundleContext ctx;

    ServiceTrackerCustomizer<Application, Application> trackerCustomizer = new ServiceTrackerCustomizer<Application, Application>() {

        @Override
        public Application addingService(ServiceReference<Application> sr) {
            Application app = ctx.getService(sr);
            if (app == null) {
                logger.warn("got a null service object from service reference {}, bundle {}", sr, sr.getBundle());
                return null;
            }
            addApplication(app, sr.getBundle());
            return app;
        }

        @Override
        public void modifiedService(ServiceReference<Application> sr, Application t) {
        }

        @Override
        public void removedService(ServiceReference<Application> sr, Application t) {
            removeApplication(t);
        }
    };

    public ApplicationTracker() {
    }

    protected void setResourceDB(ResourceDB db) {
        this.resDB = db;
    }

    protected void setChannelAccess(ChannelAccess ca) {
        this.channelAccess = ca;
    }

    protected void setHardwareManager(HardwareManager hardwareManager) {
        this.hardwareManager = hardwareManager;
    }

    @Activate
    protected synchronized void activate(BundleContext ctx, Map<String, Object> config) {
        this.ctx = ctx;
        try {
            resDBManager = new ResourceDBManager(resDB, recordedData, timerScheduler,
                    permissionManager.getAccessManager());

            tracker = new ServiceTracker<>(ctx, Application.class, trackerCustomizer);
            tracker.open();

            TimerTask drainTask = new TimerTask() {

                @Override
                public void run() {
                    drainWorkQueues();
                }
            };
            drainTimer = new Timer("Application work queue sweeper", true);
            drainTimer.schedule(drainTask, 0, drain_interval);

            logger.debug("{} activated", getClass().getName());
        } catch (Throwable t) {
            logger.error("could not activate ApplicationTracker", t);
            throw t;
        }
    }

    @Deactivate
    protected synchronized void deactivate(Map<String, Object> config) {
    	if (tracker != null)
    		tracker.close();
        synchronized (apps) {
            Iterator<Application> it = apps.keySet().iterator();
            while (it.hasNext()) {
                Application app = it.next();
                ApplicationManagerImpl appMan = apps.get(app)/* .appMan */;
                appMan.stopApplication();
                appMan.close();
                it.remove();
            }
            Iterator<AppAdminAccessImpl> itAdmin = appAdmins.values().iterator();
            while (itAdmin.hasNext()) {
            	AppAdminAccessImpl app = itAdmin.next();
            	if (app.registration != null)
            		app.registration.unregister();
            	app.registration = null;
            	itAdmin.remove();
            }
        }
        if (drainTimer != null) {
        	drainTimer.cancel();
        	drainTimer.purge();
        }
        logger.debug("ApplicationTracker deactivated.");
        tracker = null;
        resDBManager = null;
        drainTimer = null;
        ctx = null;
    }

    protected void addApplication(Application app, Bundle b) {
        synchronized (apps) {
            logger.debug("new App registered: {}@{}", app, b);
            AppIDImpl id = AppIDImpl.getNewID(b, app);
            ApplicationManagerImpl appMan = new ApplicationManagerImpl(app, this, id);
            createApplicationAdmin(app, id, b, appMan);
            apps.put(app, appMan);
            appMan.startApplication();
        }
    }

    protected AppAdminAccessImpl createApplicationAdmin(Application app, AppIDImpl id, Bundle b,
            ApplicationManagerImpl appman) {
        AccessManager am = permissionManager.getAccessManager();
        AppAdminAccessImpl aaa = new AppAdminAccessImpl(b, app, id, appman, am);
        /*
         * Initiate creation of an app role (group) used by access rights management. TODO This role has to be removed
         * if the app is uninstalled.
         */
        am.registerApp(id);

        Hashtable<String, Object> props = new Hashtable<>();
        props.put("appID", aaa.id.getIDString());
        aaa.registration = ctx.registerService(AdminApplication.class, aaa, props);
        appAdmins.put(app, aaa);
        return aaa;
    }

    protected AppID removeApplication(Application app) {
        synchronized (apps) {
            ApplicationManagerImpl appMan = apps.remove(app);
            AppAdminAccessImpl aaa = appAdmins.remove(app);
            if (appMan != null) {
                appMan.stopApplication();
                appMan.close();
            } else {
                logger.warn("tried to remove non existent app {}", app);
            }
            /*
             * The app role (group) created at the installation of the app used by access rights management has to be
             * removed because the app is uninstalled.
             */
            if (aaa != null) {
            	final PermissionManager permMan = this.permissionManager;
            	AccessManager am = permMan != null ? permMan.getAccessManager() : null;
            	if (am != null)
            		am.unregisterApp(aaa.id);
            	if (aaa.registration != null)
            		aaa.registration.unregister();
            }
            return appMan != null ? appMan.getAppID() : null;
        }
    }

    /**
     * remove finished items (Futures) from the work queues of registered
     * application managers and log all uncaught exceptions.
     */
    protected void drainWorkQueues() {
        logger.trace("draining application manager work queues ({} apps)", apps.size());
        for (ApplicationManagerImpl ad : apps.values()) {
        	ad.submitEvent(ad.drainWorkQueueTask);
        }
    }

    protected void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    protected ResourceDBManager getResourceDBManager() {
        return resDBManager;
    }

    protected TimerScheduler getTimerScheduler() {
        return timerScheduler;
    }

    protected FrameworkClock getClock() {
        return clock;
    }

    protected AccessManager getAccessManager() {
        return permissionManager.getAccessManager();
    }

    protected PermissionManager getPermissionManager() {
        return permissionManager;
    }

    protected ChannelAccess getChannelAccess() {
        return channelAccess;
    }

    protected HardwareManager getHardwareManager() {
        return hardwareManager;
    }

    protected WebAccessManager getWebAccessManager(AppID app) {
        return permissionManager.getWebAccess(app);
    }
    
    protected boolean closeWebAccessManager(AppID app) {
    	return permissionManager.closeWebAccess(app);
    }
    
}
