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
package org.ogema.driver.knxdriver;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import org.ogema.model.actors.OnOffSwitch;

import org.ogema.model.devices.buildingtechnology.ElectricDimmer;
import org.ogema.model.devices.connectiondevices.ThermalValve;
import org.ogema.model.sensors.ElectricPowerSensor;
import org.ogema.model.sensors.LightSensor;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.OccupancySensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.sensors.TouchSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.knxnetip.Discoverer;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.process.ProcessCommunicator;

public class KNXUtils {

    static final Logger LOGGER = LoggerFactory.getLogger(KNXUtils.class);
    //timeout for KNX interface search
    static final int SEARCH_TIMEOUT = 3;

    String getDpt(final ConnectionInfo conInfo) {

        String dpStr = null;

        if (conInfo.getType().equals(LightSensor.class.getSimpleName())) {
            dpStr = "9.004";
        } else if (conInfo.getType().equals(MotionSensor.class.getSimpleName())) {

            dpStr = "1.011";
        } else if (conInfo.getType().equals(OccupancySensor.class.getSimpleName())) {

            dpStr = "1.011";
        } else if (conInfo.getType().equals(TouchSensor.class.getSimpleName())) {

            dpStr = "1.011";
        }

        if (conInfo.getType().equals(TemperatureSensor.class.getSimpleName())) {

            dpStr = "9.001";
        }

        if (conInfo.getType().equals(ElectricPowerSensor.class.getSimpleName())) {

            dpStr = "12.001";
        }

        if (conInfo.getType().equals(OnOffSwitch.class.getSimpleName())) {

            dpStr = "1.001";

        }

        if (conInfo.getType().equals(ElectricDimmer.class.getSimpleName())) {

            dpStr = "5.001";

        }

        if (conInfo.getType().equals(ThermalValve.class.getSimpleName())) {

            dpStr = "9.001";

        }

        return dpStr;
    }

    int[] convertPhysicalAddress(String phyaddress) {
        String[] phyAddressArray = phyaddress.split("\\.");

        int[] convertedAddress = new int[3];

        int i = 0;

        for (String value : phyAddressArray) {
            convertedAddress[i] = Integer.parseInt(value);
            i++;
        }
        return convertedAddress;

    }

    int[] convertGroupAddress(String groupAddress) {
        String[] grAddressArray = groupAddress.split("/");

        int[] convertedAddress = new int[3];

        int i = 0;

        for (String value : grAddressArray) {
            convertedAddress[i] = Integer.parseInt(value);
            i++;
        }

        return convertedAddress;

    }

    ConnectionInfo createConnectionInfo(final String connectionString) {
        String[] connectionInfo = connectionString.split(",");

        String deviceType = connectionInfo[0];

        String name = connectionInfo[1];

        String gwAddress = connectionInfo[2];

        String groupAddress = connectionInfo[3];

        String physicalAddress = connectionInfo[4];

        int timeInterval = Integer.parseInt(connectionInfo[5]);

        String interAddress = connectionInfo[6];

        final ConnectionInfo conInfo = new ConnectionInfo(interAddress, gwAddress, groupAddress, physicalAddress, name,
                deviceType, timeInterval);

        if (conInfo.getType().equals(LightSensor.class.getSimpleName())
                || conInfo.getType().equals(OccupancySensor.class.getSimpleName())
                || conInfo.getType().equals(TemperatureSensor.class.getSimpleName())
                || conInfo.getType().equals(MotionSensor.class.getSimpleName())
                || conInfo.getType().equals(ElectricPowerSensor.class.getSimpleName())
                || conInfo.getType().equals(TouchSensor.class.getSimpleName())) {
            conInfo.setSensor(true);
        } else {
            conInfo.setSensor(false);
        }

        return conInfo;
    }

    static class SearchResult {

        final NetworkInterface iface;
        final InetAddress address;
        final List<SearchResponse> responses;

        public SearchResult(NetworkInterface iface, InetAddress address, List<SearchResponse> responses) {
            this.iface = iface;
            this.address = address;
            this.responses = responses;
        }

        @Override
        public String toString() {
            return String.format("%s/%s: %s", iface, address, responses);
        }
        
    }
    
    Collection<SearchResult> searchKnxInterfaces() {
        return AccessController.doPrivileged(new PrivilegedAction<Collection<SearchResult>>() {
            @Override
            public Collection<SearchResult> run() {
                try {
                    return searchAll();
                } catch (SocketException e) {
                    LOGGER.error("error while searching for network interfaces", e);
                    return Collections.EMPTY_LIST;
                }
            }
        });
    }

    private Collection<SearchResult> searchAll() throws SocketException {
        Collection<SearchResult> result = new ConcurrentLinkedQueue<>();
        List<NetworkInterface> allInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        List<NetworkInterface> activeInterfaces = new ArrayList<>();
        for (NetworkInterface iface: allInterfaces) {
            if (!iface.isLoopback() && iface.isUp()) {
                activeInterfaces.add(iface);
            }
        }
        for (NetworkInterface iface: activeInterfaces) {
            for (InetAddress addr: Collections.list(iface.getInetAddresses())) {
                if (!(addr instanceof Inet4Address)) {
                    continue;
                }
                Discoverer d;
                try {
                    d = new Discoverer(addr, 0, false, false);
                    d.startSearch(0, iface, SEARCH_TIMEOUT, true);
                    SearchResponse[] responses = d.getSearchResponses();
                    SearchResult sr = new SearchResult(iface, addr, Arrays.asList(responses));
                    result.add(sr);
                    LOGGER.debug(sr.toString());
                } catch (InterruptedException | KNXException ex) {
                    LOGGER.warn("error searching for KNX/IP interface on network {}", iface, ex);
                }
            }
        }
        return result;
    }

