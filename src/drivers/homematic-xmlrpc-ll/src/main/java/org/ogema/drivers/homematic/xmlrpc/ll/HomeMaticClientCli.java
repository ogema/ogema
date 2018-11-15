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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HomeMatic;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ServiceMessage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 *
 * @author jlapp
 */
public class HomeMaticClientCli {

    private final HomeMatic client;

    public HomeMaticClientCli(HomeMatic client) {
        this.client = client;
    }

    public ServiceRegistration<HomeMaticClientCli> register(BundleContext ctx, String commandScope) {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("osgi.command.scope", commandScope);
        props.put("osgi.command.function", new String[]{
            "list", "params", "tim", "read", "readValue", "valueUsage", "set",
        "addLink", "removeLink", "getLinkInfo", "getLinks",
        "deleteDevice", "abortDeleteDevice", "getServiceMessages", "client"});
        return ctx.registerService(HomeMaticClientCli.class, this, props);
    }
    
    public ServiceRegistration<HomeMaticClientCli> register(BundleContext ctx) {
        return register(ctx, "hm");
    }

    /**
     * Read and print the list of known devices along with their type and version.
     */
    public void list() throws Exception {
        List<DeviceDescription> l = client.listDevices();
        for (DeviceDescription dd : l) {
            System.out.printf("%s%s (v%s) @ %s%n", dd.isDevice() ? "" : "  ", dd.getType(), dd.getVersion(), dd.getAddress());
        }
    }

    /**
     * Read and print all available parameter descriptions from a device.
     * 
     * @param addr device address
     */
    public void params(String addr) throws Exception {
        DeviceDescription dd = client.getDeviceDescription(addr);
        for (String setName : dd.getParamsets()) {
            Map<String, ParameterDescription<?>> params = client.getParamsetDescription(addr, setName);
            for (Map.Entry<String, ParameterDescription<?>> entrySet : params.entrySet()) {
                String key = entrySet.getKey();
                ParameterDescription<?> value = entrySet.getValue();
                
                System.out.printf("[%s] %s = ", setName, key);
                printParameterDescription(value, System.out);
                System.out.printf("%n");                
            }
        }
    }
    
    private void printParameterDescription(ParameterDescription<?> desc, PrintStream out) {
        Map<String, Object> struct = desc.toMap();
        out.append("{");
        boolean firstEntry = true;
        for (Map.Entry<String, Object> e: struct.entrySet()) {
            if (firstEntry) {
                firstEntry = false;
            } else {
                out.append(", ");
            }
            out.append(e.getKey()).append("=");
            Object val = e.getValue();
            if (val.getClass().isArray()) {
                try {
                
                        out.append(Arrays.toString((Object[])val));
                } catch (StackOverflowError wtf) {
                    out.append("XXXXXXXXXXXXXXXX");
                }
            } else {
                out.append(val.toString());
            }
        }
        out.append("}");
    }
    
    /**
     * Read and print all available parameter sets from a device.
     * 
     * @param addr device address
     */
    public void read(String addr) throws Exception {
        DeviceDescription dd = client.getDeviceDescription(addr);
        for (String setName : dd.getParamsets()) {
            Map<String, Object> values = client.getParamset(addr, setName).toMap();
            List<String> keys = new ArrayList<>(values.keySet());
            Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);
            if (keys.isEmpty()) {
                System.out.printf("%s values: {}%n", setName);
            } else {
                System.out.printf("%s values: {%n", setName);
                for (String key: keys) {
                    System.out.printf("  %s=%s%n", key, values.get(key));
                }
                System.out.printf("}%n", setName);
            }
        }
    }
    
    /**
     * Toggle installation mode.
     */
    public void tim() throws Exception {
        int rem = client.getInstallMode();
        if (rem > 0) {
            client.setInstallMode(false, 0, 1);
        } else {
            client.setInstallMode(true, 900, 1);
        }
        rem = client.getInstallMode();
        if (rem == 0) {
            System.out.printf("install mode off%n");
        } else {
            System.out.printf("install mode on, time remaining = %ds%n", rem);
        }
    }
    
    public void valueUsage(String address, String valueId, int refCounter) throws Exception {
        System.out.println(client.reportValueUsage(address, valueId, refCounter));
    }
    
    public void set(String address, String valueId, Object value) throws Exception {
        client.setValue(address, valueId, value);
        System.out.printf("value=%s%n", String.valueOf(client.getValue(address, valueId)));
    }
    
    public void readValue(String address, String valueKey) throws Exception {
        System.out.println(client.<Object>getValue(address, valueKey));
    }
    
    public void addLink(String sender, String receiver, String name, String description) throws Exception {
        client.addLink(sender, receiver, name, description);
    }
    
    public void removeLink(String sender, String receiver) throws Exception {
        client.removeLink(sender, receiver);
    }
    
    public Map<String, Object> getLinkInfo(String sender, String receiver) throws Exception {
        return client.getLinkInfo(sender, receiver);
    }
    
    public void getLinks(String address, int flags) throws Exception {
        System.out.println(client.getLinks(address, flags));
    }
    
    public void deleteDevice(String address, int flags) throws Exception {
        client.deleteDevice(address, flags);
    }
    
    public void abortDeleteDevice(String address) throws Exception {
        client.abortDeleteDevice(address);
    }
    
    public void getServiceMessages() throws Exception {
        for (ServiceMessage msg: client.getServiceMessages()) {
            System.out.println(msg);
        }
    }
    
    public HomeMatic client(String none) {
        return client;
    }
    
}
