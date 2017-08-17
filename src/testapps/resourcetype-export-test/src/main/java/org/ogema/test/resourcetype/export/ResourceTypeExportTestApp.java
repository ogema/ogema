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
