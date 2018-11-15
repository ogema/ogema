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
package org.ogema.tools.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collection;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.tools.SerializationManager;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Implementation of the SerializationManager. This class only holds the
 * configuration defined for a serialization. For actually performing the
 * serialization, the functionalities of a singleton SerializationCore are used.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class SerializationManagerImpl implements SerializationManager {

    private int maxDepth;
    private boolean followReferences, parseSubschedules;
    private final SerializationCore core;

    public SerializationManagerImpl(ResourceAccess resacc, ResourceManagement resman) {
        // XXX allow resacc==null for tests =/
        core = new SerializationCore(resacc, resman);
        maxDepth = 1000;
        followReferences = true;
        parseSubschedules = false;
    }

    @Override
    public void setMaxDepth(int value) {
        maxDepth = value;
    }

    @Override
    public int getMaxDepth() {
        return maxDepth;
    }

    @Override
    public void setFollowReferences(boolean status) {
        followReferences = status;
    }

    @Override
    public boolean getFollowReferences() {
        return followReferences;
    }

    @Override
    public void setSerializeSchedules(boolean status) {
        parseSubschedules = status;
    }

    @Override
    public boolean getSerializeSchedules() {
        return parseSubschedules;
    }

    @Override
    public String toJson(Resource resource) {
        return core.toJson(resource, this);
    }

    @Override
    public String toJson(Object object) {
        return core.toJson(object, this);
    }

    @Override
    public String toXml(Resource resource) {
        return core.toXml(resource, this);
    }

    @Override
    public void writeJson(Writer output, Resource resource) throws IOException {
        core.writeJson(output, resource, this);
    }

    @Override
    public void writeJson(Writer output, Object object) throws IOException {
        core.writeJson(output, object, this);
    }

    @Override
    public void writeXml(Writer output, Resource resource) throws IOException {
        core.writeXml(output, resource, this);
    }

    @Override
    public void writeJson(Writer output, Schedule sched, long start, long end) throws IOException {
        core.writeJson(output, sched, start, end, this);
    }

    @Override
    public void writeXml(Writer output, Schedule sched, long start, long end) throws IOException {
        core.writeXml(output, sched, start, end, this);
    }

    @Override
    public String toJson(Writer output, Schedule sched, long start, long end) {
        return core.toJson(sched, start, end, this);
    }

    @Override
    public String toXml(Writer output, Schedule sched, long start, long end) {
        return core.toXml(sched, start, end, this);
    }

    @Override
    public void applyJson(String json, Resource resource, boolean forceUpdate) {
        core.applyJson(json, resource, forceUpdate);
    }

    @Override
    public void applyJson(Reader jsonReader, Resource resource, boolean forceUpdate) {
        core.applyJson(jsonReader, resource, forceUpdate);
    }

    @Override
    public void applyXml(String xml, Resource resource, boolean forceUpdate) {
        core.applyXml(xml, resource, forceUpdate);
    }

    @Override
    public void applyXml(Reader xmlReader, Resource resource, boolean forceUpdate) {
        core.applyXml(xmlReader, resource, forceUpdate);
    }

    @Override
    public void applyJson(String json, boolean forceUpdate) {
        core.applyJson(json, forceUpdate);
    }

    @Override
    public void applyJson(Reader jsonReader, boolean forceUpdate) {
        core.applyJson(jsonReader, forceUpdate);
    }

    @Override
    public void applyXml(String xml, boolean forceUpdate) {
        core.applyXml(xml, forceUpdate);
    }

    @Override
    public void applyXml(Reader xmlReader, boolean forceUpdate) {
        core.applyXml(xmlReader, forceUpdate);
    }

    @Override
    public <T extends Resource> T createFromXml(Reader xmlReader) throws ResourceAlreadyExistsException {
        try {
            return core.create(core.deserializeXml(xmlReader), null);
        } catch (IOException ioex) {
            //TODO
            throw new RuntimeException(ioex);
        }
    }

    @Override
    public <T extends Resource> T createFromXml(String xml) throws ResourceAlreadyExistsException {
        return createFromXml(new StringReader(xml));
    }

    @Override
    public <T extends Resource> T createFromXml(Reader xmlReader, Resource parent) {
        try {
            return core.create(core.deserializeXml(xmlReader), parent);
        } catch (IOException ioex) {
            //TODO
            throw new RuntimeException(ioex);
        }
    }

    @Override
    public <T extends Resource> T createFromXml(String xml, Resource parent) {
        return createFromXml(new StringReader(xml), parent);
    }

    @Override
    public <T extends Resource> T createFromJson(Reader json) {
        try {
            return core.create(core.deserializeJson(json), null);
        } catch (IOException | ClassNotFoundException ex) {
            //TODO
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <T extends Resource> T createFromJson(String json) {
        return createFromJson(new StringReader(json));
    }

    @Override
    public <T extends Resource> T createFromJson(Reader json, Resource parent) {
        try {
            return core.create(core.deserializeJson(json), parent);
        } catch (IOException | ClassNotFoundException ex) {
            //TODO
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <T extends Resource> T createFromJson(String json, Resource parent) {
        return createFromJson(new StringReader(json), parent);
    }

    @Override
    public void writeXml(Writer writer, Resource res, RecordedData data, long startTime, long endTime, long interval,
            ReductionMode mode) throws IOException {
        final String OGEMA_NS = "http://www.ogema-source.net/REST";
        final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
        try {
            XMLStreamWriter xw = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
            xw.setPrefix("xsi", XSI_NS);
            xw.setPrefix("og", OGEMA_NS);
            xw.writeStartDocument();
            xw.writeStartElement("recordedData");
            xw.writeNamespace("og", OGEMA_NS);
            xw.writeNamespace("xsi", XSI_NS);
            {
                xw.writeStartElement("resource");
                xw.writeCharacters(res.getLocation());
                xw.writeEndElement();

                xw.writeStartElement("interpolationMode");
                //FIXME: getInterpolationMode not supported
                //xw.writeCharacters(data.getInterpolationMode().toString());
                xw.writeCharacters("NONE");
                xw.writeEndElement();

                xw.writeStartElement("startTime");
                xw.writeCharacters(Long.toString(startTime));
                xw.writeEndElement();

                xw.writeStartElement("endTime");
                xw.writeCharacters(Long.toString(endTime));
                xw.writeEndElement();

                xw.writeStartElement("interval");
                xw.writeCharacters(Long.toString(interval));
                xw.writeEndElement();

                xw.writeStartElement("reductionMode");
                xw.writeCharacters(mode.toString());
                xw.writeEndElement();

                for (SampledValue sv : data.getValues(startTime, endTime, interval, mode)) {
                    xw.writeStartElement("entry");
                    xw.writeAttribute(XSI_NS, "type", "og:SampledFloat");
                    {
                        xw.writeStartElement("time");
                        xw.writeCharacters(Long.toString(sv.getTimestamp()));
                        xw.writeEndElement();

                        xw.writeStartElement("quality");
                        xw.writeCharacters(sv.getQuality().toString());
                        xw.writeEndElement();

                        xw.writeStartElement("value");
                        xw.writeCharacters(sv.getValue().getStringValue());
                        xw.writeEndElement();
                    }
                    xw.writeEndElement();
                    xw.writeCharacters("\n");
                }
            }
            xw.writeEndElement();
            xw.writeEndDocument();
            xw.flush();
        } catch (XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    @Override
    public void writeJson(Writer writer, Resource res, RecordedData data, long startTime, long endTime, long interval,
            ReductionMode mode) throws IOException {
        try (@SuppressWarnings("deprecation") JsonGenerator jg = new JsonFactory().createJsonGenerator(writer).useDefaultPrettyPrinter()) {
            jg.writeStartObject();
            jg.writeStringField("@type", "RecordedData");

            jg.writeStringField("resource", res.getLocation());

            //FIXME: getInterpolationMode not supported
            jg.writeStringField("interpolationMode", "NONE");

            jg.writeNumberField("startTime", startTime);
            jg.writeNumberField("endTime", endTime);

            jg.writeNumberField("interval", interval);

            jg.writeStringField("reductionMode", mode.toString());

            jg.writeArrayFieldStart("entry");

            for (SampledValue sv : data.getValues(startTime, endTime, interval, mode)) {
                jg.writeStartObject();
                jg.writeNumberField("time", sv.getTimestamp());
                jg.writeStringField("quality", sv.getQuality().toString());
                jg.writeStringField("value", sv.getValue().getStringValue());
                jg.writeEndObject();
            }
            jg.writeEndArray();

            jg.writeEndObject();
            jg.flush();
        }
    }

    @Override
    public String toJson(Collection<Resource> resources) {
        return core.toJson(resources, this);
    }

    @Override
    public String toXml(Collection<Resource> resources) {
        return core.toXml(resources, this);
    }

    @Override
    public void writeJson(Writer output, Collection<Resource> resources) throws IOException {
        core.writeJson(output, resources, this);
    }

    @Override
    public void writeXml(Writer output, Collection<Resource> resources) throws IOException {
        core.writeXml(output, resources, this);
    }
    
    @Override
    public Collection<Resource> createResourcesFromXml(Reader xmlReader) {
        try {
            return core.create(SerializationCore.deserializeXmlCollection(xmlReader), null);
        } catch (IOException | CloneNotSupportedException ioex) {
            //TODO
            throw new RuntimeException(ioex);
        }
    }

    @Override
    public Collection<Resource> createResourcesFromXml(String xml) {
        return createResourcesFromXml(new StringReader(xml));
    }
    
    @Override
    public Collection<Resource> createResourcesFromXml(Reader xmlReader, Resource parent) {
        try {
            return core.create(SerializationCore.deserializeXmlCollection(xmlReader), parent);
        } catch (IOException | CloneNotSupportedException ioex) {
            //TODO
            throw new RuntimeException(ioex);
        }
    }
    
    @Override
    public Collection<Resource> createResourcesFromXml(String xml, Resource parent) {
        return createResourcesFromXml(new StringReader(xml), parent);
    }

    @Override
    public Collection<Resource> createResourcesFromJson(Reader jsonReader) {
        try {
            return core.create(core.deserializeJsonCollection(jsonReader), null);
        } catch (IOException ioex) {
            //TODO
            throw new RuntimeException(ioex);
        }
    }

    @Override
    public Collection<Resource> createResourcesFromJson(String json) {
        return createResourcesFromJson(new StringReader(json));
    }

    @Override
    public Collection<Resource> createResourcesFromJson(Reader jsonReader, Resource parent) {
        try {
            return core.create(core.deserializeJsonCollection(jsonReader), parent);
        } catch (IOException ioex) {
            //TODO
            throw new RuntimeException(ioex);
        }
    }

    @Override
    public Collection<Resource> createResourcesFromJson(String json, Resource parent) {
        return createResourcesFromJson(new StringReader(json), parent);
    }
}
