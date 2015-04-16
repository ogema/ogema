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
package org.ogema.apps.admin;

import java.util.Collection;
import java.util.HashSet;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.communication.CommunicationInformation;
import org.ogema.model.locations.PhysicalDimensions;
import org.ogema.model.metering.ElectricityMeter;

public class TestResourcePopulation {

	public static final String RESNAME = "ResourceTestResource";
	static int counter = 0;

	ResourceManagement resMan;

	public TestResourcePopulation(ResourceManagement resm) {
		resMan = resm;
	}

	void populate() {
		System.out.println("======================= TEST RESOURCE POPULATION START =======================");
		addOptionalElementWorks();
		resourcesWithOverriddenElementsWork();
		addOptionalElementReturnsExistingElement();
		addOptionalElementReplacesExistingReference();
		setOptionalWithReferenceWorks();
		addDecoratorWorks();
		addDecoratorWithReferenceWorks();
		getLocationWorks();
		equalsLocationWorks();
		addDecoratorWithReferenceReplacesExistingDecoratorOfSameType();
		getSubresourceWorks();
		getSubResourcesWorks();
		//		getSubResourcesRecursiveWithTypeWorks();
		getPathWorks();
		activateWorksRecursively();
		System.out.println("======================= TEST RESOURCE POPULATION END =======================");
	}

	public void addOptionalElementWorks() throws ResourceException {
		OnOffSwitch res = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		Resource opt = res.stateControl();
		opt.create();
		res.stateControl();
	}

	public void resourcesWithOverriddenElementsWork() throws ResourceException {
		ElectricityMeter res = resMan.createResource(RESNAME + counter++, ElectricityMeter.class);
		res.connection().create();
	}

	public void addOptionalElementReturnsExistingElement() {
		OnOffSwitch res = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		res.stateControl().create();
		BooleanResource stateControl = res.stateControl();
		stateControl.setValue(true);
		stateControl.activate(false);
	}

	public void addOptionalElementReplacesExistingReference() {
		OnOffSwitch sw1 = resMan.createResource("switch1", OnOffSwitch.class);
		OnOffSwitch sw2 = resMan.createResource("switch2", OnOffSwitch.class);
		BooleanResource ctrl2 = (BooleanResource) sw2.stateControl().create();
		//		sw1.stateControl().setAsReference(ctrl2);
		ctrl2.setValue(true);
		ctrl2.setValue(false);
		BooleanResource ctrl1 = sw1.stateControl();
		sw1.create();
		ctrl1.setValue(true);
	}

	// FIXME test uses a deprecated data model
	public void setOptionalWithReferenceWorks() {
		// OnOffSwitch test1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		// OnOffSwitch test2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		// Resource a = test1.addOptionalElement("comInfo");
		// test2.setOptionalElement("comInfo", a);
		// Resource b = test2.comInfo();
	}

	public void addDecoratorWorks() {
		OnOffSwitch elSwitch = resMan.createResource("addDecoratorTest" + counter++, OnOffSwitch.class);
		elSwitch.addDecorator("foo", OnOffSwitch.class);
		elSwitch.getSubResource("foo");
	}

	public void addDecoratorWithReferenceWorks() {
		OnOffSwitch elSwitch = resMan.createResource("test" + counter++, OnOffSwitch.class);
		//		Resource physDim = elSwitch.addOptionalElement("physDim");
		//		elSwitch.addDecorator("foo", physDim);
		//		elSwitch.getSubResource("foo");
	}

	public void getLocationWorks() {
		OnOffSwitch elSwitch = resMan.createResource("test" + counter++, OnOffSwitch.class);
		//		elSwitch.getName();
		//		elSwitch.addOptionalElement("physDim");
		//		elSwitch.addDecorator("foo", elSwitch.physDim());
	}

	public void equalsLocationWorks() {
		OnOffSwitch elSwitch = resMan.createResource("test" + counter++, OnOffSwitch.class);
		elSwitch.getName();
		elSwitch.addOptionalElement("physDim");
		//		elSwitch.addDecorator("foo", elSwitch.physDim());
	}

	public void addDecoratorWithReferenceReplacesExistingDecoratorOfSameType() {
		OnOffSwitch elSwitch = resMan.createResource("test" + counter++, OnOffSwitch.class);
		// elSwitch.physDim()
		Resource physDim = elSwitch.addOptionalElement("physDim");
		elSwitch.addDecorator("foo", physDim);
		Resource physDim2 = elSwitch.addDecorator("bar", PhysicalDimensions.class);
		elSwitch.addDecorator("foo", physDim2);
		System.out.println(physDim + " " + physDim.getLocation("/"));
		System.out.println(physDim2 + " " + physDim2.getLocation("/"));
		Resource replacedDecorator = elSwitch.getSubResource("foo");
		System.out.println(replacedDecorator + " " + replacedDecorator.getLocation("/"));
	}

	public void getSubresourceWorks() {
		OnOffSwitch elSwitch = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		elSwitch.addOptionalElement("physDim");
	}

	public void getSubResourcesWorks() {
		OnOffSwitch elSwitch = resMan.createResource("test" + counter++, OnOffSwitch.class);
		HashSet<Resource> subResourcesRecursive = new HashSet<>();

		Resource physDim = elSwitch.addOptionalElement("physDim");
		subResourcesRecursive.add(physDim);
		subResourcesRecursive.add(physDim.addDecorator("foo", OnOffSwitch.class));

//		CommunicationInformation comInfo = (CommunicationInformation) elSwitch.addOptionalElement("comInfo");
//		subResourcesRecursive.add(comInfo);
//		subResourcesRecursive.add(comInfo.addOptionalElement("communicationStatus"));

		Resource height = elSwitch.physDim().addOptionalElement("height");
		subResourcesRecursive.add(height);

		new HashSet<>(elSwitch.getSubResources(true));
	}

	public void getSubResourcesRecursiveWithTypeWorks() {
		OnOffSwitch elSwitch = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		System.err.println("sub: " + elSwitch.getSubResources(true));
		Collection<Resource> simpleSubresources = new HashSet<>();
		Collection<Resource> booleanSubresources = new HashSet<>();

		simpleSubresources.add(elSwitch.controllable().create());
		booleanSubresources.add(elSwitch.controllable());
		simpleSubresources.add(elSwitch.addOptionalElement("heatCapacity"));

		OnOffSwitch elSwitch2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		elSwitch.addDecorator("fnord", elSwitch2);
		elSwitch2 = (OnOffSwitch) elSwitch.getSubResource("fnord");

		Collection<Resource> simpleSubresourcesRecursive = new HashSet<>(simpleSubresources);
		Collection<Resource> booleanSubresourcesRecursive = new HashSet<>(booleanSubresources);
		booleanSubresourcesRecursive.add(elSwitch.getSubResource("fnord").addOptionalElement("controllable"));
		simpleSubresourcesRecursive.add(elSwitch2.controllable());
		simpleSubresourcesRecursive.add(elSwitch2.addOptionalElement("heatCapacity"));
	}

	public void getPathWorks() {
		String name = "switch_" + counter++;
		OnOffSwitch elSwitch = resMan.createResource(name, OnOffSwitch.class);
		elSwitch.addOptionalElement("heatCapacity");
		elSwitch.heatCapacity();
	}

	public void activateWorksRecursively() {
		OnOffSwitch res = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		Resource opt = res.stateControl();
		opt.create();
		res.activate(true);
		res.deactivate(true);
	}

}
