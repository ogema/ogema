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

import de.fhg.iee.bacnet.tags.Tag;
import de.fhg.iee.bacnet.tags.TagConstants;
import de.fhg.iee.bacnet.tags.BitStringTag;
import de.fhg.iee.bacnet.api.BACnetEnumeration;
import de.fhg.iee.bacnet.enumerations.BACnetObjectType;
import java.util.Arrays;
import java.util.BitSet;
import static org.junit.Assert.*;

/**
 *
 * @author jlapp
 */
public class BitStringTagTest {
    
    /**
     * Test of toBitSet method, of class BitStringTag.
     */
    @org.junit.Test
    public void testToBitSet() {
        System.out.println("toBitSet");
        BACnetEnumeration[] values = new BACnetEnumeration[] {
            BACnetObjectType.accumulator, BACnetObjectType.device, BACnetObjectType.schedule
        };
        BitStringTag instance = new BitStringTag(values);
        
        BitSet expectedResult = new BitSet();
        Arrays.asList(values).forEach(e -> expectedResult.set(e.getBACnetEnumValue()));
        
        BitSet result = instance.toBitSet();
        assertEquals(values.length, result.cardinality());
        Arrays.asList(values).forEach( e -> assertTrue(result.get(e.getBACnetEnumValue())));
        assertEquals(expectedResult, result);
        
        BitStringTag start = new BitStringTag(BACnetObjectType.device);
        BitSet sx = start.toBitSet();
        sx.set(BACnetObjectType.binary_value.getBACnetEnumValue());
        BitStringTag t2 = new BitStringTag(TagConstants.TAG_BIT_STRING, Tag.TagClass.Application,sx);
        assertEquals(sx, t2.toBitSet());
    }

}
