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
import de.fhg.iee.bacnet.api.DeviceAddress;
import de.fhg.iee.bacnet.api.Indication;
import de.fhg.iee.bacnet.api.IndicationListener;
import de.fhg.iee.bacnet.api.Transport;
import de.fhg.iee.bacnet.enumerations.BACnetConfirmedServiceChoice;
import de.fhg.iee.bacnet.enumerations.BACnetUnconfirmedServiceChoice;
import de.fhg.iee.bacnet.tags.CompositeTag;
import de.fhg.iee.bacnet.tags.ObjectIdentifierTag;
import de.fhg.iee.bacnet.tags.Tag;
import de.fhg.iee.bacnet.tags.UnsignedIntTag;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class CovSubscriber implements Closeable {

    final Transport transport;
    final ObjectIdentifierTag subscribingObject;

    final AtomicInteger subscriptionID = new AtomicInteger();
    final Map<Integer, Subscription> subscriptions = new ConcurrentHashMap<>();
    final ScheduledExecutorService subscriptionRefresher = Executors.newSingleThreadScheduledExecutor();

    final Logger logger = LoggerFactory.getLogger(getClass());

    public static class CovNotification {

        final int processIdentifier;

        final ObjectIdentifierTag initiatingDevice;

        final ObjectIdentifierTag monitoredObject;

        final int timeRemaining;

        final Map<Integer, CompositeTag> values;

        protected CovNotification(int processIdentifier, ObjectIdentifierTag initiatingDevice, ObjectIdentifierTag monitoredObject, int timeRemaining, Map<Integer, CompositeTag> values) {
            this.processIdentifier = processIdentifier;
            this.initiatingDevice = initiatingDevice;
            this.monitoredObject = monitoredObject;
            this.timeRemaining = timeRemaining;
            this.values = values;
        }

        public int getProcessIdentifier() {
            return processIdentifier;
        }

        public ObjectIdentifierTag getInitiatingDevice() {
            return initiatingDevice;
        }

        public ObjectIdentifierTag getMonitoredObject() {
            return monitoredObject;
        }

        public int getTimeRemaining() {
            return timeRemaining;
        }

        public Map<Integer, CompositeTag> getValues() {
            return values;
        }

    }

    public static interface CovListener {

        void receivedNotification(CovNotification n);

    }

    public class Subscription {

        public final int id;
        public final DeviceAddress destination;
        public final ObjectIdentifierTag object;
        public final boolean confirmed;
        public final int lifetime;
        public final CovListener listener;
        private ScheduledFuture<?> refreshHandle;

        public Subscription(int id, DeviceAddress destination, ObjectIdentifierTag object, boolean confirmed, int lifetime, CovListener listener) {
            this.id = id;
            this.destination = destination;
            this.object = object;
            this.confirmed = confirmed;
            this.lifetime = lifetime;
            this.listener = listener;
        }

        public Future<Boolean> cancel() {
            logger.debug("Cancel subscription on {} / {}", object.getObjectType(), object.getInstanceNumber());
        	subscriptions.remove(id);
            if (refreshHandle != null) {
                refreshHandle.cancel(false);
            }
            try {
                //TODO: indication listener
                IndicationListener<Boolean> simpleAckListener = new IndicationListener<Boolean>() {
                    @Override
                    public Boolean event(Indication ind) {
                        ProtocolControlInformation pci = ind.getProtocolControlInfo();
                        return pci.getPduType() == ApduConstants.TYPE_SIMPLE_ACK;
                    }
                };
                return transport.request(destination, createCancellationMessage(this), Transport.Priority.Normal, true, simpleAckListener);
            } catch (IOException ex) {
                logger.error("sending of cancellation message failed", ex);
            }
            //FIXME:
            return null;
        }

    }

    public CovSubscriber(Transport transport, ObjectIdentifierTag subscribingObject) {
        this.transport = transport;
        this.subscribingObject = subscribingObject;
        transport.addListener(covNotificationListener);
    }

    private ByteBuffer createCancellationMessage(Subscription sub) {
        ByteBuffer bb = ByteBuffer.allocate(50);
        ProtocolControlInformation pci = new ProtocolControlInformation(
                ApduConstants.APDU_TYPES.CONFIRMED_REQUEST, BACnetConfirmedServiceChoice.subscribeCOV)
                .withAcceptanceInfo(false, ApduConstants.MAX_SEGMENTS.UNSPECIFIED, ApduConstants.RESPONSE_SIZE.UPTO_1476);
        pci.write(bb);
        UnsignedIntTag subscriberProcessIdentifier = new UnsignedIntTag(0, Tag.TagClass.Context, sub.id);
        subscriberProcessIdentifier.write(bb);
        ObjectIdentifierTag monitoredObjectId = new ObjectIdentifierTag(
                1, Tag.TagClass.Context, sub.object.getObjectType(), sub.object.getInstanceNumber());
        monitoredObjectId.write(bb);
        bb.flip();
        return bb;
    }

    private ByteBuffer createSubscriptionMessage(int id, ObjectIdentifierTag object, boolean confirmed, int lifetime) {

        ByteBuffer bb = ByteBuffer.allocate(50);
        ProtocolControlInformation pci = new ProtocolControlInformation(
                ApduConstants.APDU_TYPES.CONFIRMED_REQUEST, BACnetConfirmedServiceChoice.subscribeCOV)
                .withAcceptanceInfo(false, ApduConstants.MAX_SEGMENTS.UNSPECIFIED, ApduConstants.RESPONSE_SIZE.UPTO_1476);
        pci.write(bb);
        UnsignedIntTag subscriberProcessIdentifier = new UnsignedIntTag(0, Tag.TagClass.Context, id);
        subscriberProcessIdentifier.write(bb);
        ObjectIdentifierTag monitoredObjectId = new ObjectIdentifierTag(
                1, Tag.TagClass.Context, object.getObjectType(), object.getInstanceNumber());
        monitoredObjectId.write(bb);
        new UnsignedIntTag(2, Tag.TagClass.Context, confirmed ? 1 : 0).write(bb);
        new UnsignedIntTag(3, Tag.TagClass.Context, lifetime).write(bb);

        bb.flip();
        return bb;
    }

    private void scheduleRefresh(final Subscription sub) {
        if (sub.lifetime > 0) {
            Runnable refresh = new Runnable() {
                @Override
                public void run() {
                    ByteBuffer bb = createSubscriptionMessage(sub.id, sub.object, sub.confirmed, sub.lifetime);
                    try {
                        IndicationListener<Void> refreshConfirmedListener = new IndicationListener<Void>() {
                            @Override
                            public Void event(Indication ind) {
                                //TODO: check for error result
                                if (ind.getProtocolControlInfo().getPduType() == ApduConstants.TYPE_SIMPLE_ACK) {
                                    logger.trace("subscription confirmed for {}@{}", sub.object, sub.destination);
                                }
                                return null;
                            }
                        };
                        logger.debug("request refresh for subscription on {}@{}", sub.object, sub.destination);
                        transport.request(sub.destination, bb, Transport.Priority.Normal, true, refreshConfirmedListener);
                    } catch (IOException ex) {
                        logger.error("could not refresh subscription for {}@{}: {}", sub.object, sub.destination, ex);
                    }
                }
            };
            sub.refreshHandle = subscriptionRefresher.scheduleAtFixedRate(refresh, sub.lifetime, sub.lifetime, TimeUnit.SECONDS);
        }
    }

    public Future<Subscription> subscribe(DeviceAddress device, ObjectIdentifierTag object, boolean confirmed, int lifetime, CovListener l) throws IOException {
        logger.trace("Send Subscribe to "+device+" for "+object.getObjectType()+" / "+object.getInstanceNumber());
        Objects.requireNonNull(object);
        Objects.requireNonNull(l);
        int id = subscriptionID.incrementAndGet();
        ByteBuffer bb = createSubscriptionMessage(id, object, confirmed, lifetime);
        final Subscription sub = new Subscription(id, device, object, confirmed, lifetime, l);
        final IndicationListener<Subscription> subAckListener = new IndicationListener<Subscription>() {
            @Override
            public Subscription event(Indication i) {
                //TODO: handle failure (COV_SUBSCRIPTION_FAILED)
                // BACnetErrorClass.services + BACnetErrorCode.cov_subscription_failed
                if (i.getProtocolControlInfo().getPduType() == ApduConstants.TYPE_SIMPLE_ACK) {
                    logger.trace("subscription confirmed for {}@{}", object, device);
                    subscriptions.put(sub.id, sub);
                    scheduleRefresh(sub);
                }
                return sub;
            }
        };

        return transport.request(device, bb, Transport.Priority.Normal, true, subAckListener);
        //return id;
    }

    IndicationListener<Void> covNotificationListener = new IndicationListener<Void>() {
        @Override
        public Void event(Indication i) {
            ProtocolControlInformation pci = i.getProtocolControlInfo();
            boolean isConfirmed = pci.getPduType() == ApduConstants.TYPE_CONFIRMED_REQ
                    && pci.getServiceChoice() == BACnetConfirmedServiceChoice.confirmedCOVNotification.getBACnetEnumValue();
            if (isConfirmed || (pci.getPduType() == ApduConstants.TYPE_UNCONFIRMED_REQ
                    && pci.getServiceChoice() == BACnetUnconfirmedServiceChoice.unconfirmedCOVNotification.getBACnetEnumValue())) {
                int id = new UnsignedIntTag(i.getData()).getValue().intValue();

                Subscription sub = subscriptions.get(id);
                if (sub == null) {
                    //TODO: subscription cancellation ?
                    logger.debug("received COVNotification for unknown ID {}", id);
                    return null;
                }

                ObjectIdentifierTag device = new ObjectIdentifierTag(i.getData());
                ObjectIdentifierTag object = new ObjectIdentifierTag(i.getData());
                UnsignedIntTag timeRemaining = new UnsignedIntTag(i.getData());
                CompositeTag values = new CompositeTag(i.getData());
                Map<Integer, CompositeTag> valueMap = new HashMap<>();
                CompositeTag[] valuesArray = new CompositeTag[0];
                valuesArray = values.getSubTags().toArray(valuesArray);
                if (valuesArray.length % 2 != 0) {
                    logger.error("received flaky values list from {}", i.getSource());
                    return null;
                }
                for (int j = 0; j < valuesArray.length / 2; j++) {
                    CompositeTag propId = valuesArray[2 * j];
                    CompositeTag val = valuesArray[2 * j + 1];
                    if (!val.isConstructed() || val.getSubTags().isEmpty()) {
                        logger.error("cannot determine value for property #{} from {}", j, i.getSource());
                        continue;
                    }
                    valueMap.put(propId.getUnsignedInt().intValue(), val.getSubTags().iterator().next());
                }
                if (isConfirmed) {
                    sendAck(i);
                }
                CovNotification n = new CovNotification(id, device, object,
                        timeRemaining.getValue().intValue(), Collections.unmodifiableMap(valueMap));
                //already on an event thread, call listener directly
                sub.listener.receivedNotification(n);
            }
            return null;
        }
    };

    private void sendAck(Indication i) {
        ProtocolControlInformation ackPci
                = new ProtocolControlInformation(ApduConstants.APDU_TYPES.SIMPLE_ACK, BACnetConfirmedServiceChoice.confirmedCOVNotification)
                        .withInvokeId(i.getProtocolControlInfo().getInvokeId());
        logger.trace("Send Ack to "+i.getSource().toDestinationAddress()+" from CovSubscriber");
        ByteBuffer bb = ByteBuffer.allocate(10);
        ackPci.write(bb);
        bb.flip();
        try {
            i.getTransport().request(i.getSource().toDestinationAddress(), bb, Transport.Priority.Normal, false, null);
        } catch (IOException ex) {
            logger.error("failed to send confirmedCOV acknowledgement", ex);
        }
    }

    /**
     * Cancel all active subscriptions and shutdown threads.
     */
    @Override
    public void close() throws IOException {
        transport.removeListener(covNotificationListener);
        subscriptionRefresher.shutdown();
        for (Subscription sub : subscriptions.values()) {
            try {
                sub.cancel().get();
            } catch (InterruptedException | ExecutionException ex) {
                logger.warn("could not cancel subscription {} to {}: {}", sub.id, sub.destination, ex);
            }
        }
        subscriptions.clear();
    }

}
