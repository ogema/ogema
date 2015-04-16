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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.ogema.core.model.simple.IntegerResource;

import static org.ogema.serialization.JaxbResource.NS_OGEMA_REST;

/**
 * 
 * @author jlapp
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "IntegerResource", namespace = NS_OGEMA_REST)
@XmlRootElement(name = "integer", namespace = NS_OGEMA_REST)
public class JaxbInteger extends JaxbResource {

	JaxbInteger(IntegerResource r, SerializationStatus serMan) {
		super(r, serMan);
	}

	protected JaxbInteger() {
		throw new UnsupportedOperationException();
	}

	@XmlElement
	public int getValue() {
		return ((IntegerResource) res).getValue();
	}

}
