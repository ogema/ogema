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
 * Sends commands to a HomeMatic logic interface via XML-RPC, see the HomeMatic
 * XML-RPC specification, available from eQ-3 for details.
 * 
 * @author jlapp
 */
public interface HomeMatic {
    
    void ping(String callerId) throws XmlRpcException;
    
    void init(String url, String interface_id) throws XmlRpcException;
    
    List<DeviceDescription> listDevices() throws XmlRpcException;
    
    DeviceDescription getDeviceDescription(String address) throws XmlRpcException;
    
    Map<String, ParameterDescription<?>> getParamsetDescription(String address, String type) throws XmlRpcException;
    
    XmlRpcStruct getParamset(String address, String paramset_key) throws XmlRpcException;
    
    void putParamset(String address, String paramset_key, XmlRpcStruct set) throws XmlRpcException;
    
    String getParamsetId(String address, String type) throws XmlRpcException;
    
    /**
     * @return time in seconds that installation mode will remain active, 0 if inactive.
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
    
    void addLink(String sender, String receiver, String name, String description) throws XmlRpcException;
    
    void removeLink(String sender, String receiver) throws XmlRpcException;
    
    List<String> getLinkPeers(String address) throws XmlRpcException;
    
    Map<String, Object> getLinkInfo(String sender, String receiver) throws XmlRpcException;
    
    List<Map<String, Object>> getLinks(String address, int flags) throws XmlRpcException;
    
    List<ServiceMessage> getServiceMessages() throws XmlRpcException;
    
    /**
     * Flags:
     * <dl>
     * <dt>0x01</dt> <dd>DELETE_FLAG_RESET - reset device before delete</dd>
     * <dt>0x02</dt> <dd>DELETE_FLAG_FORCE - delete even if device is not reachable</dd>
     * <dt>0x04</dt> <dd>DELETE_FLAG_DEFER - delete as soon as device is reachable</dd>
     * </dl>
     * @param address device address
     * @param flags see javadoc
     * @throws XmlRpcException
     */
    void deleteDevice(String address, int flags) throws XmlRpcException;
    
    void abortDeleteDevice(String address) throws XmlRpcException;
    
    XmlRpcStruct rssiInfo() throws XmlRpcException;
    
}
