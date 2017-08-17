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
import org.ogema.core.model.units.ElectricCurrentResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.FrequencyResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.VoltageResource;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.connections.ElectricityConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.tools.resource.util.ResourceUtils;

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
    public boolean accept(DeviceDescription desc) {
        return "POWERMETER".equalsIgnoreCase(desc.getType());
    }

    @Override
    public void setup(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        LoggerFactory.getLogger(getClass()).debug("setup POWERMETER handler for address {}", desc.getAddress());
        String swName = ResourceUtils.getValidResourceName("POWERMETER_" + desc.getAddress());
        ElectricityConnection elconn = parent.addDecorator(swName, ElectricityConnection.class);
        conn.addEventListener(new PowerMeterEventListener(elconn, desc.getAddress()));
        elconn.activate(true);
    }

}
