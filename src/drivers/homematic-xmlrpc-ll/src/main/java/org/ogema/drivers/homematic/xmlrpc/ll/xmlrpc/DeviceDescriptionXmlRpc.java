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
    
    final static List<DeviceDescription.KEYS> REQUIRED_KEYS = Arrays.asList(
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
        if (a == null) {
            return null;
        }
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
    
    @Override
    public String getParent() {
        return getString(KEYS.PARENT.name());
    }
    
    @Override
    public String getParentType() {
        return getString(KEYS.PARENT_TYPE.name());
    }

    @Override
    public String getType() {
        return getString(KEYS.TYPE.name());
    }
    
    @Override
    public boolean isDevice() {
        String parent = getParent();
        return parent == null || parent.isEmpty();
    }
    
    @Override
    public int getVersion() {
        return getInt(KEYS.VERSION.name());
    }
    
    @Override
    public String[] getChildren() {
        return getStringArray(KEYS.CHILDREN.name());
    }
    
}
