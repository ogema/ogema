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
package org.ogema.drivers.homematic.xmlrpc.hl.api;

import java.util.Map;

import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;

/**
 * DeviceHandlers are called to setup OGEMA resources for their supported HomeMatic
 * devices and manage all communication between their OGEMA resources and HomeMatic.
 * The HomeMatic driver will create DeviceHandlers using a {@link DeviceHandlerFactory},
 * through which the DeviceHandler instance has access to a {@link HomeMaticConnection}
 * that provides the necessary communication methods.
 * <br>
 * The driver will call DeviceHandlers according to the OSGi service ranking
 * of their {@link DeviceHandlerFactory} and only the first handler which returns
 * true from its {@link #accept } method will have its {@link #setup } method called.
 * 
 * @author jlapp
 */
public interface DeviceHandler {
    
    /**
     * Called by the HomeMatic driver to determine whether this
     * DeviceHandler wants to control the device from the {@link DeviceDescription}.
     * 
     * @param desc HomeMatic device (or channel) description.
     * @return true if this DeviceHandler wants to control the given device.
     */
    boolean accept(DeviceDescription desc);
    
    /**
     * Called by the HomeMatic driver after the DeviceHandler returned true
     * from an {@link #accept } call.
     * The DeviceDescription will be the same as the one used in the {@code accept} call.
     * The device parameter will always be the OGEMA resource associated with
     * the main HomeMatic device, even if setup is called for a channel, since
     * additional OGEMA resources should be created on this resource.
     * <br>
     * The {@code paramSets} parameter contains all available Paramsets as
     * named by the device description ({@link DeviceDescription#getParamsets() }).
     * Each {@code paramSet} map contains the available parameter names as keys
     * along with their {@link ParameterDescription}.
     * 
     * @param device OGEMA resource representing the main HomeMatic device.
     * @param desc Description for the HomeMatic device or channel.
     * @param paramSets Parameter sets available for this logical device.
     * 
     */
    void setup(HmDevice device, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets);
    
}
