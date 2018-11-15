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
package org.ogema.drivers.homematic.xmlrpc.hl;

import java.util.ArrayList;
import java.util.List;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmLogicInterface;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmBackend;
import org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc.DeviceDescriptionXmlRpc;
import org.slf4j.Logger;

/**
 *
 * @author jlapp
 */
class Persistence implements HmBackend, DeviceListener {

    private final ApplicationManager appman;
    private final HmLogicInterface hm;
    private final Logger logger;

    public Persistence(ApplicationManager appman, HmLogicInterface hm) {
        this.appman = appman;
        this.hm = hm;
        logger = appman.getLogger();
    }

    @Override
    public List<DeviceDescription> getKnownDevices(String interfaceId) {
        List<DeviceDescription> rval = new ArrayList<>();
        for (HmDevice dev : hm.devices().getAllElements()) {
            DeviceDescriptionXmlRpc dd = new DeviceDescriptionXmlRpc(dev.address().getValue(), dev.version().getValue());
            rval.add(dd);
            for (HmDevice c: dev.channels().getAllElements()) {
                rval.add(new DeviceDescriptionXmlRpc(c.address().getValue(), c.version().getValue()));
            }
        }
        logger.debug("return {} known devices", rval.size());
        return rval;
    }
    
    private static String createResourceName(String type, String address) {
        return ("HM_" + type + "_" + address).replaceAll("[^\\p{javaJavaIdentifierPart}]", "_");
    }
    
    private void storeCommonData(HmDevice dev, DeviceDescription desc) {
        dev.type().<StringResource>create().setValue(desc.getType());
        dev.address().<StringResource>create().setValue(desc.getAddress());
        dev.version().<IntegerResource>create().setValue(desc.getVersion());
        dev.paramsets().<StringArrayResource>create().setValues(desc.getParamsets());
    }
    
    private void storeDeviceData(HmDevice dev, DeviceDescription desc) {
        storeCommonData(dev, desc);
        dev.children().<StringArrayResource>create().setValues(desc.getChildren());
    }
    
    private void storeChannelData(HmDevice dev, DeviceDescription desc) {
        storeCommonData(dev, desc);
    }
    
    @Override
    public void deviceAdded(String interfaceId, List<DeviceDescription> descriptions) {
        for (DeviceDescription dd : descriptions) {
            String deviceResName = createResourceName(dd.getType(), dd.getAddress());
            if (dd.isDevice()) {
                HmDevice res = hm.devices().addDecorator(deviceResName, HmDevice.class);
                logger.debug("new device: {}", res.getPath());
                storeDeviceData(res, dd);
                res.activate(true);
            } else {
                String parentResName = createResourceName(dd.getParentType(), dd.getParent());
                HmDevice parent = hm.devices().getSubResource(parentResName);
                if (parent == null) {
                    logger.warn("channel added for unknown parent device {}", parentResName);
                    continue;
                }
                HmDevice channel = parent.channels().addDecorator(deviceResName, HmDevice.class);
                logger.debug("new channel: {}", channel.getPath());
                storeChannelData(channel, dd);
                channel.activate(true);
            }
        }
    }

    @Override
    public void devicesDeleted(String interfaceId, List<String> addresses) {
        logger.warn("unimplemented callback: devicesDeleted({}, {})", interfaceId, addresses);
    }

    @Override
    public void deviceUpdated(String interfaceId, String address, int hint) {
        logger.warn("unimplemented callback: deviceUpdated({}, {}, {})", interfaceId, address, hint);
    }

    @Override
    public void deviceReplaced(String interfaceId, String oldDeviceAddress, String newDeviceAddress) {
        logger.warn("unimplemented callback: deviceUpdated({}, {}, {})", interfaceId, oldDeviceAddress, newDeviceAddress);
    }

    @Override
    public void deviceReadded(String interfaceId, List<String> addresses) {
        logger.warn("unimplemented callback: deviceReadded({}, {})", interfaceId, addresses);
    }

}
