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
package de.fhg.iee.bacnet.apdu;

import de.fhg.iee.bacnet.api.BACnetEnumeration;
import java.nio.ByteBuffer;

/**
 * Immutable representation of a protocol control information set, which is the
 * fixed part of a BACnet APDU.
 *
 * @author jlapp
 */
public class ProtocolControlInformation implements Cloneable {

    private final int pduType;
    private final int serviceChoice;

    private boolean segmented;
    private boolean moreFollows;
    private int sequenceNumber;
    private int proposedWindowSize;

    private boolean acceptSegmented;
    private ApduConstants.MAX_SEGMENTS maxSegmentsAccepted;
    private ApduConstants.RESPONSE_SIZE maxApduLength;

    private int invokeId;

    public ProtocolControlInformation(ApduConstants.APDU_TYPES type, int serviceChoice) {
        this.pduType = type.getMask();
        this.serviceChoice = serviceChoice;
    }

    public ProtocolControlInformation(ApduConstants.APDU_TYPES type, BACnetEnumeration serviceChoice) {
        this(type, serviceChoice.getBACnetEnumValue());
    }

    public ProtocolControlInformation(int type, BACnetEnumeration serviceChoice) {
        this(type, serviceChoice.getBACnetEnumValue());
    }

    /**
     * @param pduType pass PDU type as full 8-bit mask, as defined in
     * {@link ApduConstants}
     * @param serviceChoice
     */
    public ProtocolControlInformation(int pduType, int serviceChoice) {
        this.pduType = pduType;
        this.serviceChoice = serviceChoice;
    }

    /**
     * Initialize all fields from the ByteBuffer. The ByteBuffer's position must
     * be on the first byte of the PCI content when passed to the constructor
     * and will be on first byte of the APDU's variable part when the
     * constructor succeeds.
     *
     * @param buf ByteBuffer containing an APDU, positioned on the first PCI
     * byte.
     */
    public ProtocolControlInformation(ByteBuffer buf) {
        int b0 = getUnsignedByte(buf);
        this.pduType = b0 & 0b1111_0000;
        segmented = (b0 & ApduConstants.SEGMENTED_REQUEST) != 0;
        moreFollows = (b0 & ApduConstants.SEGMENTS_MORE_FOLLOW) != 0;
        acceptSegmented = (b0 & ApduConstants.SEGMENTED_RESPONSE_ACCEPTED) != 0;

        //TODO review cases where b1 is segment info...
        if (hasSegmentAndSizeInfo(pduType)) {
            int b1 = getUnsignedByte(buf);
            maxSegmentsAccepted = ApduConstants.MAX_SEGMENTS.fromOctet(b1);
            maxApduLength = ApduConstants.RESPONSE_SIZE.fromOctet(b1);
        }

        if (hasInvokeId(pduType)) {
            invokeId = getUnsignedByte(buf);
        }

        if (segmented) {
            sequenceNumber = getUnsignedByte(buf);
            proposedWindowSize = getUnsignedByte(buf);
        }

        serviceChoice = getUnsignedByte(buf);
    }

    private boolean hasSegmentAndSizeInfo(int pduType) {
        return pduType == ApduConstants.TYPE_CONFIRMED_REQ;
    }

    private boolean hasInvokeId(int pduType) {
        return pduType != ApduConstants.TYPE_UNCONFIRMED_REQ;
        /*
        return pduType == ApduConstants.TYPE_CONFIRMED_REQ
                || pduType == ApduConstants.TYPE_SIMPLE_ACK
                || pduType == ApduConstants.TYPE_COMPLEX_ACK
                || pduType == ApduConstants.TYPE_SEGMENT_ACK
                || pduType == ApduConstants.TYPE_ERROR;
         */
    }

    private boolean hasAcceptInfo(int pduType) {
        //FIXME: also true for other types
        return pduType == ApduConstants.TYPE_CONFIRMED_REQ;
    }

    private static int getUnsignedByte(ByteBuffer b) {
        int value = b.get();
        if (value < 0) {
            value = 256 + value;
        }
        return value;
    }

    public void write(ByteBuffer buf) {
        int b0 = pduType;
        if (pduType != ApduConstants.TYPE_ERROR && pduType != ApduConstants.TYPE_REJECT) {
            if (segmented) {
                b0 |= ApduConstants.SEGMENTED_REQUEST;
            }
            if (moreFollows) {
                b0 |= ApduConstants.SEGMENTS_MORE_FOLLOW;
            }
            if (acceptSegmented) {
                b0 |= ApduConstants.SEGMENTED_RESPONSE_ACCEPTED;
            }
        }
        buf.put((byte) b0);
        if (hasAcceptInfo(pduType)) {
            buf.put((byte) (maxSegmentsAccepted.getMask() | maxApduLength.getMask()));
        }
        if (hasInvokeId(pduType)) {
            buf.put((byte) invokeId);
        }
        if (segmented) {
            buf.put((byte) sequenceNumber);
            buf.put((byte) proposedWindowSize);
        }
        buf.put((byte) serviceChoice);
    }

    public ProtocolControlInformation withSegmentationInfo(boolean isSegmented, boolean moreFollows, int sequenceNumber, int proposedWindowSize) {
        try {
            ProtocolControlInformation clone = (ProtocolControlInformation) this.clone();
            clone.segmented = isSegmented;
            clone.moreFollows = moreFollows;
            clone.sequenceNumber = sequenceNumber;
            clone.proposedWindowSize = proposedWindowSize;
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public ProtocolControlInformation withAcceptanceInfo(boolean acceptSegmented, ApduConstants.MAX_SEGMENTS maxSegments, ApduConstants.RESPONSE_SIZE maxApduLength) {
        try {
            ProtocolControlInformation clone = (ProtocolControlInformation) this.clone();
            clone.acceptSegmented = acceptSegmented;
            clone.maxSegmentsAccepted = maxSegments;
            clone.maxApduLength = maxApduLength;
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public ProtocolControlInformation withInvokeId(int invokeId) {
        try {
            ProtocolControlInformation clone = (ProtocolControlInformation) this.clone();
            clone.invokeId = invokeId;
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int getPduType() {
        return pduType;
    }

    public int getInvokeId() {
        return invokeId;
    }

    public ApduConstants.RESPONSE_SIZE getMaxApduLength() {
        return maxApduLength;
    }

    public ApduConstants.MAX_SEGMENTS getMaxSegmentsAccepted() {
        return maxSegmentsAccepted;
    }

    public int getProposedWindowSize() {
        return proposedWindowSize;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public boolean isAcceptSegmented() {
        return acceptSegmented;
    }

    public boolean isSegmented() {
        return segmented;
    }

    public boolean isMoreFollows() {
        return moreFollows;
    }

    public int getServiceChoice() {
        return serviceChoice;
    }

}
