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
import java.math.BigInteger;

/**
 *
 * @author jlapp
 */
public class EnumeratedValueTag extends UnsignedIntTag {
    
    public EnumeratedValueTag(int tagNumber, TagClass tagClass, BACnetEnumeration value) {
        super(tagNumber, tagClass, value.getBACnetEnumValue());
    }
    
    public EnumeratedValueTag(int tagNumber, TagClass tagClass, long value) {
        super(tagNumber, tagClass, value);
    }
    
    public EnumeratedValueTag(BACnetEnumeration value) {
        super(TagConstants.TAG_ENUMERATED, TagClass.Application, value.getBACnetEnumValue());
    }
    
    public EnumeratedValueTag(BigInteger value) {
        super(TagConstants.TAG_ENUMERATED, TagClass.Application, value);
    }
    
    public EnumeratedValueTag(long value) {
        super(TagConstants.TAG_ENUMERATED, TagClass.Application, value);
    }
    
}
