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
package org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc;

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
    public String toString() {
        return struct.toString();
    }

    @Override
    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(struct);
    }
    
}
