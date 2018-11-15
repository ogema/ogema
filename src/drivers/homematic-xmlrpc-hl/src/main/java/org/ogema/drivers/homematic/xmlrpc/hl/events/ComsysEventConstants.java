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
package org.ogema.drivers.homematic.xmlrpc.hl.events;

/**
 *
 * @author jlapp
 */
public abstract class ComsysEventConstants {
    
    private ComsysEventConstants() {}
    
    public static final String TOPIC = "ogema/drivers/homematic";
    
    public static enum MESSAGE_TYPE {
        
        CommunicationFailed,
        CommunicationRestored,
        NewDevice,
        DeviceRemoved,
        LowBattery,
        LowSignalStrength,
        UserInteractionRequired,
        ManualOverrideDetected
        
    }
    
    /**
     * Type of event, a String specifying the type of event from the communication
     * system.
     */
    public static String EVENT_TYPE = "type";
    
}

