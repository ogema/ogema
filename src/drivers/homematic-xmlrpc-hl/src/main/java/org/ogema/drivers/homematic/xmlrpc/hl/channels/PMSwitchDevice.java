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
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.tools.resource.util.ResourceUtils;

/**
 * Handler for a {@code HM-ES-PMSw1-Pl} device. Does not do any HomeMatic
 * communication itself, but relies on {@link PowerMeterChannel} and
 * {@link SwitchChannel} and only aggregates the resources created by these
 * handlers into an OGEMA {@link SingleSwitchBox}.
 * @author jlapp
 */
public class PMSwitchDevice extends AbstractDeviceHandler {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public PMSwitchDevice(HomeMaticConnection conn) {
        super(conn);
    }

    @Override
    public boolean accept(DeviceDescription desc) {
        return "HM-ES-PMSw1-Pl".equals(desc.getType());
    }

    protected ResourceStructureListener subChannelListener(final HmDevice dev) {
        return new ResourceStructureListener() {
            @Override
            public void resourceStructureChanged(ResourceStructureEvent event) {
                if (event.getType() == ResourceStructureEvent.EventType.SUBRESOURCE_ADDED) {
                    if (performSwitchBoxSetup(dev)) {
                        dev.removeStructureListener(this);
                    }
                }
            }
        };
    }

    @Override
    public void setup(HmDevice pmswitch, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        ResourceStructureListener l = subChannelListener(pmswitch);
        pmswitch.addStructureListener(l);
        if (performSwitchBoxSetup(pmswitch)) {
            pmswitch.removeStructureListener(l);
        }
    }

    protected boolean performSwitchBoxSetup(HmDevice dev) {
        //logger.debug("perform high level setup for device {}", dev.address().getValue());
        List<ElectricityConnection> elConns = dev.getSubResources(ElectricityConnection.class, false);
        List<OnOffSwitch> switches = dev.getSubResources(OnOffSwitch.class, false);
        if (elConns.size() == 1 && switches.size() == 1) {
            String ssbName = ResourceUtils.getValidResourceName("HM-SingleSwitchBox-" + dev.address().getValue());
            logger.debug("set up SingleSwitchBox for HomeMatic device {}", dev.address().getValue());
            OnOffSwitch sw = switches.get(0);
            ElectricityConnection elConn = elConns.get(0);

            SingleSwitchBox ssb = dev.getSubResource(ssbName, SingleSwitchBox.class);

            ssb.onOffSwitch().stateControl().create().activate(false);
            ssb.onOffSwitch().stateFeedback().create().activate(false);
            ssb.electricityConnection().create().activate(false);
            ssb.onOffSwitch().activate(false);
            elConn.setAsReference(ssb.electricityConnection());
            sw.setAsReference(ssb.onOffSwitch());

            ssb.activate(false);
            return true;
        }
        return false;
    }

}
