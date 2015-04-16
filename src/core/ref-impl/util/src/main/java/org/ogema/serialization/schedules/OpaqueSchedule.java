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
package org.ogema.serialization.schedules;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;

import static org.ogema.serialization.JaxbResource.NS_OGEMA_REST;

import org.ogema.serialization.jaxb.SampledOpaque;
import org.ogema.serialization.SerializationStatus;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "opaque-schedule", namespace = NS_OGEMA_REST)
@XmlType(name = "OpaqueSchedule", namespace = NS_OGEMA_REST)
public class OpaqueSchedule extends JaxbSchedule<String> {

	public OpaqueSchedule() {
		throw new UnsupportedOperationException("Useless constructor, just to make JAXB happy.");
	}

	public OpaqueSchedule(Resource res, SerializationStatus serMan) {
		super(res, serMan);
	}

	public OpaqueSchedule(Resource res, SerializationStatus serMan, long start, long end) {
		super(res, serMan, start, end);
	}

	@Override
	SampledOpaque createValue(long time, Quality quality, Value value) {
		SampledOpaque sf = new SampledOpaque();
		sf.setTime(time);
		sf.setQuality(quality);
		sf.setValue(value);
		return sf;
	}

}
