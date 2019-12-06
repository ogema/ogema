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

/**
 *
 * @author jlapp
 */
public class FloatTag extends Tag {
    
    final float value;
    byte[] content;

    public FloatTag(int tag, Tag.TagClass tagClass, Float value) {
        super(tag, tagClass, 0);
        content = new byte[4];
        ByteBuffer.wrap(content).putFloat(value);
        lengthValueType = content.length;
        this.value = value;
    }
    
    public FloatTag(ByteBuffer buf) {
        super(buf);
        content = new byte[(int)lengthValueType];
        buf.get(content);
        buf.rewind();
        value = buf.getFloat();
    }
    
    public float getValue() {
        return value;
    }
    
    @Override
    protected void writeContentOctets(ByteBuffer bb) {
        bb.put(content);
    }
    
}
