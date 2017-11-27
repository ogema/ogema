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

import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.locations.Room;
import org.ogema.model.prototypes.PhysicalElement;
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
		SecurityTestUtils.addResourcePermission(ctx, resourceName + "*", Thermostat.class.getName(), appMan, "*");
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
		SecurityTestUtils.addResourcePermission(ctx, resourceName + "*", null, appMan, "*");
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
	
}
