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
package org.ogema.drivers.bacnet;

import de.fhg.iee.bacnet.TransportIP;
import de.fhg.iee.bacnet.apdu.ApduConstants;
import de.fhg.iee.bacnet.apdu.ProtocolControlInformation;
import de.fhg.iee.bacnet.api.DeviceAddress;
import de.fhg.iee.bacnet.api.Indication;
import de.fhg.iee.bacnet.api.Transport;
import de.fhg.iee.bacnet.enumerations.BACnetDeviceStatus;
import de.fhg.iee.bacnet.enumerations.BACnetObjectType;
import de.fhg.iee.bacnet.enumerations.BACnetPropertyIdentifier;
import de.fhg.iee.bacnet.enumerations.BACnetSegmentation;
import de.fhg.iee.bacnet.enumerations.BACnetServicesSupported;
import de.fhg.iee.bacnet.enumerations.BACnetUnconfirmedServiceChoice;
import de.fhg.iee.bacnet.services.ConfirmedServices;
import de.fhg.iee.bacnet.services.CovSubscriber;
import de.fhg.iee.bacnet.services.IAmListener;
import de.fhg.iee.bacnet.services.MinimalDeviceService;
import de.fhg.iee.bacnet.services.UnconfirmedServices;
import de.fhg.iee.bacnet.tags.BitStringTag;
import de.fhg.iee.bacnet.tags.BooleanTag;
import de.fhg.iee.bacnet.tags.CharacterStringTag;
import de.fhg.iee.bacnet.tags.CompositeTag;
import de.fhg.iee.bacnet.tags.EnumeratedValueTag;
import de.fhg.iee.bacnet.tags.ObjectIdentifierTag;
import de.fhg.iee.bacnet.tags.Tag;
import de.fhg.iee.bacnet.tags.UnsignedIntTag;
import org.ogema.drivers.bacnet.models.BACnetDevice;
import org.ogema.drivers.bacnet.models.BACnetExportConfig;
import org.ogema.drivers.bacnet.models.BACnetTransportConfig;
import java.io.IOException;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.sensors.GenericBinarySensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class BACnetDriver implements AutoCloseable {

    final ApplicationManager appman;
    final Logger logger = LoggerFactory.getLogger(getClass());
    final BACnetTransportConfig config;

    Transport transport;
    volatile BitStringTag supportedObjectType = new BitStringTag(BACnetObjectType.device);
    CovSubscriber subscriber;
    MinimalDeviceService ds;
    Map<BACnetExportConfig, BACnetOgemaExporter> exports = new HashMap<>();

    ResourceDemandListener<BACnetExportConfig> exportListener = new ResourceDemandListener<BACnetExportConfig>() {
        @Override
        public void resourceAvailable(BACnetExportConfig resource) {
            try {
                BACnetOgemaExporter ex = new BACnetOgemaExporter(BACnetDriver.this, appman, resource);
                exports.put(resource, ex);
                logger.info("added export config {}", resource.getPath());
            } catch (IllegalArgumentException iae) {
                logger.warn("invalid export config in " + resource.getPath(), iae);
            }
        }

        @Override
        public void resourceUnavailable(BACnetExportConfig resource) {
            BACnetOgemaExporter ex = exports.remove(resource);
            if (ex != null) {
                try {
                    ex.close();
                } catch (IOException ioex) {
                    logger.warn("close failed", ioex);
                }
            }
        }
    };

    public BACnetDriver(ApplicationManager appman, BACnetTransportConfig config) {
        this.appman = appman;
        this.config = config;
        appman.getResourceAccess().addResourceDemand(BACnetExportConfig.class, exportListener);
    }

    public void start() throws IOException {
        NetworkInterface iface = NetworkInterface.getByName(config.networkInterface().getValue());
        int port = config.port().getValue();
        logger.info("creating new BACnet/IP transport on device {}, port {}",
                iface.getName(), port);
        transport = new TransportIP(iface, port);

        BACnetDevice dev = config.device();
        ObjectIdentifierTag deviceId = new ObjectIdentifierTag(dev.identifier().type().getValue(),
                dev.identifier().instanceNumber().getValue());
        ds = new MinimalDeviceService(deviceId, logger);

        ds.addProperty(deviceId, BACnetPropertyIdentifier.object_name,
                stringTag(dev.name(), "nameless device"));
        ds.addProperty(deviceId, BACnetPropertyIdentifier.object_identifier,
                staticValue(deviceId));
        ds.addProperty(deviceId, BACnetPropertyIdentifier.object_type,
                staticValue(new UnsignedIntTag(deviceId.getObjectType())));

        ds.addProperty(deviceId, BACnetPropertyIdentifier.system_status,
                enumeratedValueTag(dev.systemStatus(), BACnetDeviceStatus.operational.getBACnetEnumValue()));
        ds.addProperty(deviceId, BACnetPropertyIdentifier.vendor_name,
                stringTag(dev.vendorName(), "IWES"));
        Supplier<UnsignedIntTag> vendorIdValue = unsignedIntTag(dev.vendorIdentifier(), 4711);
        ds.addProperty(deviceId, BACnetPropertyIdentifier.vendor_identifier,
                unsignedIntTag(dev.vendorIdentifier(), 4711));

        ds.addProperty(deviceId, BACnetPropertyIdentifier.model_name,
                stringTag(dev.modelName(), "BACnetOGEMA"));
        ds.addProperty(deviceId, BACnetPropertyIdentifier.firmware_revision,
                stringTag(dev.firmwareRevision(), "0.0.1-alpha"));
        ds.addProperty(deviceId, BACnetPropertyIdentifier.application_software_version,
                stringTag(dev.applicationSoftwareVersion(), "0.1"));

        ds.addProperty(deviceId, BACnetPropertyIdentifier.location,
                stringTag(dev.location().name(), "")); //optional
        ds.addProperty(deviceId, BACnetPropertyIdentifier.description,
                stringTag(dev.description(), "")); //optional

        ds.addProperty(deviceId, BACnetPropertyIdentifier.protocol_version,
                unsignedIntTag(dev.protocolVersion(), 1));
        ds.addProperty(deviceId, BACnetPropertyIdentifier.protocol_revision,
                unsignedIntTag(dev.protocolRevision(), 1));
        ds.addProperty(deviceId, BACnetPropertyIdentifier.protocol_services_supported,
                staticValue(new BitStringTag(BACnetServicesSupported.i_Am, BACnetServicesSupported.who_Is,
                        BACnetServicesSupported.readProperty, BACnetServicesSupported.writeProperty)));

        subscriber = new CovSubscriber(transport, deviceId);

        ds.addProperty(deviceId, BACnetPropertyIdentifier.protocol_object_types_supported,
                this::getSupportedObjectType);

        ds.addProperty(deviceId, BACnetPropertyIdentifier.max_apdu_length_accepted,
                unsignedIntTag(dev.maxApduLengthAccepted(), 1476));
        ds.addProperty(deviceId, BACnetPropertyIdentifier.segmentation_supported,
                staticValue(new EnumeratedValueTag(BACnetSegmentation.no_segmentation)));

        ds.addProperty(deviceId, BACnetPropertyIdentifier.apdu_timeout,
                staticValue(new UnsignedIntTag(250)));
        ds.addProperty(deviceId, BACnetPropertyIdentifier.number_of_APDU_retries,
                staticValue(new UnsignedIntTag(3)));

        ds.addProperty(deviceId, BACnetPropertyIdentifier.device_address_binding,
                Collections::emptyList);
        ds.addProperty(deviceId, BACnetPropertyIdentifier.database_revision,
                unsignedIntTag(dev.databaseRevision(), 0));

        transport.addListener(ds);
        transport.start();

        //FIXME i-am handling needs to be done by MinimalDeviceService
        IAmListener l = new IAmListener(deviceId.getInstanceNumber(), 1476,
                BACnetSegmentation.no_segmentation, vendorIdValue.get().getValue().intValue());
        transport.addListener(l);
        l.broadcastIAm(transport);
        transport.addListener(this::deviceIAmIndicationHandler);
        //send whois
        transport.request(transport.getBroadcastAddress(), UnconfirmedServices.createWhoisApdu(), Transport.Priority.Normal, false, null);
    }

    @Override
    public void close() throws Exception {
        try {
            subscriber.close();
        } finally {
            transport.close();
        }
    }

    public BitStringTag getSupportedObjectType() {
        return supportedObjectType;
    }

    Void deviceIAmIndicationHandler(Indication i) {
        ProtocolControlInformation pci = i.getProtocolControlInfo();
        if (pci.getPduType() == ApduConstants.TYPE_UNCONFIRMED_REQ
                && pci.getServiceChoice() == BACnetUnconfirmedServiceChoice.i_Am.getBACnetEnumValue()) {
            DeviceAddress device = i.getSource().toDestinationAddress();
            ObjectIdentifierTag oid = new ObjectIdentifierTag(i.getData());
            UnsignedIntTag maxApduLengthAccepted = new UnsignedIntTag(i.getData());
            UnsignedIntTag segmentationSupported = new UnsignedIntTag(i.getData());
            UnsignedIntTag vendorId = new UnsignedIntTag(i.getData());
            //TODO: ignore own device
            logger.debug("reading property list from {}", device);
            ByteBuffer data = ConfirmedServices.buildReadPropertyApdu(oid, BACnetPropertyIdentifier.object_list.getBACnetEnumValue());
            try {
                i.getTransport().request(device, data, Transport.Priority.Normal, true, this::processReadObjectListAck);
            } catch (IOException ex) {
                logger.warn("failed to read object list from {}: {}", device, ex);
            }
        }
        return null;
    }

    Void processReadObjectListAck(Indication i) {
        ByteBuffer apdu = i.getData();
        ProtocolControlInformation pci = i.getProtocolControlInfo();

        DeviceAddress address = i.getSource().toDestinationAddress();

        //object-id
        ObjectIdentifierTag oid = new ObjectIdentifierTag(apdu);
        //CompositeTag ct = new CompositeTag(apdu);

        //property-id
        CompositeTag propId = new CompositeTag(apdu);

        //property-values
        CompositeTag objectList = new CompositeTag(apdu);

        //FIXME: needs unique resource name
        String resourceName = "bacnet_" + oid.getInstanceNumber();
        config.remoteDevices().create().activate(false);
        BACnetDevice device = config.remoteDevices().addDecorator(
                resourceName, BACnetDevice.class);

        try {
            logger.debug("reading device properties for {}", device.getPath());
            //TODO: request & write standard device properties
            Future<CompositeTag> name = readProperty(address, oid, BACnetPropertyIdentifier.object_name.getBACnetEnumValue());
            String deviceName = name.get().getSubTags().iterator().next().getCharacterString();
            device.name().<StringResource>create().setValue(deviceName);
        } catch (IOException | InterruptedException | ExecutionException ex) {
            logger.warn("could not read device properties", ex);
        }
        logger.debug("setup up supported objects for {}", device.getPath());
        for (CompositeTag o : objectList.getSubTags()) {
            if (o.getOidType() == BACnetObjectType.binary_output.getBACnetEnumValue()) {
                setupBinaryOutput(device, o, i);
            } else if (o.getOidType() == BACnetObjectType.binary_input.getBACnetEnumValue()) {
                setupBinaryInput(device, o, i);
            }
        }
        device.activate(true);
        return null;
    }

    static CompositeTag returnReadPropertyAckData(Indication i) {
        ByteBuffer apdu = i.getData();
        ProtocolControlInformation pci = i.getProtocolControlInfo();
        //object-id
        CompositeTag oid = new CompositeTag(apdu);
        //property-id
        CompositeTag propId = new CompositeTag(apdu);
        //property-values
        CompositeTag values = new CompositeTag(apdu);
        return values;
    }

    Future<CompositeTag> readProperty(DeviceAddress device, ObjectIdentifierTag oid, int property) throws IOException {
        ByteBuffer data = ConfirmedServices.buildReadPropertyApdu(oid, property);
        return transport.request(device, data, Transport.Priority.Normal, true, BACnetDriver::returnReadPropertyAckData);
    }

    private void setupBinaryInput(BACnetDevice device, CompositeTag o, Indication i) {
        logger.debug("adding binary input (GenericBinarySensor) #{} on {}", o.getOidInstanceNumber(), device.getPath());
        ObjectIdentifierTag binaryInput
                = new ObjectIdentifierTag(o.getOidType(), o.getOidInstanceNumber());

        GenericBinarySensor input = device.objects().addDecorator(BACnetObjectType.binary_input.name() + "_" + o.getOidInstanceNumber(), GenericBinarySensor.class);
        input.reading().create();
        CovSubscriber.CovListener l = event -> {
            int reportedValue = event.getValues().get(BACnetPropertyIdentifier.present_value.getBACnetEnumValue()).getUnsignedInt().intValue();
            boolean state = reportedValue == 1;
            logger.debug("binary input {} state reported as {}", input.getPath(), state ? "on" : "off");
            input.reading().setValue(state);
        };
        try {
            Future<CovSubscriber.Subscription> sub = subscriber.subscribe(i.getSource().toDestinationAddress(),
                    binaryInput, false, 120, l);
        } catch (IOException ex) {
            logger.error("failed to setup binary input", ex);
            input.deactivate(true);
            return;
        }
        input.activate(true);
    }

    private void setupBinaryOutput(BACnetDevice device, CompositeTag o, Indication i) {
        logger.debug("adding binary ouput (OnOffSwitch) #{} on {}", o.getOidInstanceNumber(), device.getPath());
        OnOffSwitch sw = device.objects().addDecorator(BACnetObjectType.binary_output.name() + "_" + o.getOidInstanceNumber(), OnOffSwitch.class);
        sw.stateControl().create();
        sw.stateFeedback().create();
        ObjectIdentifierTag binaryOutput
                = new ObjectIdentifierTag(o.getOidType(), o.getOidInstanceNumber());
        DeviceAddress switchDeviceAddr = i.getSource().toDestinationAddress();
        CovSubscriber.CovListener l = event -> {
            int reportedValue = event.getValues().get(BACnetPropertyIdentifier.present_value.getBACnetEnumValue()).getUnsignedInt().intValue();
            boolean state = reportedValue == 1;
            logger.debug("binary output {} state reported as {}", sw.getPath(), state ? "on" : "off");
            sw.stateFeedback().setValue(state);
        };
        try {
            Future<CovSubscriber.Subscription> sub = subscriber.subscribe(switchDeviceAddr,
                    binaryOutput, false, 120, l);
            //XXX do sth with subscription?
        } catch (IOException ex) {
            logger.error("failed to setup binary output", ex);
            sw.deactivate(true);
            return;
        }

        ResourceValueListener<BooleanResource> valueListener = v -> {
            int state = v.getValue() ? 1 : 0;
            EnumeratedValueTag newState = new EnumeratedValueTag(state);
            ByteBuffer data = ConfirmedServices.buildWritePropertyApdu(binaryOutput, BACnetPropertyIdentifier.present_value.getBACnetEnumValue(), newState);
            try {
                //TODO: indicationlistener
                i.getTransport().request(switchDeviceAddr, data, Transport.Priority.Normal, true, null);
            } catch (IOException ex) {
                logger.error("writing to binary output {} failed: {}", sw.getPath(), ex);
            }
        };
        sw.stateControl().addValueListener(valueListener, true);
        device.objects().activate(false);
        sw.activate(true);
    }

    static Supplier<Tag> staticValue(final Tag v) {
        return () -> {
            return v;
        };
    }

    static Supplier<BooleanTag> booleanTag(BooleanResource res, boolean def) {
        return () -> {
            return new BooleanTag(res.isActive() ? res.getValue() : def);
        };
    }

    static Supplier<Tag> stringTag(StringResource res, String def) {
        return () -> {
            return new CharacterStringTag(res.isActive() ? res.getValue() : def);
        };
    }

    static Supplier<UnsignedIntTag> unsignedIntTag(IntegerResource res, int def) {
        return () -> {
            return new UnsignedIntTag(res.isActive() ? res.getValue() : def);
        };
    }

    static Supplier<Tag> enumeratedValueTag(IntegerResource res, int def) {
        return () -> {
            return new EnumeratedValueTag(res.isActive() ? res.getValue() : def);
        };
    }

}
