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
package org.ogema.drivers.homematic.xmlrpc.ll;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HomeMatic;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ServiceMessage;
import org.ogema.drivers.homematic.xmlrpc.ll.api.XmlRpcStruct;
import org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc.DeviceDescriptionXmlRpc;
import org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc.MapXmlRpcStruct;
import org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc.ParameterDescriptionXmlRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class HomeMaticClient implements HomeMatic {

    final XmlRpcClient client;
    final Logger logger;
    
    public static class DefaultServiceMessage implements ServiceMessage {
        
        private final String address;
        private final String ID;
        private final Object value;

        DefaultServiceMessage(String address, String ID, Object value) {
            this.address = address;
            this.ID = ID;
            this.value = value;
        }
        
        DefaultServiceMessage(Object[] a) {
            this.address = (String) a[0];
            this.ID = (String) a[1];
            this.value = a[2];
        }

        @Override
        public String getAddress() {
            return address;
        }

        @Override
        public String getID() {
            return ID;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("(%s, %s, %s)", address, ID, value);
        }
        
    }

    public HomeMaticClient(String urlString) throws IOException {
        URL url = new URL(urlString);
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(url);
        config.setEnabledForExtensions(true);
        config.setEncoding("ISO-8859-1"); //only used in modified XmlRpcStreamTransport
        config.setEnabledForExceptions(true);

        client = new XmlRpcClient();
        client.setConfig(config);
        this.logger = LoggerFactory.getLogger(getClass().getCanonicalName() + "-" + url.getHost());
    }

    @Override
    public void init(String url, String interfaceId) throws XmlRpcException {
        client.execute("init", new Object[]{url, interfaceId});
    }

    @Override
    public void ping(String callerId) throws XmlRpcException {
        client.execute("ping", new Object[]{callerId});
    }

    @Override
    @SuppressWarnings("unchecked")
    public DeviceDescription getDeviceDescription(String address) throws XmlRpcException {
        Object o = client.execute("getDeviceDescription", new Object[]{address});
        return new DeviceDescriptionXmlRpc((Map<String, Object>) o);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<DeviceDescription> listDevices() throws XmlRpcException {
        Object[] a = (Object[]) client.execute("listDevices", new Object[]{});
        List<DeviceDescription> rval = new ArrayList<>(a.length);
        for (Object o: a) {
            rval.add(new DeviceDescriptionXmlRpc((Map<String, Object>) o));
        }
        return rval;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, ParameterDescription<?>> getParamsetDescription(String address, String type) throws XmlRpcException {
        Map<String, Object> rawMap = (Map<String, Object>) client.execute("getParamsetDescription", new Object[]{address, type});
        Map<String, ParameterDescription<?>> rval = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e: rawMap.entrySet()) {
            rval.put(e.getKey(), new ParameterDescriptionXmlRpc<>((Map<String, Object>)e.getValue()));
        }
        return rval;
    }

    @Override
    @SuppressWarnings("unchecked")
    public XmlRpcStruct getParamset(String address, String paramset_key) throws XmlRpcException {
        return new MapXmlRpcStruct((Map<String, Object>)client.execute("getParamset", new Object[]{address, paramset_key}));
    }

    @Override
    public String getParamsetId(String address, String type) throws XmlRpcException {
        return (String) client.execute("getParamsetId", new Object[]{address, type});
    }

    @Override
    public void putParamset(String address, String paramset_key, XmlRpcStruct set) throws XmlRpcException {
        client.execute("putParamset", new Object[]{address, paramset_key, set.toMap()});
    }
    
    @Override
    public int getInstallMode() throws XmlRpcException {
        return ((Number) client.execute("getInstallMode", new Object[]{})).intValue();
    }

    @Override
    public void setInstallMode(boolean on, int time, int mode) throws XmlRpcException {
        client.execute("setInstallMode", new Object[]{on, time, mode});
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Object> T getValue(String address, String value_key) throws XmlRpcException {
        return (T) client.execute("getValue", new Object[]{address, value_key});
    }

    @Override
    public void setValue(String address, String value_key, Object value) throws XmlRpcException {
        Object obj = client.execute("setValue", new Object[]{address, value_key, value});
        if(obj != null)
            logger.debug("setValue returned object of type: {}, value: {}", obj.getClass().getName(), obj.toString());
        else
            logger.debug("setValue returned object null");

    }
    
    @Override
    public boolean reportValueUsage(String address, String valueId, int refCounter) throws XmlRpcException {
        return (boolean) client.execute("reportValueUsage", new Object[]{address, valueId, refCounter});
    }
    
    @Override
    public void addLink(String sender, String receiver, String name, String description) throws XmlRpcException {
        client.execute("addLink", new Object[]{sender, receiver, name, description});
    }
    
    @Override
    public void removeLink(String sender, String receiver) throws XmlRpcException {
        client.execute("removeLink", new Object[]{sender, receiver});
    }

    @Override
    public List<String> getLinkPeers(String address) throws XmlRpcException {
        Object[] rval = (Object[]) client.execute("getLinkPeers", new Object[]{address});
        List<String> peers = new ArrayList<>(rval.length);
        for (Object o: rval) {
            peers.add(String.valueOf(o));
        }
        return peers;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getLinkInfo(String sender, String receiver) throws XmlRpcException {
        return (Map<String, Object>) client.execute("getLinkInfo", new Object[]{sender, receiver});
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getLinks(String address, int flags) throws XmlRpcException {
        Object[] rval = (Object[]) client.execute("getLinks", new Object[]{address, flags});
        List<Map<String, Object>> l = new ArrayList<>();
        for (Object o: rval) {
            l.add((Map<String, Object>)o);
        }
        return l;
    }

    @Override
    public List<ServiceMessage> getServiceMessages() throws XmlRpcException {
        Object o = client.execute("getServiceMessages", new Object[]{});
        if (o != null && !(o instanceof Object[])) {
            System.err.printf("getServiceMessages strange return value: '%s'%n", o);
            return Collections.emptyList();
        }
        Object[] rval = (Object[]) o;
        if (rval == null || rval.length == 0) {
            return Collections.emptyList();
        }
        List<ServiceMessage> l = new ArrayList<>(rval.length);
        for (Object e: rval) {
            l.add(new DefaultServiceMessage((Object[])e));
        }
        return l;
    }
    
    @Override
    public void deleteDevice(String address, int flags) throws XmlRpcException {
        client.execute("deleteDevice", new Object[]{address, flags});
    }

    @Override
    public void abortDeleteDevice(String address) throws XmlRpcException {
        client.execute("abortDeleteDevice", new Object[]{address});
    }

    @Override
    @SuppressWarnings("unchecked")
    public XmlRpcStruct rssiInfo() throws XmlRpcException {
        return new MapXmlRpcStruct((Map<String, Object>)client.execute("rssiInfo", new Object[]{}));
    }
    
}
