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
package de.fhg.iee.bacnet.services;

import de.fhg.iee.bacnet.apdu.ApduConstants;
import de.fhg.iee.bacnet.apdu.ProtocolControlInformation;
import de.fhg.iee.bacnet.enumerations.BACnetConfirmedServiceChoice;
import de.fhg.iee.bacnet.tags.ObjectIdentifierTag;
import de.fhg.iee.bacnet.tags.Tag;
import de.fhg.iee.bacnet.tags.UnsignedIntTag;
import java.nio.ByteBuffer;

/**
 * Utility class containing static methods for creating some BACnet confirmed
 * service APDUs.
 *
 * @author jlapp
 */
public abstract class ConfirmedServices {

    private ConfirmedServices() {
    }

    public static ByteBuffer buildReadPropertyApdu(ObjectIdentifierTag object, int propertyId) {
        ByteBuffer buf = ByteBuffer.allocate(100);
        ProtocolControlInformation pciRequestProperty
                = new ProtocolControlInformation(ApduConstants.APDU_TYPES.CONFIRMED_REQUEST, BACnetConfirmedServiceChoice.readProperty.getBACnetEnumValue());
        pciRequestProperty = pciRequestProperty.withAcceptanceInfo(false, ApduConstants.MAX_SEGMENTS.UNSPECIFIED, ApduConstants.RESPONSE_SIZE.UPTO_1476);
        pciRequestProperty.write(buf);

        new ObjectIdentifierTag(0, Tag.TagClass.Context, object.getObjectType(), object.getInstanceNumber()).write(buf);
        new UnsignedIntTag(1, Tag.TagClass.Context, propertyId).write(buf);
        buf.flip();
        return buf;
    }

    public static ByteBuffer buildWritePropertyApdu(ObjectIdentifierTag object, int propertyId, Tag value) {
        ByteBuffer buf = ByteBuffer.allocate(100);
        ProtocolControlInformation pciRequestProperty
                = new ProtocolControlInformation(ApduConstants.APDU_TYPES.CONFIRMED_REQUEST, BACnetConfirmedServiceChoice.writeProperty.getBACnetEnumValue());
        pciRequestProperty = pciRequestProperty.withAcceptanceInfo(false, ApduConstants.MAX_SEGMENTS.UNSPECIFIED, ApduConstants.RESPONSE_SIZE.UPTO_1476);
        pciRequestProperty.write(buf);

        new ObjectIdentifierTag(0, Tag.TagClass.Context, object.getObjectType(), object.getInstanceNumber()).write(buf);
        new UnsignedIntTag(1, Tag.TagClass.Context, propertyId).write(buf);
        Tag.createOpeningTag(3).write(buf);
        value.write(buf);
        Tag.createClosingTag(3).write(buf);
        buf.flip();
        return buf;
    }
    
}
