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
package org.ogema.tools.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.io.BaseEncoding;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.serialization.jaxb.BooleanSchedule;
import org.ogema.serialization.jaxb.FloatSchedule;
import org.ogema.serialization.jaxb.IntegerSchedule;
import org.ogema.serialization.jaxb.OpaqueSchedule;
import org.ogema.serialization.jaxb.Resource;
import org.ogema.serialization.jaxb.ResourceLink;
import org.ogema.serialization.jaxb.SampledFloat;
import org.ogema.serialization.jaxb.SampledValue;
import org.ogema.serialization.jaxb.ScheduleResource;
import org.ogema.serialization.jaxb.StringSchedule;
import org.ogema.serialization.jaxb.TimeSchedule;

/**
 * Custom JSON to OGEMA deserialization using the Jackson streaming API (com.fasterxml.jackson.core.JsonParser).
 * @author jlapp
 */
public class JsonReaderJackson {

    static final JsonFactory JFAC = new JsonFactory();

    private static final String SCHEDULE_ENTRY_VALUE = "value";
    private static final String SCHEDULE_ENTRY_QUALITY = "quality";
    private static final String SCHEDULE_ENTRY_TIME = "time";

    static class CompositeResource extends Resource {

        Object value;
        
        List<SampledValue> schedule;
        Class<?> scheduleType;
        long start = 0;
        long end = 0;
        long lastUpdateTime = 0;
        long lastCalculationTime = 0;
        
        String link;

        boolean isLink() {
            return link != null;
        }

