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

import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.sensors.OccupancySensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.tools.resource.util.ResourceUtils;

/**
 *
 * @author jlapp
 */
public class MotionDetectorChannel extends AbstractDeviceHandler {

    Logger logger = LoggerFactory.getLogger(getClass());
    enum PARAMS {

        MOTION,
        BRIGHTNESS,
        ERROR

    }

    public MotionDetectorChannel(HomeMaticConnection conn) {
        super(conn);
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
    public void setup(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        logger.debug("setup MOTION_DETECTOR handler for address {}", desc.getAddress());
        String swName = ResourceUtils.getValidResourceName("MOTION_DETECTOR" + desc.getAddress());
        Map<String, ParameterDescription<?>> values = paramSets.get(ParameterDescription.SET_TYPES.VALUES.name());
        if (values == null) {
            logger.warn("received no VALUES parameters for device {}", desc.getAddress());
            return;
        }
        OccupancySensor sens = parent.addDecorator(swName, OccupancySensor.class);
        sens.reading().create();
        sens.activate(true);
        conn.addEventListener(new MotionEventListener(sens, desc.getAddress()));
    }

}
