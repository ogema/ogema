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
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.drivers.homematic.xmlrpc.hl.HomeMaticDriver;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
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
public class WeatherChannel implements ChannelHandler {

    Logger logger = LoggerFactory.getLogger(getClass());

    enum PARAMS {

        TEMPERATURE() {

                    @Override
                    public float convertInput(float v) {
                        return v + 273.15f;
                    }

                },
        HUMIDITY {

                    @Override
                    public float convertInput(float v) {
                        return v / 100f; // percentage value -> range 0..1
                    }

                };

        public float convertInput(float v) {
            return v;
        }

    }

    class WeatherEventListener implements HmEventListener {

        final Map<String, SingleValueResource> resources;
        final String address;

        public WeatherEventListener(Map<String, SingleValueResource> resources, String address) {
            this.resources = resources;
            this.address = address;
        }

        @Override
        public void event(List<HmEvent> events) {
            for (HmEvent e : events) {
                if (!address.equals(e.getAddress())) {
                    continue;
                }
                SingleValueResource res = resources.get(e.getValueKey());
                if (res == null) {
                    continue;
                }
                try {
                    PARAMS p = PARAMS.valueOf(e.getValueKey());
                    ((FloatResource)res).setValue(p.convertInput(e.getValueFloat()));
                } catch (IllegalArgumentException ex) {
                    //this block intentionally left blank
                }
            }
        }

    }

    @Override
    public boolean accept(DeviceDescription desc) {
        return "WEATHER".equalsIgnoreCase(desc.getType());
    }

    @Override
    public void setup(HmDevice parent, HomeMaticDriver hm, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        logger.debug("setup WEATHER handler for address {}", desc.getAddress());
        String swName = HomeMaticDriver.sanitizeResourcename("WEATHER" + desc.getAddress());
        Map<String, ParameterDescription<?>> values = paramSets.get(ParameterDescription.SET_TYPES.VALUES.name());
        if (values == null) {
            logger.warn("received no VALUES parameters for device {}", desc.getAddress());
            return;
        }
        Map<String, SingleValueResource> resources = new HashMap<>();
        for (Map.Entry<String, ParameterDescription<?>> e : values.entrySet()) {
            switch (e.getKey()) {
                case "TEMPERATURE": {
                    ResourceList<Sensor> sensors = parent.addDecorator(swName, SensorDevice.class).sensors();
                    sensors.create();
                    TemperatureResource reading = sensors.addDecorator(e.getKey(), TemperatureSensor.class).reading();

                    if (!reading.exists()) {
                        reading.create();
                        reading.getParent().activate(true);
                    }
                    logger.debug("found supported WEATHER parameter {} on {}", e.getKey(), desc.getAddress());
                    resources.put(e.getKey(), reading);
                    break;
                }
                case "HUMIDITY": {
                    ResourceList<Sensor> sensors = parent.addDecorator(swName, SensorDevice.class).sensors();
                    sensors.create();
                    FloatResource reading = sensors.addDecorator(e.getKey(), HumiditySensor.class).reading();

                    if (!reading.exists()) {
                        reading.create();
                        reading.getParent().activate(true);
                    }
                    logger.debug("found supported WEATHER parameter {} on {}", e.getKey(), desc.getAddress());
                    resources.put(e.getKey(), reading);
                    break;
                }
            }
        }
        hm.getHomeMaticService().addEventListener(new WeatherEventListener(resources, desc.getAddress()));
    }

}
