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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PatternMatch", namespace = FakePattern.NS_OGEMA_REST_PATTERN)
@XmlSeeAlso( { ResourceProxy.class} )
@XmlRootElement(name = "match", namespace = FakePattern.NS_OGEMA_REST_PATTERN) // TODO
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public class PatternMatch {

	@XmlTransient
	public FakePattern patternType;
	
	@XmlElement(name="demandedModel")
	public ResourceProxy demandedModel;
	
//	@XmlElementWrapper(name="fields")
//	@XmlElements(value = {
//	        @XmlElement(name = "field", type = ResourceProxy.class)} )
	@XmlTransient
	public Map<String,ResourceProxy> fields;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null ||!(obj instanceof PatternMatch))
			return false;
		PatternMatch other = (PatternMatch) obj;
		if (this.patternType != other.patternType)
			return false;
		return (this.patternType == other.patternType && this.demandedModel.demandedModel.equalsLocation(other.demandedModel.demandedModel)); 
	}
	
	@XmlElementWrapper(name="resourceFields")
	@XmlElements(value = {
			@XmlElement(name = "field", type = ResourceProxy.class)} )
	public ResourceProxy[] getResourceFields() {
		ResourceProxy[] rf = new ResourceProxy[fields.size()];
		int counter = 0;
		for (Map.Entry<String, ResourceProxy> entry: fields.entrySet()) {
			rf[counter] = entry.getValue();
			counter++;
		}
		return rf;
	}
	
	public void setResourceFields(ResourceProxy[] rps) {
		Map<String,ResourceProxy> map = new HashMap<>();
		for (ResourceProxy rp : rps) {
			map.put(rp.name, rp);
		}
		this.fields = map;
	}
	
	@Override
	public int hashCode() {
		return patternType.hashCode() * 3 + demandedModel.getLocation().hashCode();
	}
	
	@Override
	public String toString() {
		return "Pattern match: " + demandedModel.demandedModel.getLocation();
	}
	
}
