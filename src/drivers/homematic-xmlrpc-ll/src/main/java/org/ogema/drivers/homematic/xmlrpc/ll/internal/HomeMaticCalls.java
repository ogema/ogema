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
package org.ogema.drivers.homematic.xmlrpc.ll.internal;

/**
 *
 * @author jlapp
 */
public interface HomeMaticCalls {

    /**
     * @param interfaceId
     * @return Array&lt;DeviceDescription&gt;
     */
    Object listDevices(String interfaceId);

    Void newDevices(String interfaceId, Object[] descriptions);
    
    /**
     * @param interfaceId
     * @param addresses actually a String array
     * @return void
     */
    Void deleteDevices(String interfaceId, Object[] addresses);
    
    /**
     * @param interfaceId
     * @param address
     * @param hint 0: unspecified change, 1: number of links changed
     * @return void
     */
    Void updateDevice(String interfaceId, String address, int hint);
    
    /**
     * @param interfaceId
     * @param oldDeviceAddress
     * @param newDeviceAddress
     * @return void
     */
    Void replaceDevice(String interfaceId, String oldDeviceAddress, String newDeviceAddress);
    
    /**
     * @param interfaceId
     * @param addresses actually a String array
     * @return void
     */
    Void readdedDevice(String interfaceId, String[] addresses);
    
    /**
     * sent by the window/door contact, unknown whether other value types are needed...
     */
    Void event(String interfaceId, String address, String valueKey, int value);
    
}
