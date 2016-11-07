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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.drivers.homematic.xmlrpc.hl.HomeMaticDriver;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.actors.RemoteControl;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.sensors.HumiditySensor;
import org.ogema.model.sensors.Sensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class KeyChannel implements ChannelHandler {

    Logger logger = LoggerFactory.getLogger(getClass());

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
    public void setup(HmDevice parent, HomeMaticDriver hm, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        logger.debug("setup KEY handler for address {}", desc.getAddress());
        String remoteName = HomeMaticDriver.sanitizeResourcename("KEYS");
        
        RemoteControl rc = parent.addDecorator(remoteName, RemoteControl.class);
        rc.shortPress().create().activate(false);
        rc.longPress().create().activate(false);
        
        //TODO? select eventResourceName based on device type to create a useful ordering of key resources
        String eventResourceName = HomeMaticDriver.sanitizeResourcename("KEY_"+desc.getAddress());
        BooleanResource shortPressEventResource = rc.shortPress().addDecorator(eventResourceName, BooleanResource.class).create();
        BooleanResource longPressEventResource = rc.longPress().addDecorator(eventResourceName, BooleanResource.class).create();
        
        hm.getHomeMaticService().addEventListener(new KeyEventListener(desc.getAddress(), shortPressEventResource, longPressEventResource));
    }
    
    //protected String getEventResourceName(HmDevice device, DeviceDescription keyChannel);

}
