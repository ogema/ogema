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
