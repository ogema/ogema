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
package org.ogema.tests.custom.type.provider;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.model.locations.Room;
import org.slf4j.Logger;

@Component
@Service(Application.class)
public class CustomTypeProvider implements Application {

	public static final String TOPLEVEL_PATH = "customTypeTestPath1";
	public static final String SUB_PATH = "customTypeTestPath2/decorator";
	public static final String ROOM_PATH = "customTypeTestRoom";
	public static final String LIST_PATH = "customTypeTestList";
	private Logger logger;
	
	@Override
	public void start(ApplicationManager appManager) {
		Room room = appManager.getResourceManagement().createResource(ROOM_PATH, Room.class);
		// 1) create a toplevel resource of custom type
		CustomType ct = appManager.getResourceManagement().createResource(TOPLEVEL_PATH, CustomType.class);
		ct.string().create();
		ct.sensor().location().room().setAsReference(room);
		
		// 2) create a subresource of custom type
		String[] paths = SUB_PATH.split("/");
		Resource dummyTop = appManager.getResourceManagement().createResource(paths[0], Resource.class);
		dummyTop.addDecorator(paths[1], CustomType.class);

		// 3) create a resource list of custom element type
		@SuppressWarnings("unchecked")
		ResourceList<CustomType> list = appManager.getResourceManagement().createResource(LIST_PATH, ResourceList.class);
		list.setElementType(CustomType.class);
		CustomType ct1 = list.add();
		CustomType ct2 = list.add();
		ct1.string().create();
		ct2.sensor().location().room().setAsReference(room);
		list.addDecorator("dummy", Room.class);
		list.activate(true);
		
		logger = appManager.getLogger();
		logger.info("{} started",getClass());
	}

	@Override
	public void stop(AppStopReason reason) {
		logger.info("Bye bye");
		logger = null;
	}
	
	
	
}
