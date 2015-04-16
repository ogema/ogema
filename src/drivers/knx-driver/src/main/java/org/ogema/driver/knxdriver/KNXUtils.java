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
package org.ogema.driver.knxdriver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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

import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.knxnetip.Discoverer;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.process.ProcessCommunicator;

public class KNXUtils {

	String getDpt(final ConnectionInfo conInfo) {

		String dpStr = null;

		if (conInfo.getType().equals(LightSensor.class.getSimpleName())) {
			dpStr = "9.004";
		}

		else if (conInfo.getType().equals(MotionSensor.class.getSimpleName())) {

			dpStr = "1.011";
		}

		else if (conInfo.getType().equals(OccupancySensor.class.getSimpleName())) {

			dpStr = "1.011";
		}

		else if (conInfo.getType().equals(TouchSensor.class.getSimpleName())) {

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
		}
		else {
			conInfo.setSensor(false);
		}

		return conInfo;
	}

	public Map<String, String> searchInterface() throws PrivilegedActionException {

		Enumeration<NetworkInterface> en;
		Map<String, String> allInterface = new HashMap<String, String>();

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

								for (SearchResponse el : tmp2) {
									String key = in.getDisplayName() + " connect with " + el.getDevice().getName()
											+ " (" + el.getControlEndpoint().getAddress().getHostAddress() + "):"
											+ String.valueOf(el.getControlEndpoint().getPort());
									String value = tmp.getHostAddress() + "#"
											+ el.getControlEndpoint().getAddress().getHostAddress().toString() + ":"
											+ String.valueOf(el.getControlEndpoint().getPort());

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

		Discoverer d = new Discoverer(iA, 0, false);
		try {
			d.startSearch(12345, in, 5, false);
		} catch (Throwable ex) {
			// ex.printStackTrace();
			ex.printStackTrace();
		}
		// wait until search finished, and update console 4 times/second with
		// received search responses
		SearchResponse[] res = null;
		while (d.isSearching()) {
			res = d.getSearchResponses();
		}
		return res;
	}

	KNXNetworkLinkIP getNetLinkIpPrivileged(final ConnectionInfo conInfo, final String address, final int port)
			throws PrivilegedActionException {
		return AccessController.doPrivileged(new PrivilegedExceptionAction<KNXNetworkLinkIP>() {

			@Override
			public KNXNetworkLinkIP run() throws UnknownHostException, KNXException {
				return new KNXNetworkLinkIP(KNXNetworkLinkIP.TUNNEL, new InetSocketAddress(InetAddress
						.getByName(conInfo.getIntface()), 0), new InetSocketAddress(InetAddress.getByName(address),
						port), false, new TPSettings(false));
			}
		});
	}

	public void readAndDetachPrivileged(final ProcessCommunicator pc, final Datapoint dp)
			throws PrivilegedActionException {
		AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

			@Override
			public Void run() throws KNXException {
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
