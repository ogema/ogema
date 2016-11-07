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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;

/**
 *
 * @author jlapp
 */
public class DeviceDescriptionXmlRpc extends MapXmlRpcStruct implements DeviceDescription {
    
    final static List<DeviceDescription.KEYS> requiredKeys = Arrays.asList(
            DeviceDescription.KEYS.PARAMSETS,
            DeviceDescription.KEYS.RF_ADDRESS,
            DeviceDescription.KEYS.CHILDREN,
            DeviceDescription.KEYS.INTERFACE,
            DeviceDescription.KEYS.RX_MODE,
            DeviceDescription.KEYS.ADDRESS,
            DeviceDescription.KEYS.FIRMWARE,
            DeviceDescription.KEYS.PARENT,
            DeviceDescription.KEYS.VERSION,
            DeviceDescription.KEYS.FLAGS,
            DeviceDescription.KEYS.ROAMING,
            DeviceDescription.KEYS.TYPE
    );
    
    private static final Map<DeviceDescription.KEYS, Object> EMPTYDEVICE = new LinkedHashMap<>();
    static {
        EMPTYDEVICE.put(DeviceDescription.KEYS.PARAMSETS, new Object[]{});
        EMPTYDEVICE.put(DeviceDescription.KEYS.RF_ADDRESS, 0);
        EMPTYDEVICE.put(DeviceDescription.KEYS.CHILDREN, new Object[]{});
        EMPTYDEVICE.put(DeviceDescription.KEYS.UPDATABLE, 0);
        EMPTYDEVICE.put(DeviceDescription.KEYS.INTERFACE, "");
        EMPTYDEVICE.put(DeviceDescription.KEYS.RX_MODE, 0);
        EMPTYDEVICE.put(DeviceDescription.KEYS.ADDRESS, "");
        EMPTYDEVICE.put(DeviceDescription.KEYS.FIRMWARE, "?");
        EMPTYDEVICE.put(DeviceDescription.KEYS.PARENT, "");
        EMPTYDEVICE.put(DeviceDescription.KEYS.VERSION, 0);
        EMPTYDEVICE.put(DeviceDescription.KEYS.FLAGS, 0);
        EMPTYDEVICE.put(DeviceDescription.KEYS.ROAMING, 0);
        EMPTYDEVICE.put(DeviceDescription.KEYS.TYPE, "");
    }
    

    public DeviceDescriptionXmlRpc(Map<String, Object> struct) {
        super(struct);
    }
    
    public DeviceDescriptionXmlRpc(DeviceDescription desc) {
        super(new LinkedHashMap<String, Object>());
        for (String key: desc.keySet()) {
            struct.put(key, desc.getValue(key));
        }
    }

    /**
     * Creates a DeviceDescription with the minimum information required for use
     * in the HomeMatic listDevices() function.
     * 
     * @param address Homematic address string
     * @param version Homematic version
     */
    public DeviceDescriptionXmlRpc(String address, int version) {
        super(new LinkedHashMap<String, Object>());
        for (Map.Entry<DeviceDescription.KEYS, Object> e: EMPTYDEVICE.entrySet()) {
            struct.put(e.getKey().name(), e.getValue());
        }
        struct.put(DeviceDescription.KEYS.ADDRESS.name(), address);
        struct.put(DeviceDescription.KEYS.VERSION.name(), version);
    }
    
    public static String[] asStringArray(Object[] a) {
        String[] s = new String[a.length];
        for (int i = 0; i < a.length; i++) {
            s[i] = a[i].toString();
        }
        return s;
    }
    
    @Override
    public String getAddress() {
        return getString(KEYS.ADDRESS.name());
    }

    @Override
    public String[] getParamsets() {
        return getStringArray(KEYS.PARAMSETS.name());
    }
    
}
