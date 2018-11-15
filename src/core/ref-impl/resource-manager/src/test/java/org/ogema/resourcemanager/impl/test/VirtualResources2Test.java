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
package org.ogema.resourcemanager.impl.test;

import org.ogema.exam.DemandTestListener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.exam.TestApplication;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Tests for virtual resources using 2 apps.
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class VirtualResources2Test extends OsgiTestBase {

	TestApplication appTwo = new TestApplication() {
	};

	@Before
	public void register2ndApp() throws InterruptedException {
		appTwo.registerAndAwaitStart(ctx);
	}

	/* When 2 Apps hold a reference to the same virtual resource, a create
	call in one app will be visible in the other app. */
	@Test
	public void changesBy1AppAreVisibleInOtherApp() {
		final String rname = newResourceName();
		CoolingDevice cool1 = resMan.createResource(rname, CoolingDevice.class);

		CoolingDevice cool2 = appTwo.getAppMan().getResourceAccess().getResource(rname);

		assertNotNull(cool2);

		FloatResource mmxC1 = cool1.electricityConnection().powerSensor().reading();
		FloatResource mmxC2 = cool2.electricityConnection().powerSensor().reading();

		assertEquals(mmxC1, mmxC2);

		ElectricityConnection elConn2 = cool2.electricityConnection();

		assertFalse(mmxC1.exists());
		assertFalse(mmxC2.exists());

		mmxC1.create();
		assertTrue(mmxC1.exists());
		assertTrue(mmxC2.exists());
		assertTrue(elConn2.exists());
	}

	@Test
    public void delete2test() throws InterruptedException {
        DemandTestListener<CoolingDevice> l = new DemandTestListener<>();
        resAcc.addResourceDemand(CoolingDevice.class, l);
        
        CoolingDevice cool1 = resMan.createResource(newResourceName(), CoolingDevice.class);
        l.setExpectedResource(cool1);
        
        cool1.activate(true);
        assertTrue(l.awaitAvailable());        
        
        CoolingDevice coolApp2 = appTwo.getAppMan().getResourceAccess().getResource(cool1.getPath());
        assertEquals(cool1, coolApp2);
        
        coolApp2.delete();
        assertFalse(cool1.exists());
        assertFalse(coolApp2.exists());
        assertTrue(l.awaitUnavailable());
        resAcc.removeResourceDemand(CoolingDevice.class, l);
    }

	@Test
    public void delete2test_sub() throws InterruptedException {
        CoolingDevice cool1 = resMan.createResource(newResourceName(), CoolingDevice.class);
        
        DemandTestListener<ElectricityConnection> l = new DemandTestListener<>(cool1.electricityConnection());
        resAcc.addResourceDemand(ElectricityConnection.class, l);
        
        cool1.electricityConnection().create();
        cool1.activate(true);
        
        assertTrue(l.awaitAvailable());        
        
        CoolingDevice coolApp2 = appTwo.getAppMan().getResourceAccess().getResource(cool1.getPath());
        assertEquals(cool1, coolApp2);
        
        coolApp2.electricityConnection().delete();
        assertFalse(cool1.electricityConnection().exists());
        assertFalse(coolApp2.electricityConnection().exists());
        assertTrue(l.awaitUnavailable());
        resAcc.removeResourceDemand(ElectricityConnection.class, l);
    }

	@Test
    public void delete2testX() throws InterruptedException {
        CoolingDevice cool1 = resMan.createResource(newResourceName(), CoolingDevice.class);
        CoolingDevice coolApp2 = appTwo.getAppMan().getResourceAccess().getResource(cool1.getPath());
        assertEquals(cool1, coolApp2);
        
        DemandTestListener<CoolingDevice> l = new DemandTestListener<>(coolApp2);
        appTwo.getAppMan().getResourceAccess().addResourceDemand(CoolingDevice.class, l);
        
        cool1.activate(true);
        assertTrue(l.awaitAvailable());        
        
        cool1.delete();
        assertFalse(cool1.exists());
        assertFalse(coolApp2.exists());
        assertTrue(l.awaitUnavailable());
        appTwo.getAppMan().getResourceAccess().removeResourceDemand(CoolingDevice.class, l);
    }
}
