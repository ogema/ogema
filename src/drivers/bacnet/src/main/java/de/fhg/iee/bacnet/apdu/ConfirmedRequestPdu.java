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

import java.nio.ByteBuffer;

/**
 * BACnet comfirmed request APDU, see spec §20.1.
 * @author jlapp
 */
public class ConfirmedRequestPdu {
    
    final int pduType = ApduConstants.TYPE_CONFIRMED_REQ;
    
    ApduConstants.MAX_SEGMENTS segments;    
    boolean isSegmented = false;    
    boolean moreSegments = false;    
    int sequenceNumber = -1;
    int windowSize = -1;
    
    boolean acceptSegments;
    
    ApduConstants.RESPONSE_SIZE responseSize;
    
    int invokeId;
    
    int serviceChoice;
    
    public ConfirmedRequestPdu(int serviceChoice, int invokeId, ApduConstants.RESPONSE_SIZE responseSize) {
        this.responseSize = responseSize;
        this.segments = ApduConstants.MAX_SEGMENTS.UNSPECIFIED;
        this.acceptSegments = false;
        this.invokeId = invokeId;
        this.serviceChoice = serviceChoice;
    }
    
    public void write(ByteBuffer buf){
        int b0 = pduType;
        if (isSegmented) {
            b0 |= ApduConstants.SEGMENTED_REQUEST;
        }
        if (moreSegments) {
            b0 |= ApduConstants.SEGMENTS_MORE_FOLLOW;
        }
        if (acceptSegments) {
            b0 |= ApduConstants.SEGMENTED_RESPONSE_ACCEPTED;
        }
        buf.put((byte)b0);
        buf.put((byte)(segments.getMask() | responseSize.getMask()));
        
        buf.put((byte)invokeId);
        if (isSegmented) {
            buf.put((byte)sequenceNumber);
            buf.put((byte)windowSize);
        }
        buf.put((byte)serviceChoice);
    }
    
}
