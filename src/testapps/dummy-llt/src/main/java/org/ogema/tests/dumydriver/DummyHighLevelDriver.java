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
/**
 * 
 */
package org.ogema.tests.dumydriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfigurationException;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.model.locations.Room;

@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class DummyHighLevelDriver implements Application, ChannelEventListener, ResourceValueListener<TimeResource> {
	private ApplicationManager appManager;
	private OgemaLogger logger;
	private ChannelAccess channelAccess;

	int chCounter;
	Random rnd;
	private ResourceAccess resAcc;
	private ResourceManagement resMan;

	ResourceList<Room> roomsList;
	private HashMap<Integer, ChannelLocator> channels;

	HashMap<String, Integer> chByWrite;
	HashMap<Integer, Room> roomsByChInfo;

	@Override
	public void start(ApplicationManager appManager) {
		this.appManager = appManager;
		this.logger = appManager.getLogger();
		this.channelAccess = appManager.getChannelAccess();

		this.resAcc = appManager.getResourceAccess();
		this.resMan = appManager.getResourceManagement();

		channels = new HashMap<Integer, ChannelLocator>();
		chByWrite = new HashMap<String, Integer>();
		roomsByChInfo = new HashMap<Integer, Room>();

		rnd = new Random();
		initResources();
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					System.out.println("Create channel number " + chCounter);
					createChannel();
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}

	private void initResources() {
		roomsList = resAcc.getResource("MyRooms");
		if (roomsList == null) {
			roomsList = resMan.createResource("MyRooms", ResourceList.class);
			roomsList.setElementType(Room.class);
			roomsList.activate(false);
		}
	}

	@Override
	public void stop(AppStopReason reason) {
	}

	@Override
	public void channelEvent(EventType type, List<SampledValueContainer> channels) {
		for (SampledValueContainer container : channels) {
			ChannelLocator channelLocator = container.getChannelLocator();
			String chAddr = channelLocator.getChannelAddress();
			int chID = Integer.parseInt(chAddr);
			Room room = roomsByChInfo.get(chID);
			if (room == null)
				return;
			SampledValue val = container.getSampledValue();
			long value = val.getValue().getLongValue();
			TimeResource time = room.getSubResource("read");
			time.setValue(value);
		}
	}

	private void createChannel() {

		List<ChannelLocator> list = new ArrayList<ChannelLocator>();
		DeviceLocator deviceLocator;
		int devAddr = (chCounter >> 4);
		int ifAddr = (devAddr >> 4);

		String interfaceId = ifAddr + "";
		String deviceAddress = +devAddr + "";
		String deviceParameters = "";

		int chID = ifAddr << 24 | devAddr << 16 | chCounter;

		deviceLocator = channelAccess.getDeviceLocator(DummyDriver.DRIVER_ID, interfaceId, deviceAddress,
				deviceParameters);

		ChannelLocator channelLocator = channelAccess.getChannelLocator(chCounter + "", deviceLocator);
		list.add(channelLocator);

		// create channel
		try {
			int samplingRate = rnd.nextInt(countOfPrimes - 1);
			ChannelConfiguration chConf = channelAccess.getChannelConfiguration(channelLocator);
			chConf.setSamplingPeriod(samplingRate);
			channelAccess.addChannel(chConf);
			channelAccess.registerUpdateListener(list, this);
		} catch (ChannelConfigurationException e) {
			logger.error(null, e);
			chCounter--;
		}
		channels.put(chID, channelLocator);
		createResource(chID);
		chCounter++;
	}

	private void createResource(int i) {
		Room res = roomsList.add();
		res.activate(true);
		TimeResource write = res.addDecorator("write", TimeResource.class);
		TimeResource read = res.addDecorator("read", TimeResource.class);
		res.addDecorator("rJitter", IntegerResource.class);
		res.addDecorator("wJitter", IntegerResource.class);
		write.addValueListener(this);
		chByWrite.put(write.getName(), i);
		roomsByChInfo.put(i, res);
	}

	private ChannelLocator createChannelLocator(int chAddr) {

		DeviceLocator deviceLocator;
		int devAddr = (chAddr >> 4);
		int ifAddr = (devAddr >> 4);

		String interfaceId = ifAddr + "";
		String deviceAddress = +devAddr + "";
		String deviceParameters = "";

		deviceLocator = channelAccess.getDeviceLocator(DummyDriver.DRIVER_ID, interfaceId, deviceAddress,
				deviceParameters);

		return channelAccess.getChannelLocator(chAddr + "", deviceLocator);
	}

	static final int[] primes = { 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139,
			149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257,
			263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383,
			389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509,
			521, 523, 541, 547, 557, 563, 569, 571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647,
			653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773, 787, 797,
			809, 811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911, 919, 929, 937, 941,
			947, 953, 967, 971, 977, 983, 991, 997, 1009, 1013, 1019, 1021, 1031, 1033, 1039, 1049, 1051, 1061, 1063,
			1069, 1087, 1091, 1093, 1097, 1103, 1109, 1117, 1123, 1129, 1151, 1153, 1163, 1171, 1181, 1187, 1193, 1201,
			1213, 1217, 1223, 1229, 1231, 1237, 1249, 1259, 1277, 1279, 1283, 1289, 1291, 1297, 1301, 1303, 1307, 1319,
			1321, 1327, 1361, 1367, 1373, 1381, 1399, 1409, 1423, 1427, 1429, 1433, 1439, 1447, 1451, 1453, 1459, 1471,
			1481, 1483, 1487, 1489, 1493, 1499, 1511, 1523, 1531, 1543, 1549, 1553, 1559, 1567, 1571, 1579, 1583, 1597,
			1601, 1607, 1609, 1613, 1619, 1621, 1627, 1637, 1657, 1663, 1667, 1669, 1693, 1697, 1699, 1709, 1721, 1723,
			1733, 1741, 1747, 1753, 1759, 1777, 1783, 1787, 1789, 1801, 1811, 1823, 1831, 1847, 1861, 1867, 1871, 1873,
			1877, 1879, 1889, 1901, 1907, 1913, 1931, 1933, 1949, 1951, 1973, 1979, 1987, 1993, 1997, 1999, 2003, 2011,
			2017, 2027, 2029 };
	static final int countOfPrimes = primes.length;

	@Override
	public void resourceChanged(TimeResource resource) {
		String name = resource.getName();
		int ch = chByWrite.get(name);
		int ifAddr = ch & 0xFF000000;
		int devAddr = ch & 0x00FF0000;
		int chCounter = ch & 0x0000FFFF;
		ChannelLocator locator = channels.get(ch);
		try {
			channelAccess.setChannelValue(locator, new LongValue(System.currentTimeMillis()));
		} catch (ChannelAccessException e) {
			e.printStackTrace();
		}
	}
}
