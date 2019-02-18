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
 *
 * @author jlapp
 */
public class SignedIntTag extends Tag {
    
    final BigInteger value;
    byte[] content;

    public SignedIntTag(int tag, Tag.TagClass tagClass, BigInteger value) {
        super(tag, tagClass, 0);
        if (value.signum() == -1) {
            throw new IllegalArgumentException("value is negative: " + value);
        }
        content = value.toByteArray();
        lengthValueType = content.length;
        this.value = value;
    }
    
    public SignedIntTag(ByteBuffer buf) {
        super(buf);
        content = new byte[(int)lengthValueType];
        buf.get(content);
        value = new BigInteger(content);
    }
    
    public BigInteger getValue() {
        return value;
    }
    
    @Override
    protected void writeContentOctets(ByteBuffer bb) {
        bb.put(content);
    }
    
}
