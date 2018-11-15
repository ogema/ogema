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

import org.ogema.serialization.SerializationStatus;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.tools.SerializationManager;
import org.ogema.serialization.jaxb.SampledFloat;
import org.ogema.serialization.jaxb.SampledInteger;
import org.ogema.serialization.jaxb.SampledValue;
import org.ogema.serialization.JaxbResource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
@XmlType(name = "ScheduleResource", namespace = NS_OGEMA_REST, propOrder = { "interpolationMode", "lastUpdateTime",
		"lastCalculationTime", "start", "end", "entry" })
// FIXME SampledInteger, etc.?
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
	public String getInterpolationMode() {
		return ((Schedule) res).getInterpolationMode().name();
	}

	@XmlElement(required = false)
	public long getLastUpdateTime() {
		return ((Schedule) res).getLastUpdateTime();
	}

	@XmlElement(required = false)
	public Long getLastCalculationTime() {
		return ((Schedule) res).getLastCalculationTime();
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
