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

/**
 *
 * @author jlapp
 */
public interface DeviceListener {
    
    /** newDevices */
    void deviceAdded(String interfaceId, List<DeviceDescription> descriptions);
    
    void devicesDeleted(String interfaceId, List<String> addresses);
    
    void deviceUpdated(String interfaceId, String address, int hint);
    
    void deviceReplaced(String interfaceId, String oldDeviceAddress, String newDeviceAddress);
    
    void deviceReadded(String interfaceId, List<String> addresses);
    
}
