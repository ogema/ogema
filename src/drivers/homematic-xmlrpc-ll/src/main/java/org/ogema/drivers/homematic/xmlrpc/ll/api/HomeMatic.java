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

import java.util.List;
import java.util.Map;
import org.apache.xmlrpc.XmlRpcException;

/**
 *
 * @author jlapp
 */
public interface HomeMatic {
    
    void ping(String callerId) throws XmlRpcException;
    
    void init(String url, String interface_id) throws XmlRpcException;
    
    List<DeviceDescription> listDevices() throws XmlRpcException;
    
    DeviceDescription getDeviceDescription(String address) throws XmlRpcException;
    
    Map<String, ParameterDescription<?>> getParamsetDescription(String address, String type) throws XmlRpcException;
    
    XmlRpcStruct getParamset(String address, String type) throws XmlRpcException;
    
    void putParamset(String address, String paramset_key, XmlRpcStruct set) throws XmlRpcException;
    
    /**
     * @return time is seconds that installation mode will remain active, 0 if inactive.
     * @throws XmlRpcException 
     */
    int getInstallMode() throws XmlRpcException;
    
    /**
     * @param on install mode active state.
     * @param time time to remain active.
     * @param mode 1: normal mode, 2: set all MASTER parameters to their default value and delete all links.
     * @throws XmlRpcException 
     */    
    void setInstallMode(boolean on, int time, int mode) throws XmlRpcException;
    
    void setValue(String address, String value_key, Object value) throws XmlRpcException;
    
    <T extends Object> T getValue(String address, String value_key) throws XmlRpcException;
    
    boolean reportValueUsage(String address, String valueId, int refCounter) throws XmlRpcException;
    
}
