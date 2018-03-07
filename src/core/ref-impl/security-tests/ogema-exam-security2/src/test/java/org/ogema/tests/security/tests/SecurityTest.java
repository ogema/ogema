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
package org.ogema.tests.security.tests;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.locations.Room;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.PowerSensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.tests.security.testbase.CustomResourceType;
import org.ogema.tests.security.testbase.SecurityTestBase;
import org.ogema.tests.security.testbase.SecurityTestUtils;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SecurityTest extends SecurityTestBase {
	
	public SecurityTest() {
		super(false);
	}
	
	@Inject
	private BundleContext ctx;
	
	@Test
	public void testAppStarts() throws InvalidSyntaxException, BundleException, InterruptedException {
		// tests before and after methods
	}
	
	@Test(expected=SecurityException.class)
	public void missingCreatePermissionLeadsToError() throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationManager appMan = getApplicationManager();
		SecurityTestUtils.addResourcePermission(ctx, "*", Thermostat.class.getName(), appMan, "READ,WRITE,ADDSUB,DELETE,ACTIVITY");
		final Resource res = appMan.getResourceManagement().createResource(newResourceName(), Thermostat.class);
		Assert.assertNotNull(res);
		res.delete();
	}
	
	@Ignore("FIXME")
	@Test
	public void resourceCreatePermissionWorks0() throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, "*", Thermostat.class.getName(), appMan, "CREATE");
		try {
			appMan.getResourceManagement().createResource(resourceName, Thermostat.class);
		} catch (SecurityException e) {} // due to missing read permission; nevertheless, the resource should be created
		SecurityTestUtils.addResourcePermission(ctx, "*", Thermostat.class.getName(), appMan, "READ");
		Assert.assertNotNull(appMan.getResourceAccess().getResource(resourceName));
	}
	
	@Test
	public void resourceCreatePermissionWorks1() throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationManager appMan = getApplicationManager();
		SecurityTestUtils.addResourcePermission(ctx, null, Thermostat.class.getName(), appMan, "CREATE,READ");
		Assert.assertNotNull(appMan.getResourceManagement().createResource(newResourceName(), Thermostat.class));
	}
	
	@Test
	public void resourceCreatePermissionWorks2() throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, resourceName, null, appMan, "CREATE,READ");
		Assert.assertNotNull(appMan.getResourceManagement().createResource(resourceName, Thermostat.class));
	}
	
	@Test
	public void resourceCreateWorksForCustom() {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, null, CustomResourceType.class.getName(), appMan, "CREATE,READ");
		Assert.assertNotNull(appMan.getResourceManagement().createResource(resourceName, CustomResourceType.class));
	}
	
	@Test
	public void resourceCreateWorksForCustom2() {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, null, CustomResourceType.class.getName(), appMan, "CREATE,WRITE,READ,ADDSUB");
		@SuppressWarnings("unchecked")
		final ResourceList<CustomResourceType> list = appMan.getResourceManagement().createResource(resourceName, ResourceList.class);
		list.setElementType(CustomResourceType.class);
		Assert.assertNotNull(list.add());
	}
	
	@Test(expected=SecurityException.class)
	public void customResourceWithoutPermissionsFails() {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		final Resource r = appMan.getResourceManagement().createResource(resourceName, CustomResourceType.class);
		r.delete();
	}
	
	@Test(expected=SecurityException.class)
	public void customResourceWithoutPermissionsFails2() {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, resourceName, null, appMan, "CREATE,WRITE,READ");
		@SuppressWarnings("unchecked")
		final ResourceList<CustomResourceType> list = appMan.getResourceManagement().createResource(resourceName, ResourceList.class);
		list.setElementType(CustomResourceType.class);
		list.add();
		list.delete();
	}
	
	@Test(expected=SecurityException.class)
	public void missingDeletePermissionLeadsToError() throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationManager appMan = getApplicationManager();
		SecurityTestUtils.addResourcePermission(ctx, "*", Thermostat.class.getName(), appMan, "CREATE,READ");
		final Resource res = appMan.getResourceManagement().createResource(newResourceName(), Thermostat.class);
		Assert.assertNotNull(res);
		res.delete();
	}
	
	@Test
	public void deletePermissionWorks() throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, "*", Thermostat.class.getName(), appMan, "CREATE,READ,DELETE");
		final Resource res = appMan.getResourceManagement().createResource(resourceName, Thermostat.class);
		Assert.assertNotNull(res);
		res.delete();
	}
	
	@Test(expected=SecurityException.class)
	public void missingActivityPermissionLeadsToError() throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationManager appMan = getApplicationManager();
		SecurityTestUtils.addResourcePermission(ctx, "*", Thermostat.class.getName(), appMan, "CREATE,READ,WRITE,DELETE,ADDSUB");
		final Thermostat res = appMan.getResourceManagement().createResource(newResourceName(), Thermostat.class);
		Assert.assertNotNull(res);
		res.name().<StringResource> create().setValue("test");
		res.activate(true);
		res.delete();
	}
	
	@Test
	public void activationPermssionWorks() throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationManager appMan = getApplicationManager();
		SecurityTestUtils.addResourcePermission(ctx, "*", Thermostat.class.getName(), appMan, "CREATE,READ,WRITE,DELETE,ADDSUB,ACTIVITY");
		final Thermostat res = appMan.getResourceManagement().createResource(newResourceName(), Thermostat.class);
		Assert.assertNotNull(res);
		res.name().<StringResource> create().setValue("test");
		res.activate(true);
		res.delete();
	}
	
	@Test(expected=SecurityException.class)
	public void denyResourcePermssionWorks() throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		SecurityTestUtils.denyResourcePermission(ctx, null, Thermostat.class.getName(), appMan, "CREATE");
		SecurityTestUtils.addResourcePermission(ctx, "*", Thermostat.class.getName(), appMan, "*");
		final Thermostat res = appMan.getResourceManagement().createResource(resourceName, Thermostat.class);
		res.delete();
	}
	
	@Test
	public void multipleApplicableResourcePermssionsWorks() throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		// see all devices, create only thermostats
		SecurityTestUtils.addResourcePermission(ctx, null, PhysicalElement.class.getName(), appMan, "READ");
		SecurityTestUtils.addResourcePermission(ctx, null, Thermostat.class.getName(), appMan, "CREATE,READ,DELETE");
		final Thermostat res = appMan.getResourceManagement().createResource(resourceName, Thermostat.class);
		Assert.assertNotNull(res);
		res.delete();
	}
    
    /* if the permission contains a path, only resources on that path are returned */
    @Test
    public void getResourcesReturnsOnlyResourcesOnPermittedPaths() {
        final String topname = newResourceName();
        final String forbiddenTopLevel = newResourceName();
        final String allowedTopLevel = newResourceName();
        SensorDevice unrestrictedRes = getUnrestrictedAppManager().getResourceManagement().createResource(topname, SensorDevice.class);
        getUnrestrictedAppManager().getResourceManagement().createResource(forbiddenTopLevel, PowerSensor.class);
        getUnrestrictedAppManager().getResourceManagement().createResource(allowedTopLevel, PowerSensor.class);
        
        String allowedSubRes = unrestrictedRes.addDecorator("allowed", PowerSensor.class).getLocation();
        String forbiddenSubRes = unrestrictedRes.addDecorator("forbidden", PowerSensor.class).getLocation();
        Assert.assertEquals(4, getUnrestrictedAppManager().getResourceAccess().getResources(PowerSensor.class).size());
        
        SecurityTestUtils.addResourcePermission(ctx, allowedTopLevel,
                PowerSensor.class.getName(), getApplicationManager(), "READ");
        Assert.assertEquals(1, getApplicationManager().getResourceAccess().getResources(PowerSensor.class).size());
        
        SecurityTestUtils.addResourcePermission(ctx, topname,
                SensorDevice.class.getName(), getApplicationManager(), "READ");
        getApplicationManager().getResourceAccess().getResource(topname);
        SecurityTestUtils.addResourcePermission(ctx, allowedSubRes,
                PowerSensor.class.getName(), getApplicationManager(), "READ");
        
        Assert.assertEquals(2, getApplicationManager().getResourceAccess().getResources(PowerSensor.class).size());
        
        //cleanup
        unrestrictedRes.delete();
        getUnrestrictedAppManager().getResourceAccess().getResource(allowedTopLevel).delete();
        getUnrestrictedAppManager().getResourceAccess().getResource(forbiddenTopLevel).delete();
        Assert.assertEquals(0, getUnrestrictedAppManager().getResourceAccess().getResources(PowerSensor.class).size());
    }
    
    /* tests resource permission with type but no path */
    @Test
    public void getResourcesReturnsAllResourcesWhenPathIsUnspecified() {
        final String topname = newResourceName();
        final String top1 = newResourceName();
        final String top2 = newResourceName();
        SensorDevice unrestrictedRes = getUnrestrictedAppManager().getResourceManagement().createResource(topname, SensorDevice.class);
        getUnrestrictedAppManager().getResourceManagement().createResource(top1, PowerSensor.class);
        getUnrestrictedAppManager().getResourceManagement().createResource(top2, PowerSensor.class);
        
        String sub1 = unrestrictedRes.addDecorator("sub1", PowerSensor.class).getLocation();
        String sub2 = unrestrictedRes.addDecorator("sub2", PowerSensor.class).getLocation();
        String notAPowerSensor = unrestrictedRes.addDecorator("notAPowerSensor", PhysicalElement.class).getLocation();
        Assert.assertEquals(4, getUnrestrictedAppManager().getResourceAccess().getResources(PowerSensor.class).size());
        
        SecurityTestUtils.addResourcePermission(ctx, null,
                PowerSensor.class.getName(), getApplicationManager(), "READ");
        
        try {
            Resource r = getApplicationManager().getResourceAccess().getResource(notAPowerSensor);
            Assert.fail("got resource without permission: " + r);
        } catch (SecurityException se) {
            // expected exception!
        }
        
        Assert.assertEquals(4, getApplicationManager().getResourceAccess().getResources(PowerSensor.class).size());
        
        SecurityTestUtils.denyResourcePermission(ctx, sub2, null, getApplicationManager(), "read");
        SecurityTestUtils.printBundlePermissions(getApplicationManager().getAppID().getBundle(), System.out);
        Assert.assertEquals(3, getApplicationManager().getResourceAccess().getResources(PowerSensor.class).size());
        //cleanup
        getUnrestrictedAppManager().getResourceAccess().getResource(topname).delete();
        getUnrestrictedAppManager().getResourceAccess().getResource(top1).delete();
        getUnrestrictedAppManager().getResourceAccess().getResource(top2).delete();
        Assert.assertEquals(0, getUnrestrictedAppManager().getResourceAccess().getResources(PowerSensor.class).size());
    }

	@Test
	public void multipleApplicableResourcePermssionsWorks2() throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		// see all thermostats, create only a specific one
		SecurityTestUtils.addResourcePermission(ctx, null, Thermostat.class.getName(), appMan, "READ");
		SecurityTestUtils.addResourcePermission(ctx, resourceName, Thermostat.class.getName(), appMan, "CREATE,READ,DELETE");
		final Thermostat res = appMan.getResourceManagement().createResource(resourceName, Thermostat.class);
		Assert.assertNotNull(res);
		final Thermostat otherRes = getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), Thermostat.class);
		Assert.assertNotNull(appMan.getResourceAccess().getResource(otherRes.getLocation()));
		res.delete();
		otherRes.delete();
		try {
			appMan.getResourceManagement().createResource(newResourceName(), Thermostat.class);
			throw new AssertionError("Resource creation succeeded despite missing permission");
		} catch (SecurityException expected) {}
		
	}
	
	@Test
	public void getSubresourcesWorksInPresenceOfForbiddenDecorator() {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, resourceName + "/*", Thermostat.class.getName(), appMan, "*");
		final Thermostat thermo = appMan.getResourceManagement().createResource(resourceName, Thermostat.class);
		final Room forbidden = getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), Room.class);
		getUnrestrictedAppManager().getResourceAccess().getResource(resourceName).addDecorator("room", forbidden);
		final List<Resource> subresources = thermo.getSubResources(false);
		Assert.assertEquals(0, subresources.size()); // tbd: should the restricted app see the referencing resource at all?
        /*
		try {
			final Room room2 = subresources.get(0).getLocationResource();
			throw new AssertionError("Subresource " + room2 + " should not be accessible to the restricted app manager");
		} catch (SecurityException expected) {}
        */
		thermo.delete();
		forbidden.delete();
	}
	
	@Test
	public void getAllElementsWorksInPresenceOfForbiddenElement() throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, resourceName + "/*", null, appMan, "*");
		final ResourceList<?> list = appMan.getResourceManagement().createResource(resourceName, ResourceList.class);
		list.setElementType(Thermostat.class);
		final Thermostat forbidden = getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), Thermostat.class);
		list.add();
		getUnrestrictedAppManager().getResourceAccess().<ResourceList<Thermostat>> getResource(resourceName).addDecorator("decorator", forbidden);
		final List<?> subresources = list.getAllElements();
        //TODO discuss: number of readable list elements < actual list size
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(1, subresources.size());
		list.delete();
		forbidden.delete();
	}
	
	@Test
	public void getResourcesWorksInPresenceOfForbiddenSubresource() {
		final ApplicationManager appMan = getApplicationManager();
		final String resourceName = newResourceName();
		final String decorator = "test";
		SecurityTestUtils.denyResourcePermission(ctx, resourceName + "/" + decorator, null, appMan, "READ");
		SecurityTestUtils.addResourcePermission(ctx, null, TemperatureSensor.class.getName(), appMan, "READ");
		SecurityTestUtils.addResourcePermission(ctx, null, Room.class.getName(), appMan, "READ");
		
		final Room room = getUnrestrictedAppManager().getResourceManagement().createResource(resourceName, Room.class);
		final Thermostat denied = room.addDecorator(decorator, Thermostat.class);
		final TemperatureSensor tempSens = denied.temperatureSensor().create();
		try {
			appMan.getResourceAccess().getResource(resourceName + "/" + decorator);
			Assert.fail("Resource access to forbidden path succeeded");
		} catch (SecurityException expected) {}
		final Room room2 = appMan.getResourceAccess().getResource(room.getPath());
		try {
			room2.getSubResource(decorator);
			Assert.fail("Resource access to forbidden path succeeded");
		} catch (SecurityException expected) {}
		
		final List<TemperatureSensor> sensors = appMan.getResourceAccess().getResources(TemperatureSensor.class);
		boolean found = false;
		for (TemperatureSensor ts : sensors) {
			if (ts.getPath().equals(tempSens.getPath())) {
				found = true;
				break;
			}
		}
		Assert.assertTrue("Temperature sensor not found",found);
		room.delete();
	}
	
	@Test
	public void typePermissionImpliesSubresourcePermission() {
		final ApplicationManager appMan = getApplicationManager();
		SecurityTestUtils.addResourcePermission(ctx, null, Room.class.getName(), appMan, "READ");
		final String resourceName = newResourceName();
		final Room room = getUnrestrictedAppManager().getResourceManagement().createResource(resourceName, Room.class);
		room.temperatureSensor().create();
		final List<TemperatureSensor> sensors = appMan.getResourceAccess().getResources(TemperatureSensor.class);
		boolean found = false;
		for (TemperatureSensor ts : sensors) {
			if (ts.getPath().equals(room.temperatureSensor().getPath())) {
				found = true;
				break;
			}
		}
		Assert.assertTrue("Temperature sensor not found",found);
		room.delete();
	}
    
    @Test public void canDenyWriteFromReadWriteAvailableResource() {
        final ApplicationManager appMan = getApplicationManager();
        SecurityTestUtils.addResourcePermission(ctx, null, TemperatureSensor.class.getName(), appMan, "CREATE,READ,WRITE,DELETE,ADDSUB,ACTIVITY");
        //FIXME? deny cannot be set dynamically after resource is available, because access rights are cached.
        SecurityTestUtils.denyResourcePermission(ctx, null, TemperatureSensor.class.getName(), appMan, "WRITE,DELETE,ADDSUB,ACTIVITY");
        TemperatureSensor tempSensU = getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
        tempSensU.reading().create();
        tempSensU.reading().setCelsius(27.0f);
        TemperatureSensor tempSens = appMan.getResourceAccess().getResource(tempSensU.getPath());
        Assert.assertEquals(27.0f, tempSens.reading().getCelsius(), 0f);
        try {
            tempSens.reading().setCelsius(29.0f);
            Assert.fail("should not have write permission");
        } catch (SecurityException e) {
            //expected
        }
    }
	
	@Test
	public void getResourcesDoesNotReturnSubresourcesOnDeniedResourceType() {
		final ApplicationManager appMan = getApplicationManager();
		SecurityTestUtils.addResourcePermission(ctx, null, TemperatureSensor.class.getName(), appMan, "READ");
		SecurityTestUtils.denyResourcePermission(ctx, null, Room.class.getName(), appMan, "READ");
		final String resourceName = newResourceName();
		final Room room = getUnrestrictedAppManager().getResourceManagement().createResource(resourceName, Room.class);
		TemperatureSensor deniedTempSens = room.temperatureSensor().create(); //room is denied
        TemperatureSensor allowedTempSens =
                getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		final List<TemperatureSensor> sensors = appMan.getResourceAccess().getResources(TemperatureSensor.class);
		Assert.assertTrue("Temperature sensor not found", sensors.contains(allowedTempSens));
        Assert.assertFalse("Temperature in denied Room found", sensors.contains(deniedTempSens));
		room.delete();
        allowedTempSens.delete();
	}
    
    @Test
	public void getResourcesDoesNotReturnSubresourcesOnDeniedPath() {
		final ApplicationManager appMan = getApplicationManager();
        final String deniedPath = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, null, TemperatureSensor.class.getName(), appMan, "READ");
		SecurityTestUtils.denyResourcePermission(ctx, deniedPath + "/*", null, appMan, "READ");
		
		final Room room = getUnrestrictedAppManager().getResourceManagement().createResource(deniedPath, Room.class);
		TemperatureSensor deniedTempSens = room.temperatureSensor().create(); //room is denied
        TemperatureSensor allowedTempSens =
                getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		final List<TemperatureSensor> sensors = appMan.getResourceAccess().getResources(TemperatureSensor.class);
		Assert.assertTrue("Temperature sensor not found", sensors.contains(allowedTempSens));
        Assert.assertFalse("Temperature in denied Room found", sensors.contains(deniedTempSens));
		room.delete();
        allowedTempSens.delete();
	}
    
    @Test
	public void getResourcesDoesNotReturnResourcesDeniedByPath() {
		final ApplicationManager appMan = getApplicationManager();
        final String deniedPath = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, null, TemperatureSensor.class.getName(), appMan, "READ");
		SecurityTestUtils.denyResourcePermission(ctx, deniedPath, null, appMan, "READ");
		TemperatureSensor deniedTempSens =
                getUnrestrictedAppManager().getResourceManagement().createResource(deniedPath, TemperatureSensor.class);
        TemperatureSensor allowedTempSens =
                getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		final List<TemperatureSensor> sensors = appMan.getResourceAccess().getResources(TemperatureSensor.class);
		Assert.assertTrue("Temperature sensor not found", sensors.contains(allowedTempSens));
        Assert.assertFalse("Temperature in denied Room found", sensors.contains(deniedTempSens));
        allowedTempSens.delete();
        deniedTempSens.delete();
	}
	
    @Test
    public void demandListenerWorks() throws InterruptedException {
    	final ApplicationManager appMan = getApplicationManager();
        final CountDownLatch availableLatch = new CountDownLatch(1);
        final AtomicReference<TemperatureSensor> lastAvailable = new AtomicReference<TemperatureSensor>(null);
        final ResourceDemandListener<TemperatureSensor> listener = new ResourceDemandListener<TemperatureSensor>() {

			@Override
			public void resourceAvailable(TemperatureSensor resource) {
				lastAvailable.set(resource);
				availableLatch.countDown();
			}

			@Override
			public void resourceUnavailable(TemperatureSensor resource) {}
        	
        };
        appMan.getResourceAccess().addResourceDemand(TemperatureSensor.class, listener);
        final ResourceManagement resMan = getUnrestrictedAppManager().getResourceManagement();
        final TemperatureSensor sensor = resMan.createResource(newResourceName(), TemperatureSensor.class);
        sensor.activate(false);
        Assert.assertNotNull(sensor);
        Assert.assertFalse("Callback despite missing permission",availableLatch.await(2, TimeUnit.SECONDS));
        Assert.assertNull(lastAvailable.get());
        final String path = newResourceName();
    	SecurityTestUtils.addResourcePermission(ctx, path, null, appMan, "READ");
        final TemperatureSensor sensor2 = resMan.createResource(path, TemperatureSensor.class);
        sensor2.activate(false);
        Assert.assertTrue("Callback missing",availableLatch.await(5, TimeUnit.SECONDS));
        Assert.assertNotNull(lastAvailable.get());
        Assert.assertEquals(sensor2.getLocation(), lastAvailable.get().getLocation());
        appMan.getResourceAccess().removeResourceDemand(TemperatureSensor.class, listener);
        sensor.delete();
        sensor2.delete();
    }
    
    @Test
    public void structureListenerWorksWithNewReference() throws InterruptedException {
    	final Map<EventType, CountDownLatch> latches = new EnumMap<>(EventType.class);
    	for (EventType type : EventType.values())
    		latches.put(type, new CountDownLatch(1));
    	final ResourceStructureListener listener = new ResourceStructureListener() {
			
			@Override
			public void resourceStructureChanged(ResourceStructureEvent event) {
				latches.get(event.getType()).countDown();
			}
		};
		final String path = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, path, null, getApplicationManager(), "READ");
		final Room room0 = getUnrestrictedAppManager().getResourceManagement().createResource(path, Room.class);
		room0.activate(false);
		final Room room1 = getApplicationManager().getResourceAccess().getResource(path);
		Assert.assertNotNull(room1);
		room1.addStructureListener(listener);
		final TemperatureSensor sensor = getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		sensor.activate(false);
		sensor.location().room().setAsReference(room0);
		Assert.assertTrue(sensor.location().room().isActive());
		Assert.assertFalse("Callback despite missing permission", latches.get(EventType.REFERENCE_ADDED).await(2, TimeUnit.SECONDS));
		final String path2 = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, path2 + "/*", null, getApplicationManager(), "READ");
		final TemperatureSensor sensor2 = getUnrestrictedAppManager().getResourceManagement().createResource(path2, TemperatureSensor.class);
		sensor2.activate(false);
		sensor2.location().room().setAsReference(room0);
		Assert.assertTrue(sensor2.location().room().isActive());
		Assert.assertTrue("Callback missing", latches.get(EventType.REFERENCE_ADDED).await(5, TimeUnit.SECONDS));
		room1.removeStructureListener(listener);
		room0.delete();
		sensor.delete();
		sensor2.delete();
    }
    
    @Test
    public void structureListenerWorksWithNewReference2() throws InterruptedException {
    	final Map<EventType, CountDownLatch> latches = new EnumMap<>(EventType.class);
    	for (EventType type : EventType.values())
    		latches.put(type, new CountDownLatch(1));
    	final ResourceStructureListener listener = new ResourceStructureListener() {
			
			@Override
			public void resourceStructureChanged(ResourceStructureEvent event) {
				latches.get(event.getType()).countDown();
			}
		};
		final String path = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, path + "/*", null, getApplicationManager(), "READ");
		final TemperatureSensor sensor = getUnrestrictedAppManager().getResourceManagement().createResource(path, TemperatureSensor.class);
		sensor.location().room().create();
		sensor.activate(true);
		final Room room0 = getApplicationManager().getResourceAccess().getResource(sensor.location().room().getPath());
		Assert.assertNotNull(room0);
		room0.addStructureListener(listener);
		final Room room1 = getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), Room.class);
		room1.activate(false);
		sensor.location().room().setAsReference(room1);
		Assert.assertTrue(sensor.location().room().isReference(false));
		Assert.assertTrue("Delete callback missing",latches.get(EventType.RESOURCE_DELETED).await(5, TimeUnit.SECONDS));
		Assert.assertTrue("Created callback missing",latches.get(EventType.RESOURCE_CREATED).await(5, TimeUnit.SECONDS));
		room0.removeStructureListener(listener);
		sensor.delete();
		room1.delete();
    }
    
    @Test
    public void structureListenerWorksWithSubresource() throws InterruptedException {
    	final Map<EventType, CountDownLatch> latches = new EnumMap<>(EventType.class);
    	for (EventType type : EventType.values())
    		latches.put(type, new CountDownLatch(1));
    	final ResourceStructureListener listener = new ResourceStructureListener() {
			
			@Override
			public void resourceStructureChanged(ResourceStructureEvent event) {
				latches.get(event.getType()).countDown();
			}
		};
		final String path = newResourceName();
		SecurityTestUtils.addResourcePermission(ctx, path, null, getApplicationManager(), "READ");
		final Room room0 = getUnrestrictedAppManager().getResourceManagement().createResource(path, Room.class);
		room0.activate(false);
		final Room room1 = getApplicationManager().getResourceAccess().getResource(path);
		Assert.assertNotNull(room1);
		room1.addStructureListener(listener);
		room0.name().create().activate(false);
		Assert.assertTrue(room0.name().isActive());
		Assert.assertFalse("Callback despite missing permission", latches.get(EventType.SUBRESOURCE_ADDED).await(2, TimeUnit.SECONDS));
		SecurityTestUtils.addResourcePermission(ctx, path + "/*", null, getApplicationManager(), "READ");
		room0.temperatureSensor().create().activate(false);
		Assert.assertTrue(room0.temperatureSensor().isActive());
		Assert.assertTrue("Callback missing", latches.get(EventType.SUBRESOURCE_ADDED).await(5, TimeUnit.SECONDS));
		room0.delete();
    }
    
    
}
