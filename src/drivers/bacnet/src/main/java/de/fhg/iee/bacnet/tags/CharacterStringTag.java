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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author jlapp
 */
public class CharacterStringTag extends Tag {
    
    public static enum Encoding {
        
        X00_UTF8((byte) 0, "UTF-8", StandardCharsets.UTF_8),
        X01_DBCS((byte) 1, "IBM850", null),
        X02_JIS_C_6226((byte) 2, "x-JIS0208", null),
        X03_UCS4((byte) 3, "UTF-32", null),
        X04_UCS2((byte) 4, "UTF-16BE", StandardCharsets.UTF_16BE),
        X05_ISO8859_1((byte) 5, "ISO-8859-1", StandardCharsets.ISO_8859_1);
        
        private final byte code;
        private final String encodingName;
        private Charset cs;
        
        private Encoding(byte code, String enc, Charset cs) {
            this.code = code;
            this.encodingName = enc;
            this.cs = cs;
        }
        
        public byte getCode() {
            return code;
        }
        
        public Charset getCharset() {
            if (cs == null) {
                return cs = Charset.forName(encodingName);
            } else {
                return cs;
            }
        }
        
        public static Encoding forCode(byte code) {
            for (Encoding e: values()) {
                if (code == e.getCode()) {
                    return e;
                }
            }
            throw new IllegalArgumentException("unknown character string encoding: " + code);
        }
        
    }
    
    final String value;
    byte[] content;
    Encoding enc;
    
    /**
     * Create a new string tag using UTF-8 encoding.
     * @param value
     */
    public CharacterStringTag(String value) {
        this(value, Encoding.X00_UTF8);
    }
    
    public CharacterStringTag(String value, Encoding enc) {
        this(TagConstants.TAG_CHARACTER_STRING, TagClass.Application, value, enc);
    }

    public CharacterStringTag(int tag, Tag.TagClass tagClass, String value, Encoding enc) {
        super(tag, tagClass, 0);
        byte[] chars = value.getBytes(enc.getCharset());
        content = new byte[chars.length+1];
        content[0] = enc.getCode();
        System.arraycopy(chars, 0, content, 1, chars.length);
        lengthValueType = content.length;
        this.value = value;
        this.enc = enc;
    }
    
    public CharacterStringTag(ByteBuffer buf) {
        super(buf);
        content = new byte[(int)lengthValueType];
        buf.get(content);
        enc = Encoding.forCode(content[0]);
        value = new String(content, 1, content.length-1, enc.getCharset());
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    protected void writeContentOctets(ByteBuffer bb) {
        bb.put(content);
    }
    
}
