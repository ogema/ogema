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
package org.ogema.test.resourcetype.export;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.ResourceList;
import org.ogema.model.sensors.TemperatureSensor;

/**
 * A test app for use in automated framework tests.
 */
// TODO create references
@Service(Application.class)
@Component
public class ResourceTypeExportTestApp implements Application{
	
	/**
	 * These are used in a test in resource-manager-test-addons.
	 * Do not change.
	 */ 
	private final static String RESOURCE_NAME_TOPLEVEL = "resTypeExpoTest1";
	private final static String RESOURCE_NAME_LIST_TOPLEVEL = "resTypeExpoTest2";
	private final static String RESOURCE_NAME_SUB = "resTypeExpoTest3";
	
	private void createDeclaredSubresources(CustomDevice cd) {
		cd.list().create();
		cd.temperature().create();
		cd.color().create();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void start(ApplicationManager appManager) {
		final CustomDevice cd = appManager.getResourceManagement().createResource(RESOURCE_NAME_TOPLEVEL, CustomDevice.class);
		createDeclaredSubresources(cd);
		cd.activate(true);
		
		final ResourceList<CustomDevice> toplist = appManager.getResourceManagement().createResource(RESOURCE_NAME_LIST_TOPLEVEL, ResourceList.class);
		toplist.setElementType(CustomDevice.class);
		final CustomDevice cd2 = toplist.add();
		createDeclaredSubresources(cd2);
		toplist.activate(true);
		
		final TemperatureSensor ts = appManager.getResourceManagement().createResource(RESOURCE_NAME_SUB, TemperatureSensor.class);
		final CustomDevice cd3 = ts.getSubResource("sub", CustomDevice.class);
		createDeclaredSubresources(cd3);
		ts.activate(true);
		
		final ResourceList<CustomDevice> list = ts.getSubResource("list", ResourceList.class);
		list.setElementType(CustomDevice.class);
		final CustomDevice cd4 = list.add();
		createDeclaredSubresources(cd4);
		list.activate(true);
		
		appManager.getLogger().info("Resource type export test app started. New resource: {}", cd);
	}

	@Override
	public void stop(AppStopReason reason) {
	}
    
    
}
