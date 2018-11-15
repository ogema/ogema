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
package org.ogema.drivers.homematic.xmlrpc.hl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.xmlrpc.XmlRpcException;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandler;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandlerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmLogicInterface;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
@Component(service = Application.class)
public class HomeMaticDriver implements Application {

    private ApplicationManager appman;
    private EventAdmin eventAdmin;
    private ComponentContext ctx;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<HmLogicInterface, HmConnection> connections = new HashMap<>();

    private final SortedSet<HandlerRegistration> handlerFactories = new TreeSet<>();
   
    // store accepted devices (by address) so they are not offered again on a different connection
    private final Map<String, Class<? extends DeviceHandler>> acceptedDevices = new HashMap<>();

    private static class HandlerRegistration implements Comparable<HandlerRegistration> {

        DeviceHandlerFactory fac;
        int ranking;

        public HandlerRegistration(DeviceHandlerFactory fac, int ranking) {
            this.fac = fac;
            this.ranking = ranking;
        }

        @Override
        public int compareTo(HandlerRegistration o) {
            int rankCompare = Integer.compare(ranking, o.ranking);
            return rankCompare == 0 ? o.fac.getClass().getCanonicalName().compareTo(fac.getClass().getCanonicalName())
                    : - rankCompare;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof HandlerRegistration) && fac.getClass() == ((HandlerRegistration) obj).fac.getClass();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(fac);
        }

        @Override
        public String toString() {
            return String.format("%d: %s", ranking, fac.getClass().getCanonicalName());
        }
        
    }
    
    // this is a static + greedy reference so that external handler factories
    // can always replace the built-in handlers.
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY)
    protected void addHandlerFactory(DeviceHandlerFactory fac, Map<String, Object> serviceProperties) {
        int ranking = 1;
        if (serviceProperties.containsKey(Constants.SERVICE_RANKING)) {
            ranking = (int) serviceProperties.get(Constants.SERVICE_RANKING);
        }
        logger.info("adding handler factory {}, rank {}", fac, ranking);
        synchronized (handlerFactories) {
            handlerFactories.add(new HandlerRegistration(fac, ranking));
        }
    }
    
    protected void removeHandlerFactory(DeviceHandlerFactory fac, Map<String, Object> serviceProperties) {
        // nothing to do for static reference handling
    }

    @Activate
    protected void activate(ComponentContext ctx) {
        this.ctx = ctx;
    }

    final ResourceDemandListener<HmLogicInterface> configListener = new ResourceDemandListener<HmLogicInterface>() {

        @Override
        public void resourceAvailable(HmLogicInterface t) {
            List<DeviceHandlerFactory> l = new ArrayList<>(handlerFactories.size());
            synchronized (handlerFactories) {
                for (HandlerRegistration reg: handlerFactories) {
                    l.add(reg.fac);
                }
            }
            HmConnection conn = new HmConnection(l, appman, eventAdmin, ctx, logger, HomeMaticDriver.this, t);
            connections.put(t, conn);
            conn.init();
        }

        @Override
        public void resourceUnavailable(HmLogicInterface t) {
            connections.remove(t).close();
        }

    };
    

    @Override
    public void start(ApplicationManager am) {
        appman = am;
        logger = am.getLogger();
        // delay actual setup so that all external ChannelHandlerFactories
        // are available (cosmetic change, quick restarts seem to work fine)
        final Timer t = appman.createTimer(2000);
        t.addListener(new TimerListener() {
            @Override
            public void timerElapsed(Timer timer) {
                logger.info("HomeMatic driver ready, configuration pending");
                appman.getResourceAccess().addResourceDemand(HmLogicInterface.class, configListener);
                t.destroy();
            }
        });
    }

    @Override
    public void stop(AppStopReason asr) {
        if (appman != null) {
            appman.getResourceAccess().removeResourceDemand(HmLogicInterface.class, configListener);
        }
    	final Iterator<HmConnection> it = connections.values().iterator();
    	while (it.hasNext()) {
    		final HmConnection conn = it.next();
    		it.remove();
    		conn.close();
    	}
    }

    protected void pollParameters(HmConnection connection) {
        for (HmDevice dev : connection.baseResource.devices().getAllElements()) {
            for (HmDevice sub : dev.channels().getAllElements()) {
                setupDevice(sub);
            }
        }
    }

    protected HmConnection findConnection(HmDevice dev) {
        Resource p = dev;
        while (p.getParent() != null) {
            p = p.getParent();
        }
        if (!(p instanceof HmLogicInterface)) {
            throw new IllegalStateException("HmDevice in wrong place: " + dev.getPath());
        }
        return connections.get((HmLogicInterface) p);
    }

    public void setupDevice(HmDevice dev) {
        String address = dev.address().getValue();
        if (acceptedDevices.containsKey(address)) {
            logger.debug("device {} already controlled by handler type {}", address, acceptedDevices.get(address));
            return;
        }
        HmConnection conn = findConnection(dev);
        if (conn == null) {
            logger.warn("no connection for device {}", dev.getPath());
            return;
        }
        try {
            DeviceDescription channelDesc = conn.client.getDeviceDescription(address);
            if (channelDesc.isDevice()) {
                dev.addStructureListener(new DeletionListener(address, conn.client, true, logger));
            }
            for (DeviceHandler h : conn.handlers) {
                if (h.accept(channelDesc)) {
                    logger.debug("handler available for {}: {}", address, h.getClass().getCanonicalName());
                    Map<String, Map<String, ParameterDescription<?>>> paramSets = new HashMap<>();
                    for (String set : dev.paramsets().getValues()) {
                        logger.trace("requesting paramset {} of device {}", set, address);
                        paramSets.put(set, conn.client.getParamsetDescription(address, set));
                    }
                    HmDevice masterDevice = channelDesc.isDevice() ?
                            dev : (HmDevice) dev.getParent().getParent();
                    h.setup(masterDevice, channelDesc, paramSets);
                    acceptedDevices.put(address, h.getClass().asSubclass(DeviceHandler.class));
                    break;
                }
            }
        } catch (XmlRpcException ex) {
            logger.error("failed to configure value resources for device " + dev.getPath()+ " address:"+address, ex);
        }
    }

    public void storeEvent(HmEvent e, SingleValueResource res) {
        logger.debug("storing event data for {}@{} to {}", e.getValueKey(), e.getAddress(), res.getPath());
        if (res instanceof FloatResource) {
            ((FloatResource) res).setValue(e.getValueFloat());
        } else if (res instanceof IntegerResource) {
            ((IntegerResource) res).setValue(e.getValueInt());
        } else if (res instanceof StringResource) {
            ((StringResource) res).setValue(e.getValueString());
        } else if (res instanceof BooleanResource) {
            ((BooleanResource) res).setValue(e.getValueBoolean());
        } else {
            logger.warn("HomeMatic parameter resource is of unsupported type: {}", res.getResourceType());
        }
    }

    @Reference
    public void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

}
