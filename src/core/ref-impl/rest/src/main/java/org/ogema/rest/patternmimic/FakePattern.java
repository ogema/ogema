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
 * Similar to {@link org.ogema.core.resourcemanager.pattern.ResourcePattern}, except
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
