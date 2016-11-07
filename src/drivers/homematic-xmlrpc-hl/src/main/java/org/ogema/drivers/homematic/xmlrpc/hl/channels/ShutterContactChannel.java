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

import org.ogema.drivers.homematic.xmlrpc.hl.HomeMaticDriver;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.sensors.DoorWindowSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class ShutterContactChannel implements ChannelHandler {

    Logger logger = LoggerFactory.getLogger(getClass());
    private static final float LOWBATVALUE = 0.1f;

    enum PARAMS {

        STATE,
        LOWBAT

    }

    class ShutterContactListener implements HmEventListener {

        final DoorWindowSensor sens;
        final String address;

        public ShutterContactListener(DoorWindowSensor sens, String address) {
            this.sens = sens;
            this.address = address;
        }

        @Override
        public void event(List<HmEvent> events) {
            for (HmEvent e : events) {
                if (!address.equals(e.getAddress())) {
                    continue;
                }
                if (PARAMS.STATE.name().equals(e.getValueKey())) {
                    sens.reading().setValue(e.getValueBoolean());
                } else if (PARAMS.LOWBAT.name().equals(e.getValueKey())) {
                    sens.battery().chargeSensor().reading().setValue(e.getValueBoolean() ? LOWBATVALUE : 1.0f);
                }
            }
        }

    }

    @Override
    public boolean accept(DeviceDescription desc) {
        return "SHUTTER_CONTACT".equalsIgnoreCase(desc.getType());
    }

    @Override
    public void setup(HmDevice parent, HomeMaticDriver hm, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        logger.debug("setup SHUTTER_CONTACT handler for address {}", desc.getAddress());
        String swName = HomeMaticDriver.sanitizeResourcename("SHUTTER_CONTACT" + desc.getAddress());
        Map<String, ParameterDescription<?>> values = paramSets.get(ParameterDescription.SET_TYPES.VALUES.name());
        if (values == null) {
            logger.warn("received no VALUES parameters for device {}", desc.getAddress());
            return;
        }
        DoorWindowSensor sens = parent.addDecorator(swName, DoorWindowSensor.class);
        sens.reading().create();
        sens.activate(true);
        sens.battery().chargeSensor().reading().create();
        sens.activate(true);
        hm.getHomeMaticService().addEventListener(new ShutterContactListener(sens, desc.getAddress()));
    }

}
