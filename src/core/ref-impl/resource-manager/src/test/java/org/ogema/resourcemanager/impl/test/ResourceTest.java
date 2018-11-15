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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.exam.ResourceAssertions;
import org.ogema.exam.StructureTestListener;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.buildingtechnology.ElectricDimmer;
import org.ogema.model.devices.buildingtechnology.ElectricLight;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.locations.PhysicalDimensions;
import org.ogema.model.metering.ElectricityMeter;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.BinaryRange;
import org.ogema.model.sensors.StateOfChargeSensor;
import org.ogema.resourcemanager.impl.test.types.TypeTestResource;
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
//		String topName = elSwitch.getName();
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
		@SuppressWarnings("unused")
		Resource replacedDecorator = elSwitch.getSubResource("foo");
		assertTrue("not the same resource", physDim2.equalsLocation(elSwitch.getSubResource("foo")));
	}

	//@Test(expected = ResourceAlreadyExistsException.class)
    //this is OK now ...
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
		@SuppressWarnings("unused")
		Resource physDim = elSwitch.physDim().create();
		elSwitch.addDecorator("ratedValues", BinaryRange.class);
	}

	@Test
	public void addDecoratorWithReferenceWorksOnOptionalElementWithCompatibleType() {
		OnOffSwitch elSwitch = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch elSwitch2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		elSwitch2.ratedValues().create();

		@SuppressWarnings("unused")
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
	
	@Test
	public void deleteRemovesStructureListenerRegistrationFromReferencedSubresource() 	{
		String suffix = newResourceName();
		CoolingDevice cd1 = getApplicationManager().getResourceManagement().createResource("fridge1_" + suffix, CoolingDevice.class);
		CoolingDevice cd2 = getApplicationManager().getResourceManagement().createResource("fridge2_" + suffix, CoolingDevice.class);
		StructureTestListener listener = new StructureTestListener();
		cd2.temperatureSensor().location().create();
		cd1.temperatureSensor().setAsReference(cd2.temperatureSensor());
		cd1.temperatureSensor().addStructureListener(listener);
		
		cd1.delete();
		cd2.delete(); 
	}
	
	// see test above
	@Test
	public void deleteRemovesStructureListenerRegistrationFromTransitivelyReferencedSubresource() 	{
		String suffix = newResourceName();
		CoolingDevice cd1 = getApplicationManager().getResourceManagement().createResource("fridge1_" + suffix, CoolingDevice.class);
		CoolingDevice cd2 = getApplicationManager().getResourceManagement().createResource("fridge2_" + suffix, CoolingDevice.class);
		CoolingDevice cd3 = getApplicationManager().getResourceManagement().createResource("fridge3_" + suffix, CoolingDevice.class);
		StructureTestListener listener = new StructureTestListener();
		cd3.temperatureSensor().location().create();
		cd2.temperatureSensor().setAsReference(cd3.temperatureSensor());
		cd1.temperatureSensor().setAsReference(cd2.temperatureSensor());
		cd1.temperatureSensor().addStructureListener(listener);
		
		cd1.delete();
		cd3.delete(); 
		cd2.delete();
	}
	
	// similar to the tests above, but the exception might occur in a different location
	@Test
	public void rereferencingRemovesStructureListenerRegistrationFromReferencedSubresource() {
		String suffix = newResourceName();
		StructureTestListener listener = new StructureTestListener();
		CoolingDevice cd1 = getApplicationManager().getResourceManagement().createResource("baseResource_" + suffix, CoolingDevice.class);
		PhysicalElement device1 = cd1.location().device().<PhysicalElement> create();
		ElectricLight light = resMan.createResource("light1_" + suffix, ElectricLight.class);
		light.dimmer().name().create();
		CoolingDevice fridge = resMan.createResource("fridge1_" + suffix, CoolingDevice.class);
		device1.setAsReference(light);
		device1.getSubResource("dimmer", ElectricDimmer.class).addStructureListener(listener);
		device1.setAsReference(fridge);
		
		light.delete();
		fridge.delete();
		device1.delete();
	}
	
	@Test
	public void newActivationStatusIsVisibleImmediately() {
		ElectricityStorage battery = resMan.createResource(newResourceName(), ElectricityStorage.class);
		StateOfChargeSensor socs = battery.chargeSensor();
		socs.reading();
		socs.create().activate(false);
		socs.reading();
		ResourceAssertions.assertActive(socs);
		battery.delete();
	}
    
    @Test(expected = NoSuchResourceException.class)
    public void illegalResourceNamesAreIllegal() {
        resMan.createResource("4711", ElectricityStorage.class);
    }
    
    @Test(expected = NoSuchResourceException.class)
    public void illegalSubResourceNamesAreIllegal() {
        ElectricityStorage battery = resMan.createResource(newResourceName(), ElectricityStorage.class);
        battery.addDecorator("4711", IntegerResource.class);
    }
    
    @Test(expected = NoSuchResourceException.class)
    public void illegalSubResourceNamesAreIllegal2() {
        ElectricityStorage battery = resMan.createResource(newResourceName(), ElectricityStorage.class);
        battery.addDecorator("4711", battery);
    }
    
    @Test
    public void addDecoratorCanConstrainOptionalElementType() {
        final TypeTestResource test = resMan.createResource(newResourceName(), TypeTestResource.class);
        IntegerResource sub = test.addDecorator("value", IntegerResource.class);
        ResourceAssertions.assertExists(test.value());
        assertEquals(IntegerResource.class, test.value().getResourceType());
        //this failed:
        test.addDecorator("value", IntegerResource.class);
    }
    
    @Test
    public void virtualOptionalElementTypeCanBeFurtherRestricted() {
    	final TypeTestResource test = resMan.createResource(newResourceName(), TypeTestResource.class);
    	final ValueResource val = test.value();
    	FloatResource valueAsFloat = test.getSubResource(val.getName(), FloatResource.class);
    	PowerResource valueAsPower = test.getSubResource(val.getName(), PowerResource.class);
    	ResourceAssertions.assertLocationsEqual(valueAsFloat, val);
    	ResourceAssertions.assertLocationsEqual(valueAsFloat, valueAsPower);
    	Assert.assertEquals(PowerResource.class, valueAsPower.getResourceType());
    	test.delete();
    }
    
    @Test
    public void virtualOptionalElementTypeCanBeFurtherRestricted2() {
    	final TypeTestResource test = resMan.createResource(newResourceName(), TypeTestResource.class);
    	final ValueResource val = test.value();
    	FloatResource valueAsFloat = test.addDecorator(val.getName(), FloatResource.class);
    	valueAsFloat.delete(); // if we did not delete the resource, the next line should throw an exception
    	PowerResource valueAsPower = test.addDecorator(val.getName(), PowerResource.class);
    	ResourceAssertions.assertLocationsEqual(valueAsFloat, val);
    	ResourceAssertions.assertLocationsEqual(valueAsFloat, valueAsPower);
    	Assert.assertEquals(PowerResource.class, valueAsPower.getResourceType());
    	test.delete();
    }
    
    @Test
    public void virtualOptionalElementTypeCanBeFurtherRestricted3() {
    	final TypeTestResource test = resMan.createResource(newResourceName(), TypeTestResource.class);
    	final ValueResource val = test.value();
    	FloatResource valueAsFloat = test.getSubResource(val.getName(), FloatResource.class).create();
    	Assert.assertEquals(FloatResource.class, valueAsFloat.getResourceType());
    	ResourceAssertions.assertLocationsEqual(val, valueAsFloat);
    	valueAsFloat.delete();
    	PowerResource valueAsPower = test.addDecorator(val.getName(), PowerResource.class);
    	ResourceAssertions.assertLocationsEqual(valueAsFloat, valueAsPower);
    	Assert.assertEquals(PowerResource.class, valueAsPower.getResourceType());
    	test.delete();
    }
    
    @Test
    public void virtualOptionalElementTypeCanBeRecreatedWithIncompatibleType() {
    	final TypeTestResource test = resMan.createResource(newResourceName(), TypeTestResource.class);
    	final ValueResource val = test.value();
    	FloatResource valueAsFloat = test.getSubResource(val.getName(), FloatResource.class).create();
    	Assert.assertEquals(FloatResource.class, valueAsFloat.getResourceType());
    	ResourceAssertions.assertLocationsEqual(val, valueAsFloat);
    	valueAsFloat.delete();
        IntegerResource someInteger = test.addDecorator("x", IntegerResource.class);
    	//IntegerResource valueAsInteger = test.addDecorator(val.getName(), IntegerResource.class);
        IntegerResource valueAsInteger = test.value().setAsReference(someInteger);
    	ResourceAssertions.assertLocationsEqual(valueAsFloat, valueAsInteger);
    	Assert.assertEquals(IntegerResource.class, valueAsInteger.getResourceType());
    	test.delete();
    }
       
    @Test
    public void virtualDecoratorTypeCanBeFurtherRestricted() {
    	final TypeTestResource test = resMan.createResource(newResourceName(), TypeTestResource.class);
    	FloatResource valueAsFloat = test.getSubResource("value2", FloatResource.class);
    	PowerResource valueAsPower = test.getSubResource("value2", PowerResource.class);
    	ResourceAssertions.assertLocationsEqual(valueAsFloat, valueAsPower);
    	Assert.assertEquals(PowerResource.class, valueAsPower.getResourceType());
    	test.delete();
    }
    
    @Test
    public void virtualDecoratorTypeCanBeFurtherRestricted2() {
    	final TypeTestResource test = resMan.createResource(newResourceName(), TypeTestResource.class);
    	FloatResource valueAsFloat = test.addDecorator("value2", FloatResource.class);
    	valueAsFloat.delete();
    	PowerResource valueAsPower = test.addDecorator("value2", PowerResource.class);
    	ResourceAssertions.assertLocationsEqual(valueAsFloat, valueAsPower);
    	Assert.assertEquals(PowerResource.class, valueAsPower.getResourceType());
    	test.delete();
    }
    
    // to be discussed... maybe this should be possible?
    @Test(expected=NoSuchResourceException.class) 
    public void virtualResourceCannotBeRestrictedToIncompatibleType() {
    	final TypeTestResource test = resMan.createResource(newResourceName(), TypeTestResource.class);
    	final ValueResource val = test.value();
    	PowerResource valueAsPower = test.getSubResource(val.getName(), PowerResource.class);
    	TemperatureResource valueAsTemp = test.getSubResource(val.getName(), TemperatureResource.class);
    	ResourceAssertions.assertLocationsEqual(valueAsTemp, valueAsPower);
    	test.delete();
    }
    
    // to be discussed... maybe this should be possible?
    @Test(expected=ResourceAlreadyExistsException.class) 
    public void typeOfExistingResourceCannotBeFurtherRestricted() {
    	final TypeTestResource test = resMan.createResource(newResourceName(), TypeTestResource.class);
    	final ValueResource val = test.value();
    	FloatResource valueAsPower = test.addDecorator(val.getName(), FloatResource.class);
    	TemperatureResource valueAsTemp = test.addDecorator(val.getName(), TemperatureResource.class);
    	ResourceAssertions.assertLocationsEqual(valueAsTemp, valueAsPower);
    	test.delete();
    }

    // tbd: this is a nice-to-have feature - if the virtual resource is no longer referenced,
    // it would be ok to change its type to an incompatible one... 
    @Ignore
    @Test
    public void unreachableVirtualResourceOfIncompatibleTypeCanBeOverwritten() {
    	final TypeTestResource test = resMan.createResource(newResourceName(), TypeTestResource.class);
    	test.getSubResource("value", PowerResource.class);
    	// now the PowerResource is no longer referenced, and can be overwritten
    	TemperatureResource valueAsTemp = test.getSubResource("value", TemperatureResource.class); 
    	ResourceAssertions.assertLocationsEqual(valueAsTemp, test.value());
    	test.delete();
    }
    
    @Test(expected = NoSuchResourceException.class)
    public void illegalResourceNamesCauseExpectionInGetSubResource1() {
        Resource top = resMan.createResource(newResourceName(), Resource.class);
        Resource noSuchResource = top.getSubResource("17", FloatResource.class);
    }
    
    @Test(expected = NoSuchResourceException.class)
    public void illegalResourceNamesCauseExpectionInGetSubResource2() {
        Resource top = resMan.createResource(newResourceName(), Resource.class);
        Resource noSuchResource = top.getSubResource("17");
    }
    
    
}
