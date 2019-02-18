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
//FIXME: boolean context tags have content size 1 and 1/0 in content byte.
public class BooleanTag extends Tag {
    
    final boolean value;

    public BooleanTag(int tag, Tag.TagClass tagClass, boolean value) {
        super(tag,tagClass, 0);
        lengthValueType = value ? 1 : 0;
        this.value = value;
    }
    
    public BooleanTag(boolean value) {
        super(TagConstants.TAG_BOOLEAN, TagClass.Application, 0);
        lengthValueType = value ? 1 : 0;
        this.value = value;
    }
    
    public BooleanTag(ByteBuffer buf) {
        super(buf);
        this.value = (lengthValueType & 0b1) > 0;
    }
    
}
