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
package de.fhg.iee.bacnet;

import de.fhg.iee.bacnet.api.IndicationListener;
import de.fhg.iee.bacnet.enumerations.BACnetSegmentation;
import de.fhg.iee.bacnet.enumerations.BACnetUnconfirmedServiceChoice;
import de.fhg.iee.bacnet.services.IAmListener;
import de.fhg.iee.bacnet.tags.ObjectIdentifierTag;
import de.fhg.iee.bacnet.tags.UnsignedIntTag;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jan.lapp@iee.fraunhofer.de
 */
public class TransportIPTest {
    
    static final Logger LOGGER = LoggerFactory.getLogger(TransportIPTest.class);
    
    TransportIP tp1;
    
    @Before
    public void setup() throws Exception {
        // loopback has no broadcast
        Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
        NetworkInterface notLoopback = null;
        while (ifs.hasMoreElements()) {
            NetworkInterface i = ifs.nextElement();
            if (!i.isLoopback()) {
                notLoopback = i;
                break;
            }
        }
        if (notLoopback == null) {
            throw new RuntimeException("could not find a usable network interface");
        }
        LOGGER.debug("using network {}", notLoopback);
        tp1 = new TransportIP(notLoopback, 0);
        tp1.start();
    }
    
    public TransportIPTest() {
    }

    @Test
    public void talkingToMyself() throws Exception {
        final int vendorId = 4711;
        IAmListener iam1 = new IAmListener(1, 1400, BACnetSegmentation.no_segmentation, vendorId);
        CountDownLatch iamReceived = new CountDownLatch(1);
        IndicationListener iamListener = (in) -> {
            if (in.getProtocolControlInfo().getServiceChoice() == BACnetUnconfirmedServiceChoice.i_Am.getBACnetEnumValue()) {
                LOGGER.debug("IAM received from {}", in.getSource());
                ByteBuffer data = in.getData();
                ObjectIdentifierTag oid = new ObjectIdentifierTag(data);
                UnsignedIntTag maxApduSizeAccepted = new UnsignedIntTag(data);
                UnsignedIntTag segmentationSupport = new UnsignedIntTag(data);
                UnsignedIntTag vendorIdTag = new UnsignedIntTag(data);
                LOGGER.debug("IAM from instance number {}, vendor ID {}", 
                        oid.getInstanceNumber(), vendorIdTag.getValue());
                if (vendorId == vendorIdTag.getValue().intValue()) {
                    iamReceived.countDown();
                }
            }
            return null;
        };
        tp1.addListener(iamListener);
        iam1.broadcastIAm(tp1);
        assertTrue(iamReceived.await(3, TimeUnit.SECONDS));
    }
    
}
