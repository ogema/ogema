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
package org.ogema.drivers.homematic.xmlrpc.ll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.Servlet;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmBackend;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HomeMatic;
import org.ogema.drivers.homematic.xmlrpc.ll.internal.DefaultHmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc.DeviceDescriptionXmlRpc;
import org.ogema.drivers.homematic.xmlrpc.ll.internal.HomeMaticCalls;
import org.ogema.drivers.homematic.xmlrpc.ll.internal.HomeMaticXmlRpcServlet;
import org.ogema.drivers.homematic.xmlrpc.ll.internal.SystemCalls;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class HomeMaticService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String interfaceUrl;
    private List<DeviceListener> deviceListeners = new CopyOnWriteArrayList<>();
    private List<HmEventListener> eventListeners = new CopyOnWriteArrayList<>();
    private final ServiceRegistration<Servlet> registration;
    private final HomeMaticXmlRpcServlet servlet;
    
    private HmBackend backend;
    
    public HomeMaticService(BundleContext ctx, String urlBase, String alias) {
        this.interfaceUrl = urlBase + alias;
        
        servlet = new HomeMaticXmlRpcServlet(ctx, procfac);
        @SuppressWarnings("UseOfObsoleteCollectionType")
        Dictionary<String, Object> parameters = new java.util.Hashtable<>();
        parameters.put("osgi.http.whiteboard.servlet.pattern", alias);
        parameters.put("servlet.init.enabledForExtensions", "true");
        parameters.put("servlet.init.encoding", "ISO-8859-1");
        registration = ctx.registerService(Servlet.class, servlet, parameters);
    }
    
    public void close() {
    	try {
    		registration.unregister();
    		servlet.destroy();
    	} catch (Exception e) {
    		logger.error("Error removing HomeMatic servlet",e);
    	}
   	}

    HomeMaticCalls defaultHandler = new HomeMaticCalls() {

        @Override
        public Object listDevices(String param) {
            logger.info("listDevices {}", param);
            List<Object> rval = new ArrayList<>();
            for (DeviceDescription dd: backend.getKnownDevices(param)) {
                rval.add(new DeviceDescriptionXmlRpc(dd).getStruct());
            }
            return rval;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Void newDevices(String interfaceId, Object[] descriptions) {
            logger.info("newDevices interface: {}", interfaceId);

            List<DeviceDescription> dds = new ArrayList<>(descriptions.length);
            for (Object o : descriptions) {
                dds.add(new DeviceDescriptionXmlRpc((Map<String, Object>) o));
            }
            for (DeviceListener l : deviceListeners) {
                l.deviceAdded(interfaceId, Collections.unmodifiableList(dds));
            }
            return null;
        }

        @Override
        public Void deleteDevices(String interfaceId, Object[] addresses) {
            logger.warn("received unsupported deleteDevices calls: iterface={}, addresses={}",
                    interfaceId, Arrays.asList(addresses));
            return null;
        }

        @Override
        public Void readdedDevice(String interfaceId, String[] addresses) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Void replaceDevice(String interfaceId, String oldDeviceAddress, String newDeviceAddress) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Void updateDevice(String interfaceId, String address, int hint) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Void event(String interfaceId, String address, String valueKey, int value) {
            HmEvent e = new DefaultHmEvent(interfaceId, address, valueKey, value);
            logger.trace("event({},{},{},{})", interfaceId, address, valueKey, value);
            List<HmEvent> events = Collections.singletonList(e);
            for (HmEventListener l : eventListeners) {
                l.event(events);
            }
            return null;
        }

    };

    SystemCalls systemHandler = new SystemCalls() {

        @Override
        public Object multicall(Object[] calls) {
            if (logger.isDebugEnabled()) {
                for (int i = 0; i < calls.length; i++) {
                    @SuppressWarnings("unchecked")
                    Map<String, ?> call = (Map<String, ?>) calls[i];
                    String methodName = call.get("methodName").toString();
                    Object[] callParams = (Object[]) call.get("params");
                    logger.debug("multicall {}/{}: {}({})", i+1, calls.length, methodName, Arrays.asList(callParams));
                }
            }
            List<HmEvent> events = new ArrayList<>();
            for (Object callO : calls) {
                @SuppressWarnings(value = "unchecked")
                        Map<String, ?> call = (Map<String, ?>) callO;
                String methodName = call.get("methodName").toString();
                Object[] callParams = (Object[]) call.get("params");
                if ("event".equals(methodName)) {
                    String interfaceId = String.valueOf(callParams[0]);
                    String address = String.valueOf(callParams[1]);
                    String valueKey = String.valueOf(callParams[2]);
                    Object value = callParams[3];
                    logger.trace("event({},{},{},{})", interfaceId, address, valueKey, value);
                    events.add(new DefaultHmEvent(interfaceId, address, valueKey, value));
                } else {
                    logger.warn("unsupported multicall method: {}", methodName);
                    for (int j = 0; j < callParams.length; j++) {
                        logger.debug("param {}: {}", j, callParams[j]);
                    }
                }
            }
            if (!events.isEmpty()) {
            	logger.debug("Processing "+events.size()+" for "+eventListeners.size()+" event listeners. First address:"+events.get(0).getAddress());
                for (HmEventListener l : eventListeners) {
                    l.event(Collections.unmodifiableList(events));
                }
            }
            Object[] result = new Object[calls.length];
            
            return result;
        }

        @Override
        public Object listMethods(String s) {
            return new String[]{"listDevices", "newDevices"};
        }

    };

    RequestProcessorFactoryFactory procfac = new RequestProcessorFactoryFactory() {

        RequestProcessorFactoryFactory.RequestProcessorFactory rpfSystem = new RequestProcessorFactoryFactory.RequestProcessorFactory() {
            @Override
            public Object getRequestProcessor(XmlRpcRequest pRequest) throws XmlRpcException {
                return systemHandler;
            }
        };

        RequestProcessorFactoryFactory.RequestProcessorFactory rpfDefault = new RequestProcessorFactoryFactory.RequestProcessorFactory() {
            @Override
            public Object getRequestProcessor(XmlRpcRequest pRequest) throws XmlRpcException {
                return defaultHandler;
            }
        };

        @Override
        @SuppressWarnings("rawtypes")
        public RequestProcessorFactoryFactory.RequestProcessorFactory getRequestProcessorFactory(Class pClass) throws XmlRpcException {
            if (pClass.equals(SystemCalls.class)) {
                return rpfSystem;
            } else if (pClass.equals(HomeMaticCalls.class)) {
                return rpfDefault;
            } else {
                return null;
            }
        }

    };
    
    public void addDeviceListener(DeviceListener l) {
        deviceListeners.add(l);
    }
    
    public void removeDeviceListener(DeviceListener l) {
        deviceListeners.remove(l);
    }
    
    public void addEventListener(HmEventListener l) {
        eventListeners.add(l);
    }
    
    public void removeEventListener(HmEventListener l) {
        eventListeners.remove(l);
    }
    
    public void setBackend(HmBackend b) {
        backend = b;
    }
    
    public void init(HomeMatic client, String interfaceId) throws XmlRpcException {
        logger.debug("register logic interface {}, url {}", interfaceId, interfaceUrl);
        client.init(interfaceUrl, interfaceId);
    }
    
}
