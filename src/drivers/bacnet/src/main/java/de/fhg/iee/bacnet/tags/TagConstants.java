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

/**
 *
 * @author jlapp
 */
public abstract class TagConstants {
    
    public static final int TAG_NULL = 0;
    public static final int TAG_BOOLEAN = 1;
    public static final int TAG_UNSIGNED_INTEGER = 2;
    public static final int TAG_SIGNED_INTEGER = 3;
    public static final int TAG_REAL = 4;
    public static final int TAG_DOUBLE = 5;
    public static final int TAG_OCTET_STRING = 6;
    public static final int TAG_CHARACTER_STRING = 7;
    public static final int TAG_BIT_STRING = 8;
    public static final int TAG_ENUMERATED = 9;
    public static final int TAG_DATE = 10;
    public static final int TAG_TIME = 11;
    public static final int TAG_OBJECT_IDENTIFIER = 12;
    
    public static final int CONTEXT_OPENING_TAG = 0b110;
    public static final int CONTEXT_CLOSING_TAG = 0b111;
    
    private TagConstants() {}
    
}
