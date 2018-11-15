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

import static org.ogema.serialization.JaxbResource.NS_OGEMA_REST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.ogema.core.model.Resource;
import org.ogema.serialization.jaxb.ScheduleResource;
import org.ogema.serialization.schedules.BooleanSchedule;
import org.ogema.serialization.schedules.FloatSchedule;
import org.ogema.serialization.schedules.IntegerSchedule;
import org.ogema.serialization.schedules.StringSchedule;
import org.ogema.serialization.schedules.TimeSchedule;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.ogema.serialization.JaxbFactory;
import org.ogema.serialization.JaxbLink;
import org.ogema.serialization.JaxbResource;
import org.ogema.serialization.SerializationStatus;

/**
 *
 * @author jlapp
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "resources", namespace = NS_OGEMA_REST) 
@XmlSeeAlso( { JaxbResourceList.class, JaxbBoolean.class, JaxbFloat.class, JaxbInteger.class, JaxbOpaque.class,
	JaxbString.class, JaxbTime.class, JaxbLink.class, BooleanSchedule.class, FloatSchedule.class,
	IntegerSchedule.class, StringSchedule.class, TimeSchedule.class, JaxbBooleanArray.class, JaxbByteArray.class,
	JaxbFloatArray.class, JaxbIntegerArray.class, JaxbStringArray.class, JaxbTimeArray.class })
@JsonSubTypes( { @JsonSubTypes.Type(org.ogema.serialization.jaxb.Resource.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.ResourceList.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.BooleanResource.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.FloatResource.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.IntegerResource.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.OpaqueResource.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.StringResource.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.TimeResource.class),
	@JsonSubTypes.Type(ScheduleResource.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.BooleanArrayResource.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.ByteArrayResource.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.FloatArrayResource.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.IntegerArrayResource.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.StringArrayResource.class),
	@JsonSubTypes.Type(org.ogema.serialization.jaxb.TimeArrayResource.class), })
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonRootName("resources")
@XmlRootElement(name = "resources", namespace = NS_OGEMA_REST) // FIXME namespace
public class JaxbResourceCollection {

	private final Collection<Resource> resources;
	private final SerializationStatus status;

	public JaxbResourceCollection(Collection<Resource> resources,SerializationStatus status) {
		this.resources = resources;
		this.status = status;
	}

	public JaxbResourceCollection() {
		throw new UnsupportedOperationException("Useless constructor, only there to make JAXB happy.");
	}

	@XmlElements(value = {
        @XmlElement(name = "resource", type = JaxbResource.class),
        @XmlElement(name = "resourcelink", type = JaxbLink.class)})
    public List<Object> getResources() throws CloneNotSupportedException {
        final List<Object> result = new ArrayList<>();

        for (Resource subres : resources) {
            if (status.linkResource(subres)) {
                result.add(new JaxbLink(subres));
            } else {
            	SerializationStatus newStatus = ((SerializationStatus) status.clone()).increaseDepth();
                result.add(JaxbFactory.createJaxbResource(subres, newStatus));
            }
        }
        //status.increaseDepth();
        return result;
    }
}
