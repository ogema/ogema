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

/**
 *
 * @author jlapp
 */
public interface HomeMaticCalls {

    /**
     * @param interfaceId
     * @return Array<DeviceDescription>
     */
    Object listDevices(String interfaceId);

    Void newDevices(String interfaceId, Object[] descriptions);
    
    /**
     * @param interfaceId
     * @param addresses actually a String array
     * @return void
     */
    Void deleteDevices(String interfaceId, String[] addresses);
    
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
    
}
