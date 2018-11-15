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
package org.ogema.channelmapper.impl;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MappingConfiguration", namespace = "http://ogema.org/core/channelmapper/v1")
public class MappingConfiguration {
	private List<MappedResource> mappedResources = new LinkedList<MappedResource>();

	@XmlElement(name = "resource")
	public List<MappedResource> getMappedResources() {
		return mappedResources;
	}

	public void setMappedResources(List<MappedResource> mappedResources) {
		this.mappedResources = mappedResources;
	}

	public void addMappedResource(MappedResource mappedResource) {
		if (mappedResource == null) {
			mappedResources = new LinkedList<MappedResource>();
		}

		mappedResources.add(mappedResource);
	}

	public MappedResource getMappedResource(String resourceName) {

		for (MappedResource mappedResource : mappedResources) {

			if (mappedResource.getResourceName().equals(resourceName)) {
				return mappedResource;
			}
		}

		return null;
	}

	public void removeMappedResource(String resourceName) {
		MappedResource mappedResource = getMappedResource(resourceName);

		mappedResources.remove(mappedResource);
	}
}
