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
package org.ogema.serialization;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ogema.core.model.Resource;

import static org.ogema.serialization.JaxbResource.NS_OGEMA_REST;

/**
 * Wrapper for an Ogema resource that can be used as a Jersey REST resource and is serializable to XML via JAXB.
 * Deserialization from XML or JSON creates instances of {@link org.ogema.rest.xml.Resource}.
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
