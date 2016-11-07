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
package org.ogema.rest.patternmimic;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.ogema.core.model.Resource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Similar to {@see org.ogema.core.resourcemanager.pattern.ResourcePattern}, except
 * that here the patterns are objects (instances of this class) instead of classes. 
 * Some further differences exists, so the concepts are not equivalent. 
 * Currently, this is only used for pattern requests via the REST servlet, 
 * but in the future other remote access methods might use this as well.  
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Pattern", propOrder = { "modelClass", "resourceFields" }, namespace = FakePattern.NS_OGEMA_REST_PATTERN)
@XmlSeeAlso( { ResourceProxy.class} )
@XmlRootElement(name = "pattern", namespace = FakePattern.NS_OGEMA_REST_PATTERN) // TODO
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public class FakePattern {
	
	public static final String NS_OGEMA_REST_PATTERN = "http://www.ogema-source.net/REST_PATTERN";

	@XmlTransient
	public Class<? extends Resource> modelClass;
	
	@XmlElementWrapper(name="resourceFields")   //generates a <fields>...</fields> wrapper around fields
	@XmlElements(value = {
	        @XmlElement(name = "field", type = ResourceProxy.class)} )
	public List<ResourceProxy> resourceFields = new ArrayList<>();  
	
	protected FakePattern() {
	}
	
	public FakePattern(Class<? extends Resource> modelClass) {
		this.modelClass = modelClass;
	}
	
	void addField(ResourceProxy field) {
		resourceFields.add(field);
	}
	
	ResourceProxy getField(String name) {
		for (ResourceProxy proxy : resourceFields) {
			if (proxy.name.equals(name))
				return proxy;
		}
		return null;
	}

	@XmlElement(name="modelClass")
	public String getModelClass() {
		return modelClass.getName();
	}
	
	@SuppressWarnings("unchecked")
	public void setModelClass(String className) throws ClassNotFoundException {
		this.modelClass = (Class<? extends Resource>) Class.forName(className);
	}
	
	
}
