/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
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
