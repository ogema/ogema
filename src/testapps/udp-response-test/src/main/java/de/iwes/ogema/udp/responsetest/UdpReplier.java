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
package de.iwes.ogema.udp.responsetest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.Transaction;

/**
 * Listens for {@link UdpTestData} resources and on every updated sends the
 * received long value back to the source.
 * @author jlapp
 */
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class UdpReplier implements Application{
    
    List<UdpTestData> resources = new ArrayList<>();
    ApplicationManager appman;
    DatagramChannel channel;
    
    ResourceDemandListener<UdpTestData> dl = new ResourceDemandListener<UdpTestData> () {

        @Override
        public void resourceAvailable(UdpTestData t) {
            appman.getLogger().debug("adding listener to {}", t.sequenceNumber().getPath());
            t.sequenceNumber().addValueListener(vl);
        }

        @Override
        public void resourceUnavailable(UdpTestData t) {
            if (t.exists()){
                t.sequenceNumber().removeValueListener(vl);
            }
            // FIXME: if top level has been deleted, sub resources will return null (t.sequenceNumber())
        }
        
    };
    
    ResourceValueListener<TimeResource> vl = new ResourceValueListener<TimeResource> () {

        @Override
        public void resourceChanged(TimeResource t) {
            UdpTestData d = (UdpTestData) t.getParent();
            Transaction trans = appman.getResourceAccess().createTransaction();
            trans.addResource(d.sequenceNumber());
            trans.addResource(d.sourceHost());
            trans.addResource(d.sourcePort());
            
            trans.read();
            
            InetSocketAddress addr = new InetSocketAddress(
                    trans.getString(d.sourceHost()), trans.getInteger(d.sourcePort()));
            ByteBuffer buf = ByteBuffer.allocate(8);
            buf.putLong(trans.getTime(d.sequenceNumber()));
            buf.rewind();
            
            try {
                channel.send(buf, addr);
            } catch (IOException ex) {
                appman.getLogger().error("reply failed", ex);
            }
        }
        
    };

    @Override
    public void start(ApplicationManager am) {
        appman = am;
        try {
            channel = DatagramChannel.open();
        } catch (IOException ex) {
            throw new RuntimeException("start failed", ex);
        }
        appman.getResourceAccess().addResourceDemand(UdpTestData.class, dl);
    }

    @Override
    public void stop(AppStopReason asr) {
        try {
            channel.close();
        } catch (IOException ex) {
            throw new RuntimeException("stop failed", ex);
        }
    }    
    
}
