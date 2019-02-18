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

import de.fhg.iee.bacnet.enumerations.BACnetObjectType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author jlapp
 */
public class ObjectIdentifierTag extends Tag {
    
    int objectType;
    int instanceNumber;
    
    public static final int MAX_OBJECT_TYPE = (1 << 10) - 1; //a 10-bit value
    public static final int MAX_INSTANCE_NUMBER = (1 << 22) - 1; //a 22-bit value
    
    /** 
     * Create a new application tag with the given objectType and instance number.
     * @param objectType 
     * @param instanceNumber 
     */
    public ObjectIdentifierTag(int objectType, int instanceNumber) {
        this(TagConstants.TAG_OBJECT_IDENTIFIER, TagClass.Application, objectType, instanceNumber);
    }
    
    /** 
     * Create a new application tag with the given objectType and instance number.
     * @param objectType 
     * @param instanceNumber 
     */
    public ObjectIdentifierTag(BACnetObjectType objectType, int instanceNumber) {
        this(TagConstants.TAG_OBJECT_IDENTIFIER, TagClass.Application, objectType.getBACnetEnumValue(), instanceNumber);
    }

    public ObjectIdentifierTag(int tagNumber, TagClass tagClass, int objectType, int instanceNumber) {
        super(tagNumber, tagClass, 4);
        if (objectType < 0 || objectType > MAX_OBJECT_TYPE) {
            throw new IllegalArgumentException("objectType out of range (10 bit): " + objectType);
        }
        if (instanceNumber < 0 || instanceNumber > MAX_INSTANCE_NUMBER) {
            throw new IllegalArgumentException("instanceNumber out of range (22 bit): " + instanceNumber);
        }
        this.objectType = objectType;
        this.instanceNumber = instanceNumber;
    }
    
    public ObjectIdentifierTag(ByteBuffer buffer) {
        super(buffer);
        int bits = buffer.order(ByteOrder.BIG_ENDIAN).getInt();
        objectType = bits >> 22;
        instanceNumber = bits & MAX_INSTANCE_NUMBER;
    }

    @Override
    protected void writeContentOctets(ByteBuffer bb) {
        bb.order(ByteOrder.BIG_ENDIAN).putInt((objectType << 22) | instanceNumber);
    }
    
    public int getInstanceNumber() {
        return instanceNumber;
    }

    public int getObjectType() {
        return objectType;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.objectType;
        hash = 67 * hash + this.instanceNumber;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ObjectIdentifierTag other = (ObjectIdentifierTag) obj;
        if (this.objectType != other.objectType) {
            return false;
        }
        if (this.instanceNumber != other.instanceNumber) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String typeString;
        try {
            typeString = BACnetObjectType.forEnumValue(getObjectType()).toString();
        } catch (IllegalArgumentException iae) {
            typeString = "type " + getObjectType();
        }
        return typeString + ", " + getInstanceNumber();
    }

}
