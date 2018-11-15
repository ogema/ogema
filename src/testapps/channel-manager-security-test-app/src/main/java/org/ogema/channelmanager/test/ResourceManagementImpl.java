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
package org.ogema.channelmanager.test;

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
