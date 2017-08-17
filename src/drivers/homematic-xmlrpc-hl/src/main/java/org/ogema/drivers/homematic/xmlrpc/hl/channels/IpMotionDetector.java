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

import org.ogema.drivers.homematic.xmlrpc.hl.api.AbstractDeviceHandler;
import java.util.List;
import java.util.Map;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandler;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandlerFactory;

import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.sensors.OccupancySensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.sensors.LightSensor;
import org.ogema.tools.resource.util.ResourceUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author jlapp
 */
@Component(service = {DeviceHandlerFactory.class}, property = {Constants.SERVICE_RANKING + ":Integer=1"})
public class IpMotionDetector extends AbstractDeviceHandler implements DeviceHandlerFactory {

    Logger logger = LoggerFactory.getLogger(getClass());
    enum PARAMS {

        MOTION,
        ILLUMINATION
    }

    @Override
    public DeviceHandler createHandler(HomeMaticConnection connection) {
        return new IpMotionDetector(connection);
    }
    
    public IpMotionDetector() { //service factory constructor called by SCR
        super(null);
    }

    public IpMotionDetector(HomeMaticConnection conn) {
        super(conn);
    }
    
    class MotionEventListener implements HmEventListener {

        final OccupancySensor sens;
        final LightSensor illumination;
        final String address;

        public MotionEventListener(OccupancySensor sens, LightSensor illumination, String address) {
            this.sens = sens;
            this.illumination = illumination;
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
                    logger.debug("MOTIONDETECTOR_TRANSCEIVER {} motion detected: {}", e.getAddress(), e.getValueBoolean());
                } else if (PARAMS.ILLUMINATION.name().equals(e.getValueKey())) {
                    illumination.reading().setValue(e.getValueFloat());
                    logger.debug("MOTIONDETECTOR_TRANSCEIVER {} illumination: {}", e.getAddress(), e.getValueFloat());
                } else {
                    logger.trace("unsupported / ignored event: {}", e);
                }
            }
        }

    }

    @Override
    public boolean accept(DeviceDescription desc) {
        return "MOTIONDETECTOR_TRANSCEIVER".equalsIgnoreCase(desc.getType());
    }

    @Override
    public void setup(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        logger.debug("setup MOTIONDETECTOR_TRANSCEIVER handler for address {}", desc.getAddress());
        String swName = ResourceUtils.getValidResourceName("MOTIONDETECTOR_TRANSCEIVER" + desc.getAddress());
        Map<String, ParameterDescription<?>> values = paramSets.get(ParameterDescription.SET_TYPES.VALUES.name());
        if (values == null) {
            logger.warn("received no VALUES parameters for device {}", desc.getAddress());
            return;
        }
        SensorDevice sd = parent.addDecorator(swName, SensorDevice.class);
        //OccupancySensor sens = parent.addDecorator(swName, OccupancySensor.class);
        OccupancySensor sens = sd.sensors().addDecorator("motion", OccupancySensor.class);
        sens.reading().create();
        //sens.activate(true);
        
        LightSensor illumination = sd.sensors().addDecorator("illumination", LightSensor.class);
        illumination.reading().create();
        //illumination.activate(true);
        
        conn.addEventListener(new MotionEventListener(sens, illumination, desc.getAddress()));
        
        sd.activate(true);
        //sd.sensors().activate(false);
    }

}
