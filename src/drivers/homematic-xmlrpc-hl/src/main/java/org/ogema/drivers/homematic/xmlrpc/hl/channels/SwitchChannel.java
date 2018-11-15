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
package org.ogema.drivers.homematic.xmlrpc.hl.channels;

import org.ogema.drivers.homematic.xmlrpc.hl.api.AbstractDeviceHandler;
import java.util.List;
import java.util.Map;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.actors.OnOffSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.tools.resource.util.ResourceUtils;

/**
 *
 * @author jlapp
 */
public class SwitchChannel extends AbstractDeviceHandler {
    
    protected final Logger logger = LoggerFactory.getLogger(SwitchChannel.class);

    public SwitchChannel(HomeMaticConnection conn) {
        super(conn);
    }
    
    class SingleChangeUpdater implements ResourceValueListener<Resource> {
        
        protected final String address;
        protected final String valueKey;
        
        public SingleChangeUpdater(String address, String valueKey) {
            this.address = address;
            this.valueKey = valueKey;
        }
        
        @Override
        public void resourceChanged(Resource res) {
            logger.debug("OGEMA value changed for HomeMatic {}/{}", address, valueKey);
            if (res instanceof FloatResource) {
                float value = ((FloatResource) res).getValue();
                conn.performSetValue(address, valueKey, value);
            } else if (res instanceof IntegerResource) {
                int value = ((IntegerResource) res).getValue();
                conn.performSetValue(address, valueKey, value);
            } else if (res instanceof StringResource) {
                String value = ((StringResource) res).getValue();
                conn.performSetValue(address, valueKey, value);
            } else if (res instanceof BooleanResource) {
                boolean value = ((BooleanResource) res).getValue();
                conn.performSetValue(address, valueKey, value);
            } else {
                LoggerFactory.getLogger(SwitchChannel.class).warn("HomeMatic parameter resource is of unsupported type: {}", res.getResourceType());
            }
        }
    }
    
    class SwitchEventListener implements HmEventListener {
        
        final OnOffSwitch sw;
        final String address;

        public SwitchEventListener(OnOffSwitch sw, String address) {
            this.sw = sw;
            this.address = address;
        }
        
        @Override
        public void event(List<HmEvent> events) {
            for (HmEvent e: events) {
                /*
                if (isDeviceUnreach(e, address)) {
                    handleUnreach(e);
                    continue;
                } else 
                 */
                if (!address.equals(e.getAddress())) {
                    continue;
                }
                switch (e.getValueKey()) {
                    case "STATE" : sw.stateFeedback().setValue(e.getValueBoolean()); break;
                }
            }
        }
        
        private boolean isDeviceUnreach(HmEvent event, String channel) {
            if ("UNREACH".equals(event.getValueKey())) {
                int baseAddressEnd = channel.indexOf(':');
                String baseAddress = baseAddressEnd == -1 ? channel : channel.substring(0, baseAddressEnd);
                return event.getAddress().startsWith(baseAddress);
            }
            return false;
        }
        
        private void handleUnreach(HmEvent e) {
            if (!e.getValueBoolean()) {
                logger.debug("device {} is reachable again, resending state control {}={}",
                        address, sw.stateControl().getPath(), sw.stateControl().getValue());
                conn.performSetValue(address, "STATE", sw.stateControl().getValue());
            } else {
                logger.debug("device {} unreachable", address);
            }
        }
        
        
    }
            
    
    @Override
    public boolean accept(DeviceDescription desc) {
        return "SWITCH".equalsIgnoreCase(desc.getType());
    }
    
    @Override
    public void setup(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        LoggerFactory.getLogger(getClass()).debug("setup SWITCH handler for address {}", desc.getAddress());
        String swName = ResourceUtils.getValidResourceName("SWITCH_" + desc.getAddress());
        OnOffSwitch sw = parent.addDecorator(swName, OnOffSwitch.class);
        sw.stateControl().create();
        sw.stateFeedback().create();
        sw.stateControl().addValueListener(new SingleChangeUpdater(desc.getAddress(), "STATE"));
        conn.addEventListener(new SwitchEventListener(sw, desc.getAddress()));
        sw.activate(true);
    }
    
}
