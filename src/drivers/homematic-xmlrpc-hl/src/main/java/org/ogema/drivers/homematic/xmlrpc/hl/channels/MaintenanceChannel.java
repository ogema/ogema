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
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmMaintenance;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class MaintenanceChannel implements ChannelHandler {

    Logger logger = LoggerFactory.getLogger(getClass());
    enum PARAMS {

        LOWBAT,
        RSSI_DEVICE

    }

    class MaintenanceEventListener implements HmEventListener {

        final HmMaintenance mnt;
        final String address;

        public MaintenanceEventListener(HmMaintenance mnt, String address) {
            this.mnt = mnt;
            this.address = address;
        }

        @Override
        public void event(List<HmEvent> events) {
            for (HmEvent e : events) {
                if (!address.equals(e.getAddress())) {
                    continue;
                }
                if (PARAMS.LOWBAT.name().equals(e.getValueKey())) {
                    if (!mnt.batteryLow().isActive()) {
                        mnt.batteryLow().create().activate(false);
                    }
                    mnt.batteryLow().setValue(e.getValueBoolean());
                } else if (PARAMS.RSSI_DEVICE.name().equals(e.getValueKey())) {
                    if (!mnt.rssiDevice().isActive()) {
                        mnt.rssiDevice().create().activate(false);
                    }
                    mnt.rssiDevice().setValue(e.getValueInt());
                }
            }
        }

    }

    @Override
    public boolean accept(DeviceDescription desc) {
        return "MAINTENANCE".equalsIgnoreCase(desc.getType());
    }

    @Override
    public void setup(HmDevice parent, HomeMaticDriver hm, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        logger.debug("setup MAINTENANCE handler for address {}", desc.getAddress());
        String swName = HomeMaticDriver.sanitizeResourcename("MAINTENANCE" + desc.getAddress());
        Map<String, ParameterDescription<?>> values = paramSets.get(ParameterDescription.SET_TYPES.VALUES.name());
        if (values == null) {
            logger.warn("received no VALUES parameters for device {}", desc.getAddress());
            return;
        }
        HmMaintenance mnt = parent.addDecorator(swName, HmMaintenance.class);
        // create the battery field as it will be probably be linked into higher level models
        mnt.batteryLow().create();
        mnt.activate(true);
        hm.getHomeMaticService().addEventListener(new MaintenanceEventListener(mnt, desc.getAddress()));
    }

}
