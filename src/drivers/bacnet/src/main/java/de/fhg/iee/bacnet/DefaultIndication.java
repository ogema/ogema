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
package de.fhg.iee.bacnet;

import de.fhg.iee.bacnet.apdu.ProtocolControlInformation;
import de.fhg.iee.bacnet.api.DeviceAddress;
import de.fhg.iee.bacnet.api.Indication;
import de.fhg.iee.bacnet.api.Transport;
import java.nio.ByteBuffer;

/**
 *
 * @author jlapp
 */
public class DefaultIndication implements Indication {
    
    private final DeviceAddress src;
    private final ProtocolControlInformation pci;
    private final ByteBuffer data;
    private final Transport.Priority prio;
    private final boolean expectReply;
    private final Transport transport;

    public DefaultIndication(DeviceAddress src, ProtocolControlInformation pci, ByteBuffer data, Transport.Priority prio, boolean expectReply, Transport transport) {
        this.src = src;
        this.pci = pci;
        this.data = data;
        this.prio = prio;
        this.expectReply = expectReply;
        this.transport = transport;
    }
    
    public DefaultIndication(Indication i) {
        this.src = i.getSource();
        this.pci = i.getProtocolControlInfo();
        this.data = i.getData().duplicate();
        this.prio = i.getPriority();
        this.expectReply = i.getExpectingReply();
        this.transport = i.getTransport();
    }
    
    @Override
    public DeviceAddress getSource() {
        return src;
    }

    @Override
    public ProtocolControlInformation getProtocolControlInfo() {
        return pci;
    }

    @Override
    public ByteBuffer getData() {
        return data;
    }

    @Override
    public Transport.Priority getPriority() {
        return prio;
    }

    @Override
    public boolean getExpectingReply() {
        return expectReply;
    }

    @Override
    public Transport getTransport() {
        return transport;
    }
    
}
