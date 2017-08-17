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
package org.ogema.tests.dumydriver.hld;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.ObjectValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.locations.Room;
import org.ogema.tests.dumydriver.lld.DummyDriver;
import org.osgi.framework.BundleContext;

@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class DummyHighLevelDriver implements Application {
	private static final int DEFAULT_SAMPLING_PERIOD = 1000;
	private OgemaLogger logger;
	private ChannelAccess channelAccess;
	HashMap<String, WriteTimeListener> listeners;
	private ConcurrentHashMap<String, ChannelConfiguration> writeChannels;
	int chCounter;
	Random rnd;
	private ResourceAccess resAcc;
	private ResourceManagement resMan;

	ResourceList<Room> roomsList;

	HashMap<String, Integer> chByWrite;
	HashMap<Integer, Room> roomsByChInfo;
	private boolean running;
	private int maxChannels;
	int readSamplingRate;
	private int channelCreationPeriod;
	private int loggingChannelCount;

	@Activate
	public void activate(final BundleContext context, Map<String, Object> config) throws Exception {
		new ShellCommands(context, this);
		listeners = new HashMap<>();
		writeChannels = new ConcurrentHashMap<>();
		// sampling rate configuration for synchronous read channels
		try {
			String tmp = context.getProperty("org.ogema.perf.measure.readsamplingrate");
			if (tmp != null)
				this.readSamplingRate = Integer.valueOf(tmp);
			else
				this.readSamplingRate = DEFAULT_SAMPLING_PERIOD;
		} catch (NumberFormatException e) {
			this.readSamplingRate = DEFAULT_SAMPLING_PERIOD;
		} catch (AccessControlException e) {
			e.printStackTrace();
			this.readSamplingRate = DEFAULT_SAMPLING_PERIOD;
		}

		// period of new channel creation
		try {
			String tmp = context.getProperty("org.ogema.perf.measure.newchannelperiod");
			if (tmp != null) {
				this.channelCreationPeriod = Integer.valueOf(tmp);
			}
			else {
				this.channelCreationPeriod = 600000;
			}
		} catch (NumberFormatException e) {
			this.channelCreationPeriod = 600000;
		} catch (AccessControlException e) {
			e.printStackTrace();
			this.channelCreationPeriod = 600000;
		}
		// number channel creation cycles where the resources are configured for logging
		try {
			String tmp = context.getProperty("org.ogema.perf.measure.loggingchannelscount");
			if (tmp != null) {
				this.loggingChannelCount = Integer.valueOf(tmp);
			}
			else {
				this.loggingChannelCount = 1;
			}
		} catch (NumberFormatException e) {
			this.loggingChannelCount = 1;
		} catch (AccessControlException e) {
			e.printStackTrace();
			this.loggingChannelCount = 1;
		}
		// number of maximum channel tuples created
		try {
			String tmp = context.getProperty("org.ogema.perf.measure.maxchannels");
			if (tmp != null) {
				this.maxChannels = Integer.valueOf(tmp);
			}
			else {
				this.maxChannels = Integer.MAX_VALUE;
			}
		} catch (NumberFormatException e) {
			this.maxChannels = Integer.MAX_VALUE;
		} catch (AccessControlException e) {
			e.printStackTrace();
			this.maxChannels = Integer.MAX_VALUE;
		}
	}

	@Override
	public void start(ApplicationManager appManager) {
		this.logger = appManager.getLogger();
		
		logger.info(String.format("Read sampling period: %d", this.readSamplingRate));
		logger.info(String.format("New channel period: %d", this.channelCreationPeriod));
		logger.info(String.format("Number of logging channels : %d", this.loggingChannelCount));
		logger.info(String.format("Number of maximum channels : %d", this.maxChannels));

		this.channelAccess = appManager.getChannelAccess();

		this.resAcc = appManager.getResourceAccess();
		this.resMan = appManager.getResourceManagement();

		chByWrite = new HashMap<String, Integer>();
		roomsByChInfo = new HashMap<Integer, Room>();

		rnd = new Random();
		initResources();
		running = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (running) {
					if (chCounter >= maxChannels) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}
					else {
						System.out.println("Create channel number " + chCounter);

						Room r = createResource(chCounter);

						try {
							createChannel(chCounter++, r);
						} catch (ChannelAccessException e) {
							e.printStackTrace();
						}
						try {
							Thread.sleep(channelCreationPeriod);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}).start();

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				running = true;
				while (running) {
					Set<Entry<String, ChannelConfiguration>> entries = writeChannels.entrySet();
					for (Entry<String, ChannelConfiguration> e : entries) {
						ChannelConfiguration chConf = e.getValue();
						long TS1 = System.currentTimeMillis();
						TimeStampContainer tsc = new TimeStampContainer();
						tsc.TS1 = TS1;
						try {
							channelAccess.setChannelValue(chConf, new ObjectValue(tsc));
						} catch (ChannelAccessException e1) {
							running = false;
							break;
						}
					}
					Thread.yield();
				}
			}
		});
		t.setName("Dummy-Driver-Writer-Thread");
		t.start();
	}

	private void initResources() {
		roomsList = resAcc.getResource("MyRooms");
		if (roomsList == null) {
			roomsList = resMan.createResource("MyRooms", ResourceList.class);
			roomsList.setElementType(Room.class);
			roomsList.activate(false);
		}
		List<Room> rooms = roomsList.getAllElements();
		int i = 0;
		for (Room r : rooms) {
			initResource(r, i);
			try {
				createChannel(i, r);
			} catch (ChannelAccessException e) {

				// create channel failed, because the driver wasn't ready yet.
				// Wait a little and try again
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
				try {
					createChannel(i, r);
				} catch (ChannelAccessException e1) {
					// it had have got a fair chance
				}
			}
			i++;
		}
		chCounter = i;
		if (maxChannels < chCounter)
			maxChannels = chCounter;
	}

	@Override
	public void stop(AppStopReason reason) {
		running = false;
		Set<Entry<String, WriteTimeListener>> entries = listeners.entrySet();
		for (Entry<String, WriteTimeListener> e : entries) {
			((ThreadControl) e.getValue()).stop();
		}
	}

	private void createChannel(int i, Room r) throws ChannelAccessException {

		List<ChannelConfiguration> list = new ArrayList<ChannelConfiguration>();
		DeviceLocator deviceLocator;
		int devAddr = (i >> 4);
		int ifAddr = (devAddr >> 4);

		String interfaceId = ifAddr + "";
		String deviceAddress = +devAddr + "";
		String deviceParameters = "";

		// int chID = ifAddr << 24 | devAddr << 16 | i;

		deviceLocator = new DeviceLocator(DummyDriver.DRIVER_ID, interfaceId, deviceAddress, deviceParameters);

		// create read channel
		ChannelLocator channelLocator = new ChannelLocator(i + ":read", deviceLocator);

		int samplingRate;

		if (readSamplingRate == -1) {
			samplingRate = rnd.nextInt(countOfPrimes - 1);
			samplingRate = primes[samplingRate];
		}
		else
			samplingRate = readSamplingRate;
		System.out.println("Read channel sampling rate in ms: " + samplingRate);

		ChannelConfiguration chConf = channelAccess.addChannel(channelLocator, Direction.DIRECTION_INPUT, samplingRate);
		ChannelEventListener listener = new ReadChannelListener(r, samplingRate, System.currentTimeMillis(), logger,
				channelLocator);
		list.add(chConf);
		channelAccess.registerUpdateListener(list, listener);

		// create write channel
		list.clear();
		channelLocator = new ChannelLocator(i + ":write", deviceLocator);

		chConf = channelAccess.addChannel(channelLocator, Direction.DIRECTION_INOUT,
				ChannelConfiguration.LISTEN_FOR_UPDATE);
		writeChannels.put(chConf.getChannelLocator().getChannelAddress(), chConf);
		list.add(chConf);
		WriteTimeListener wtl;// = listeners.get(devAddr);
		// if (wtl == null) {
		wtl = new WriteTimeListener(r, channelAccess, logger, this, chConf);
		listeners.put(channelLocator.getChannelAddress(), wtl);
		channelAccess.registerUpdateListener(list, wtl);
		// }
		// wtl.addChannel(channelLocator.getChannelAddress(), chConf);
	}

	private Room createResource(int i) {
		Room res = roomsList.add();
		res.activate(true);
		TimeResource write = res.addDecorator("write", TimeResource.class);
		TimeResource read = res.addDecorator("read", TimeResource.class);
		IntegerResource wJitter = write.addDecorator("jitter", IntegerResource.class);
		IntegerResource rJitter = read.addDecorator("jitter", IntegerResource.class);
		IntegerResource loop = write.addDecorator("loopTime", IntegerResource.class);
		IntegerResource ack = write.addDecorator("lldAck", IntegerResource.class);
		write.addDecorator("ts", TimeResource.class);
		write.activate(true);
		read.activate(true);

		if (this.loggingChannelCount > 0) {
			configureLogging(read);
			configureLogging(rJitter);
			configureLogging(write);
			configureLogging(wJitter);
			configureLogging(loop);
			configureLogging(ack);
			this.loggingChannelCount--;
		}
		// write.addValueListener(this);
		chByWrite.put(write.getName(), i);
		roomsByChInfo.put(i, res);
		return res;
	}

	private void initResource(Room r, int i) {
		TimeResource write = r.getSubResource("write");
		// write.addValueListener(this);
		chByWrite.put(write.getName(), i);
		roomsByChInfo.put(i, r);
		chCounter = i;
	}

	private ChannelLocator createChannelLocator(int chAddr) {

		DeviceLocator deviceLocator;
		int devAddr = (chAddr >> 4);
		int ifAddr = (devAddr >> 4);

		String interfaceId = ifAddr + "";
		String deviceAddress = +devAddr + "";
		String deviceParameters = "";

		deviceLocator = new DeviceLocator(DummyDriver.DRIVER_ID, interfaceId, deviceAddress, deviceParameters);

		return new ChannelLocator(chAddr + "", deviceLocator);
	}

	static final int[] primes = { 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139,
			149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257,
			263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383,
			389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509,
			521, 523, 541, 547, 557, 563, 569, 571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647,
			653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773, 787, 797,
			809, 811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911, 919, 929, 937, 941,
			947, 953, 967, 971, 977, 983, 991,
			997/*
				 * , 1009, 1013, 1019, 1021, 1031, 1033, 1039, 1049, 1051, 1061, 1063, 1069, 1087, 1091, 1093, 1097,
				 * 1103, 1109, 1117, 1123, 1129, 1151, 1153, 1163, 1171, 1181, 1187, 1193, 1201, 1213, 1217, 1223, 1229,
				 * 1231, 1237, 1249, 1259, 1277, 1279, 1283, 1289, 1291, 1297, 1301, 1303, 1307, 1319, 1321, 1327, 1361,
				 * 1367, 1373, 1381, 1399, 1409, 1423, 1427, 1429, 1433, 1439, 1447, 1451, 1453, 1459, 1471, 1481, 1483,
				 * 1487, 1489, 1493, 1499, 1511, 1523, 1531, 1543, 1549, 1553, 1559, 1567, 1571, 1579, 1583, 1597, 1601,
				 * 1607, 1609, 1613, 1619, 1621, 1627, 1637, 1657, 1663, 1667, 1669, 1693, 1697, 1699, 1709, 1721, 1723,
				 * 1733, 1741, 1747, 1753, 1759, 1777, 1783, 1787, 1789, 1801, 1811, 1823, 1831, 1847, 1861, 1867, 1871,
				 * 1873, 1877, 1879, 1889, 1901, 1907, 1913, 1931, 1933, 1949, 1951, 1973, 1979, 1987, 1993, 1997, 1999,
				 * 2003, 2011, 2017, 2027, 2029
				 */ };
	static final int countOfPrimes = primes.length;

	public String step(int add) {
		maxChannels += add;
		return "New channel number is " + maxChannels;
	}

	public void configureLogging(TimeResource time) {
		// configure meter for logging once per minute
		final RecordedDataConfiguration config = new RecordedDataConfiguration();
		config.setStorageType(StorageType.ON_VALUE_UPDATE);
		// meterConfig.setFixedInterval(3 * 1000l);
		time.getHistoricalData().setConfiguration(config);
	}

	public void configureLogging(IntegerResource res) {
		// configure meter for logging once per minute
		final RecordedDataConfiguration config = new RecordedDataConfiguration();
		config.setStorageType(StorageType.ON_VALUE_UPDATE);
		// meterConfig.setFixedInterval(3 * 1000l);
		res.getHistoricalData().setConfiguration(config);
	}

}
