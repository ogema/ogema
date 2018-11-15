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
package org.ogema.serialization;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ogema.core.model.Resource;

import static org.ogema.serialization.JaxbResource.NS_OGEMA_REST;

/**
 * Wrapper for an Ogema resource that can be used as a Jersey REST resource and is serializable to XML via JAXB.
 * Deserialization from XML or JSON creates instances of org.ogema.rest.xml.Resource.
 * 
 * @author jlapp
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "ResourceLink", namespace = NS_OGEMA_REST, propOrder = { "link", "type", "name" })
public class JaxbLink {

	final protected Resource res;

	public JaxbLink(Resource res) {
		this.res = res;
	}

	protected JaxbLink() {
		this.res = null;
	}

	@XmlElement
	public String getLink() {
		return res.getLocation("/");
	}

	@XmlElement
	public String getType() {
		return res.getResourceType().getCanonicalName();
	}

	@XmlElement
	public String getName() {
		return res.getName();
	}

}
