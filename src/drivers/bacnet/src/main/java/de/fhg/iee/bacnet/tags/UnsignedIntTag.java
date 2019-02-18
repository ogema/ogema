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

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * A BACnet Unsigned Integer tag.
 * @author jlapp
 */
public class UnsignedIntTag extends Tag {
    
    final BigInteger value;
    byte[] content;
    
    public UnsignedIntTag(long value) {
        this(TagConstants.TAG_UNSIGNED_INTEGER, TagClass.Application, BigInteger.valueOf(value));
    }
    
    public UnsignedIntTag(BigInteger value) {
        this(TagConstants.TAG_UNSIGNED_INTEGER, TagClass.Application, value);
    }
    
    public UnsignedIntTag(int tagNumber, Tag.TagClass tagClass, long value) {
        this(tagNumber, tagClass, BigInteger.valueOf(value));
    }

    public UnsignedIntTag(int tagNumber, Tag.TagClass tagClass, BigInteger value) {
        super(tagNumber, tagClass, 0);
        if (value.signum() == -1) {
            throw new IllegalArgumentException("value is negative: " + value);
        }
        byte[] b = value.toByteArray();
        if (b.length > 1 && b[0] == 0) {
            content = new byte[b.length-1];
            System.arraycopy(b, 1, content, 0, b.length-1);
        } else {
            content = b;
        }
        lengthValueType = content.length;
        this.value = value;
    }
    
    public UnsignedIntTag(ByteBuffer buf) {
        super(buf);
        content = new byte[(int)lengthValueType];
        buf.get(content);
        value = new BigInteger(1, content);
    }
    
    public BigInteger getValue() {
        return value;
    }

    @Override
    protected void writeContentOctets(ByteBuffer bb) {
        bb.put(content);
    }
    
}
