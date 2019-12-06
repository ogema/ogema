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

import java.util.List;
import java.util.Map;

import org.ogema.core.model.units.ElectricCurrentResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.FrequencyResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.VoltageResource;
import org.ogema.drivers.homematic.xmlrpc.hl.api.AbstractDeviceHandler;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.tools.resource.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class PowerMeterChannel extends AbstractDeviceHandler {

    Logger logger = LoggerFactory.getLogger(getClass());

    public PowerMeterChannel(HomeMaticConnection conn) {
        super(conn);
    }
    
    class PowerMeterEventListener implements HmEventListener {

        final ElectricityConnection elconn;
        final String address;

        public PowerMeterEventListener(ElectricityConnection elconn, String address) {
            this.elconn = elconn;
            this.address = address;
        }

        @Override
        public void event(List<HmEvent> events) {
            for (HmEvent e : events) {
                if (!address.equals(e.getAddress())) {
                    continue;
                }
                switch (e.getValueKey()) {
                    case "POWER":
                        PowerResource pwr = elconn.powerSensor().reading();
                        if (!pwr.exists()) {
                            pwr.create();
                            elconn.powerSensor().activate(true);
                        }
                        pwr.setValue(e.getValueFloat());
                        logger.debug("power reading updated: {} = {}", pwr.getPath(), e.getValueFloat());
                        break;
                    case "CURRENT": {
                        ElectricCurrentResource reading = elconn.currentSensor().reading();
                        if (!reading.exists()) {
                            reading.create();
                            elconn.currentSensor().activate(true);
                        }
                        reading.setValue(e.getValueFloat() / 1000.0f);
                        logger.debug("current reading updated: {} = {}", reading.getPath(), e.getValueFloat());
                        break;
                    }
                    case "VOLTAGE": {
                        VoltageResource reading = elconn.voltageSensor().reading();
                        if (!reading.exists()) {
                            reading.create();
                            elconn.voltageSensor().activate(true);
                        }
                        reading.setValue(e.getValueFloat());
                        logger.debug("voltage reading updated: {} = {}", reading.getPath(), e.getValueFloat());
                        break;
                    }
                    case "FREQUENCY": {
                        FrequencyResource reading = elconn.frequencySensor().reading();
                        if (!reading.exists()) {
                            reading.create();
                            elconn.frequencySensor().activate(true);
                        }
                        reading.setValue(e.getValueFloat());
                        logger.debug("frequency reading updated: {} = {}", reading.getPath(), e.getValueFloat());
                        break;
                    }
                    case "ENERGY_COUNTER": {
                        EnergyResource reading = elconn.energySensor().reading();
                        if (!reading.exists()) {
                            reading.create();
                            elconn.energySensor().activate(true);
                        }
                        //FIXME: value conversion required!
                        reading.setValue(e.getValueFloat());
                        logger.debug("energy reading updated: {} = {}", reading.getPath(), e.getValueFloat());
                        break;
                    }
                }
            }
        }

    }

    @Override
    /** Note: The main detection is performed in {@link PMSwitchDevice}. Here
     * you should only enter the sub channel relevant for the power meter
     */
    public boolean accept(DeviceDescription desc) {
        return "POWERMETER".equalsIgnoreCase(desc.getType())
        		||"ENERGIE_METER_TRANSMITTER".equalsIgnoreCase(desc.getType());
    }

    @Override
    public void setup(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        LoggerFactory.getLogger(getClass()).debug("setup POWERMETER handler for address {}", desc.getAddress());
        String swName = ResourceUtils.getValidResourceName("POWERMETER_" + desc.getAddress());
        ElectricityConnection elconn = parent.addDecorator(swName, ElectricityConnection.class);
        conn.addEventListener(new PowerMeterEventListener(elconn, desc.getAddress()));
        elconn.activate(true);
        
        //Switch box
        /*String ssbName = ResourceUtils.getValidResourceName("HM-SingleSwitchBox-" + parent.address().getValue());
        SingleSwitchBox ssb = parent.getSubResource(ssbName, SingleSwitchBox.class);
        ssb.onOffSwitch().stateControl().create().activate(false);
        ssb.onOffSwitch().stateFeedback().create().activate(false);
        if(elconn.exists()) ssb.electricityConnection().setAsReference(elconn);
        ssb.onOffSwitch().activate(false);
        ssb.activate(false);*/
        
    }

}
