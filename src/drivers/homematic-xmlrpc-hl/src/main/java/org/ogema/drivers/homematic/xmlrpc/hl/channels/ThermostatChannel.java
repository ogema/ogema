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
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.drivers.homematic.xmlrpc.hl.HomeMaticDriver;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class ThermostatChannel implements ChannelHandler {

    Logger logger = LoggerFactory.getLogger(getClass());

    enum PARAMS {

        SET_TEMPERATURE() {

                    @Override
                    public float convertInput(float v) {
                        return v + 273.15f;
                    }

                    @Override
                    public float convertOutput(float v) {
                        return v - 273.15f;
                    }

                },
        ACTUAL_TEMPERATURE() {

                    @Override
                    public float convertInput(float v) {
                        return v + 273.15f;
                    }

                },
        VALVE_STATE,
        BATTERY_STATE;

        public float convertInput(float v) {
            return v;
        }

        public float convertOutput(float v) {
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
                    logger.debug("resource updated: {} = {}", res.getPath(), e.getValue());
                } catch (IllegalArgumentException ex) {
                    //this block intentionally left blank
                }
            }
        }

    }

    @Override
    public boolean accept(DeviceDescription desc) {
        //System.out.println("parent type = " + desc.getParentType());
        return "HM-CC-RT-DN".equalsIgnoreCase(desc.getParentType()) && "CLIMATECONTROL_RT_TRANSCEIVER".equalsIgnoreCase(desc.getType());
    }

    @Override
    public void setup(HmDevice parent, final HomeMaticDriver hm, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        final String address = desc.getAddress();
        logger.debug("setup THERMOSTAT handler for address {} type {}", desc.getAddress(), desc.getType());
        String swName = HomeMaticDriver.sanitizeResourcename("THERMOSTAT" + desc.getAddress());
        Map<String, ParameterDescription<?>> values = paramSets.get(ParameterDescription.SET_TYPES.VALUES.name());
        if (values == null) {
            logger.warn("received no VALUES parameters for device {}", desc.getAddress());
            return;
        }

        Thermostat thermos = parent.addDecorator(swName, Thermostat.class);
        Map<String, SingleValueResource> resources = new HashMap<>();
        for (Map.Entry<String, ParameterDescription<?>> e : values.entrySet()) {
            switch (e.getKey()) {
                case "SET_TEMPERATURE": {
                    TemperatureResource reading = thermos.temperatureSensor().deviceFeedback().setpoint();
                    if (!reading.exists()) {
                        reading.create();
                        thermos.activate(true);
                    }
                    logger.debug("found supported thermostat parameter {} on {}", e.getKey(), desc.getAddress());
                    resources.put(e.getKey(), reading);
                    break;
                }
                case "ACTUAL_TEMPERATURE": {
                    TemperatureResource reading = thermos.temperatureSensor().reading();
                    if (!reading.exists()) {
                        reading.create();
                        thermos.activate(true);
                    }
                    logger.debug("found supported thermostat parameter {} on {}", e.getKey(), desc.getAddress());
                    resources.put(e.getKey(), reading);
                    break;
                }
                case "VALVE_STATE": {
                    FloatResource reading = thermos.valve().setting().stateFeedback();
                    if (!reading.exists()) {
                        reading.create();
                        thermos.activate(true);
                    }
                    logger.debug("found supported thermostat parameter {} on {}", e.getKey(), desc.getAddress());
                    resources.put(e.getKey(), reading);
                    break;
                }
                case "BATTERY_STATE": {
                    FloatResource reading = thermos.battery().internalVoltage().reading();
                    if (!reading.exists()) {
                        reading.create();
                        thermos.activate(true);
                    }
                    logger.debug("found supported thermostat parameter {} on {}", e.getKey(), desc.getAddress());
                    resources.put(e.getKey(), reading);
                    break;
                }
            }
        }
        
        TemperatureResource setpoint = thermos.temperatureSensor().settings().setpoint();
        setpoint.create();
        thermos.activate(true);
        
        setpoint.addValueListener((TemperatureResource t) -> {
            //System.out.printf("%s=%f%n", t.getPath(), t.getCelsius());
            //XXX fails without the toString conversion...
            hm.performSetValue(address, "SET_TEMPERATURE", Float.toString(t.getCelsius()));
        }, true);
        
        hm.getHomeMaticService().addEventListener(new WeatherEventListener(resources, desc.getAddress()));
    }

}
