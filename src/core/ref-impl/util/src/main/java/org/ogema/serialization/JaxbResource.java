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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;

import static org.ogema.serialization.JaxbResource.NS_OGEMA_REST;

import org.ogema.serialization.jaxb.ScheduleResource;
import org.ogema.serialization.schedules.BooleanSchedule;
import org.ogema.serialization.schedules.FloatSchedule;
import org.ogema.serialization.schedules.IntegerSchedule;
import org.ogema.serialization.schedules.StringSchedule;
import org.ogema.serialization.schedules.TimeSchedule;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author jlapp
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "Resource", namespace = NS_OGEMA_REST, propOrder = { "name", "type", "path", "decorating", "active",
		"referencing", "subresources" })
@XmlSeeAlso( { JaxbResourceList.class, JaxbBoolean.class, JaxbFloat.class, JaxbInteger.class, JaxbOpaque.class,
		JaxbString.class, JaxbTime.class, JaxbLink.class, BooleanSchedule.class, FloatSchedule.class,
		IntegerSchedule.class, StringSchedule.class, TimeSchedule.class, JaxbBooleanArray.class, JaxbByteArray.class,
		JaxbFloatArray.class, JaxbIntegerArray.class, JaxbStringArray.class, JaxbTimeArray.class })
@JsonSubTypes( { @JsonSubTypes.Type(org.ogema.serialization.jaxb.ResourceList.class),
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
@JsonRootName("resource")
@XmlRootElement(name = "resource", namespace = NS_OGEMA_REST)
public class JaxbResource {

	public static final String NS_OGEMA_REST = "http://www.ogema-source.net/REST";
	protected final Resource res;

	/**
	 * Serialization status of the current serialization.
	 */
	protected final SerializationStatus status;

	protected JaxbResource(Resource res, SerializationStatus status) {
		this.res = res;
		this.status = status;
		status.addParsedLocation(res.getLocation("/"));
	}

	public JaxbResource() {
		throw new UnsupportedOperationException("Useless constructor, only there to make JAXB happy.");
	}

	@XmlElement
	public String getName() {
		return res.getName();
	}

	@XmlElement
	public Class<? extends Resource> getType() {
		return res.getResourceType();
	}

	@XmlElement
	public String getPath() {
		return res.getPath("/");
	}

	@XmlElement
	public boolean isDecorating() {
		return res.isDecorator();
	}

	@XmlElement
	public boolean isActive() {
		return res.isActive();
	}

	@XmlElement
	public String getReferencing() {
		return res.isReference(false) ? res.getLocation() : null;
	}

	@XmlElements(value = {
        @XmlElement(name = "resource", type = JaxbResource.class),
        @XmlElement(name = "resourcelink", type = JaxbLink.class)})
    @SuppressWarnings("unchecked")
    // generics on ResourceList
    public List<Object> getSubresources() {
        final List<Object> result = new ArrayList<>();

        List<Resource> subresources = res.getSubResources(false);
        if (res instanceof ResourceList) {
            ResourceList<Resource> car = (ResourceList) res;
            List<Resource> iterationOrder = new ArrayList<>(subresources.size());
            List<Resource> listElements = car.getAllElements();
            iterationOrder.addAll(listElements);
            if (subresources.size() > listElements.size()) {
                for (Resource subres : subresources) {
                    if (!listElements.contains(subres)) {
                        iterationOrder.add(subres);
                    }
                }
            }
            subresources = iterationOrder;
        }
        for (Resource subres : subresources) {
            if (status.linkResource(subres)) {
                result.add(new JaxbLink(subres));
            } else {
                result.add(JaxbFactory.createJaxbResource(subres, status.increaseDepth()));
            }
        }
        //status.increaseDepth();
        return result;
    }
}
