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
package org.ogema.drivers.homematic.xmlrpc.ll.api;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author jlapp
 */
public interface XmlRpcStruct {
    
    int getInt(String key);
    
    void setInt(String key, int val);
    
    boolean getBoolean(String key);
    
    void setBoolean(String key, boolean val);
    
    String getString(String key);
    
    void setString(String key, String val);
    
    String[] getStringArray(String key);
    
    void setStringArray(String key, String[] val);
    
    Object getValue(String key);
    
    void setValue(String key, Object val);
    
    Set<String> keySet();
    
    boolean containsKey(String key);
    
    Map<String, Object> toMap();
    
}