    public Map<String, String> searchInterface() throws PrivilegedActionException {
        final Map<String, String> allInterface = new HashMap<>();
        AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception {
                Collection<SearchResult> results = searchAll();
                for (SearchResult r : results) {
                    for (SearchResponse resp : r.responses) {
                        String key = r.iface.getDisplayName() + " connect with " + resp.getDevice().getName()
                                + " (" + resp.getControlEndpoint().getAddress().getHostAddress() + "):"
                                + String.valueOf(resp.getControlEndpoint().getPort());
                        String value = r.address.getHostAddress() + "#"
                                + resp.getControlEndpoint().getAddress().getHostAddress() + ":"
                                + String.valueOf(resp.getControlEndpoint().getPort());
                        allInterface.put(key, value);
                    }
                }
                return null;
            }
        });
        return allInterface;
    }

    public Map<String, String> searchInterfaceOLD() throws PrivilegedActionException {
        Enumeration<NetworkInterface> en;
        Map<String, String> allInterface = new HashMap<>();
        try {
            en = getNetworkInterfacesPrivileged();
            while (en.hasMoreElements()) {
                NetworkInterface in = en.nextElement();
                if (!in.isLoopback()) {
                    Enumeration<InetAddress> allAdress = getInetAddressesPrivileged(in);
                    while (allAdress.hasMoreElements()) {
                        InetAddress tmp = allAdress.nextElement();
                        if (Pattern.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+", tmp.getHostAddress())) {
                            SearchResponse[] tmp2 = null;
                            try {
                                tmp2 = searchPrivileged(in, tmp);
                                for (SearchResponse resp : tmp2) {
                                    String key = in.getDisplayName() + " connect with " + resp.getDevice().getName()
                                            + " (" + resp.getControlEndpoint().getAddress().getHostAddress() + "):"
                                            + String.valueOf(resp.getControlEndpoint().getPort());
                                    String value = tmp.getHostAddress() + "#"
                                            + resp.getControlEndpoint().getAddress().getHostAddress() + ":"
                                            + String.valueOf(resp.getControlEndpoint().getPort());
                                    allInterface.put(key, value);
                                }
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                // e.printStackTrace();
                            }
                        }
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return allInterface;
    }

    private SearchResponse[] searchPrivileged(final NetworkInterface in, final InetAddress tmp)
            throws PrivilegedActionException {
        return AccessController.doPrivileged(new PrivilegedExceptionAction<SearchResponse[]>() {
            @Override
            public SearchResponse[] run() throws Exception {
                return search(in, tmp);
            }
        });
    }

    private Enumeration<InetAddress> getInetAddressesPrivileged(final NetworkInterface in)
            throws PrivilegedActionException {
        return AccessController.doPrivileged(new PrivilegedExceptionAction<Enumeration<InetAddress>>() {
            @Override
            public Enumeration<InetAddress> run() throws SocketException {
                return in.getInetAddresses();
            }
        });
    }

    private Enumeration<NetworkInterface> getNetworkInterfacesPrivileged() throws PrivilegedActionException {
        return AccessController.doPrivileged(new PrivilegedExceptionAction<Enumeration<NetworkInterface>>() {
            @Override
            public Enumeration<NetworkInterface> run() throws SocketException {
                return NetworkInterface.getNetworkInterfaces();
            }
        });
    }

    private static SearchResponse[] search(NetworkInterface in, InetAddress iA) throws Exception {
        Discoverer d = new Discoverer(iA, 0, false, false);
        try {
            d.startSearch(0, in, 5, true);
        } catch (InterruptedException | KNXException ex) {
            LOGGER.warn("error searching for KNX/IP interface on network {}", in, ex);
        }
        return d.getSearchResponses();
    }

    KNXNetworkLinkIP getNetLinkIpPrivileged(final String localAddress, final String address, final int port)
            throws PrivilegedActionException {
        return AccessController.doPrivileged(new PrivilegedExceptionAction<KNXNetworkLinkIP>() {

            @Override
            public KNXNetworkLinkIP run() throws UnknownHostException, KNXException, InterruptedException {
                return new KNXNetworkLinkIP(KNXNetworkLinkIP.TUNNELING, new InetSocketAddress(InetAddress
                        .getByName(localAddress), 0), new InetSocketAddress(InetAddress.getByName(address),
                        port), false, new TPSettings());
            }
        });
    }

    public void readAndDetachPrivileged(final ProcessCommunicator pc, final Datapoint dp)
            throws PrivilegedActionException {
        AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

            @Override
            public Void run() throws KNXException, InterruptedException {
                pc.read(dp);
                pc.detach();
                return null;
            }
        });
    }

    public void writeAndDetachPrivileged(final ProcessCommunicator pc, final Datapoint dp, final String value)
            throws PrivilegedActionException {
        AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

            @Override
            public Void run() throws KNXException {
                pc.write(dp, value);
                pc.detach();
                return null;
            }
        });
    }

    public int getPort(final ConnectionInfo conInfo) {
        return Integer.parseInt(conInfo.getKnxRouter().substring(conInfo.getKnxRouter().indexOf(":") + 1,
                conInfo.getKnxRouter().length()));
    }

    public String getIPAddress(final ConnectionInfo conInfo) {
        return conInfo.getKnxRouter().substring(0, conInfo.getKnxRouter().indexOf(":"));
    }

}
