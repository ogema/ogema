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
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.actors.RemoteControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.tools.resource.util.ResourceUtils;

/**
 *
 * @author jlapp
 */
public class KeyChannel extends AbstractDeviceHandler {

    Logger logger = LoggerFactory.getLogger(getClass());

    public KeyChannel(HomeMaticConnection conn) {
        super(conn);
    }
    
    class KeyEventListener implements HmEventListener {

        final String address;
        final BooleanResource longPressEventResource;
        final BooleanResource shortPressEventResource;

        public KeyEventListener(String address, BooleanResource shortPress, BooleanResource longPress) {
            this.address = address;
            shortPressEventResource = shortPress;
            longPressEventResource = longPress;
        }

        @Override
        public void event(List<HmEvent> events) {
            for (HmEvent e : events) {
                if (!address.equals(e.getAddress())) {
                    continue;
                }
                switch (e.getValueKey()) {
                    case "PRESS_SHORT" : {
                        shortPressEventResource.activate(false);
                        shortPressEventResource.setValue(true);
                        logger.debug("short press event on {}", shortPressEventResource.getPath());
                        break;
                    }
                    case "PRESS_LONG" : {
                        longPressEventResource.activate(false);
                        longPressEventResource.setValue(true);
                        logger.debug("long press event on {}", longPressEventResource.getPath());
                        break;
                    }
                    default : {
                        logger.trace("unsupported KEY event '{}'", e.getValueKey());
                    }
                }
            }
        }

    }

    @Override
    public boolean accept(DeviceDescription desc) {
        return "KEY".equalsIgnoreCase(desc.getType());
    }

    @Override
    public void setup(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        logger.debug("setup KEY handler for address {}", desc.getAddress());
        String remoteName = "KEYS";
        
        RemoteControl rc = parent.addDecorator(remoteName, RemoteControl.class);
        rc.shortPress().create();
        rc.longPress().create();
        rc.activate(true);
        
        String eventResourceName = getEventResourceName(parent, desc);
        logger.debug("mapping channel {} of device type {} to {}", desc.getAddress(), parent.type().getValue(), eventResourceName);
        BooleanResource shortPressEventResource = rc.shortPress().addDecorator(eventResourceName, BooleanResource.class).create();
        BooleanResource longPressEventResource = rc.longPress().addDecorator(eventResourceName, BooleanResource.class).create();
        
        conn.addEventListener(new KeyEventListener(desc.getAddress(), shortPressEventResource, longPressEventResource));
    }
    
    protected String getEventResourceName(HmDevice device, DeviceDescription keyChannel) {
        if ("HM-RC-4-3".equals(device.type().getValue())) {
            if (keyChannel.getAddress().endsWith(":2")) {
                return "KEY1";
            }
            if (keyChannel.getAddress().endsWith(":1")) {
                return "KEY2";
            }
            if (keyChannel.getAddress().endsWith(":4")) {
                return "KEY3";
            }
            if (keyChannel.getAddress().endsWith(":3")) {
                return "KEY4";
            }
        }
        String channelAddress = keyChannel.getAddress();
        int i = channelAddress.lastIndexOf(":");
        if (i > -1) {
            try {
                int channelNum = Integer.parseInt(channelAddress.substring(i+1));
                //fits HM-RC-8
                return "KEY" + channelNum;
            } catch (Exception e) {
                //nevermind
            }
        }
        return ResourceUtils.getValidResourceName("KEY_"+keyChannel.getAddress());
    }

}
