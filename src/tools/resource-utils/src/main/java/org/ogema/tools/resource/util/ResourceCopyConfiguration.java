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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ogema.core.model.Resource;

/**
 * Get an instance via {@link ResourceCopyConfigurationBuilder}.
 */
public class ResourceCopyConfiguration {

	private final boolean copyActivationState;
	private final boolean copyScheduleValues;
	private final boolean copyReferences;
	private final boolean copyLoggingState;
	private final Collection<Class<? extends Resource>> excludedResourceTypes;
	private final Collection<String> excludedRelativePaths;
	
	ResourceCopyConfiguration(boolean copyActivationState, boolean copyScheduleValues, boolean copyReferences, boolean copyLoggingState,
			List<Class<? extends Resource>> excludedResourceTypes, List<String> excludedRelativePaths) {
		this.copyActivationState = copyActivationState;
		this.copyScheduleValues = copyScheduleValues;
		this.copyReferences = copyReferences;
		this.copyLoggingState = copyLoggingState;
		this.excludedResourceTypes = excludedResourceTypes == null || excludedResourceTypes.isEmpty() ? null : 
				Collections.unmodifiableList(excludedResourceTypes);
		this.excludedRelativePaths = excludedRelativePaths == null || excludedRelativePaths.isEmpty() ? null :
				Collections.unmodifiableList(excludedRelativePaths);
	}

	public boolean isCopyActivationState() {
		return copyActivationState;
	}
	
	public boolean isCopyScheduleValues() {
		return copyScheduleValues;
	}

	public boolean isCopyReferences() {
		return copyReferences;
	}
	
	public boolean isCopyLoggingState() {
		return copyLoggingState;
	}

	public Collection<Class<? extends Resource>> getExcludedResourceTypes() {
		return excludedResourceTypes;
	}

	public Collection<String> getExcludedRelativePaths() {
		return excludedRelativePaths;
	}
	
}
