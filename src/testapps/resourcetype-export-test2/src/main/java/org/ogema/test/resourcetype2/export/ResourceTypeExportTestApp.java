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
package org.ogema.test.resourcetype2.export;

import java.util.Random;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.ResourceList;

/**
 * A test app for use in automated framework tests.
 */
// TODO create references
@Service(Application.class)
@Component
public class ResourceTypeExportTestApp implements Application{
	
	/**
	 * 
	 */
	public volatile ResourceList<CustomType2> list;
	
	@SuppressWarnings("unchecked")
	@Override
	public void start(ApplicationManager appManager) {
		list = appManager.getResourceManagement().createResource("list_" + (new Random().nextInt(10000)), ResourceList.class);
		list.setElementType(CustomType2.class);
		list.activate(true);
	}

	@Override
	public void stop(AppStopReason reason) {
	}
    
    
}
