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
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.ogema.core.model.array.TimeArrayResource;

import static org.ogema.serialization.JaxbResource.NS_OGEMA_REST;

/**
 * 
 * @author jlapp
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "TimeArrayResource", namespace = NS_OGEMA_REST)
@XmlRootElement(name = "resource", namespace = NS_OGEMA_REST)
public class JaxbTimeArray extends JaxbResource {

	JaxbTimeArray(TimeArrayResource r, SerializationStatus serMan) {
		super(r, serMan);
	}

	protected JaxbTimeArray() {
		throw new UnsupportedOperationException();
	}

	@XmlElement
	@XmlList
	public long[] getValues() {
		return ((TimeArrayResource) res).getValues();
	}

}
