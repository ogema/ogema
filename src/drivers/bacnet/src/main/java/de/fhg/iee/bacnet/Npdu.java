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

import de.fhg.iee.bacnet.api.Transport;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 *
 * @author jlapp
 */
public class Npdu implements Cloneable {

    private static final int CONTROL_EXPECTINGREPLY = 1 << 2;
    private static final int CONTROL_DNET = 1 << 5;
    private static final int CONTROL_SNET = 1 << 3;
    private static final int CONTROL_MTYPE = 1 << 7;
    private static final int DNET_BROADCAST = 0xFFFF;
    private final int protocolVersion = 0x01;

    private boolean expectingReply;

    private boolean hasDestination = false;
    private byte[] dnet;
    private byte[] dadr;
    private int hopCount;

    private boolean hasSource = false;
    private byte[] snet;
    private byte[] sadr;

    private int messageType = -1;
    private int vendorId;

    private Transport.Priority prio = Transport.Priority.Normal;
    
    public Npdu() {
    }
    
    public Npdu(ByteBuffer buf) {
        read(buf);
    }

    public Npdu(Npdu b) {
        this.expectingReply = b.expectingReply;
        this.dnet = b.dnet;
        this.dadr = b.dadr;
        this.hasDestination = b.hasDestination;
        this.hopCount = b.hopCount;
        this.snet = b.snet;
        this.sadr = b.sadr;
        this.hasSource = b.hasSource;
        this.vendorId = b.vendorId;
    }

    public Npdu withMessageType(int type, int vendorId) {
        if (type < 0 || type > 255) {
            throw new IllegalArgumentException("type out of range: " + type);
        }
        if (vendorId < 0 || vendorId > ((1 << 16) - 1)) {
            throw new IllegalArgumentException("vendorId out of range: " + vendorId);
        }
        Npdu copy = new Npdu(this);
        copy.messageType = type;
        copy.vendorId = vendorId;
        return copy;
    }
    
    public Npdu asApduMessage() {
        Npdu copy = new Npdu(this);
        copy.messageType = -1;
        return copy;
    }

    /**
     * @param dnet destination network.
     * @param dadr destination address, may be null.
     * @param hops hop count, 0-255.
     * @return this builder.
     */
    public Npdu withDestination(int dnet, byte[] dadr, int hops) {
        if (dnet < 0 || dnet > ((1 << 16) - 1)) {
            throw new IllegalArgumentException("dnet out of range: " + dnet);
        }
        if (dadr != null && dadr.length > 255) {
            throw new IllegalArgumentException("dadr too big: " + sadr.length);
        }
        Npdu copy = new Npdu(this);
        copy.hasDestination = true;
        copy.hopCount = hops & 255;
        copy.dnet = new byte[]{(byte) ((dnet & 0xFF00) >> 8), (byte) (dnet & 0xFF)};
        copy.dadr = dadr;
        return copy;
    }
    
    public int getDestinationNet() {
        return dnet != null ? (dnet[0] << 8 | dnet[1]) : 0;
    }
    
    public int getSourceNet() {
        return snet != null ? (snet[0] << 8 | snet[1]) : 0;
    }
    
    public int getMessageType() {
        return messageType;
    }
    
    public byte[] getDestinationAddress() {
        return dadr;
    }
    
    public byte[] getSourceAddress() {
        return sadr;
    }
    
    public Npdu withoutDestination() {
        Npdu copy = new Npdu(this);
        copy.hasDestination = false;
        copy.hopCount = 0;
        copy.dnet = null;
        copy.dadr = null;
        return copy;
    }

    public Npdu withExpectingReply(boolean exp) {
        Npdu copy = new Npdu(this);
        copy.expectingReply = exp;
        return copy;
    }

    /**
     * @param snet source network.
     * @param sadr source address.
     * @return this builder.
     */
    public Npdu withSource(int snet, byte[] sadr) {
        Objects.requireNonNull(sadr);
        if (snet < 0 || snet > ((1 << 16) - 1)) {
            throw new IllegalArgumentException("snet out of range: " + snet);
        }
        if (sadr.length == 0 || sadr.length > 255) {
            throw new IllegalArgumentException("bad sadr size: " + sadr.length);
        }
        Npdu copy = new Npdu(this);
        copy.hasSource = true;
        copy.snet = new byte[]{(byte) ((snet & 0xFF00) >> 8), (byte) (snet & 0xFF)};
        copy.sadr = sadr;
        return copy;
    }
    
