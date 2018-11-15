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
package org.ogema.serialization.schedules;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;

import static org.ogema.serialization.JaxbResource.NS_OGEMA_REST;

import org.ogema.serialization.jaxb.SampledInteger;
import org.ogema.serialization.SerializationStatus;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "integer-schedule", namespace = NS_OGEMA_REST)
@XmlType(name = "IntegerSchedule", namespace = NS_OGEMA_REST)
public class IntegerSchedule extends JaxbSchedule<String> {

	public IntegerSchedule() {
		throw new UnsupportedOperationException("Useless constructor, just to make JAXB happy.");
	}

	public IntegerSchedule(Resource res, SerializationStatus serMan) {
		super(res, serMan);
	}

	public IntegerSchedule(Resource res, SerializationStatus serMan, long start, long end) {
		super(res, serMan, start, end);
	}

	@Override
	SampledInteger createValue(long time, Quality quality, Value value) {
		SampledInteger sf = new SampledInteger();
		sf.setTime(time);
		sf.setQuality(quality);
		sf.setValue(value);
		return sf;
	}

}
