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

import org.ogema.serialization.SerializationStatus;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.tools.SerializationManager;
import org.ogema.serialization.jaxb.SampledFloat;
import org.ogema.serialization.jaxb.SampledValue;
import org.ogema.serialization.JaxbResource;

import static org.ogema.serialization.JaxbResource.NS_OGEMA_REST;

/**
 * Dummy implementation of a JAXB-compatible wrapper for OGEMA schedules.
 * 
 * 
 * @author jlapp
 * @param <T>
 *            type of schedule values.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
// @XmlRootElement(name = "resource", namespace = NS_OGEMA_REST)
@XmlType(name = "ScheduleResource", namespace = NS_OGEMA_REST, propOrder = { "start", "end", "entry" })
@XmlSeeAlso( { ScheduleEntry.class, BooleanSchedule.class, FloatSchedule.class, IntegerSchedule.class,
		OpaqueSchedule.class, StringSchedule.class, TimeSchedule.class, SampledValue.class, SampledFloat.class })
public abstract class JaxbSchedule<T> extends JaxbResource {

	protected long start = 0;
	protected long end = Long.MAX_VALUE;

	public JaxbSchedule() {
		throw new UnsupportedOperationException("Useless constructor, just to make JAXB happy.");
	}

	public JaxbSchedule(Resource res, SerializationStatus status, long start, long end) {
		super(res, status);
		this.start = start;
		this.end = end;
	}

	public JaxbSchedule(Resource res, SerializationStatus status, long start) {
		this(res, status, start, Long.MAX_VALUE);
	}

	public JaxbSchedule(Resource res, SerializationStatus status) {
		this(res, status, 0, Long.MAX_VALUE);
	}

	@XmlElement(required = false)
	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	@XmlElement(required = false)
	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	@XmlElements({ @XmlElement(name = "entry", type = ScheduleEntry.class) })
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
	public List<SampledValue> getEntry() {
		final Schedule schedule = (Schedule) this.res;
		List<org.ogema.core.channelmanager.measurements.SampledValue> values;
        if (start == -1){
            values = schedule.getValues(0);
        } else {
            if (end == -1){
                values = schedule.getValues(start);
            } else {
                values = schedule.getValues(start, end);
            }
        }
		List<SampledValue> result = new ArrayList<>(values.size());
		for (org.ogema.core.channelmanager.measurements.SampledValue value : values) {
			result.add(createValue(value.getTimestamp(), value.getQuality(), value.getValue()));
		}
		return result;
	}

	abstract SampledValue createValue(long time, Quality quality, Value value);

}
