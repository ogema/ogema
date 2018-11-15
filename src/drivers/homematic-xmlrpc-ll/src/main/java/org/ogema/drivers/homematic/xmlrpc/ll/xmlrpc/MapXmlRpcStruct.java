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
package org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.ogema.drivers.homematic.xmlrpc.ll.api.XmlRpcStruct;

/**
 *
 * @author jlapp
 */
public class MapXmlRpcStruct implements XmlRpcStruct {
    
    protected final Map<String, Object> struct;
    
    public MapXmlRpcStruct(Map<String, Object> map) {
        struct = map;
    }
    
        @Override
    public String[] getStringArray(String key) {
        return DeviceDescriptionXmlRpc.asStringArray((Object[]) struct.get(key));
    }
    
    @Override
    public boolean getBoolean(String key) {
        return ((Number) struct.get(key)).intValue() != 0;
    }
    
    @Override
    public int getInt(String key) {
        return ((Number) struct.get(key)).intValue();
    }
    
    public Map<String, Object> getStruct() {
        return struct;
    }

    @Override
    public String getString(String key) {
        return String.valueOf(struct.get(key));
    }

    @Override
    public void setStringArray(String key, String[] val) {
        struct.put(key, val);
    }    

    @Override
    public void setBoolean(String key, boolean val) {
        struct.put(key, val);
    }

    @Override
    public void setInt(String key, int val) {
        struct.put(key, val);
    }

    @Override
    public void setString(String key, String val) {
        struct.put(key, val);
    }

    @Override
    public Object getValue(String key) {
        return struct.get(key);
    }

    @Override
    public void setValue(String key, Object val) {
        struct.put(key, val);
    }

    @Override
    public boolean containsKey(String key) {
        return struct.containsKey(key);
    }
    
    @Override
    public Set<String> keySet() {
        return struct.keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, Object> e: struct.entrySet()) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(e.getKey()).append("=");
            Object val = e.getValue();
            if (val.getClass().isArray()) {
                sb.append(Arrays.toString((Object[])val));
            } else if (val instanceof Map) {
                sb.append(new MapXmlRpcStruct((Map<String, Object>)val).toString());
            } else {
                sb.append(val);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(struct);
    }
    
}
