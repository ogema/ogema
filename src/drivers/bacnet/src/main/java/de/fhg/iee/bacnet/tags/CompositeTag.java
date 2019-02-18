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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author jlapp
 */
public class CompositeTag {

    Tag basicTagData;
    ByteBuffer content;
    Collection<CompositeTag> subTags = Collections.EMPTY_LIST;

    public CompositeTag(ByteBuffer bb) {
        int sourcePosStart = bb.position();
        basicTagData = new Tag(bb);
        if (basicTagData.isStructureTag()) {
            if (basicTagData.isClosingTag()) {
                return;
            }
            int structureTagNum = basicTagData.getTagNumber();
            CompositeTag c;
            subTags = new ArrayList<>();
            //System.out.println("reading structure tag " + structureTagNum);
            do {
                c = new CompositeTag(bb);
                if (c.isClosingTag() && c.getTagNumber() == structureTagNum) {
                    break;
                }
                subTags.add(c);
            } while (true);
            //System.out.printf("read %d sub tags%n", subTags.size());
            int pos = bb.position();
            bb.position(sourcePosStart);
            content = slice(bb, pos - sourcePosStart);
            bb.position(pos);
        } else {
            if (basicTagData.getContentLength() > 0) {
                content = slice(bb, (int) basicTagData.getContentLength());
                bb.position(bb.position() + content.capacity());
            }
            //else: content is encoded in lower 4 bits of start octet.
        }
    }

    private ByteBuffer slice(ByteBuffer bb, int length) {
        int l = bb.limit();
        bb.limit(bb.position() + length);
        ByteBuffer n = bb.slice();
        bb.limit(l);
        return n;
    }

    public int getTagNumber() {
        return basicTagData.getTagNumber();
    }

    public boolean isConstructed() {
        return basicTagData.isStructureTag();
    }

    private boolean isOpeningTag() {
        return basicTagData.isStructureTag()
                && ((basicTagData.lengthValueType & TagConstants.CONTEXT_OPENING_TAG) == TagConstants.CONTEXT_OPENING_TAG);
    }

    private boolean isClosingTag() {
        return basicTagData.isStructureTag()
                && ((basicTagData.lengthValueType & TagConstants.CONTEXT_CLOSING_TAG) == TagConstants.CONTEXT_CLOSING_TAG);
    }

    public Collection<CompositeTag> getSubTags() {
        return Collections.unmodifiableCollection(subTags);
    }

    @Override
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true);
        print(ps);
        return baos.toString();
    }
    
    public void print(PrintStream out) {
        if (isConstructed()) {
            out.print("{ ");
            out.println(getTagNumber());
            for (CompositeTag t : subTags) {
                t.print(out);
            }
            out.print(getTagNumber());
            out.println(" }");
        } else {
            if (basicTagData.getTagClass() == Tag.TagClass.Application) {
                switch (getTagNumber()) {
                    case TagConstants.TAG_CHARACTER_STRING: {
                        out.println("CharacterString: " + getCharacterString());
                        break;
                    }
                    case TagConstants.TAG_ENUMERATED: {
                        out.println("Enumerated: " + getUnsignedInt());
                        break;
                    }
                    case TagConstants.TAG_OBJECT_IDENTIFIER: {
                        out.print("ObjectIdentifier: ");
                        String type = Integer.toString(getOidType());
                        try {
                            type = BACnetObjectType.forEnumValue(getOidType()).name();
                        } catch (IllegalArgumentException iae) {
                            //nevermind
                        }
                        out.print(type);
                        out.print(", ");
                        out.println(getOidInstanceNumber());
                        break;
                    }
                    default: {
                        out.println("application tag [" + getTagNumber() + "] (size " + basicTagData.getContentLength() + ")");
                    }
                }
            } else {
                out.println("context tag [" + getTagNumber() + "] (size " + content != null ? content.limit() : 0 + ")");
            }
        }
    }

    public String getCharacterString() {
        CharacterStringTag.Encoding enc = CharacterStringTag.Encoding.forCode(content.get());
        byte[] bytes = new byte[(int) basicTagData.getContentLength() - 1];
        content.get(bytes);
        String s = new String(bytes, enc.getCharset());
        content.rewind();
        return s;
    }

    /**
     * @return the object type on an ObjectIdentifier tag.
     */
    public int getOidType() {
        if (content == null || content.limit() < 4) {
            throw new UnsupportedOperationException("tag content does not match OID type");
        }
        int bits = content.order(ByteOrder.BIG_ENDIAN).getInt();
        content.rewind();
        return bits >> 22;
    }

    /**
     * @return the instance number on an ObjectIdentifier tag.
     */
    public int getOidInstanceNumber() {
        if (content == null || content.limit() < 4) {
            throw new UnsupportedOperationException("tag content does not match OID type");
        }
        int bits = content.order(ByteOrder.BIG_ENDIAN).getInt();
        content.rewind();
        return bits & ObjectIdentifierTag.MAX_INSTANCE_NUMBER;
    }
    
    public BigInteger getUnsignedInt() {
        byte[] bytes = new byte[(int) basicTagData.getContentLength()];
        content.get(bytes);
        content.rewind();
        return new BigInteger(1, bytes);
    }

}
