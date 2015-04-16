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
package org.ogema.channelmanager.impl;

import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceManagement;

public class ResourceManagementImpl implements ResourceManagement {

	@Override
	public <T extends Resource> T createResource(String name, Class<T> type) throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteResource(String name) throws ResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Class<? extends Resource>> getResourceTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUniqueResourceName(String appResourceName) {
		// TODO Auto-generated method stub
		return null;
	}

}
