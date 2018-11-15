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
package org.ogema.administration.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.administration.RegisteredAccessModeRequest;
import org.ogema.core.administration.RegisteredResourceDemand;
import org.ogema.core.administration.RegisteredStructureListener;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.actors.OnOffSwitch;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.ServiceReference;

/**
 * 
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class ApplicationAdminTest extends OsgiAppTestBase {

	@Test
	public void applicationAdminIsAvailable() throws Exception {
		AdministrationManager admin = getApplicationManager().getAdministrationManager();
		assertNotNull(admin);
		AppID appId = getApplicationManager().getAppID();
		String id = appId.getIDString();
		AdminApplication appAdmin = admin.getAppById(id);

		System.out.println(app);
		for (ServiceReference<AdminApplication> sr : ctx.getServiceReferences(AdminApplication.class, null)) {
			System.out.printf("%s, appId=%s%n", sr, sr.getProperty("appId"));
		}

		assertNotNull(appAdmin);
	}

	AdminApplication getAdminApplication() {
		AdministrationManager admin = getApplicationManager().getAdministrationManager();
		assertNotNull(admin);

		// XXX ? getIDString
		AdminApplication appAdmin = admin.getAppById(getApplicationManager().getAppID().getIDString());
		assertEquals(getApplicationManager().getAppID(), appAdmin.getID());

		return appAdmin;
	}

	@Test
    @SuppressWarnings("deprecation")
	public void registeredResourceListenersAreAvailableViaAdministration() {
		OnOffSwitch sw = getApplicationManager().getResourceManagement().createResource(newResourceName(),
				OnOffSwitch.class);

		AdminApplication appAdmin = getAdminApplication();

		assertTrue(appAdmin.getResourceListeners().isEmpty());

		org.ogema.core.resourcemanager.ResourceListener l = new org.ogema.core.resourcemanager.ResourceListener() {

			@Override
			public void resourceChanged(Resource resource) {

			}
		};
		sw.stateFeedback().addResourceListener(l, false);

		assertFalse(appAdmin.getResourceListeners().isEmpty());
		assertTrue(appAdmin.getResourceListeners().get(0).getListener() == l);
	}
    
    @Test
	public void registeredValueListenersAreAvailableViaAdministration() {
		OnOffSwitch sw = getApplicationManager().getResourceManagement().createResource(newResourceName(),
				OnOffSwitch.class);

		AdminApplication appAdmin = getAdminApplication();

		assertTrue(appAdmin.getResourceListeners().isEmpty());

		ResourceValueListener<Resource> l = new ResourceValueListener<Resource>() {

            @Override
            public void resourceChanged(Resource resource) {
            }
        };
		sw.stateFeedback().addValueListener(l, false);

		assertFalse(appAdmin.getValueListeners().isEmpty());
		assertTrue(appAdmin.getValueListeners().get(0).getValueListener() == l);
	}

	@Test
	public void registeredAccessDemandsAreAvailable() {
		OnOffSwitch sw = getApplicationManager().getResourceManagement().createResource(newResourceName(),
				OnOffSwitch.class);
		sw.stateControl().create();

		AdminApplication appAdmin = getAdminApplication();

		assertTrue(appAdmin.getAccessModeRequests().isEmpty());

		sw.stateControl().requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_DEVICESPECIFIC);

		assertFalse(appAdmin.getAccessModeRequests().isEmpty());

		RegisteredAccessModeRequest amr = appAdmin.getAccessModeRequests().get(0);

		assertEquals(sw.stateControl(), amr.getResource());
		assertEquals(AccessMode.EXCLUSIVE, amr.getRequiredAccessMode());
		assertEquals(appAdmin, amr.getApplication());

		sw.stateControl().requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
		assertTrue(appAdmin.getAccessModeRequests().isEmpty());
	}

	@Test
	public void registeredStructureListenersAreAvailable() {
		OnOffSwitch sw = getApplicationManager().getResourceManagement().createResource(newResourceName(),
				OnOffSwitch.class);

		ResourceStructureListener sl = new ResourceStructureListener() {

			@Override
			public void resourceStructureChanged(ResourceStructureEvent event) {

			}
		};

		AdminApplication appAdmin = getAdminApplication();

		assertTrue(appAdmin.getStructureListeners().isEmpty());
		sw.addStructureListener(sl);

		assertFalse(appAdmin.getStructureListeners().isEmpty());
		RegisteredStructureListener rsl = appAdmin.getStructureListeners().get(0);

		assertEquals(sw, rsl.getResource());
		assertEquals(appAdmin, rsl.getApplication());
		assertEquals(sl, rsl.getListener());

		sw.removeStructureListener(sl);
		assertTrue(appAdmin.getStructureListeners().isEmpty());
	}

	@Test
	public void resourceDemandsAreAvailable() {
		AdminApplication appAdmin = getAdminApplication();

		ResourceDemandListener<OnOffSwitch> l = new ResourceDemandListener<OnOffSwitch>() {

			@Override
			public void resourceAvailable(OnOffSwitch resource) {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			@Override
			public void resourceUnavailable(OnOffSwitch resource) {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};

		assertTrue(appAdmin.getResourceDemands().isEmpty());

		getApplicationManager().getResourceAccess().addResourceDemand(OnOffSwitch.class, l);
		assertFalse(appAdmin.getResourceDemands().isEmpty());

		RegisteredResourceDemand rrd = appAdmin.getResourceDemands().get(0);
		assertEquals(appAdmin, rrd.getApplication());
		assertEquals(OnOffSwitch.class, rrd.getTypeDemanded());
		assertEquals(l, rrd.getListener());

		getApplicationManager().getResourceAccess().removeResourceDemand(OnOffSwitch.class, l);
		assertTrue(appAdmin.getResourceDemands().isEmpty());
	}

	@Test
	public void registeredTimersAreAvailable() {
		AdminApplication appAdmin = getAdminApplication();

		TimerListener tl = new TimerListener() {

			@Override
			public void timerElapsed(Timer timer) {
			}
		};

		assertTrue(appAdmin.getTimers().isEmpty());

		getApplicationManager().createTimer(500, tl);
		assertFalse(appAdmin.getTimers().isEmpty());
		assertFalse(appAdmin.getTimers().get(0).getListeners().isEmpty());

		assertEquals(tl, appAdmin.getTimers().get(0).getListeners().get(0));
	}

}
