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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.units.TemperatureResource;
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
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.tools.resource.util.ResourceUtils;

/**
 *
 * @author jlapp
 */
public class WeatherChannel extends AbstractDeviceHandler {

    Logger logger = LoggerFactory.getLogger(getClass());

    public WeatherChannel(HomeMaticConnection conn) {
        super(conn);
    }

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
                    ((FloatResource) res).setValue(p.convertInput(e.getValueFloat()));
                } catch (IllegalArgumentException ex) {
                    //this block intentionally left blank
                }
            }
        }

    }

    @Override
    public boolean accept(DeviceDescription desc) {
        return "WEATHER".equalsIgnoreCase(desc.getType()) // WDS40
                || "WEATHER_TRANSMIT".equalsIgnoreCase(desc.getType()); // TC-IT-WM-W
    }

    @Override
    public void setup(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        logger.debug("setup WEATHER handler for address {}", desc.getAddress());
        String swName = ResourceUtils.getValidResourceName("WEATHER" + desc.getAddress());
        Map<String, ParameterDescription<?>> values = paramSets.get(ParameterDescription.SET_TYPES.VALUES.name());
        if (values == null) {
            logger.warn("received no VALUES parameters for device {}", desc.getAddress());
            return;
        }
        HmDevice weatherChannel = conn.getChannel(parent, desc.getAddress());
        Map<String, SingleValueResource> resources = new HashMap<>();
        for (Map.Entry<String, ParameterDescription<?>> e : values.entrySet()) {
            switch (e.getKey()) {
                case "TEMPERATURE": {
                    ResourceList<Sensor> sensors = parent.addDecorator(swName, SensorDevice.class).sensors();
                    sensors.create();
                    TemperatureResource reading = sensors.addDecorator(e.getKey(), TemperatureSensor.class).reading();
                    conn.registerControlledResource(weatherChannel, reading.getParent());

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
                    conn.registerControlledResource(weatherChannel, reading.getParent());

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
        conn.addEventListener(new WeatherEventListener(resources, desc.getAddress()));
    }

}
