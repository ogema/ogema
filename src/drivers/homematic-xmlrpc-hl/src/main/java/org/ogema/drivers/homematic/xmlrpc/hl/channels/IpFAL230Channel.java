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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.drivers.homematic.xmlrpc.hl.api.AbstractDeviceHandler;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandler;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandlerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.devices.connectiondevices.ThermalValve;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.tools.resource.util.ResourceUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
@Component(service = {DeviceHandlerFactory.class}, property = {Constants.SERVICE_RANKING + ":Integer=1"})
public class IpFAL230Channel extends AbstractDeviceHandler implements DeviceHandlerFactory {
    
    Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public DeviceHandler createHandler(HomeMaticConnection connection) {
        return new IpFAL230Channel(connection);
    }

    public IpFAL230Channel() {
        super(null);
    }

    public IpFAL230Channel(HomeMaticConnection conn) {
        super(conn);
    }
    
    enum PARAMS {
        STATE() {

                    @Override
                    public float convertInput(boolean v) {
                    	// FAL provides only false / true values for the valve state
                        return v?1.0f:0.0f;
                    }

                },
        BATTERY_STATE;

        public float convertInput(boolean b) {
            return b?1.0f:0.0f;
        }
        public float convertInput(float b) {
            return b;
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
                    ((FloatResource) res).setValue(p.convertInput(e.getValueBoolean()));
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
        return (("HmIP-FAL230-C10".equalsIgnoreCase(desc.getParentType()) || "HmIP-FAL230-C6".equalsIgnoreCase(desc.getParentType())) && "CLIMATECONTROL_FLOOR_TRANSCEIVER".equalsIgnoreCase(desc.getType()));
    }

    @Override
    public void setup(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
        final String deviceAddress = desc.getAddress();
        logger.debug("setup FAL230-VALVE handler for address {} type {}", desc.getAddress(), desc.getType());
        String swName = ResourceUtils.getValidResourceName("VALVE" + desc.getAddress());
        Map<String, ParameterDescription<?>> values = paramSets.get(ParameterDescription.SET_TYPES.VALUES.name());
        if (values == null) {
            logger.warn("received no VALUES parameters for device {}", desc.getAddress());
            return;
        }

        SensorDevice actDev = parent.getSubResource("valves", SensorDevice.class);
        if(!actDev.isActive()) {
        	actDev.create();
        	actDev.activate(false);
        }
        ThermalValve valve = actDev.addDecorator(swName, ThermalValve.class);
        conn.registerControlledResource(conn.getChannel(parent, deviceAddress), valve);
        Map<String, SingleValueResource> resources = new HashMap<>();
        for (Map.Entry<String, ParameterDescription<?>> e : values.entrySet()) {
            switch (e.getKey()) {
                case "STATE": {
                    FloatResource reading = valve.setting().stateFeedback();
                    if (!reading.exists()) {
                        reading.create();
                        valve.activate(true);
                    }
                    logger.debug("found supported valve parameter {} on {}", e.getKey(), desc.getAddress());
                    resources.put(e.getKey(), reading);
                    break;
                }
             }
        }
        
        valve.activate(true);
        
        conn.addEventListener(new WeatherEventListener(resources, desc.getAddress()));
        setupHmParameterValues(valve, parent.address().getValue());
        //setupTempSensLinking(thermos);
    }
    
    class ParameterListener implements ResourceValueListener<SingleValueResource> {
        
        final String address;

        public ParameterListener(String address) {
            this.address = address;
        }        

        @Override
        public void resourceChanged(SingleValueResource resource) {
            String paramName = resource.getName();
            
            Object resourceValue = null;
            if (resource instanceof IntegerResource) {
                resourceValue = ((IntegerResource) resource).getValue();
            } else {
                logger.warn("unsupported parameter type: " + resource);
            }
            
            Map<String, Object> parameterSet = new HashMap<>();
            parameterSet.put(paramName, resourceValue);
            conn.performPutParamset(address, "MASTER", parameterSet);
            logger.info("Parameter set 'MASTER' updated for {}: {}", address, parameterSet);
        }
        
    };
    
    private void setupHmParameterValues(ThermalValve valve, String address) {
        //XXX address mangling (parameters are set on device, not channel)
        if (address.lastIndexOf(":") != -1) {
            address = address.substring(0, address.lastIndexOf(":"));
        }
        @SuppressWarnings("unchecked")
        ResourceList<SingleValueResource> masterParameters = valve.addDecorator("HmParametersMaster", ResourceList.class);
        if (!masterParameters.exists()) {
            masterParameters.setElementType(SingleValueResource.class);
            masterParameters.create();
        }
    }
}
