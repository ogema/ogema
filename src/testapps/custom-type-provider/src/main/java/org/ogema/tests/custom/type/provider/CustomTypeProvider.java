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
