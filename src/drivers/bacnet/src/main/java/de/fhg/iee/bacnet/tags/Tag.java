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

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Base class for BACnet tags, contains methods for encoding the initial octet
 * of a tag.
 * @author jlapp
 */
public class Tag {
    
    public static final long MAX_LENGTH = (long) Math.pow(2, 32)-1;
    
    int tagNumber;
    TagClass tagClass;
    protected long lengthValueType;
    protected ByteBuffer buffer;
    
    public static enum TagClass {
        Application,
        Context //0b1000
    }
    
    /**
     * Construct a tag from a ByteBuffer by reading the tag and length data,
     * after the constructor has finished, the buffer position will be on the
     * first content octet (if there is content).
     * 
     * @param buffer
     */
    public Tag(ByteBuffer buffer) {
        int initialOctet = getUnsignedByte(buffer);
        tagNumber = initialOctet >> 4;
        if (tagNumber == 15) {
            tagNumber = getUnsignedByte(buffer);
        }
        if ((initialOctet & 0b1000) > 0) {
            tagClass = TagClass.Context;
        } else {
            tagClass = TagClass.Application;
        }
        lengthValueType = initialOctet & 0b111;
        if (lengthValueType > 4 && !isStructureTag()) {
            lengthValueType = getUnsignedByte(buffer);
            if (lengthValueType > 253) {
                if (lengthValueType == 254) {
                    lengthValueType = getUnsignedByte(buffer) << 8
                            | getUnsignedByte(buffer);
                } else {
                    lengthValueType = ((long) getUnsignedByte(buffer) << 24)
                            | getUnsignedByte(buffer) << 16
                            | getUnsignedByte(buffer) << 8
                            | getUnsignedByte(buffer);
                }
            }
        }
    }
    
    public long getContentLength() {
        if (isStructureTag()) {
            return 0;
        }
        if (getTagClass() == TagClass.Application && getTagNumber() == TagConstants.TAG_BOOLEAN) {
            return 0;
        }
        return getLengthValueType();
    }
    
    public Tag(int tagNumber, TagClass tagClass, long lengthValueType) {
        if (tagNumber < 0 || tagNumber > 254) {
            throw new IllegalArgumentException("tag number out of range: " + tagNumber);
        }
        Objects.requireNonNull(tagClass, "tag class must not be null");
        this.tagNumber = tagNumber;
        this.tagClass = tagClass;
        this.lengthValueType = lengthValueType;
    }
    
    public static Tag createOpeningTag(int tagNumber) {
        return new Tag(tagNumber, TagClass.Context, TagConstants.CONTEXT_OPENING_TAG);
    }
    
    public static Tag createClosingTag(int tagNumber) {
        return new Tag(tagNumber, TagClass.Context, TagConstants.CONTEXT_CLOSING_TAG);
    }
    
    public long getLengthValueType() {
        return lengthValueType;
    }
    
    public final int getTagNumber() {
        return tagNumber;
    }
    
    public TagClass getTagClass() {
        return tagClass;
    }
    
    private static int getUnsignedByte(ByteBuffer b) {
        int value = b.get();
        if (value < 0) {
            value = 256 + value;
        }
        return value;
    }
    
    public final int write(ByteBuffer bb) {
        if (lengthValueType < 0 || lengthValueType > MAX_LENGTH) {
            throw new IllegalArgumentException("value for Length/Value/Type field out of range: " + lengthValueType);
        }
        int positionStart = bb.position();
        int initialOctet = 0;
        if (tagNumber > 14) {
            initialOctet |= 0b11110000;
        } else {
            initialOctet |= (tagNumber << 4);
        }
        if (tagClass.equals(TagClass.Context)) {
            initialOctet |= 0b1000;
        }
        //FIXME this needs a hasLength method or sth
        if (lengthValueType < 5 || isStructureTag()) {
            initialOctet |= lengthValueType;
        } else {
            initialOctet |= 0b101;
        }
        bb.put((byte) initialOctet);
        if (tagNumber > 14) {
            bb.put((byte) tagNumber);
        }
        if (lengthValueType > 4 && !isStructureTag()) {
            if (lengthValueType < 254) {
                bb.put((byte)lengthValueType);
            } else if (lengthValueType < 65636) {
                bb.put((byte) 254);
                bb.put((byte) (lengthValueType >> 8));
                bb.put((byte) (lengthValueType & 255));
            } else {
                bb.put((byte) 255);
                bb.put((byte) (lengthValueType >> 24));
                bb.put((byte) ((lengthValueType >> 16) & 255));
                bb.put((byte) ((lengthValueType >> 8) & 255));
                bb.put((byte) (lengthValueType & 255));
            }
        }
        writeContentOctets(bb);
        return bb.position() - positionStart;
    }
    
    /** @return tag is an opening or closing tag. */
    public final boolean isStructureTag() {
        return tagClass == TagClass.Context && (lengthValueType == 0b110 || lengthValueType == 0b111);
    }
    
    public boolean isOpeningTag() {
        return tagClass == TagClass.Context && (lengthValueType == 0b110);
    }
    
    public boolean isClosingTag() {
        return tagClass == TagClass.Context && (lengthValueType == 0b111);
    }
    
    protected void writeContentOctets(ByteBuffer bb) {
    }
    
}
