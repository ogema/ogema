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
package de.fhg.iee.bacnet.api;

import de.fhg.iee.bacnet.apdu.ProtocolControlInformation;
import java.nio.ByteBuffer;

/**
 * Event object passed from the transport to registered applications when
 * a message arrived or an error is detected.
 * 
 * @author jlapp
 */
public interface Indication {
    
    DeviceAddress getSource();
    
    Transport getTransport();
    
    /**
     * The application protocol control information contained in the message.
     * 
     * @return Message PCI.
     */
    ProtocolControlInformation getProtocolControlInfo();
    
    /**
     * Buffer containing the full APDU with its position and mark set to the
     * first byte of service related data (i.e. after the PCI).
     * 
     * @return APDU data, positioned after PCI data.
     */
    ByteBuffer getData();
    
    Transport.Priority getPriority();
    
    boolean getExpectingReply();    
    
}
