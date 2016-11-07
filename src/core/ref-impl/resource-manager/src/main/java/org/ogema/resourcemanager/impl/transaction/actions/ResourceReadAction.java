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
		if (resource.exists()) 
			return true;
		if (config == ReadConfiguration.FAIL)
			throw new IllegalStateException("Resource " + resource + " is virtual");
		return false;
	}
	
	private boolean checkActiveState(Resource resource) {
		if (resource.isActive())
			return true;
		if (config == ReadConfiguration.FAIL)
			throw new IllegalStateException("Resource " + resource + " is inactive");
		return (config == ReadConfiguration.IGNORE);
	}


}
