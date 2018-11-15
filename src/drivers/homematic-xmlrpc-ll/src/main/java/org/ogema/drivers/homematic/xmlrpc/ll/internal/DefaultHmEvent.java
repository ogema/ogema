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
