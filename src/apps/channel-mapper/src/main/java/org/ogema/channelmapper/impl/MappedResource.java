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

public class MappedResource {

	private String resourceType;

	private String resourceName;

	private List<MappedElement> mappedElements = null;

	public MappedResource() {
		resourceType = null;
		resourceName = null;
		mappedElements = new LinkedList<MappedElement>();
	}

	public MappedResource(String resourceType, String resourceName) {
		this.resourceType = resourceType;
		this.resourceName = resourceName;
		mappedElements = new LinkedList<MappedElement>();
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getResourceName() {
		return resourceName;
	}

	@XmlElement(name = "mappedElement")
	public List<MappedElement> getMappedElements() {
		return mappedElements;
	}

	public void setMappedElements(List<MappedElement> mappedElements) {
		this.mappedElements = mappedElements;
	}

	public void addMappedChannel(MappedElement mappedChannel) {
		mappedElements.add(mappedChannel);
	}

	public void deleteMappedChannel(MappedElement mappedElement) {
		mappedElements.remove(mappedElement);
	}
}
