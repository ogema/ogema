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
 * Handler for a {@code HM-ES-PMSw1-Pl} or {@code HM-ES-PMSw1-Pl-DN-R1} device. Does not do any HomeMatic
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

    /** Enter main device for power meter and/or switching device here*/
    @Override
    public boolean accept(DeviceDescription desc) {
    	String type = desc.getType();
    	return "HM-ES-PMSw1-Pl".equals(type) || "HM-ES-PMSw1-Pl-DN-R1".equals(type)
    			|| "HMIP-PSM".equals(type);
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
