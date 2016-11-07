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
