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
import de.fhg.iee.bacnet.enumerations.BACnetSegmentation;
import de.fhg.iee.bacnet.enumerations.BACnetUnconfirmedServiceChoice;
import de.fhg.iee.bacnet.tags.ObjectIdentifierTag;
import de.fhg.iee.bacnet.tags.Tag;
import de.fhg.iee.bacnet.tags.TagConstants;
import de.fhg.iee.bacnet.tags.UnsignedIntTag;
import java.nio.ByteBuffer;

/**
 * Utility class containing static methods for creating some BACnet unconfirmed
 * service APDUs.
 *
 * @author jlapp
 */
public abstract class UnconfirmedServices {

    private UnconfirmedServices() {
    }

    ;
    
    public static ByteBuffer createIAmApdu(ObjectIdentifierTag oid, int maxApduSizeAccepted, BACnetSegmentation segmentationSupport, int vendorId) {
        ByteBuffer iAmPdu = ByteBuffer.allocate(50);
        ProtocolControlInformation pciIAm = new ProtocolControlInformation(ApduConstants.APDU_TYPES.UNCONFIRMED_REQ, BACnetUnconfirmedServiceChoice.i_Am);
        pciIAm.write(iAmPdu);
        new ObjectIdentifierTag(oid.getObjectType(), oid.getInstanceNumber()).write(iAmPdu);
        new UnsignedIntTag(maxApduSizeAccepted).write(iAmPdu);
        new UnsignedIntTag(TagConstants.TAG_ENUMERATED, Tag.TagClass.Application, segmentationSupport.getBACnetEnumValue()).write(iAmPdu);
        new UnsignedIntTag(vendorId).write(iAmPdu);
        iAmPdu.flip();
        return iAmPdu;
    }

    public static ByteBuffer createWhoisApdu() {
        ByteBuffer buf = ByteBuffer.allocate(20);
        ProtocolControlInformation pciWhoIs = new ProtocolControlInformation(
                ApduConstants.TYPE_UNCONFIRMED_REQ, BACnetUnconfirmedServiceChoice.who_Is);
        pciWhoIs.write(buf);
        buf.flip();
        return buf;
    }

    public static ByteBuffer createWhoisApdu(long instanceRangeLower, long instanceRangeUpper) {
        ByteBuffer buf = ByteBuffer.allocate(40);
        ProtocolControlInformation pciWhoIs = new ProtocolControlInformation(
                ApduConstants.TYPE_UNCONFIRMED_REQ, BACnetUnconfirmedServiceChoice.who_Is);
        pciWhoIs.write(buf);
        new UnsignedIntTag(0, Tag.TagClass.Context, instanceRangeLower).write(buf); // instance range lower limit
        new UnsignedIntTag(1, Tag.TagClass.Context, instanceRangeUpper).write(buf); // instance range upper limit
        buf.flip();
        return buf;
    }

}
