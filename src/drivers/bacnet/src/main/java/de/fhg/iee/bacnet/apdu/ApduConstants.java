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

/**
 * Contains bitmasks (8 bit) used in APDUs.
 * 
 * @author jlapp
 */
public abstract class ApduConstants {
    
    public static final byte TYPE_CONFIRMED_REQ = 0;
    public static final byte TYPE_UNCONFIRMED_REQ = 0b0001_0000;
    public static final byte TYPE_SIMPLE_ACK      = 0b0010_0000;
    public static final byte TYPE_COMPLEX_ACK     = 0b0011_0000;
    public static final byte TYPE_SEGMENT_ACK     = 0b0100_0000;
    public static final byte TYPE_ERROR           = 0b0101_0000;
    public static final byte TYPE_REJECT          = 0b0110_0000;
    public static final byte TYPE_ABORT           = 0b0111_0000;
    
    public enum APDU_TYPES implements BACnetEnumeration {
        CONFIRMED_REQUEST(TYPE_CONFIRMED_REQ),
        UNCONFIRMED_REQ(TYPE_UNCONFIRMED_REQ),
        SIMPLE_ACK(TYPE_SIMPLE_ACK),
        COMPLEX_ACK(TYPE_COMPLEX_ACK),
        ERROR(TYPE_ERROR),
        REJECT(TYPE_REJECT),
        ABORT(TYPE_ABORT);
        
        private final int mask;
        
        private APDU_TYPES(int mask) {
            this.mask = mask;
        }

        public int getMask() {
            return mask;
        }
        
        public static APDU_TYPES forEnumValue(int val) {
 			for (APDU_TYPES o: values()) {
                if (val == o.getBACnetEnumValue()){
                    return o;
                }
            }
            return null;
            //throw new IllegalArgumentException("unknown enum value: " + val);
        }

		@Override
		public int getBACnetEnumValue() {
			return mask;
		}

    }
    
    public enum MAX_SEGMENTS {
        
        UNSPECIFIED(0),
        TWO(0b001_0000),
        FOUR(0b010_0000),
        EIGHT(0b011_0000),
        SIXTEEN(0b100_0000),
        THIRTYTWO(0b101_0000),
        SIXTYFOUR(0b110_0000),
        GREATER_THAN_SIXTYFOUR(0b111_0000);
        
        private final int mask;
        
        private MAX_SEGMENTS(int mask) {
            this.mask = mask;
        }
        
        public int getMask() {
            return mask;
        }
        
        public static MAX_SEGMENTS fromOctet(int octet) {
            octet &= 0b0111_0000;
            for (MAX_SEGMENTS s: MAX_SEGMENTS.values()) {
                if (octet == s.mask) {
                    return s;
                }
            }
            throw new IllegalArgumentException("invalid input: " + Integer.toHexString(octet));
        }
        
    }
    
    public static final int MAX_SEGMENTS_UNSPECIFIED = 0;
    public static final int MAX_SEGMENTS_2  = 0b001_0000;
    public static final int MAX_SEGMENTS_4  = 0b010_0000;
    public static final int MAX_SEGMENTS_8  = 0b011_0000;
    public static final int MAX_SEGMENTS_16 = 0b100_0000;
    public static final int MAX_SEGMENTS_32 = 0b101_0000;
    public static final int MAX_SEGMENTS_64 = 0b110_0000;
    public static final int MAX_SEGMENTS_64PLUS  = 0b111_0000;
    
    public enum RESPONSE_SIZE {
        
        MINIMUM(0, 50, "Up to MinimumMessageSize (50 octets)"),
        UPTO_128(0b0001, 128, "Up to 128 octets"),
        UPTO_206(0b0010, 206, "Up to 206 octets (fits in a LonTalk frame)"),
        UPTO_480(0b0011, 480, "Up to 480 octets (fits in an ARCNET frame)"),
        UPTO_1024(0b0100, 1024, "Up to 1024 octets"),
        UPTO_1476(0b0101, 1476, "Up to 1476 octets (fits in an ISO 8802-3 frame)");
        
        private final int mask;
        private final int size;
        private final String description;
        
        private RESPONSE_SIZE(int mask, int size, String description) {
            this.mask = mask;
            this.size = size;
            this.description = description;
        }
        
        public int getMask() {
            return mask;
        }

        public String getDescription() {
            return description;
        }

        public int getSize() {
            return size;
        }
        
        public static RESPONSE_SIZE fromOctet(int octet) {
            octet &= 0b1111;
            for (RESPONSE_SIZE s: RESPONSE_SIZE.values()) {
                if (octet == s.mask) {
                    return s;
                }
            }
            throw new IllegalArgumentException("invalid input");
        }
        
    }
    
    public static final int MAX_APDU_LENGTH_MIN  = 0b0000;
    public static final int MAX_APDU_LENGTH_128  = 0b0001;
    public static final int MAX_APDU_LENGTH_206  = 0b0010;
    public static final int MAX_APDU_LENGTH_480  = 0b0011;
    public static final int MAX_APDU_LENGTH_1024 = 0b0100;
    public static final int MAX_APDU_LENGTH_1476 = 0b0101;
    
    public static final int SEGMENTED_REQUEST = 0b1000;
    public static final int SEGMENTS_MORE_FOLLOW = 0b0100;
    public static final int SEGMENTED_RESPONSE_ACCEPTED = 0b0010;
    
    private ApduConstants() {}
    
}
