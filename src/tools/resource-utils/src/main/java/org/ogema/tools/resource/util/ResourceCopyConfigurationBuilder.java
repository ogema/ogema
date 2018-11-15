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
package org.ogema.tools.resource.util;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.model.Resource;

/**
 * Create a {@link ResourceCopyConfiguration}. 
 */
public class ResourceCopyConfigurationBuilder {
	
	private boolean copyActivationState = true;
	private boolean copyScheduleValues = true;
	private boolean copyReferences = true;
	private boolean copyLoggingState = false;
	private List<Class<? extends Resource>> excludedResourceTypes= null; 
	private List<String> excludedRelativePaths = null;

	private ResourceCopyConfigurationBuilder() {}
	
	/**
	 * Get a new instance.
	 * @return
	 */
	public static ResourceCopyConfigurationBuilder newInstance() {
		return new ResourceCopyConfigurationBuilder();
	}
	
	/**
	 * Build the configuration
	 * @return
	 */
	public ResourceCopyConfiguration build() {
		return new ResourceCopyConfiguration(
				copyActivationState,
				copyScheduleValues, 
				copyReferences, 
				copyLoggingState, 
				excludedResourceTypes, 
				excludedRelativePaths);
	}
	
	/**
	 * Activate resources if the corresponding source resource is active?
	 * Default: true
	 * @param copyActivationState
	 * @return
	 */
	public ResourceCopyConfigurationBuilder setCopyActivationState(boolean copyActivationState) {
		this.copyActivationState = copyActivationState;
		return this;
	}

	/**
	 * Copy schedule values to new schedule resources?
	 * Default: true
	 * @param copyScheduleValues
	 * @return
	 */
	public ResourceCopyConfigurationBuilder setCopyScheduleValues(boolean copyScheduleValues) {
		this.copyScheduleValues = copyScheduleValues;
		return this;
	}

	/**
	 * Copy references (set reference to same target resource as for the source resource) ?
	 * Default: true
	 * @param copyReferences
	 * @return
	 */
	public ResourceCopyConfigurationBuilder setCopyReferences(boolean copyReferences) {
		this.copyReferences = copyReferences;
		return this;
	}

	/**
	 * Activate logging if logging is enabled for the source resource?
	 * Default: false 
	 * @param copyLoggingState
	 * @return
	 */
	public ResourceCopyConfigurationBuilder setCopyLoggingState(boolean copyLoggingState) {
		this.copyLoggingState = copyLoggingState;
		return this;
	}

	/**
	 * Exclude certain resource types from the copy operation?
	 * Default: null
	 * @param excludedResourceTypes
	 * @return
	 */
	public ResourceCopyConfigurationBuilder setExcludedResourceTypes(List<Class<? extends Resource>> excludedResourceTypes) {
		this.excludedResourceTypes = excludedResourceTypes == null || excludedResourceTypes.isEmpty() ? null :
				new ArrayList<>(excludedResourceTypes);
		return this;
	}

	/**
	 * Exclude certain relative paths from the copy operation?
	 * Default: null
	 * @param excludedRelativePaths
	 * 		a list of relative paths (not starting with "/").
	 * @return
	 */
	public ResourceCopyConfigurationBuilder setExcludedRelativePaths(List<String> excludedRelativePaths) {
		this.excludedRelativePaths = excludedRelativePaths == null || excludedRelativePaths.isEmpty() ? null :
				new ArrayList<>(excludedRelativePaths);
		return this;
	}
	
}
