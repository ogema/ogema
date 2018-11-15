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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;

/**
 *
 * @author jlapp
 */
public class ParameterDescriptionXmlRpc<T extends Object> extends MapXmlRpcStruct implements ParameterDescription<T> {

    public ParameterDescriptionXmlRpc(Map<String, Object> map) {
        super(map);
    }

    @Override
    public int getTabOrder() {
        return getInt(KEYS.TAB_ORDER.name());
    }

    @Override
    public TYPES getType() {
        return TYPES.valueOf(getString(KEYS.TYPE.name()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getDefault() {
        return (T) getValue(KEYS.DEFAULT.name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, T> getSpecial() {
        Object[] svals = (Object[]) struct.get(KEYS.SPECIAL.name());
        if (svals == null || svals.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, T> rval = new HashMap<>();
        for (Object o: svals) {
            Map<String, Object> vstruct = (Map<String, Object>) o;
            rval.put(String.valueOf(vstruct.get(SPECIAL_KEYS.ID.name())), (T) vstruct.get(SPECIAL_KEYS.VALUE.name()));
        }
        return rval;
    }
    
    @Override
    public int getOperations() {
        return getInt(KEYS.OPERATIONS.name());
    }

    @Override
    public boolean isReadable() {
        return (getOperations() & 0x01) != 0;
    }

    @Override
    public boolean isWritable() {
        return (getOperations() & 0x02) != 0;
    }

    @Override
    public boolean isEvent() {
        return (getOperations() & 0x04) != 0;
    }

    @Override
    public String getUnit() {
        return getString(KEYS.UNIT.name());
    }
    
}