        @SuppressWarnings("deprecation")
        Object toSpecializedResource() throws ClassNotFoundException {
            if (link != null) {
                ResourceLink l = new ResourceLink();
                l.setLink(link);
                l.setName(name);
                l.setType(type);
                return l;
            }
            Class<?> c = Class.forName(getType());
            Resource r = null;
            if (ValueResource.class.isAssignableFrom(c)) {
                //XXX null values?
                if (SingleValueResource.class.isAssignableFrom(c)) {
                    if (BooleanResource.class.isAssignableFrom(c)) {
                        org.ogema.serialization.jaxb.BooleanResource br = new org.ogema.serialization.jaxb.BooleanResource();
                        if (value != null)
                        	br.setValue(Boolean.parseBoolean(String.valueOf(value)));
                        r = br;
                    } else if (IntegerResource.class.isAssignableFrom(c)) {
                        org.ogema.serialization.jaxb.IntegerResource ir = new org.ogema.serialization.jaxb.IntegerResource();
                        if (value != null)
                        	ir.setValue(Integer.parseInt(String.valueOf(value)));
                        r = ir;
                    } else if (FloatResource.class.isAssignableFrom(c)) {
                        org.ogema.serialization.jaxb.FloatResource fr = new org.ogema.serialization.jaxb.FloatResource();
                        if (value != null)
                        	fr.setValue(Float.parseFloat(String.valueOf(value)));
                        r = fr;
                    } else if (StringResource.class.isAssignableFrom(c)) {
                        org.ogema.serialization.jaxb.StringResource sr = new org.ogema.serialization.jaxb.StringResource();
                        if (value != null)
                        	sr.setValue(String.valueOf(value));
                        r = sr;
                    } else if (TimeResource.class.isAssignableFrom(c)) {
                        org.ogema.serialization.jaxb.TimeResource tr = new org.ogema.serialization.jaxb.TimeResource();
                        if (value != null)
                        	tr.setValue(Long.parseLong(String.valueOf(value)));
                        r = tr;
                    } else {
                        throw new RuntimeException("unsupported type: " + c);
                    }
                } else if (ArrayResource.class.isAssignableFrom(c)) {
                    if (BooleanArrayResource.class.isAssignableFrom(c)) {
                        org.ogema.serialization.jaxb.BooleanArrayResource ar = new org.ogema.serialization.jaxb.BooleanArrayResource();
                        r = ar;
                        if (value != null) {
                        	@SuppressWarnings("unchecked")
                        	List<String> l = (List<String>) value;
	                        for (String s : l) {
	                            ar.getValues().add(Boolean.valueOf(s));
	                        }
                        }
                    } else if (StringArrayResource.class.isAssignableFrom(c)) {
                        org.ogema.serialization.jaxb.StringArrayResource ar = new org.ogema.serialization.jaxb.StringArrayResource();
                        if (value != null) {
	                        @SuppressWarnings("unchecked")
	                        List<String> l = (List<String>) value;
	                        ar.getValues().addAll(l);
	                        r = ar;
                        }
                    } else if (FloatArrayResource.class.isAssignableFrom(c)) {
                        org.ogema.serialization.jaxb.FloatArrayResource ar = new org.ogema.serialization.jaxb.FloatArrayResource();
                        r = ar;
	                    if (value != null) {
	                        @SuppressWarnings("unchecked")
	                        List<String> l = (List<String>) value;
	                        for (String s : l) {
	                            ar.getValues().add(Float.valueOf(s));
	                        }
                        }
                    } else if (IntegerArrayResource.class.isAssignableFrom(c)) {
                        org.ogema.serialization.jaxb.IntegerArrayResource ar = new org.ogema.serialization.jaxb.IntegerArrayResource();
                        r = ar;
                        if (value != null) {
	                        @SuppressWarnings("unchecked")
	                        List<String> l = (List<String>) value;
	                        for (String s : l) {
	                            ar.getValues().add(Integer.valueOf(s));
	                        }
                        }
                    } else if (TimeArrayResource.class.isAssignableFrom(c)) {
                        org.ogema.serialization.jaxb.TimeArrayResource ar = new org.ogema.serialization.jaxb.TimeArrayResource();
                        r = ar;
	                        if (value != null) {
	                        @SuppressWarnings("unchecked")
	                        List<String> l = (List<String>) value;
	                        for (String s : l) {
	                            ar.getValues().add(Long.valueOf(s));
	                        }
                        }
                    } else if (ByteArrayResource.class.isAssignableFrom(c) || org.ogema.core.model.simple.OpaqueResource.class.isAssignableFrom(c)) {
                        org.ogema.serialization.jaxb.OpaqueResource tr = new org.ogema.serialization.jaxb.OpaqueResource();
                        if (value != null) 
                        	tr.setValue(BaseEncoding.base64().decode(String.valueOf(value)));
                        r = tr;
                    } else {
                        throw new RuntimeException("unsupported array type: " + c);
                    }
                } else if (Schedule.class.isAssignableFrom(c)) {
                    assert scheduleType != null;
                    ScheduleResource sched = null;
                    if (BooleanResource.class.isAssignableFrom(scheduleType)) {
                        sched = new BooleanSchedule();
                    } else if (FloatResource.class.isAssignableFrom(scheduleType)) {
                        sched = new FloatSchedule();
                    } else if (IntegerResource.class.isAssignableFrom(scheduleType)) {
                        sched = new IntegerSchedule();
                    } else if (ByteArrayResource.class.isAssignableFrom(scheduleType) || org.ogema.core.model.simple.OpaqueResource.class.isAssignableFrom(scheduleType)) {
                        sched = new OpaqueSchedule();
                    } else if (StringResource.class.isAssignableFrom(scheduleType)) {
                        sched = new StringSchedule();
                    } else if (TimeResource.class.isAssignableFrom(scheduleType)) {
                        sched = new TimeSchedule();
                    } else {
                        throw new RuntimeException("unsupported schedule resource type: " + c);
                    }
                    sched.getEntry().addAll(schedule);
                    sched.setStart(start);
                    sched.setEnd(end);
                    sched.setLastUpdateTime(lastUpdateTime);
                    sched.setLastCalculationTime(lastCalculationTime);
                    r = sched;
                } else {
                    throw new RuntimeException("unsupported value resource type: " + c);
                }
            } else {
                r = new Resource();
            }
            r.setName(name);
            r.setPath(path);
            r.setType(c);
            r.getSubresources().addAll(getSubresources());
            r.setActive(isActive());
            return r;
        }

    }
    
