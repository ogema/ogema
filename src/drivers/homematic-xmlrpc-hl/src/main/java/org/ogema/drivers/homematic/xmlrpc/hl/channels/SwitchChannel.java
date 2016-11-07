/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.drivers.homematic.xmlrpc.hl.channels;

import java.util.List;
import java.util.Map;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.drivers.homematic.xmlrpc.hl.HomeMaticDriver;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.actors.OnOffSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class SwitchChannel implements ChannelHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SwitchChannel.class);
    
    static class SingleChangeUpdater implements ResourceValueListener<Resource> {
        
        protected final HomeMaticDriver hm;
        protected final String address;
        protected final String valueKey;
        
        public SingleChangeUpdater(HomeMaticDriver hm, String address, String valueKey) {
            this.hm = hm;
            this.address = address;
            this.valueKey = valueKey;
        }
        
        @Override
        public void resourceChanged(Resource res) {
            logger.debug("OGEMA value changed for HomeMatic {}/{}", address, valueKey);
            if (res instanceof FloatResource) {
                float value = ((FloatResource) res).getValue();
                hm.performSetValue(address, valueKey, value);
            } else if (res instanceof IntegerResource) {
                int value = ((IntegerResource) res).getValue();
                hm.performSetValue(address, valueKey, value);
            } else if (res instanceof StringResource) {
                String value = ((StringResource) res).getValue();
                hm.performSetValue(address, valueKey, value);
            } else if (res instanceof BooleanResource) {
                boolean value = ((BooleanResource) res).getValue();
                hm.performSetValue(address, valueKey, value);
            } else {
                LoggerFactory.getLogger(SwitchChannel.class).warn("HomeMatic parameter resource is of unsupported type: {}", res.getResourceType());
            }
        }
    }
    
    static class SwitchEventListener implements HmEventListener {
        
        final OnOffSwitch sw;
        final String address;

        public SwitchEventListener(OnOffSwitch sw, String address) {
            this.sw = sw;
            this.address = address;
        }
        
        @Override
        public void event(List<HmEvent> events) {
            for (HmEvent e: events) {
                if (!address.equals(e.getAddress())) {
                    continue;
                }
                switch (e.getValueKey()) {
                    case "STATE" : sw.stateFeedback().setValue(e.getValueBoolean()); break;
                }
            }
        }
        
    }
            
    
    @Override
    public boolean accept(DeviceDescription desc) {
        return "SWITCH".equalsIgnoreCase(desc.getType());
    }
    
    @Override
    public void setup(HmDevice parent, HomeMaticDriver hm, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        LoggerFactory.getLogger(getClass()).debug("setup SWITCH handler for address {}", desc.getAddress());
        String swName = HomeMaticDriver.sanitizeResourcename("SWITCH_" + desc.getAddress());
        OnOffSwitch sw = parent.addDecorator(swName, OnOffSwitch.class);
        sw.stateControl().create();
        sw.stateFeedback().create();
        sw.stateControl().addValueListener(new SingleChangeUpdater(hm, desc.getAddress(), "STATE"));
        hm.getHomeMaticService().addEventListener(new SwitchEventListener(sw, desc.getAddress()));
        sw.activate(true);
    }
    
}
