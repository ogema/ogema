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
package org.ogema.resourcemanager.impl.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.locations.PhysicalDimensions;
import org.ogema.model.metering.ElectricityMeter;
import org.ogema.model.ranges.BinaryRange;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class ResourceTest extends OsgiTestBase {

	public static final String RESNAME = "ResourceTestResource";

	@Before
	@Override
	public void doBefore() {
	}

	@ProbeBuilder
	public TestProbeBuilder customize(TestProbeBuilder builder) {
		return builder;
	}

	@Test
	public void addOptionalElementWorks() throws ResourceException {
		OnOffSwitch res = resMan.createResource(newResourceName(), OnOffSwitch.class);
		Resource opt = res.addOptionalElement("stateControl");
		assertNotNull(opt);
		assertFalse(opt.isActive());
		assertEquals("stateControl", opt.getName());
		Resource child = res.getSubResource("stateControl");
		assertNotNull(child);
		assertEquals("stateControl", child.getName());
		assertEquals(opt, child);
		assertFalse(res.stateControl().isDecorator());
	}

	@Test
	public void resourcesWithOverriddenElementsWork() throws ResourceException {
		ElectricityMeter res = resMan.createResource(newResourceName(), ElectricityMeter.class);
		ElectricityConnection conn = (ElectricityConnection) res.addOptionalElement("connection");
		assertNotNull(conn);
	}

	@Test(expected = NoSuchResourceException.class)
	public void addOptionalElementThrowsExceptionOnNonExistingElement() throws ResourceException {
		Resource res = resMan.createResource(newResourceName(), OnOffSwitch.class);
		try {
			res.addOptionalElement("stat_Control");
		} catch (NoSuchResourceException nsre) {
			throw nsre;
		}
	}

	@Test
	// if the optional element already exists, addOptionalElement simply returns it
	public void addOptionalElementReturnsExistingElement() {
		OnOffSwitch res = resMan.createResource(newResourceName(), OnOffSwitch.class);
		Resource opt = res.addOptionalElement("stateControl");
		assertNotNull(opt);
		assertNotNull(res.stateControl());
		assertFalse(opt.isActive());
		assertEquals("stateControl", opt.getName());
		Resource child = res.getSubResource("stateControl");
		assertNotNull(child);
		assertEquals("stateControl", child.getName());
		assertEquals(opt, child);
		opt.activate(true);
		res.stateControl().setValue(true);
		BooleanResource stateControl = (BooleanResource) res.addOptionalElement("stateControl");
		assertEquals(child, stateControl);
		assertTrue(stateControl.isActive());
		assertTrue(stateControl.getValue());
	}

	@Test
	public void addOptionalElementReplacesExistingReference() throws Exception {
		OnOffSwitch sw1 = resMan.createResource("switch1", OnOffSwitch.class);
		OnOffSwitch sw2 = resMan.createResource("switch2", OnOffSwitch.class);
		BooleanResource ctrl2 = (BooleanResource) sw2.addOptionalElement("stateControl");
		sw1.setOptionalElement("stateControl", ctrl2);
		ctrl2.setValue(true);
		assertEquals(ctrl2.getValue(), sw1.stateControl().getValue());
		ctrl2.setValue(false);
		assertEquals(ctrl2.getValue(), sw1.stateControl().getValue());
		BooleanResource ctrl1 = (BooleanResource) sw1.addOptionalElement("stateControl");
		ctrl1.setValue(true);
		assertNotSame(ctrl2.getValue(), ctrl1.getValue());
	}

	@Test
	public void setOptionalWithReferenceWorks() {
		OnOffSwitch test1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch test2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		Resource ratedValues = test1.ratedValues().create();
		test2.setOptionalElement("ratedValues", ratedValues);
		Resource b = test2.ratedValues();
		assertNotNull(b);
		assertFalse(test2.ratedValues().isDecorator());
	}

	@Test
	public void setOptionalWithReferenceReplacesExistingElement() {
		OnOffSwitch test1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch test2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		Resource a = test1.ratedValues().create();
		Resource b = test2.ratedValues().create();
		//		Resource a = test1.addOptionalElement("comInfo");
		//		Resource b = test2.addOptionalElement("comInfo");
		assert !a.equalsLocation(b);
		test2.setOptionalElement("ratedValues", a);
		assert a.equalsLocation(test2.ratedValues());
	}

	@Test(expected = NoSuchResourceException.class)
	public void setOptionalThrowsExceptionForNonExistentElement() {
		OnOffSwitch test1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch test2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		Resource a = test1.addOptionalElement("comInfo");
		test2.setOptionalElement("com_Info", a);
	}

	@Test(expected = ResourceException.class)
	public void setOptionalThrowsExceptionOnTypeMismatch() {
		OnOffSwitch test1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch test2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		test1.setOptionalElement("comInfo", test2);
	}

	@Test
	public void addDecoratorWorks() throws Exception {
		OnOffSwitch elSwitch = resMan.createResource("addDecoratorTest" + counter++, OnOffSwitch.class);
		Resource dec = elSwitch.addDecorator("foo", OnOffSwitch.class);
		assertNotNull(dec);
		assertFalse(dec.isActive());
		assertTrue(dec.exists());
		dec = elSwitch.getSubResource("foo");
		assertNotNull(dec);
		assertEquals("foo", dec.getName());
		assertEquals(OnOffSwitch.class, dec.getResourceType());
		assertTrue(elSwitch.getSubResource("foo").isDecorator());
	}

	@Test
	public void addDecoratorWithReferenceWorks() throws Exception {
		OnOffSwitch elSwitch = resMan.createResource("test" + counter++, OnOffSwitch.class);
		// elSwitch.physDim()
		Resource physDim = elSwitch.addOptionalElement("physDim");
		elSwitch.addDecorator("foo", physDim);
		Resource dec = elSwitch.getSubResource("foo");
		assertNotNull(dec);
		assertEquals("foo", dec.getName());
		assertEquals(PhysicalDimensions.class, dec.getResourceType());
		assertTrue(elSwitch.getSubResource("foo").isDecorator());
	}

	@Test
	public void getLocationWorks() {
		OnOffSwitch elSwitch = resMan.createResource("test" + counter++, OnOffSwitch.class);
		String topName = elSwitch.getName();
		elSwitch.addOptionalElement("physDim");
		String physDimLocation = topName + "/physDim";
		assertEquals(physDimLocation, elSwitch.physDim().getLocation("/"));
		elSwitch.addDecorator("foo", elSwitch.physDim());
		assertEquals(physDimLocation, elSwitch.getSubResource("foo").getLocation("/"));
	}

	@Test
	public void equalsLocationWorks() {
		OnOffSwitch elSwitch = resMan.createResource("test" + counter++, OnOffSwitch.class);
		String topName = elSwitch.getName();
		elSwitch.addOptionalElement("physDim");
		elSwitch.addDecorator("foo", elSwitch.physDim());
		assertTrue("must the same location", elSwitch.physDim().equalsLocation(elSwitch.getSubResource("foo")));
		assertFalse("must be different path", elSwitch.physDim().equalsPath(null));
		assertFalse("must be different location", elSwitch.physDim().equalsLocation(elSwitch));
	}

	@Test
	public void addDecoratorWithReferenceReplacesExistingDecoratorOfSameType() throws Exception {
		OnOffSwitch elSwitch = resMan.createResource("test" + counter++, OnOffSwitch.class);
		Resource physDim = elSwitch.addOptionalElement("physDim");
		elSwitch.addDecorator("foo", physDim);
		assertNotNull(elSwitch.getSubResource("foo"));
		Resource physDim2 = elSwitch.addDecorator("bar", PhysicalDimensions.class);
		elSwitch.addDecorator("foo", physDim2);
		assertNotNull(elSwitch.getSubResource("foo"));
		Resource replacedDecorator = elSwitch.getSubResource("foo");
		assertTrue("not the same resource", physDim2.equalsLocation(elSwitch.getSubResource("foo")));
	}

	@Test(expected = ResourceAlreadyExistsException.class)
	public void addDecoratorWithReferenceBarfsOnExistingDecoratorOfIncompatibleType() throws Exception {
		OnOffSwitch elSwitch = resMan.createResource("test" + counter++, OnOffSwitch.class);
		Resource physDim = elSwitch.addOptionalElement("physDim");
		elSwitch.addDecorator("foo", physDim);
		assertNotNull(elSwitch.getSubResource("foo"));
		Resource notAPhysDim = elSwitch.addDecorator("bar", FloatResource.class);
		elSwitch.addDecorator("foo", notAPhysDim);
	}

	@Test(expected = ResourceAlreadyExistsException.class)
	public void addDecoratorFailsOnOptionalElementIfTypeIsIncompatible() {
		OnOffSwitch elSwitch = resMan.createResource("test" + counter++, OnOffSwitch.class);
		elSwitch.addDecorator("physDim", OnOffSwitch.class);
	}

	@Test(expected = ResourceAlreadyExistsException.class)
	public void addDecoratorWithReferenceFailsOnOptionalElementsIfTypeIsIncompatible() {
		OnOffSwitch elSwitch = resMan.createResource("test", OnOffSwitch.class);
		Resource physDim = elSwitch.physDim().create();
		elSwitch.addDecorator("ratedValues", physDim);
	}

	@Test
	public void addDecoratorWorksOnOptionalElementWithCompatibleType() {
		OnOffSwitch elSwitch = resMan.createResource("test", OnOffSwitch.class);
		Resource physDim = elSwitch.physDim().create();
		elSwitch.addDecorator("ratedValues", BinaryRange.class);
	}

	@Test
	public void addDecoratorWithReferenceWorksOnOptionalElementWithCompatibleType() {
		OnOffSwitch elSwitch = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch elSwitch2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		elSwitch2.ratedValues().create();

		Resource physDim = elSwitch.physDim().create();
		elSwitch.addDecorator("ratedValues", elSwitch2.ratedValues());
	}

	@Test
	public void getSubresourceWorks() {
		OnOffSwitch elSwitch = resMan.createResource(newResourceName(), OnOffSwitch.class);
		Resource physDim = elSwitch.addOptionalElement("physDim");
		assertEquals(physDim, elSwitch.getSubResource("physDim"));
		assertNull(elSwitch.getSubResource("fnord"));
	}

	@Test
    public void getSubResourcesWorks() {
        OnOffSwitch elSwitch = resMan.createResource("test" + counter++, OnOffSwitch.class);
        assertTrue(elSwitch.getSubResources(false).isEmpty());
        HashSet<Resource> subResourcesRecursive = new HashSet<>();

        Resource physDim = elSwitch.addOptionalElement("physDim");
        assertTrue(elSwitch.getSubResources(false).size() == 1);
        assertTrue(elSwitch.getSubResources(false).contains(physDim));
        subResourcesRecursive.add(physDim);
        subResourcesRecursive.add(physDim.addDecorator("foo", OnOffSwitch.class));

        BinaryRange ratedValues = elSwitch.ratedValues();
        ratedValues.create();
        assertTrue(elSwitch.getSubResources(false).size() == 2);
        assertTrue(elSwitch.getSubResources(false).contains(physDim));
        assertTrue(elSwitch.getSubResources(false).contains(elSwitch.ratedValues()));
        subResourcesRecursive.add(ratedValues);
        final BooleanResource upperLimit = ratedValues.upperLimit();
        upperLimit.create();
        subResourcesRecursive.add(upperLimit);

        Resource height = elSwitch.physDim().addOptionalElement("height");
        assertTrue(elSwitch.getSubResources(false).size() == 2);
        assertTrue(elSwitch.getSubResources(false).contains(physDim));
        assertTrue(elSwitch.getSubResources(false).contains(elSwitch.ratedValues()));
        subResourcesRecursive.add(height);

        final HashSet<Resource> subresources = new HashSet<>(elSwitch.getSubResources(true));
        assertEquals(subResourcesRecursive, subresources);
    }

	@Test
    public void getSubResourcesRecursiveWithTypeWorks() {
        OnOffSwitch elSwitch = resMan.createResource(newResourceName(), OnOffSwitch.class);
        assertTrue(elSwitch.getSubResources(true).isEmpty());
        Collection<Resource> simpleSubresources = new HashSet<>();
        Collection<Resource> booleanSubresources = new HashSet<>();

        simpleSubresources.add(elSwitch.addOptionalElement("controllable"));
        booleanSubresources.add(elSwitch.controllable());
        simpleSubresources.add(elSwitch.heatCapacity().create());

        assertEquals(booleanSubresources, new HashSet<>(elSwitch.getSubResources(BooleanResource.class, true)));
        assertEquals(simpleSubresources, new HashSet<>(elSwitch.getSubResources(ValueResource.class, true)));

        OnOffSwitch elSwitch2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
        elSwitch.addDecorator("fnord", elSwitch2);
        elSwitch2 = (OnOffSwitch) elSwitch.getSubResource("fnord");

        Collection<Resource> simpleSubresourcesRecursive = new HashSet<>(simpleSubresources);
        Collection<Resource> booleanSubresourcesRecursive = new HashSet<>(booleanSubresources);
        booleanSubresourcesRecursive.add(elSwitch.getSubResource("fnord").addOptionalElement("controllable"));
        simpleSubresourcesRecursive.add(elSwitch2.controllable());
        simpleSubresourcesRecursive.add(elSwitch2.heatCapacity().create());

        // check that non-recusrive result has not changed
        assertEquals(booleanSubresources, new HashSet<>(elSwitch.getSubResources(BooleanResource.class, false)));
        assertEquals(simpleSubresources, new HashSet<>(elSwitch.getSubResources(ValueResource.class, false)));

        // check recursive result
        assertEquals(booleanSubresourcesRecursive, new HashSet<>(elSwitch.getSubResources(BooleanResource.class, true)));
        assertEquals(simpleSubresourcesRecursive, new HashSet<>(elSwitch.getSubResources(ValueResource.class, true)));
    }

	@Test
	public void getPathWorks() {
		String name = "Switch_" + counter++;
		OnOffSwitch elSwitch = resMan.createResource(name, OnOffSwitch.class);
		FloatResource f = elSwitch.heatCapacity().create();
		String separator = "/";
		assertEquals(name + separator + "heatCapacity", f.getPath(separator));
	}

	@Test
    public void getDirectSubResourcesWorks() {
        OnOffSwitch elSwitch = resMan.createResource(newResourceName(), OnOffSwitch.class);
        assertTrue(elSwitch.getDirectSubResources(false).isEmpty());

        // check that optional elements show up
        elSwitch.controllable().create();
        elSwitch.heatCapacity().create();
        Set<Resource> expectedResources = new HashSet<>(Arrays.<Resource>asList(elSwitch.controllable(),
                elSwitch.heatCapacity()));
        assertEquals(expectedResources, new HashSet<>(elSwitch.getDirectSubResources(false)));

        // check that decorators show up as well
        expectedResources.add(elSwitch.addDecorator("fnord", OnOffSwitch.class));
        assertEquals(expectedResources, new HashSet<>(elSwitch.getDirectSubResources(false)));

        // check that referenced optional or decorators do not appear in list
        OnOffSwitch elSwitch2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
        elSwitch.stateControl().setAsReference(elSwitch2.stateControl().create());
        elSwitch.addDecorator("foo", elSwitch2);

        assertEquals(expectedResources, new HashSet<>(elSwitch.getDirectSubResources(false)));
    }

	@Test
    public void getDirectSubResourcesWorksRecursively() {
        OnOffSwitch elSwitch = resMan.createResource(newResourceName(), OnOffSwitch.class);
        elSwitch.ratedValues().upperLimit().forecast().create();
        
        Set<Resource> expectedResources = new HashSet<>();
        expectedResources.addAll(Arrays.asList(elSwitch.ratedValues(), elSwitch.ratedValues().upperLimit(), elSwitch.ratedValues().upperLimit().forecast()));        
        assertEquals(expectedResources, new HashSet<>(elSwitch.getDirectSubResources(true)));
        
        expectedResources.add(elSwitch.ratedValues().addDecorator("foo", StringResource.class));
        assertEquals(expectedResources, new HashSet<>(elSwitch.getDirectSubResources(true)));
        
        OnOffSwitch elSwitch2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
        elSwitch.ratedValues().lowerLimit().setAsReference(elSwitch2.ratedValues().lowerLimit().create());
        assertEquals(expectedResources, new HashSet<>(elSwitch.getDirectSubResources(true)));
        
        elSwitch.ratedValues().addDecorator("bar", elSwitch2.heatCapacity().create());        
        assertEquals(expectedResources, new HashSet<>(elSwitch.getDirectSubResources(true)));
    }

	@Test
	public void activateWorksRecursively() throws ResourceException {
		Resource res = resMan.createResource(newResourceName(), OnOffSwitch.class);
		Resource opt = res.addOptionalElement("stateControl");
		assertFalse(res.isActive());
		assertFalse(opt.isActive());
		res.activate(true);
		assertTrue(res.isActive());
		assertTrue(opt.isActive());
		res.deactivate(true);
		assertFalse(res.isActive());
		assertFalse(opt.isActive());
	}

	@Test 
	public void addDecoratorCreatesResource() {
		Resource res = resMan.createResource(newResourceName(), OnOffSwitch.class);
		String subres = "justatest";
		res.getSubResource(subres, StringResource.class);
		Resource sub = res.addDecorator(subres, StringResource.class);
		assertTrue("Subresource expected to exist, but found virtual.", sub.exists());
		try {
			Resource sub1 = res.getSubResource("test1", StringResource.class);
			res.addDecorator("test2", sub1);  // set a reference to a virtual resource, expect a VirtualResourceException
			throw new RuntimeException("VirtualResourceException expected");
		} catch (VirtualResourceException | ClassCastException e) {}
		res.delete();
	}
}
