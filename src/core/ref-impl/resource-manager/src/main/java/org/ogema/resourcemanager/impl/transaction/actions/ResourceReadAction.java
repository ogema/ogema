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
package org.ogema.resourcemanager.impl.transaction.actions;

import java.util.Objects;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.transaction.ReadConfiguration;
import org.ogema.tools.resource.util.ValueResourceUtils;

/*
 * TODO move creation and activation to their own AtomicActions, create composite action?
 */
public class ResourceReadAction<T, V extends ValueResource> extends GenericReadAction<T, V> {
	
	private final ReadConfiguration config;
	

	public ResourceReadAction(V resource, ReadConfiguration config) {
		super(resource);
		Objects.requireNonNull(config);
		this.config = config;
	}

	@SuppressWarnings("unchecked")
	protected T readResource(V resource) {
		return (T) ValueResourceUtils.getValue(resource);
	}
	
	@Override
	protected final T read(V resource) {
		if (!checkExistence(resource) || !checkActiveState(resource)) // throws exceptions
			return null;
		return readResource(resource);
	}
	
	/**
	 * If this returns false, the value null must be returned
	 * @return
	 */
	private boolean checkExistence(Resource resource) {
		if (resource != null && resource.exists()) 
			return true;
		if (config == ReadConfiguration.FAIL)
			throw new IllegalStateException("Resource " + resource + " is virtual");
		return false;
	}
	
	private boolean checkActiveState(Resource resource) {
		if (resource != null && resource.isActive())
			return true;
		if (config == ReadConfiguration.FAIL)
			throw new IllegalStateException("Resource " + resource + " is inactive");
		return (config == ReadConfiguration.IGNORE);
	}


}
