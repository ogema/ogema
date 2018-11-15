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