    Collection<Resource> readCollection(Reader reader) throws IOException {
        Collection<Resource> c = new ArrayList<>();
        try (JsonParser p = JFAC.createParser(reader)) {
            acceptStartArray(p);
            p.nextToken();
            do {
                c.add((Resource)readResource(null, p));
            } while (p.nextToken() != JsonToken.END_ARRAY);
        } catch (JsonParseException | ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
        return c;
    }

    Resource read(Reader reader) throws IOException, ClassNotFoundException {
        try (JsonParser p = JFAC.createParser(reader)) {
            p.nextToken(); //TODO: test startObject
            return (Resource) readResource(null, p);
        } catch (JsonParseException jpe) {
            throw new IOException(jpe.getMessage());
        }
    }

    /**
     * call when on object start.
     */
    private Object readResource(CompositeResource parent, JsonParser p) throws IOException, ClassNotFoundException {
        assert p.getCurrentToken() == JsonToken.START_OBJECT;
        JsonToken tok;
        CompositeResource res = new CompositeResource();
        while ((tok = p.nextToken()) != JsonToken.END_OBJECT) {
            if (tok != JsonToken.FIELD_NAME) {
                throw new IOException("malformed document, expected field at " + p.getCurrentLocation().toString());
            }
            String fieldName = p.getText();
            switch (fieldName) {
                case "name":
                    res.setName(p.nextTextValue());
                    break;
                case "type":
                    res.setType(p.nextTextValue());
                    break;
                case "path":
                    res.setPath(p.nextTextValue());
                    break;
                case "active":
                    res.setActive(p.nextBooleanValue());
                    break;
                case "link":
                    res.link = p.nextTextValue();
                    break;
                case "subresources":
                    res.getSubresources().addAll(readSubResources(res, p));
                    break;
                case "start":
                    res.start = p.nextLongValue(0);
                    break;
                case "end":
                    res.end = p.nextLongValue(0);
                    break;
                case "lastUpdateTime":
                    res.lastUpdateTime = p.nextLongValue(0);
                    break;
                case "lastCalculationTime":
                    res.lastCalculationTime = p.nextLongValue(0);
                    break;
                case "entry":
                    readScheduleEntries(res, parent, p);
                    break;
                case "value":
                    p.nextToken();
                    res.value = p.getText();
                    break;
                case "values":
                    readArrayValues(res, p);
                    break;
                default:
                    p.nextToken();
                    if (p.getCurrentToken() == JsonToken.START_OBJECT && res.getName() == null) {
                        // assume wrapper object
                        Object rval = readResource(parent, p);
                        p.nextToken();
                        return rval;
                    }
                //FIXME: else skip unknown                //FIXME: else skip unknown
            }
        }
        return res.toSpecializedResource();
    }

    private List<Object> readSubResources(CompositeResource parent, JsonParser p) throws IOException, ClassNotFoundException {
        acceptStartArray(p);
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return Collections.emptyList();
        }
        if (p.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IOException("malformed document, expected object start at " + p.getCurrentLocation().toString());
        }
        List<Object> resources = new ArrayList<>();
        do {
            resources.add(readResource(parent, p));
        } while (p.nextToken() == JsonToken.START_OBJECT);
        acceptEndArray(p);
        return resources;
    }

    private void acceptStartArray(JsonParser p) throws IOException {
        if (p.nextToken() != JsonToken.START_ARRAY) {
            throw new IOException("malformed document, expected array at " + p.getCurrentLocation().toString());
        }
    }

    private void acceptEndArray(JsonParser p) throws IOException {
        if (p.getCurrentToken() == JsonToken.END_ARRAY) {
            return;
        }
        if (p.nextToken() != JsonToken.END_ARRAY) {
            throw new IOException("malformed document, expected array end but found "
                    + p.getCurrentToken() + " at " + p.getCurrentLocation().toString());
        }
    }

    @SuppressWarnings("deprecation")
    private void readScheduleEntries(CompositeResource res, CompositeResource parent, JsonParser p) throws IOException, ClassNotFoundException {
        acceptStartArray(p);
        if (parent == null || parent.getType() == null) {
            throw new IOException("malformed document, cannot determine schedule type at " + p.getCurrentLocation());
        }
        Class<?> parentClass = Class.forName(parent.getType());
        if (!SingleValueResource.class.isAssignableFrom(parentClass)) {
            throw new IOException("malformed document, schedule on unsupported parent type '" + parentClass + "' at " + p.getCurrentLocation());
        }
        res.scheduleType = parentClass;
        if (BooleanResource.class.isAssignableFrom(parentClass)) {
            SCHEDULEREADERBOOLEAN.readSchedule(res, p);
        } else if (FloatResource.class.isAssignableFrom(parentClass)) {
            SCHEDULEREADERFLOAT.readSchedule(res, p);
        } else if (IntegerResource.class.isAssignableFrom(parentClass)) {
            SCHEDULEREADERINT.readSchedule(res, p);
        } else if (org.ogema.core.model.simple.OpaqueResource.class.isAssignableFrom(parentClass) || ByteArrayResource.class.isAssignableFrom(parentClass)) {
            SCHEDULEREADEROPAQUE.readSchedule(res, p);
        } else if (StringResource.class.isAssignableFrom(parentClass)) {
            SCHEDULEREADERSTRING.readSchedule(res, p);
        } else if (TimeResource.class.isAssignableFrom(parentClass)) {
            SCHEDULEREADERTIME.readSchedule(res, p);
        } else {
            throw new RuntimeException("unsupported schedule type: " + parentClass);
        }
    }

    private static abstract class ScheduleReader<T> {

        abstract T readValue(JsonParser p) throws IOException;

        abstract Value createScheduleValue(T value);

