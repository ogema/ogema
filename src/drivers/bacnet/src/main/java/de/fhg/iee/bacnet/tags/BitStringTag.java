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
package de.fhg.iee.bacnet.tags;

import de.fhg.iee.bacnet.api.BACnetEnumeration;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 *
 * @author jlapp
 */
public class BitStringTag extends Tag {

    int unusedBits;
    byte[] bits;

    public BitStringTag(ByteBuffer buffer) {
        super(buffer);
        unusedBits = buffer.get();
        bits = new byte[buffer.remaining()];
        buffer.get(bits);
        lengthValueType = 1 + bits.length;
    }

    public BitStringTag(int tagNumber, TagClass tagClass, BitSet bits) {
        super(tagNumber, tagClass, 0);
        initFromBitSet(bits);
    }
    
    public BitStringTag(int tagNumber, TagClass tagClass, BACnetEnumeration ... values) {
        super(tagNumber, tagClass, 0);
        BitSet bs = new BitSet();
        for (BACnetEnumeration e: values) {
            bs.set(e.getBACnetEnumValue());
        }
        initFromBitSet(bs);
    }
    
    public BitStringTag(BACnetEnumeration ... values) {
        super(TagConstants.TAG_BIT_STRING, TagClass.Application, 0);
        BitSet bs = new BitSet();
        for (BACnetEnumeration e: values) {
            bs.set(e.getBACnetEnumValue());
        }
        initFromBitSet(bs);
    }
    
    private void initFromBitSet(BitSet bs) {
        if (bs.isEmpty()) {
            unusedBits = 0;
            this.bits = new byte[0];
        } else {
            this.bits = new byte[bs.length() / 8 + 1];
            for (int i = 0; i <= bs.length(); i++) {
                if (bs.get(i)) {
                    bits[i/8] |= 1 << (7-(i%8));
                }
            }
            unusedBits = this.bits.length * 8 - bs.length();
        }
        lengthValueType = 1 + this.bits.length;
        //System.out.printf("bits set: %d, unused: %d%n", bs.cardinality(), unusedBits);
    }
    
    public BitSet toBitSet() {
        BitSet bs = new BitSet();
        for (int byteIndex = 0; byteIndex < bits.length; byteIndex++) {
            for (int bit = 7; bit > -1; bit--) {
                if ((bits[byteIndex] & (1<<bit)) > 0){
                    int bitNum = byteIndex * 8 + (7-bit);
                    bs.set(bitNum);
                }
            }
        }
        return bs;
    }

    @Override
    protected void writeContentOctets(ByteBuffer bb) {
        bb.put((byte) unusedBits);
        bb.put(bits);
    }
    
}
