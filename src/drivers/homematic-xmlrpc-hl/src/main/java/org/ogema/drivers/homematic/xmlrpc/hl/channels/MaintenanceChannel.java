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

import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmMaintenance;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.tools.resource.util.ResourceUtils;

/**
 * Handler for {@code MAINTENANCE} channels on BidCos and HmIP devices.
 * See {@link PARAMS} for supported parameters.
 *
 * @author jlapp
 */
public class MaintenanceChannel extends AbstractDeviceHandler {

    Logger logger = LoggerFactory.getLogger(getClass());
    public enum PARAMS {

        ERROR_CODE,
        LOWBAT,
        OPERATING_VOLTAGE,
        RSSI_DEVICE,
        RSSI_PEER,

    }

    public MaintenanceChannel(HomeMaticConnection conn) {
        super(conn);
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
                if (PARAMS.ERROR_CODE.name().equals(e.getValueKey())) {
                    if (!mnt.errorCode().isActive()) {
                        mnt.errorCode().create().activate(false);
                    }
                    mnt.errorCode().setValue(e.getValueInt());
                } else if (PARAMS.LOWBAT.name().equals(e.getValueKey())) {
                    if (!mnt.batteryLow().isActive()) {
                        mnt.batteryLow().create().activate(false);
                    }
                    mnt.batteryLow().setValue(e.getValueBoolean());
                } else if (PARAMS.RSSI_DEVICE.name().equals(e.getValueKey())) {
                    if (!mnt.rssiDevice().isActive()) {
                        mnt.rssiDevice().create().activate(false);
                    }
                    mnt.rssiDevice().setValue(e.getValueInt());
                } else if (PARAMS.RSSI_PEER.name().equals(e.getValueKey())) {
                    if (!mnt.rssiPeer().isActive()) {
                        mnt.rssiPeer().create().activate(false);
                    }
                    mnt.rssiPeer().setValue(e.getValueInt());
                } else if (PARAMS.OPERATING_VOLTAGE.name().equals(e.getValueKey())) {
                    if (!mnt.battery().internalVoltage().reading().isActive()) {
                        mnt.battery().internalVoltage().reading().create().activate(false);
                        mnt.battery().internalVoltage().activate(false);
                        mnt.battery().activate(false);
                    }
                    mnt.battery().internalVoltage().reading().setValue(e.getValueFloat());
                }
            }
        }

    }

    @Override
    public boolean accept(DeviceDescription desc) {
        return "MAINTENANCE".equalsIgnoreCase(desc.getType());
    }

    @Override
    public void setup(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        logger.debug("setup MAINTENANCE handler for address {}", desc.getAddress());
        String swName = ResourceUtils.getValidResourceName("MAINTENANCE" + desc.getAddress());
        Map<String, ParameterDescription<?>> values = paramSets.get(ParameterDescription.SET_TYPES.VALUES.name());
        if (values == null) {
            logger.warn("received no VALUES parameters for device {}", desc.getAddress());
            return;
        }
        HmMaintenance mnt = parent.addDecorator(swName, HmMaintenance.class);
        // create the battery field as it will be probably be linked into higher level models
        mnt.batteryLow().create();
        mnt.activate(true);
        conn.addEventListener(new MaintenanceEventListener(mnt, desc.getAddress()));
    }

}