    public Npdu withoutSource() {
        Npdu copy = new Npdu(this);
        copy.hasSource = false;
        copy.snet = null;
        copy.sadr = null;
        return copy;
    }

    public Npdu withPriority(Transport.Priority prio) {
        Objects.requireNonNull(prio);
        Npdu copy = new Npdu(this);
        copy.prio = prio;
        return copy;
    }
    
    public boolean isNetworkMessage() {
        return messageType != -1;
    }
    
    public boolean isExpectingReply() {
        return expectingReply;
    }
    
    public boolean isBroadcast() {
        return hasDestination && (dadr == null || dadr.length == 0);
    }
    
    public boolean hasSource() {
        return hasSource;
    }
    
    public boolean hasDestination() {
        return hasDestination;
    }

    public Transport.Priority getPriority() {
        return prio;
    }
    
    byte[] toArray() {
        int control = 0;
        if (messageType > -1) {
            control |= CONTROL_MTYPE;
        }
        if (expectingReply) {
            control |= CONTROL_EXPECTINGREPLY;
        }
        if (hasSource) {
            control |= CONTROL_SNET;
        }
        if (hasDestination) {
            control |= CONTROL_DNET;
        }

        switch (prio) {
            case LifeSafety:
                control |= 0b11;
                break;
            case CriticalEquipment:
                control |= 0b10;
                break;
            case Urgent:
                control |= 0b01;
                break;
            default:
                break;
        }

        int size = 2;
        if ((control & CONTROL_DNET) > 0) {
            size += 4 + (dadr != null ? dadr.length : 0);
        }
        if ((control & CONTROL_SNET) > 0) {
            size += 3 + sadr.length;
        }
        if ((control & CONTROL_MTYPE) > 0) {
            size += 1;
            if (messageType >= 0x80) {
                size += 2;
            }
        }
        byte[] npdu = new byte[size];

        npdu[0] = (byte) protocolVersion;
        npdu[1] = (byte) control;
        int idx = 2;
        if ((control & CONTROL_DNET) > 0) {
            npdu[idx++] = dnet[0];
            npdu[idx++] = dnet[1];
            if (dadr != null) {
                npdu[idx++] = (byte) dadr.length;
                System.arraycopy(dadr, 0, npdu, idx, dadr.length);
                idx += dadr.length;
            } else {
                npdu[idx++] = 0;
            }
            npdu[idx++] = (byte) hopCount;
        }
        if ((control & CONTROL_SNET) > 0) {
            npdu[idx++] = snet[0];
            npdu[idx++] = snet[1];
            npdu[idx++] = (byte) sadr.length;
            System.arraycopy(sadr, 0, npdu, idx, sadr.length);
            idx += sadr.length;
        }
        if ((control & CONTROL_MTYPE) > 0) {
            npdu[idx++] = (byte) (messageType & 0xFF);
            if (messageType >= 0x80) {
                npdu[idx++] = (byte) ((vendorId & 0xFF00) >> 8);
                npdu[idx++] = (byte) (vendorId & 0xFF);
            }
        }
        return npdu;
    }
    
    static int getUnsignedByte(ByteBuffer b) {
        int value = b.get();
        if (value < 0) {
            value = 256 + value;
        }
        return value;
    }
    
    private void read(ByteBuffer buf) {
        int version = getUnsignedByte(buf);
        int control = getUnsignedByte(buf);
        if ((control & CONTROL_DNET) > 0) {
            hasDestination = true;
            dnet = new byte[2];
            buf.get(dnet);
            int dlen = getUnsignedByte(buf);
            dadr = new byte[dlen];
            buf.get(dadr);
        }
        if ((control & CONTROL_SNET) > 0) {
            hasSource = true;
            snet = new byte[2];
            buf.get(snet);
            int slen = getUnsignedByte(buf);
            sadr = new byte[slen];
            buf.get(sadr);
        }
        if (hasDestination) {
            hopCount = getUnsignedByte(buf);
        }
        if ((control & CONTROL_MTYPE) > 0) {
            messageType = getUnsignedByte(buf);
            if (messageType >= 0x80) {
                vendorId = buf.getChar();
            }
        }
    }

}
