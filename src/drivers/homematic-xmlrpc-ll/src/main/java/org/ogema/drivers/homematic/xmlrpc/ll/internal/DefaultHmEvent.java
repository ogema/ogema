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
package org.ogema.drivers.homematic.xmlrpc.ll.internal;

import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;

/**
 *
 * @author jlapp
 */
public class DefaultHmEvent implements HmEvent {
    
    final String interfaceId;
    final String address;
    final String valueKey;
    final Object value;

    public DefaultHmEvent(String interfaceId, String address, String valueKey, Object value) {
        this.interfaceId = interfaceId;
        this.address = address;
        this.valueKey = valueKey;
        this.value = value;
    }

    @Override
    public String getInterfaceId() {
        return interfaceId;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public String getValueKey() {
        return valueKey;
    }

    @Override
    public Object getValue() {
        return value;
    }
    
    @Override
    public boolean getValueBoolean() {
        //return ((Number) value).intValue() != 0;
        return Boolean.valueOf(getValueString());
    }
    
    @Override
    public float getValueFloat() {
        return ((Number) value).floatValue();
    }
    
    @Override
    public int getValueInt() {
        return ((Number) value).intValue();
    }
    
    @Override
    public String getValueString() {
        return value.toString();
    }

    @Override
    public String toString() {
        return String.format("Event for %s: %s@%s = %s", interfaceId, valueKey, address, value);
    }
    
}
