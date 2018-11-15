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
package org.ogema.drivers.homematic.xmlrpc.hl.types;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.communication.CommunicationStatus;
import org.ogema.model.devices.storage.ElectricityStorage;

/**
 * Stores information from the channel 'Maintenance' that is available on
 * every HomeMatic device.
 * 
 * @author jlapp
 */
public interface HmMaintenance extends Resource {
    
    IntegerResource errorCode();
    
    IntegerResource rssiDevice();
    
    IntegerResource rssiPeer();
    
    BooleanResource batteryLow();
    
    /**
     * A battery resource should only be created for devices that report
     * an {@code OPERATING_VOLTAGE}, which will be available as
     * {@link ElectricityStorage#internalVoltage() }.
     * @return device battery
     */
    ElectricityStorage battery();
    
    /**
     * Report the HomeMatic UNREACH status as {@link CommunicationStatus#communicationDisturbed()}.
     * 
     * @return device communication status.
     */
    CommunicationStatus communicationStatus();
    
}
