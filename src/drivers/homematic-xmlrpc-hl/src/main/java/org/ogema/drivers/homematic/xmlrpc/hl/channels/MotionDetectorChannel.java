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
import org.ogema.model.sensors.OccupancySensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class MotionDetectorChannel implements ChannelHandler {

    Logger logger = LoggerFactory.getLogger(getClass());
    enum PARAMS {

        MOTION,
        BRIGHTNESS,
        ERROR

    }

    class MotionEventListener implements HmEventListener {

        final OccupancySensor sens;
        final String address;

        public MotionEventListener(OccupancySensor sens, String address) {
            this.sens = sens;
            this.address = address;
        }

        @Override
        public void event(List<HmEvent> events) {
            for (HmEvent e : events) {
                if (!address.equals(e.getAddress())) {
                    continue;
                }
                if (PARAMS.MOTION.name().equals(e.getValueKey())) {
                    sens.reading().setValue(e.getValueBoolean());
                    logger.debug("MOTION_DETECTOR {} motion detected: {}", e.getAddress(), e.getValueBoolean());
                } else if (PARAMS.ERROR.name().equals(e.getValueKey())) {
                    int err = e.getValueInt();
                    //XXX no idea what this event signals...
                }
            }
        }

    }

    @Override
    public boolean accept(DeviceDescription desc) {
        return "MOTION_DETECTOR".equalsIgnoreCase(desc.getType());
    }

    @Override
    public void setup(HmDevice parent, HomeMaticDriver hm, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        logger.debug("setup MOTION_DETECTOR handler for address {}", desc.getAddress());
        String swName = HomeMaticDriver.sanitizeResourcename("MOTION_DETECTOR" + desc.getAddress());
        Map<String, ParameterDescription<?>> values = paramSets.get(ParameterDescription.SET_TYPES.VALUES.name());
        if (values == null) {
            logger.warn("received no VALUES parameters for device {}", desc.getAddress());
            return;
        }
        OccupancySensor sens = parent.addDecorator(swName, OccupancySensor.class);
        sens.reading().create();
        sens.activate(true);
        hm.getHomeMaticService().addEventListener(new MotionEventListener(sens, desc.getAddress()));
    }

}