        void readSchedule(CompositeResource res, JsonParser p) throws IOException {
            res.schedule = new ArrayList<>();
            long time = -1;
            Quality quality = null;
            T value = null;
            while (p.nextToken() != JsonToken.END_ARRAY) {
                switch (p.getCurrentToken()) {
                    case START_OBJECT:
                        time = -1;
                        quality = null;
                        value = null;
                        break;
                    case FIELD_NAME:
                        switch (p.getCurrentName()) {
                            case SCHEDULE_ENTRY_TIME:
                                time = p.nextLongValue(-1);
                                break;
                            case SCHEDULE_ENTRY_QUALITY:
                                p.nextValue();
                                quality = qualityFromToken(p);
                                break;
                            case SCHEDULE_ENTRY_VALUE:
                                p.nextValue();
                                value = readValue(p);
                                break;
                            default:
                                p.nextToken();
                                break;
                        }
                        break;
                    case END_OBJECT:
                        checkScheduleEntry(time, quality, p);
                        SampledFloat v = new SampledFloat();
                        v.setTime(time);
                        v.setValue(createScheduleValue(value));
                        v.setQuality(quality);
                        res.schedule.add(v);
                        break;
                }
            }
        }

    }

    private static final ScheduleReader<Float> SCHEDULEREADERFLOAT = new ScheduleReader<Float>() {
        @Override
        Float readValue(JsonParser p) throws IOException {
            return p.getFloatValue();
        }

        @Override
        Value createScheduleValue(Float value) {
            return new FloatValue(value);
        }
    };
    
    private static final ScheduleReader<Integer> SCHEDULEREADERINT = new ScheduleReader<Integer>() {
        @Override
        Integer readValue(JsonParser p) throws IOException {
            return p.getIntValue();
        }

        @Override
        Value createScheduleValue(Integer value) {
            return new IntegerValue(value);
        }
    };
    
    private static final ScheduleReader<Long> SCHEDULEREADERTIME = new ScheduleReader<Long>() {
        @Override
        Long readValue(JsonParser p) throws IOException {
            return p.getLongValue();
        }

        @Override
        Value createScheduleValue(Long value) {
            return new LongValue(value);
        }
    };

    private static final ScheduleReader<String> SCHEDULEREADERSTRING = new ScheduleReader<String>() {
        @Override
        String readValue(JsonParser p) throws IOException {
            return p.getText();
        }

        @Override
        Value createScheduleValue(String value) {
            return new StringValue(value);
        }
    };
    
    private static final ScheduleReader<Boolean> SCHEDULEREADERBOOLEAN = new ScheduleReader<Boolean>() {
        @Override
        Boolean readValue(JsonParser p) throws IOException {
            return p.getBooleanValue();
        }

        @Override
        Value createScheduleValue(Boolean value) {
            return new BooleanValue(value);
        }
    };
    
    private static final ScheduleReader<byte[]> SCHEDULEREADEROPAQUE = new ScheduleReader<byte[]>() {
        @Override
        byte[] readValue(JsonParser p) throws IOException {
            return BaseEncoding.base64().decode(p.getText());
        }

        @Override
        Value createScheduleValue(byte[] value) {
            return new ByteArrayValue(value);
        }
    };

    private static void checkScheduleEntry(long time, Quality quality, JsonParser p) throws IOException {
        if (time == -1 || quality == null) {
            throw new IOException("malformed document, incomplete schedule entry at " + p.getCurrentLocation());
        }
    }

    private static Quality qualityFromToken(JsonParser p) throws IOException {
        try {
            return Quality.valueOf(p.getText());
        } catch (IllegalArgumentException iae) {
            throw new IOException("malformed document, illegal quality value at " + p.getCurrentLocation());
        }
    }

    private void readArrayValues(CompositeResource res, JsonParser p) throws IOException {
        JsonToken next = p.nextToken();
        if (null == next) {
            throw new IOException("malformed document, not a supported array value at "
                    + p.getCurrentLocation().toString());
        } else switch (next) {
            case VALUE_STRING:
                res.value = p.getText();
                break;
            case START_ARRAY:
                if (p.nextToken() == JsonToken.END_ARRAY) {
                    res.value = Collections.EMPTY_LIST;
                } else {
                    ArrayList<String> a = new ArrayList<>();
                    res.value = a;
                    do {
                        a.add(p.getText());
                    } while (p.nextToken() != JsonToken.END_ARRAY);
                }   break;
            default:
                throw new IOException("malformed document, not a supported array value at "
                        + p.getCurrentLocation().toString());
        }
    }

    public static void main(String[] args) throws Exception {
        FileReader r = new FileReader("/home/jlapp/temp/resource-test-3.json");
        //URL u = new URL("http://localhost:8080/rest/resources/__testFloatRes__?user=rest&pw=rest&depth=10&schedules=true");
        //Reader r = new InputStreamReader(u.openStream());
        new JsonReaderJackson().read(r);
    }

}
