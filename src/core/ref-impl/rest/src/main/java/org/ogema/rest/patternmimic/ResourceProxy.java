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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessMode;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlType(name = "field", propOrder = { "name", "relativePath", "type", "optional", "accessMode", "location", "value" },
			namespace = FakePattern.NS_OGEMA_REST_PATTERN)
public class ResourceProxy {
	
	@XmlTransient
	public final Resource demandedModel;
	@XmlTransient
	public final Resource res;
	
	public ResourceProxy() {
		this.res = null;
		this.demandedModel = null;
	}
	
	public ResourceProxy(Resource demandedModel, Resource res) {
		this.res=res;
		this.demandedModel = demandedModel;
	}
	
	String name;
	String relativePath;
	String type;
	AccessMode accessMode;
	// for value resources; only resources with matching value are considered; may be null
	String value;
	boolean optional;
	// relevant for references; when creating a pattern, this should only be provided for 
	// references.
	String location;

	@XmlElement(required=true)
	public String getName() {
		return res.getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name="relativePath",required=true)
	public String getRelativePath() {
		String path = res.getPath();
		String base = demandedModel.getPath();
		if (path.length() == base.length() && path.equals(base)) 
			return "";	
		else if (path.startsWith(base)) 
			return path.substring(base.length() +1); // cut base path + "/"
		else 
			throw new IllegalStateException("Subresource path does not start with resource path: " + path + "; " + base);
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	@XmlElement(name="type",required=true)
	public String getType() {
		return res.getResourceType().getName();
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(name="accessMode")
	public AccessMode getAccessMode() {
		return res.getAccessMode();
	}

	public void setAccessMode(String accessMode) {
		this.accessMode = AccessMode.valueOf(accessMode);
	}
	
	@XmlElement(name="value")
	public String getValue() {
		return FakePatternAccess.getValue(res);
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@XmlElement(name="optional")
	public Boolean getOptional() {
		return null;
	}
	
	public void setOptional(Boolean optional) {
		this.optional = optional;
	}

	@XmlElement(name="location")
	public String getLocation() {
		return res.getLocation();
	}

	public void setLocation(String location) {
		this.location = location;
	}

	
}
