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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HomeMatic;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
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

    public ServiceRegistration<HomeMaticClientCli> register(BundleContext ctx) {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("osgi.command.scope", "hm");
        props.put("osgi.command.function", new String[]{"list", "params", "tim", "read", "readValue", "valueUsage", "set"});
        return ctx.registerService(HomeMaticClientCli.class, this, props);
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
                System.out.printf("[%s] %s = %s%n", setName, key, value);
            }
        }
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
            System.out.printf("%s values: %s%n", setName, values);
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
    
}
