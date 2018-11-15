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
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandler;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandlerFactory;

import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.tools.resource.util.ResourceUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

/**
 * Handler for the HomeMatic HM-CC-SCD CO2 Sensor, created as 
 * {@link org.ogema.model.sensors.CO2Sensor} resource in OGEMA.
 * 
 * @author jlapp
 */
@Component(service = {DeviceHandlerFactory.class}, property = {Constants.SERVICE_RANKING + ":Integer=1"})
public class CO2Sensor extends AbstractDeviceHandler implements DeviceHandlerFactory {

    Logger logger = LoggerFactory.getLogger(getClass());
    enum PARAMS {

        STATE,
    }
    /*
     * lowest threshold is 350 according to eQ-3
     */
    private static final int[] STATE2PPM_MAPPING = {400, 1000, 1500, 2000};

    @Override
    public DeviceHandler createHandler(HomeMaticConnection connection) {
        return new CO2Sensor(connection);
    }
    
    public CO2Sensor() { //service factory constructor called by SCR
        super(null);
    }

    public CO2Sensor(HomeMaticConnection conn) {
        super(conn);
    }
    
    class StateEventListener implements HmEventListener {

        final org.ogema.model.sensors.CO2Sensor sens;
        final String address;

        public StateEventListener(org.ogema.model.sensors.CO2Sensor sens, String address) {
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
                    int stateVal = e.getValueInt();
                    if (stateVal < 0 || stateVal > STATE2PPM_MAPPING.length-1) {
                        logger.error("CO2Sensor STATE value out off supported range: {}", stateVal);
                    } else {
                        int ppm = STATE2PPM_MAPPING[stateVal];
                        sens.reading().setValue(ppm);
                        logger.debug("SENSOR_FOR_CARBON_DIOXIDE {}: state={} (>={}ppm)", e.getAddress(), stateVal, ppm);
                    }
                } else {
                    logger.trace("unsupported / ignored event: {}", e);
                }
            }
        }

    }

    @Override
    public boolean accept(DeviceDescription desc) {
        return "SENSOR_FOR_CARBON_DIOXIDE".equalsIgnoreCase(desc.getType());
    }

    @Override
    public void setup(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        logger.debug("setup SENSOR_FOR_CARBON_DIOXIDE handler for address {}", desc.getAddress());
        String swName = ResourceUtils.getValidResourceName("SENSOR_FOR_CARBON_DIOXIDE" + desc.getAddress());
        Map<String, ParameterDescription<?>> values = paramSets.get(ParameterDescription.SET_TYPES.VALUES.name());
        if (values == null) {
            logger.warn("received no VALUES parameters for device {}", desc.getAddress());
            return;
        }
        //SensorDevice sd = parent.addDecorator(swName, SensorDevice.class);
        org.ogema.model.sensors.CO2Sensor sens = parent.addDecorator(swName, org.ogema.model.sensors.CO2Sensor.class);
        sens.reading().create();
        conn.addEventListener(new StateEventListener(sens, desc.getAddress()));
        sens.activate(true);
    }

}
