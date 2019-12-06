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
import de.fhg.iee.bacnet.api.Indication;
import de.fhg.iee.bacnet.api.IndicationListener;
import de.fhg.iee.bacnet.api.Transport;
import de.fhg.iee.bacnet.enumerations.BACnetConfirmedServiceChoice;
import de.fhg.iee.bacnet.enumerations.BACnetErrorClass;
import de.fhg.iee.bacnet.enumerations.BACnetErrorCode;
import de.fhg.iee.bacnet.enumerations.BACnetPropertyIdentifier;
import de.fhg.iee.bacnet.enumerations.BACnetRejectReason;
import de.fhg.iee.bacnet.tags.CompositeTag;
import de.fhg.iee.bacnet.tags.EnumeratedValueTag;
import de.fhg.iee.bacnet.tags.ObjectIdentifierTag;
import de.fhg.iee.bacnet.tags.Tag;
import de.fhg.iee.bacnet.tags.UnsignedIntTag;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class MinimalDeviceService implements IndicationListener<Void> {
    
    /*
    22.1.5  Minimum Device Requirements
    A device that conforms to the BACnet protocol and contains an application layer shall:
    (a) contain exactly one Device object,
    (b) execute the ReadProperty service,
    (c) execute the Who-Has and Who-Is services (and thus initiate the I-Have and I-Am services) unless the device is an
    MS/TP slave device,
    (d) execute the WriteProperty service if the device executes the WritePropertyMultiple, AddListElement or
    RemoveListElement services,
    (e) allow the WriteProperty service to modify any properties that are modifiable by the AddListElement or
    RemoveListElement services, and
    (f) execute the WriteProperty service if the device contains any objects with properties that are required to be writable.
    TODO: i-have, i-am and array properties
     */
    final ObjectIdentifierTag oid;
    
    final Logger logger;

    public MinimalDeviceService(ObjectIdentifierTag oid, Logger logger) {
        this.oid = oid;
        this.logger = logger;
    }

    static class PropertyConversion {

        final Supplier<? extends Object> supplier;
        final Consumer<CompositeTag> consumer;

        public PropertyConversion(Supplier<? extends Object> supplier, Consumer<CompositeTag> consumer) {
            this.supplier = supplier;
            this.consumer = consumer;
        }
    }
    Map<ObjectIdentifierTag, Map<Integer, PropertyConversion>> objects = new HashMap<>();

    public void addProperty(ObjectIdentifierTag oid, BACnetPropertyIdentifier prop, Supplier<? extends Object> value) {
        addProperty(oid, prop, value, null);
    }

    public void addProperty(ObjectIdentifierTag oid, BACnetPropertyIdentifier prop, Supplier<? extends Object> value, Consumer<CompositeTag> writer) {
        objects.computeIfAbsent(oid, (de.fhg.iee.bacnet.tags.ObjectIdentifierTag __) -> {
            return new HashMap<>();
        }).put(prop.getBACnetEnumValue(), new PropertyConversion(value, writer));
    }

    @Override
    public Void event(Indication i) {
        ProtocolControlInformation pci = i.getProtocolControlInfo();
        if (pci.getPduType() == ApduConstants.TYPE_CONFIRMED_REQ) {
            if (pci.getServiceChoice() == BACnetConfirmedServiceChoice.readProperty.getBACnetEnumValue()) {
                serviceReadPropertyRequest(i);
            } else if (pci.getServiceChoice() == BACnetConfirmedServiceChoice.writeProperty.getBACnetEnumValue()) {
                serviceWritePropertyRequest(i);
            } else if (pci.getServiceChoice() == BACnetConfirmedServiceChoice.readPropertyMultiple.getBACnetEnumValue()) {
                ObjectIdentifierTag requestOid = new ObjectIdentifierTag(i.getData());
                if (objects.containsKey(requestOid)) {
                    sendReject(i, BACnetRejectReason.unrecognized_service.getBACnetEnumValue());
                }
            }
        }
        return null;
    }

    //TODO: array access for readProperty
    private void serviceReadPropertyRequest(Indication i) {
        ObjectIdentifierTag requestOid = new ObjectIdentifierTag(i.getData());
        if (objects.containsKey(requestOid)) {
            UnsignedIntTag propId = new UnsignedIntTag(i.getData());
            logger.debug("got readProperty request for property {}", propId.getValue());
            if (i.getData().remaining() > 0) {
                UnsignedIntTag index = new UnsignedIntTag(i.getData());
                logger.warn("got unsupported readProperty request with index for {}[{}]", propId.getValue(), index);
            }
            int id = propId.getValue().intValue();
            if (id == BACnetPropertyIdentifier.object_list.getBACnetEnumValue()) {
                sendObjectList(i);
            } else {
                sendProperty(i, requestOid, id);
            }
        }
    }

    private void serviceWritePropertyRequest(Indication i) {
        ObjectIdentifierTag requestOid = new ObjectIdentifierTag(i.getData());
        if (objects.containsKey(requestOid)) {
            UnsignedIntTag propId = new UnsignedIntTag(i.getData());
            logger.debug("got writeProperty request for property {}", propId.getValue());
            int id = propId.getValue().intValue();
            PropertyConversion p = objects.computeIfAbsent(requestOid, (de.fhg.iee.bacnet.tags.ObjectIdentifierTag __) -> {
                return Collections.emptyMap();
            }).get(id);
            if (p == null || p.consumer == null) {
                sendError(i, BACnetErrorClass.property, BACnetErrorCode.write_access_denied);
            } else {
                CompositeTag ct = new CompositeTag(i.getData());
                UnsignedIntTag priority = new UnsignedIntTag(i.getData());
                p.consumer.accept(ct);
                sendAck(i);
            }
        }
    }

    private void sendAck(Indication i) {
        logger.trace("Send Ack to "+i.getSource().toDestinationAddress()+" from MinimalDeviceService");
        ByteBuffer buf = ByteBuffer.allocate(30);
        ProtocolControlInformation pci = new ProtocolControlInformation(ApduConstants.APDU_TYPES.SIMPLE_ACK, i.getProtocolControlInfo().getServiceChoice()).withInvokeId(i.getProtocolControlInfo().getInvokeId());
        pci.write(buf);
        buf.flip();
        try {
            i.getTransport().request(i.getSource(), buf, Transport.Priority.Normal, false, null);
        } catch (IOException ex) {
            LoggerFactory.getLogger(MinimalDeviceService.class).error("sending of ACK message failed", ex);
        }
    }

    private void sendReject(Indication i, int reason) {
        logger.trace("Send Reject to "+i.getSource().toDestinationAddress()+" from MinimalDeviceService");
        ByteBuffer buf = ByteBuffer.allocate(30);
        ProtocolControlInformation pci = new ProtocolControlInformation(ApduConstants.APDU_TYPES.REJECT, reason).withInvokeId(i.getProtocolControlInfo().getInvokeId());
        pci.write(buf);
        new EnumeratedValueTag(reason).write(buf);
        buf.flip();
        try {
            i.getTransport().request(i.getSource(), buf, Transport.Priority.Normal, false, null);
        } catch (IOException ex) {
            LoggerFactory.getLogger(MinimalDeviceService.class).error("sending of ACK message failed", ex);
        }
    }

    private void sendError(Indication i, BACnetErrorClass eClass, BACnetErrorCode eCode) {
        logger.trace("Send Error to "+i.getSource().toDestinationAddress()+" from MinimalDeviceService");
        ByteBuffer buf = ByteBuffer.allocate(30);
        ProtocolControlInformation pci = new ProtocolControlInformation(ApduConstants.APDU_TYPES.ERROR, i.getProtocolControlInfo().getServiceChoice()).withInvokeId(i.getProtocolControlInfo().getInvokeId());
        pci.write(buf);
        new UnsignedIntTag(eClass.getBACnetEnumValue()).write(buf);
        new UnsignedIntTag(eCode.getBACnetEnumValue()).write(buf);
        buf.flip();
        try {
            i.getTransport().request(i.getSource(), buf, Transport.Priority.Normal, false, null);
        } catch (IOException ex) {
            LoggerFactory.getLogger(MinimalDeviceService.class).error("sending of error message failed", ex);
        }
    }

    private void sendProperty(Indication i, ObjectIdentifierTag oid, int propId) {
        logger.trace("Send Property to "+i.getSource().toDestinationAddress()+" from MinimalDeviceService");
        PropertyConversion p = objects.getOrDefault(oid, Collections.emptyMap()).get(propId);
        if (p == null || p.supplier == null) {
            sendError(i, BACnetErrorClass.property, BACnetErrorCode.unknown_property);
            return;
        }
        Supplier<? extends Object> propertyTag = p.supplier;
        ByteBuffer buf = ByteBuffer.allocate(1500);
        ProtocolControlInformation pci = new ProtocolControlInformation(ApduConstants.APDU_TYPES.COMPLEX_ACK, BACnetConfirmedServiceChoice.readProperty);
        pci = pci.withInvokeId(i.getProtocolControlInfo().getInvokeId());
        pci.write(buf);
        new ObjectIdentifierTag(0, Tag.TagClass.Context, oid.getObjectType(), oid.getInstanceNumber()).write(buf);
        new UnsignedIntTag(1, Tag.TagClass.Context, propId).write(buf);
        Tag.createOpeningTag(3).write(buf);
        Object value = propertyTag.get();
        if (value != null) {
            if (value instanceof Tag) {
                ((Tag) value).write(buf);
            } else if (value instanceof Collection) {
                for (Object e : ((Collection) value)) {
                    ((Tag) e).write(buf);
                }
            } else if (value.getClass().isArray()) {
                for (Tag e : ((Tag[]) value)) {
                    e.write(buf);
                }
            }
        }
        Tag.createClosingTag(3).write(buf);
        buf.flip();
        try {
            i.getTransport().request(i.getSource(), buf, Transport.Priority.Normal, false, null);
        } catch (IOException ex) {
            LoggerFactory.getLogger(MinimalDeviceService.class).error("sending of object list failed", ex);
        }
    }

    private void sendObjectList(Indication i) {
        logger.trace("Send objectList to "+i.getSource().toDestinationAddress()+" from MinimalDeviceService");
        ByteBuffer buf = ByteBuffer.allocate(1500);
        ProtocolControlInformation pci = new ProtocolControlInformation(
                ApduConstants.APDU_TYPES.COMPLEX_ACK, BACnetConfirmedServiceChoice.readProperty);
        pci = pci.withInvokeId(i.getProtocolControlInfo().getInvokeId());
        pci.write(buf);
        new ObjectIdentifierTag(0, Tag.TagClass.Context, oid.getObjectType(), oid.getInstanceNumber()).write(buf);
        new UnsignedIntTag(1, Tag.TagClass.Context, BACnetPropertyIdentifier.object_list.getBACnetEnumValue()).write(buf);
        Tag.createOpeningTag(3).write(buf);
        oid.write(buf); // always include self
        for (ObjectIdentifierTag id : objects.keySet()) {
            if (!oid.equals(id)) {
                id.write(buf);
            }
        }
        Tag.createClosingTag(3).write(buf);
        buf.flip();
        try {
            i.getTransport().request(i.getSource(), buf, Transport.Priority.Normal, false, null);
        } catch (IOException ex) {
            LoggerFactory.getLogger(MinimalDeviceService.class).error("sending of object list failed", ex);
        }
    }
    
}
