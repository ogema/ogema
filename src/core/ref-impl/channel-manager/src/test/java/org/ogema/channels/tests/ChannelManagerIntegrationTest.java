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
package org.ogema.channels.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.junit.Ignore;
import org.junit.Test;
import org.ogema.channels.tests.utils.ConditionCheck;
import org.ogema.channels.tests.utils.RangeMatcher;
import org.ogema.channels.tests.utils.TestChannelScanListener;
import org.ogema.channels.tests.utils.TestDeviceScanListener;
import org.ogema.channels.tests.utils.TestDriver;
import org.ogema.channels.tests.utils.TestDriver.Device;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.NoSuchDriverException;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.NoSuchChannelException;
import org.ogema.core.channelmanager.driverspi.NoSuchDeviceException;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

// starting the container per method can cause a PermGen Out of Memory Error on the CI server... 
// hence we need to clean up by hand after each test 
@ExamReactorStrategy(PerClass.class) 
public class ChannelManagerIntegrationTest extends ChannelManagerTestBase  {

	private ServiceRegistration<ChannelDriver> mbusDriverService;
	private static final AtomicInteger counter = new AtomicInteger(0);

	@Test
	public void testDriverInstallation() throws InterruptedException {

		// Test installation of multiple drivers
		
		startDriverBundle(new TestDriver("modbus", "modbus test driver"));
		startDriverBundle(new TestDriver("mbus", "wired mbus test driver"));
		List<String> drivers = channelAccess.getDriverIds();
		assertEquals(2, drivers.size());
		assertTrue(driverIsListed(drivers, "modbus"));
		assertTrue(driverIsListed(drivers, "mbus"));
	}

	@Test(expected = ChannelAccessException.class)
	public void testRequestDeviceListFromUnknownDriver() throws ChannelAccessException, InterruptedException {
		
		// negative test if driver is not found
		
		startDriverBundle(new TestDriver("modbus", "modbus test driver"));
		channelAccess.discoverDevices("mbus", "/dev/ttyUSB0", null);
	}

	@Test
	public void testChannelAccessServiceLookup() {
		
		// test if the ChannelManager service is available
		
		ChannelAccess accessService = lookupService(ChannelAccess.class);
		assertNotNull(accessService);
		List<ChannelLocator> channels = accessService.getAllConfiguredChannels();
		assertNotNull(channels);
		assertEquals(0, channels.size());
	}

	@Test
	public void testChannelConfiguration() throws InterruptedException, ChannelAccessException {

		// Test if open channels are listed
		
		startDriverBundle(new TestDriver("modbus", "modbus test driver"));
		startMbus();
		
		List<ChannelConfiguration> configs = addChannelsToConfigurationForDevice1(channelAccess, 1000);
		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();
		assertEquals(configs.size(), channels.size());
		
		for (ChannelConfiguration config: configs) {
			ChannelLocator found = null;
			
			for(ChannelLocator locator : channels) {
				if (locator.equals(config.getChannelLocator()))
				{
					found = locator;
					break;
				}
			}
			assertNotNull(found);
		}
	}

	@Test
	public void testDeleteChannelFromConfiguration() throws ChannelAccessException {

		startMbus();

		List<ChannelConfiguration> channelConfigs =  addChannelsToConfigurationForDevice1(channelAccess, 1000);

		channelAccess.deleteChannel(channelConfigs.get(0));
		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();
		assertEquals(1, channels.size());
		ChannelLocator channel1 = channels.get(0);
		assertEquals("02/823b", channel1.getChannelAddress());
	}

//	@Test(expected = ChannelConfigurationException.class)
//	public void testDeletionOfUnconfiguredChannel() throws InterruptedException {
//		try {
//			startMbus();
//			DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
//			ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);
//			channelAccess.deleteChannel(channelLocator);
//		} finally {
//			stop(); 
//		}
//	}

	@Test
	public void testChannelDirectionConfiguration() throws ChannelAccessException {
		startMbus();
		List<ChannelConfiguration> cfs = addChannelsToConfigurationForDevice1(channelAccess, 1000);
//		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();
//		ChannelLocator channel1 = channels.get(0);
//		ChannelConfiguration channelConfig1 = channelAccess.getChannelConfiguration(channel1);
//		System.out.println(channelConfig1.getChannelLocator() + " direction: " + channelConfig1.getDirection());
		assertEquals(Direction.DIRECTION_INOUT, cfs.get(0).getDirection());
//		ChannelLocator channel2 = channels.get(1);
//		ChannelConfiguration channelConfig2 = channelAccess.getChannelConfiguration(channel2);
//		System.out.println(channelConfig2.getChannelLocator() + " direction: " + channelConfig2.getDirection());
		assertEquals(Direction.DIRECTION_INPUT, cfs.get(1).getDirection());
		stop(); 
	}

	@Test
	public void testAddChannelsTwice() throws ChannelAccessException {
		startModbus();
		startMbus();
		DeviceLocator deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB0", "p"+counter.getAndIncrement(), null);
		List<ChannelConfiguration> configs1 = configureChannelsForDevice(channelAccess, 1000, deviceLocator);
		List<ChannelConfiguration> configs2 = configureChannelsForDevice(channelAccess, 1000, deviceLocator);
		
		// Use of == is intentional; reopening the same channel returns the same configuration
		for(int i = 0; i < configs1.size(); i++) {
			assertTrue(configs1.get(i) == configs2.get(i));
		}
	}

	@Test
	public void testChannelRead1000ms() throws ChannelAccessException {

		startModbus();
		final TestDriver d = startMbus();

		addChannelsToConfigurationForDevice1(channelAccess, 1000);

		assertTrue(checkConditionWithTimeout(new ConditionCheck(d) {
			@Override
			public boolean check() {
				return (((TestDriver) testObject).channelAddedCalled == 2);
			}
		}, 500));

		assertTrue(checkConditionWithTimeout(new ConditionCheck(d) {
			@Override
			public boolean check() {
				return (((TestDriver) testObject).readChannelsCalled >= 1);
			}
		}, 2000));
		stop(); 

	}

	// this test is too unpredictable... relies on timing
	@Ignore 
	@Test
	public void testChannelRead100ms() throws InterruptedException, ChannelAccessException {

		final TestDriver d = startMbus();
		d.setExpectedChannelAddeds(2);
		
		addChannelsToConfigurationForDevice1(channelAccess, 100);
		d.assertChannelsAdded();
//		assertTrue(checkConditionWithTimeout(new ConditionCheck(this.testDriver) {
//			@Override
//			public boolean check() {
//				return (((TestDriver) testObject).channelAddedCalled == 2);
//			}
//		}, 500));
		assertThat(d.readChannelsCalled, RangeMatcher.inRange(9, 14));
		stop(); 
	}

	// Test deaktiviert weil er anscheinend auf CI-Server sparadisch fehlschlägt.
	@Ignore
	@Test
	public void testChannelReadMultipleDevices() throws InterruptedException, ChannelAccessException {

		final TestDriver d = startMbus();
		d.setExpectedChannelAddeds(4);
		
		addChannelsToConfigurationForDevice1(channelAccess, 100);
		addChannelsToConfigurationForDevice1(channelAccess, 100);
		d.assertChannelsAdded();

//		assertTrue(checkConditionWithTimeout(new ConditionCheck(this.testDriver) {
//			@Override
//			public boolean check() {
//				return (((TestDriver) testObject).channelAddedCalled == 4);
//			}
//		}, 500));

		assertTrue(checkConditionWithTimeout(new ConditionCheck(d) {
			@Override
			public boolean check() {
				return (((TestDriver) testObject).readChannelsCalled >= 20);
			}
		}, 1600));
		stop(); 
	}

	@Test
	public void testChannelAccess() throws ChannelAccessException,
			IllegalConversionException, InterruptedException {

		TestDriver d = startMbus();
		d.setExpectedChannelAddeds(4);
		List<ChannelConfiguration> channels = new ArrayList<>();
		channels.addAll(addChannelsToConfigurationForDevice1(channelAccess, 100));
		channels.addAll(addChannelsToConfigurationForDevice1(channelAccess, 100));
		d.assertChannelsAdded();
//		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(4, channels.size());

//		ChannelLocator channel1 = channels.get(0);
		d.setExpectedChannelRead(channels.get(0), 2);
		d.assertAllChannelsRead();
		SampledValue sampledValue = channelAccess.getChannelValue(channels.get(0));

		Value value = sampledValue.getValue();
		System.out.println("   Channel value " + value.getIntegerValue());
//		assertTrue(value.getIntegerValue() > 2 && value.getIntegerValue() < 6);
		stop(); 
	}

	@Test
	public void testReadSingleChannelWithMissingValue() throws ChannelAccessException {
		
		// Test initial value of channel without first update
		
		TestDriver d = startMbus();

		d.ignoreListenChannels = true;
		
		DeviceLocator deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
		ChannelLocator channelLocator = new ChannelLocator("1", deviceLocator);
		ChannelConfiguration chConf = channelAccess.addChannel(channelLocator,Direction.DIRECTION_INOUT, ChannelConfiguration.LISTEN_FOR_UPDATE);
		SampledValue value = channelAccess.getChannelValue(chConf);
		
		assertEquals(Quality.BAD, value.getQuality());
		assertEquals(null, value.getValue());
	}

	@Test
	public void testWriteSingleChannel() throws ChannelAccessException,
			IllegalConversionException, NoSuchDeviceException, NoSuchChannelException, InterruptedException {

		TestDriver d = startMbus();
		d.setExpectedChannelAddeds(2);
		List<ChannelConfiguration> channels = addChannelsToConfigurationForDevice1(channelAccess, 100);
		d.assertChannelsAdded();

//		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", address, null);
//
//		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		Value value = new LongValue(1012);
		ChannelConfiguration cc = channels.get(0);
		d.setExpectedChannelWrites(cc, 1);
		channelAccess.setChannelValue(cc, value);
		d.assertAllChannelsWritten();

		List<SampledValueContainer> containers = new ArrayList<>();
		containers.add(new SampledValueContainer(cc.getChannelLocator()));
		channelAccess.readUnconfiguredChannels(containers);
//		SampledValue sampledValue = channelAccess.getChannelValue(channelLocator);
		SampledValue sampledValue = containers.get(0).getSampledValue();
		assertEquals(1012, sampledValue.getValue().getIntegerValue());
		stop(); 

	}

	@Test(expected = ChannelAccessException.class)
	public void testWriteReadOnlyChannel() throws ChannelAccessException {
		try {
			startMbus();
	
			List<ChannelConfiguration> channels = addChannelsToConfigurationForDevice1(channelAccess, 100);
	
//			DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", address, null);
//			ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862b", deviceLocator);
	
			Value value = new IntegerValue(100);
	
			channelAccess.setChannelValue(channels.get(1), value);
		} finally {
			stop(); 
		}
	}

	@Test(expected = ChannelAccessException.class)
	public void testWriteChannelWithDriverNotAvailable() throws ChannelAccessException,
			NoSuchDeviceException, NoSuchChannelException {
		try {
			registerMbusTestDriver();
			List<ChannelConfiguration> channels = addChannelsToConfigurationForDevice1(channelAccess, 100);
			unregisterMbusTestDriver();
//			DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", address, null);
//			ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);
			Value value = new LongValue(1012);
			channelAccess.setChannelValue(channels.get(0), value);
		} finally {
			stop(); 
		}
	}

	@Test
	public void testDiscoverDevices() throws UnsupportedOperationException, NoSuchInterfaceException,
			NoSuchDriverException, IOException, InterruptedException, ChannelAccessException {

		final TestDriver d = startMbus();

		setupTestDevices(d);

		List<DeviceLocator> devices = channelAccess.discoverDevices("mbus", "/dev/ttyUSB0", null);

		assertNotNull(devices);

		assertEquals(3, devices.size());

		devices = channelAccess.discoverDevices("mbus", "/dev/ttyUSB1", null);

		assertEquals(1, devices.size());
		stop(); 
	}

	@Test
	public void testDiscoverDevicesAsnyc() throws UnsupportedOperationException, NoSuchInterfaceException,
			NoSuchDriverException, IOException, InterruptedException, ChannelAccessException {

		final TestDriver d = startMbus();

		setupTestDevices(d);

		final TestDeviceScanListener deviceScanListener = new TestDeviceScanListener();

		channelAccess.discoverDevices("mbus", "/dev/ttyUSB0", null, deviceScanListener);

		assertTrue(checkConditionWithTimeout(new ConditionCheck(deviceScanListener) {
			@Override
			public boolean check() {
				return deviceScanListener.finished;
			}
		}, 500));

		assertEquals(3, deviceScanListener.foundDevices.size());
		assertEquals(1.f, deviceScanListener.ratio, 0.001);
	}

	@Test(expected = ChannelAccessException.class)
	public void testDiscoverDevicesNoSuchDriver() throws ChannelAccessException {
		try {
			channelAccess.discoverDevices("mbus", "/dev/ttyUSB0", null);
		} finally{ 
			stop(); 
		}
	}

	@Test(expected = ChannelAccessException.class)
	public void testDiscoverDevicesOperationNotSupported() throws ChannelAccessException {
		try {
			startMbus();
			channelAccess.discoverDevices("mbus", "/dev/ttyUSB0", null);
		} finally {
			stop(); 
		}
	}

	@Test(expected = ChannelAccessException.class)
	public void testDiscoverDevicesUnknownInterface() throws ChannelAccessException {
		try {
			final TestDriver d = startMbus();
			setupTestDevices(d);
			channelAccess.discoverDevices("mbus", "/dev/ttyUSB2", null);
		} finally {
			stop(); 
		}
	}

	//Anfang Clara

	@Test
	public void testsetMultipleChannelValues() throws ChannelAccessException, IllegalConversionException, InterruptedException {

		TestDriver d = startMbus();

		List<ChannelConfiguration> channelLocators = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100);

		List<Value> values = new LinkedList<Value>();

		values.add(new LongValue(1012));
		values.add(new LongValue(700));
		d.setExpectedChannelWrites(channelLocators.get(0), 1);
		d.setExpectedChannelWrites(channelLocators.get(1), 1);
		channelAccess.setMultipleChannelValues(channelLocators, values);
		d.assertAllChannelsWritten();
		
		List<SampledValueContainer> list = new ArrayList<>();
		list.add(new SampledValueContainer(channelLocators.get(0).getChannelLocator()));
		list.add(new SampledValueContainer(channelLocators.get(1).getChannelLocator()));
		channelAccess.readUnconfiguredChannels(list);
		SampledValue sampledValue = list.get(0).getSampledValue();
		
//		SampledValue sampledValue = channelAccess.getChannelValue(channelLocators.get(0)); // FIXME why does this not work? Should it?

		assertEquals(1012, sampledValue.getValue().getIntegerValue());
		sampledValue= list.get(1).getSampledValue();
//		sampledValue = channelAccess.getChannelValue(channelLocators.get(1));

		assertEquals(700, sampledValue.getValue().getIntegerValue());
		stop(); 
	}

	@Test(expected = IllegalArgumentException.class)
	public void testsetMultipleChannelValuesMissingValues() throws ChannelAccessException,
			IllegalConversionException, NoSuchDeviceException, NoSuchChannelException, InterruptedException {
		try {
		startMbus();

		List<ChannelConfiguration> channelLocators = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100);

		List<Value> values = new LinkedList<Value>();

		values.add(new LongValue(1012));

		channelAccess.setMultipleChannelValues(channelLocators, values);
		} finally {
			stop(); 
		}
	}

	@Test(expected = NullPointerException.class)
	public void testNullsetMultipleChannelValues() throws ChannelAccessException, NoSuchDeviceException,
			NoSuchChannelException {
		try {
			channelAccess.setMultipleChannelValues(null, null);
		} finally {
			stop(); 
		}

	}

	// cannot work... a write operation is never seen by a direct channel read (would work with readUnconfiguredChannels though)
	@Ignore
	@Test
	public void testGetMultipleChannelValues() throws ChannelAccessException, IllegalConversionException, InterruptedException {

		TestDriver d = startMbus();

		List<ChannelConfiguration> channelLocators = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100);

		List<Value> values = new LinkedList<Value>();

		values.add(new LongValue(1012));
		values.add(new LongValue(700));
		d.setExpectedChannelWrites(channelLocators.get(0), 1);
		d.setExpectedChannelWrites(channelLocators.get(1), 1);
		channelAccess.setMultipleChannelValues(channelLocators, values);
		d.assertAllChannelsWritten();
			
		List<SampledValue> sampledValues = channelAccess.getMultipleChannelValues(channelLocators);

		assertEquals(1012, sampledValues.get(0).getValue().getIntegerValue());

		assertEquals(700, sampledValues.get(1).getValue().getIntegerValue());
		stop(); 
	}

	@Test(expected = NullPointerException.class)
	public void testNullMultipleChannelValues() throws ChannelAccessException {
		channelAccess.getMultipleChannelValues(null);
	}

	@Test
	public void testNullMultipleChannelValues2() throws ChannelAccessException {
		List<SampledValue> sampledValues = channelAccess.getMultipleChannelValues(Collections.<ChannelConfiguration> emptyList());
		assertEquals(sampledValues.size(), 0);
	}

	@Test(expected = ChannelAccessException.class)
	public void testNoChannelFoundForDevice() throws ChannelAccessException {
		DeviceLocator deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
		channelAccess.discoverChannels(deviceLocator);
	}

	@Test
	public void testDiscoverChannels() throws UnsupportedOperationException,
			NoSuchInterfaceException, NoSuchDriverException, NoSuchDeviceException, IOException, InterruptedException, ChannelAccessException {

//		registerMbusTestDriver();
		final TestDriver d = startMbus();

//		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
		DeviceLocator deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);

		Device device = d.addDevice(deviceLocator);
		ChannelLocator cl1 = new ChannelLocator("02/862a", deviceLocator);
		ChannelLocator cl2 = new ChannelLocator("02/823b", deviceLocator);
//		ChannelLocator cl1 = channelAccess.getChannelLocator("02/862a", deviceLocator);
//		ChannelLocator cl2 = channelAccess.getChannelLocator("02/823b", deviceLocator);
		
		device.addChannel(cl1);
		device.addChannel(cl2);
		
		channelAccess.addChannel(cl1, Direction.DIRECTION_INOUT, ChannelConfiguration.LISTEN_FOR_UPDATE);
		channelAccess.addChannel(cl2,Direction.DIRECTION_INOUT, ChannelConfiguration.LISTEN_FOR_UPDATE);

		List<ChannelLocator> channels = channelAccess.discoverChannels(deviceLocator);

		assertEquals(2, channels.size());

		assertEquals("02/862a", channels.get(0).getChannelAddress());
		assertEquals("02/823b", channels.get(1).getChannelAddress());
		stop(); 
	}

	@Test
	public void testDiscoverChannelsAsync() throws UnsupportedOperationException, NoSuchInterfaceException,
			NoSuchDriverException, IOException, InterruptedException, ChannelAccessException {

//		registerMbusTestDriver();
		final TestDriver d = startMbus();

//		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
		DeviceLocator deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);

		Device device = d.addDevice(deviceLocator);
		ChannelConfiguration cc = channelAccess.addChannel(new ChannelLocator("02/862a", deviceLocator), Direction.DIRECTION_INOUT, ChannelConfiguration.LISTEN_FOR_UPDATE);
		device.addChannel(cc.getChannelLocator());
		cc = channelAccess.addChannel(new ChannelLocator("02/823b", deviceLocator), Direction.DIRECTION_INOUT, ChannelConfiguration.LISTEN_FOR_UPDATE);
		device.addChannel(cc.getChannelLocator());
		cc = channelAccess.addChannel(new ChannelLocator("04/822f", deviceLocator), Direction.DIRECTION_INOUT, ChannelConfiguration.LISTEN_FOR_UPDATE);
		device.addChannel(cc.getChannelLocator());

//		device.addChannel(channelAccess.getChannelLocator("02/862a", deviceLocator));
//		device.addChannel(channelAccess.getChannelLocator("02/823b", deviceLocator));
//		device.addChannel(channelAccess.getChannelLocator("04/822f", deviceLocator));

		final TestChannelScanListener channelScanListener = new TestChannelScanListener();

		channelAccess.discoverChannels(deviceLocator, channelScanListener);

		assertTrue(checkConditionWithTimeout(new ConditionCheck(channelScanListener) {
			@Override
			public boolean check() {
				return channelScanListener.finished;
			}
		}, 500));

		List<ChannelLocator> channels = channelScanListener.foundChannels;

		assertEquals(3, channels.size());

		assertEquals("02/862a", channels.get(0).getChannelAddress());
		assertEquals("02/823b", channels.get(1).getChannelAddress());
		stop(); 
	}

	@Test
	public void testDeletionOfDeletedChannel() throws ChannelAccessException {

		// double delete of channel is allowed

		startMbus();

		ChannelConfiguration cc = addAndConfigureWritableChannel("mbus", "/dev/ttyUSB0",
				"p" + counter.getAndIncrement(), "02/862a", ChannelConfiguration.LISTEN_FOR_UPDATE);

		assertTrue(channelAccess.deleteChannel(cc));
		assertFalse(channelAccess.deleteChannel(cc));
	}

	@Test(expected = ChannelAccessException.class)
	public void testWriteChannelWithChannelNotAvailable() throws ChannelAccessException {
		startMbus();
		List<ChannelConfiguration> channels = addChannelsToConfigurationForDevice1(channelAccess, 100);
		channelAccess.deleteChannel(channels.get(0));
		Value value = new LongValue(1012);
		channelAccess.setChannelValue(channels.get(0), value);
	}

	@Test
	public void testDeleteChannelFromConfiguration2() throws InterruptedException, ChannelAccessException {

		startMbus();

		List<ChannelConfiguration> channels = addChannelsToConfigurationForDevice1(channelAccess, 1000);
//		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", address, null);
//		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		channelAccess.deleteChannel(channels.get(0));

		List<ChannelLocator> channelsa = channelAccess.getAllConfiguredChannels();

		assertEquals(1, channelsa.size());

//		channelLocator = channelAccess.getChannelLocator("02/823b", deviceLocator);

		channelAccess.deleteChannel(channels.get(1));

		channelsa = channelAccess.getAllConfiguredChannels();

		assertEquals(0, channelsa.size());
		stop(); 
	}

	@Test
	public void testDeleteChannelFromConfiguration3() throws InterruptedException, ChannelAccessException {

		startMbus();

		List<ChannelConfiguration> channels = addChannelsToConfigurationForDevice1(channelAccess, 1000);
//
//		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", address1, null);
//
//		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		channelAccess.deleteChannel(channels.get(0));

		List<ChannelLocator> channelsa = channelAccess.getAllConfiguredChannels();

		assertEquals(1, channelsa.size());
//		channelLocator = channelAccess.getChannelLocator("02/823b", deviceLocator);

		@SuppressWarnings("unused")
		List<ChannelConfiguration> channels2 = addChannelsToConfigurationForDevice1(channelAccess, 1000);

		channelsa = channelAccess.getAllConfiguredChannels();

		assertEquals(3, channelsa.size());

//		deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", address2, null);
//
//		channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		channelAccess.deleteChannel(channels.get(1));

		channelsa = channelAccess.getAllConfiguredChannels();

		assertEquals(2, channels.size());
		stop(); 
	}

	@Test
	public void testAddChannelsAfterDeleteFromConfiguration() throws InterruptedException, ChannelAccessException {

		startMbus();

		List<ChannelConfiguration> channels = addChannelsToConfigurationForDevice1(channelAccess, 1000);

//		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", address, null);
//
//		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		channelAccess.deleteChannel(channels.get(0));
//		channelLocator = channelAccess.getChannelLocator("02/823b", deviceLocator);
		channelAccess.deleteChannel(channels.get(1));
		addChannelsToConfigurationForDevice1(channelAccess, 1000);
		stop(); 
	}

	// FIXME write -> read not working... 
//	@Ignore
	@Test
	public void testWriteSingleChannelNegativeValue() throws ChannelAccessException,
			IllegalConversionException, NoSuchDeviceException, InterruptedException {

		TestDriver d= startMbus();

		List<ChannelConfiguration> channels = addChannelsToConfigurationForDevice1(channelAccess, ChannelConfiguration.NO_READ_NO_LISTEN);
//		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", address, null);
//		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		Value value = new LongValue(-1012);
		d.setExpectedChannelWrites(channels.get(0), 1);
		channelAccess.setChannelValue(channels.get(0), value);
		d.assertAllChannelsWritten();

		SampledValue sampledValue = channelAccess.getChannelValue(channels.get(0));

		assertEquals(-1012, sampledValue.getValue().getIntegerValue());
		stop(); 

	}

	// FIXME write -> read not working... 
//	@Ignore
	@Test
	public void testWriteSingleChannelTwice() throws ChannelAccessException,
			IllegalConversionException, NoSuchDeviceException, InterruptedException {

		TestDriver d = startMbus();

		List<ChannelConfiguration> channels = addChannelsToConfigurationForDevice1(channelAccess, ChannelConfiguration.NO_READ_NO_LISTEN);
//		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", address, null);
//		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);
		d.setExpectedChannelWrites(channels.get(0), 1);
		Value value = new LongValue(1012);
		channelAccess.setChannelValue(channels.get(0), value);
		d.assertAllChannelsWritten();
		
		SampledValue sampledValue = channelAccess.getChannelValue(channels.get(0));
		assertEquals(1012, sampledValue.getValue().getIntegerValue());
		
		value = new LongValue(700);
		d.setExpectedChannelWrites(channels.get(0), 1);
		channelAccess.setChannelValue(channels.get(0), value);
		d.assertAllChannelsWritten();

		sampledValue = channelAccess.getChannelValue(channels.get(0));
		assertEquals(700, sampledValue.getValue().getIntegerValue());
		stop(); 

	}

//	@Test(expected = ChannelAccessException.class)
//	public void testWriteNonExistingChannel() throws ChannelAccessException,
//			IllegalConversionException, NoSuchDeviceException {
//		try {
//			DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p"+counter.getAndIncrement(), null);
//			ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);
//			Value value = new LongValue(1012);
//			channelAccess.setChannelValue(channelLocator, value);
//		} finally {
//			stop(); 
//		}
//	}

	@Test(expected = ChannelAccessException.class)
	public void testWriteNullChannel() throws ChannelAccessException,
			IllegalConversionException, NoSuchDeviceException {
		try {
			ChannelConfiguration channelLocator = null;
			Value value = new LongValue(1012);
			channelAccess.setChannelValue(channelLocator, value);
		} finally {
			stop(); 
		}
	}

	@Test(expected = ChannelAccessException.class)
	public void testReadNullChannel() throws ChannelAccessException,
			IllegalConversionException {
		try {
			ChannelConfiguration channelLocator = null;
			channelAccess.getChannelValue(channelLocator);
		} finally {
			stop(); 
		}
	}

//	@Test
//	public void testDriverInstallation2() throws InterruptedException {
//
//		startMbus();
//		startModbus();
//		List<String> drivers = channelAccess.getDriverIds();
//
//		assertEquals(2, drivers.size()); 
//	}

//	@Test
//	public void testExamineExistingConfig() throws InterruptedException {
//
//		startMbus();
//
////		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p"+counter.getAndIncrement(), null);
////		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/823b", deviceLocator);
//		ChannelConfiguration channelConfiguration = addAndConfigureWritableChannel("mbus", "/dev/ttyUSB0", "p"+counter.getAndIncrement(), "02/823b", 100);
//
////		ChannelConfiguration channelConfiguration = channelAccess.addChannel(channelLocator, Direction.DIRECTION_INPUT, 100);
////		channelConfiguration.setDirection(Direction.DIRECTION_INPUT); /* read-only channel */
////		channelConfiguration.setSamplingPeriod(100);
////		channelAccess.addChannel(channelConfiguration);
//
////		ChannelConfiguration channelConfig2 = channelAccess.getChannelConfiguration(channelLocator);
//
//		assertEquals(channelConfig2.getDirection(), channelConfiguration.getDirection());
//		assertEquals(channelConfig2.getSamplingPeriod(), channelConfiguration.getSamplingPeriod());
//		assertEquals(channelConfig2.getSamplingPeriod(), 100);
//		stop(); 
//	}

//	@Test
//	public void testExamineNonExistingConfig() throws InterruptedException {
//
//		startMbus();
//
//		ChannelLocator channelLocator = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100).get(1);
//
//		channelAccess.deleteChannel(channelLocator);
//
//		ChannelConfiguration channelConfig2 = channelAccess.getChannelConfiguration(channelLocator);
//
//		assertEquals(channelConfig2.getDirection(), Direction.DIRECTION_INOUT); // Default direction
//		assertEquals(channelConfig2.getSamplingPeriod(), 1000); //Default SamplingPeriod
//		stop(); 
//	}

	@Test
	public void testExamineAllConfiguredChannels() throws InterruptedException, ChannelAccessException {

		startMbus();
		ChannelConfiguration channelLocator = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100).get(1);

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertTrue(channelIsListed(channels, channelLocator.getChannelLocator()));
	}

	@Test
	public void testChannelListener() throws InterruptedException, ChannelAccessException {

		// Test if changedListener is called
		
		startDriverBundle(new TestDriver("mbus", "wired mbus test driver"));

		List<ChannelConfiguration> channels = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100);
		final CountDownLatch latch1  = new CountDownLatch(5);
		final CountDownLatch latch2 = new CountDownLatch(5);
		
		channelAccess.registerChangedListener(channels, new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				for (SampledValueContainer c : channels) {
					try {
						System.out.println("Channel: " + c.getChannelLocator().toString() + ", Changed Value: "
								+ c.getSampledValue().getValue().getIntegerValue());
						if (c.getChannelLocator().getChannelAddress().equals("02/862a"))
							latch1.countDown();
						else if (c.getChannelLocator().getChannelAddress().equals("02/823b"))
							latch2.countDown();
					} catch (IllegalConversionException e) {
						System.out.println("Changed channel value could not be read");
					}
				}
			}
		});

		assertTrue(latch1.await(5, TimeUnit.SECONDS));
		assertTrue(latch2.await(5, TimeUnit.SECONDS));
	}

	// no assert?
	@Test
	public void testChannelListenerReadingInterval() throws InterruptedException, ChannelAccessException {

		startMbus();

		List<ChannelConfiguration> channels = addAndConfigureWritableChannelsForDevice1(channelAccess, 25, 100);

		channelAccess.registerChangedListener(channels, new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				for (SampledValueContainer c : channels) {
					try {
						System.out.println("Channel: " + c.getChannelLocator().toString() + ", Changed Value: "
								+ c.getSampledValue().getValue().getIntegerValue());
					} catch (IllegalConversionException e) {
						System.out.println("Changed channel value could not be read");
					}
				}
			}
		});

		Thread.sleep(10);
	}

	@Test
	public void testUpdateListener() throws InterruptedException, ChannelAccessException {

		startMbus();

		List<ChannelConfiguration> channels = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100);

		channelAccess.registerUpdateListener(channels, new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				for (SampledValueContainer c : channels) {
					System.out.println("Channel: " + c.getChannelLocator().toString() + ", timestamp: "
							+ c.getSampledValue().getTimestamp());
				}
			}
		});

		Thread.sleep(500);
	}

	@Test
	public void testUpdateListenerReadingInterval() throws InterruptedException, ChannelAccessException {

		startMbus();

		List<ChannelConfiguration> channels = addAndConfigureWritableChannelsForDevice1(channelAccess, 25, 100);

		channelAccess.registerUpdateListener(channels, new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				for (SampledValueContainer c : channels) {
					System.out.println("Channel: " + c.getChannelLocator().toString() + ", timestamp: "
							+ c.getSampledValue().getTimestamp());
				}
			}
		});

		Thread.sleep(500);
	}

	@Test
	public void testReadingInterval() throws ChannelAccessException, IllegalConversionException, InterruptedException {

		TestDriver d = startMbus();
		d.setExpectedChannelAddeds(2);
		List<ChannelConfiguration> channelConfigs = addAndConfigureWritableChannelsForDevice1(channelAccess, 25, ChannelConfiguration.NO_READ_NO_LISTEN);
		d.assertChannelsAdded();

		Thread.sleep(1500);

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(2,channels.size());

		SampledValue sampledValue1 = channelAccess.getChannelValue(channelConfigs.get(0));
		SampledValue sampledValue2 = channelAccess.getChannelValue(channelConfigs.get(1));

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue();

		System.out.println("Value of channel1: " + value1.getIntegerValue() + ", Value of channel2: "
				+ value2.getIntegerValue());

		assertTrue(value1.getIntegerValue() > value2.getIntegerValue());
	}

	@Test
	public void testChannelListeners() throws ChannelAccessException, InterruptedException {

		TestDriver d = startMbus();
		List<ChannelConfiguration> configs = addAndConfigureWritableChannelsForDevice1(channelAccess, 200, 100);
		final List<ChannelLocator> configuredChannels = channelAccess.getAllConfiguredChannels();
		
//		final int[] channelEventOccured = new int[] { 0, 0 };
		final AtomicIntegerArray channelEventOccured = new AtomicIntegerArray(new int[]{0, 0});

		channelAccess.registerUpdateListener(configs, new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {

				if (channels.get(0).getChannelLocator().equals(configuredChannels.get(0))) {
					channelEventOccured.getAndIncrement(0);
				}

				if (channels.get(0).getChannelLocator().equals(configuredChannels.get(1))) {
					channelEventOccured.getAndIncrement(1);
				}
			}
		});
		Thread.sleep(1000);
		d.setExpectedChannelRead(configs.get(0), 10);
		d.assertAllChannelsRead(); // wait until we have a statistically relevant number of reads

		float ratio = (float) channelEventOccured.get(1) / (float) channelEventOccured.get(0);

		System.out.println("channelEventOccured[0]:" + channelEventOccured.get(0) + " channelEventOccured[1]:"	+ channelEventOccured.get(1));

		assertEquals(2.0, ratio, 0.5);

//		assertTrue(channelEventOccured[0] > 4);
//		assertTrue(channelEventOccured[1] > 8);
	}

	@Ignore("Test depends on timing and fails sporadically")
	@Test
	public void testReadingInterval2() throws ChannelAccessException,
			IllegalConversionException, InterruptedException {

		startMbus();

		List<ChannelConfiguration> configs = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, -1);

		try {
			Thread.sleep(1500); // fails for 500
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(channels.size(), 2);

//		ChannelLocator channel1 = channels.get(0);
//		ChannelLocator channel2 = channels.get(1);

		SampledValue sampledValue1 = channelAccess.getChannelValue(configs.get(0));
		SampledValue sampledValue2 = channelAccess.getChannelValue(configs.get(1));

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue();

		System.out.println("Value of channel1: " + value1.getIntegerValue() + ", Value of channel2: "
				+ value2.getIntegerValue());

		assertTrue(value1.getIntegerValue() > 2 && value1.getIntegerValue() < 6);
		assertEquals(value2.getIntegerValue(), 3);
	}

	@Ignore("Test depends on timing and fails sporadically")
	@Test
	public void testReadingInterval3() throws ChannelAccessException,
			IllegalConversionException, InterruptedException {

		startMbus();

		List<ChannelConfiguration> configs = addAndConfigureWritableChannelsForDevice1(channelAccess, -1, 100);

		try {
			Thread.sleep(1500);// fails for 500
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(channels.size(), 2);

//		ChannelLocator channel1 = channels.get(0);
//		ChannelLocator channel2 = channels.get(1);

		SampledValue sampledValue1 = channelAccess.getChannelValue(configs.get(0));
		SampledValue sampledValue2 = channelAccess.getChannelValue(configs.get(1));

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue();

		System.out.println("Value of channel1: " + value1.getIntegerValue() + ", Value of channel2: "
				+ value2.getIntegerValue());

		assertEquals(value1.getIntegerValue(), 3);
		assertTrue(value2.getIntegerValue() > 2 && value2.getIntegerValue() < 6);
	}

//	@Ignore // fails
//	@Test
//	public void testReadingInterval4() throws ChannelAccessException,
//			IllegalConversionException, InterruptedException {
//
//		TestDriver d = startMbus();
//		d.setExpectedChannelAddeds(2);
//		addAndConfigureWritableChannelsForDevice1(channelAccess, 0, 100);
//		d.assertChannelsAdded();
//		
//		Thread.sleep(1500);
//
//		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();
//
//		assertEquals(2,channels.size());
//
//		ChannelLocator channel2 = channels.get(1);
//
//		SampledValue sampledValue2 = channelAccess.getChannelValue(channel2);
//
//		Value value2 = sampledValue2.getValue();
//
//		System.out.println("Value of channel2: " + value2.getIntegerValue());
//
//		assertTrue(value2.getIntegerValue() > 2 && value2.getIntegerValue() < 6);
//		stop(); 
//	}

//	@Ignore // fails
//	@Test
//	public void testReadingInterval5() throws ChannelAccessException,
//			IllegalConversionException, InterruptedException {
//
//		TestDriver d = startMbus();
//		d.setExpectedChannelAddeds(2);
//		addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 0);
//		d.assertChannelsAdded();
//
//		Thread.sleep(1500);
//
//		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();
//
//		assertEquals(2,channels.size());
//
//		ChannelLocator channel1 = channels.get(0);
//
//		SampledValue sampledValue1 = channelAccess.getChannelValue(channel1);
//
//		Value value1 = sampledValue1.getValue();
//
//		System.out.println("Value of channel1: " + value1.getIntegerValue());
//
//		assertTrue(value1.getIntegerValue() > 2 && value1.getIntegerValue() < 6);
//		stop(); 
//	}

//	@Ignore
//	@Test(expected = ChannelAccessException.class)
//	public void testReadingInterval6() throws ChannelAccessException, InterruptedException {
//		TestDriver d = startMbus();
//		d.setExpectedChannelAddeds(2);
//		List<ChannelConfiguration> configs = addAndConfigureWritableChannelsForDevice1(channelAccess, 0, 0);
//		d.assertChannelsAdded();
//
//		Thread.sleep(1500);
//
//		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();
//
//		assertEquals(channels.size(), 2);
//
//		SampledValue sampledValue1 = channelAccess.getChannelValue(configs.get(0));
//
//		sampledValue1.getValue();
//	}

//	@Ignore
//	@Test
//	public void testReadingInterval8() throws ChannelAccessException,
//			IllegalConversionException, InterruptedException {
//
//		TestDriver d = startMbus();
//		d.setExpectedChannelAddeds(2);
//		addAndConfigureWritableChannelsForDevice1(channelAccess, 0, -1);
//		d.assertChannelsAdded();
//
//		Thread.sleep(1500);
//
//		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();
//
//		assertEquals(2,channels.size());
//
//		ChannelLocator channel2 = channels.get(1);
//
//		SampledValue sampledValue2 = channelAccess.getChannelValue(channel2);
//
//		Value value2 = sampledValue2.getValue();
//
//		System.out.println("Value of channel2: " + value2.getIntegerValue());
//
//		assertEquals(value2.getIntegerValue(), 3);
//		stop(); 
//	}

//	@Ignore
//	@Test
//	public void testReadingInterval9() throws ChannelAccessException,
//			IllegalConversionException, InterruptedException {
//
//		TestDriver d = startMbus();
//		d.setExpectedChannelAddeds(2);
//		addAndConfigureWritableChannelsForDevice1(channelAccess, 0, 100);
//		d.assertChannelsAdded();
//
//		Thread.sleep(1500);
//
//		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();
//
//		assertEquals(channels.size(), 2);
//
//		ChannelLocator channel1 = channels.get(0);
//
//		SampledValue sampledValue1 = channelAccess.getChannelValue(channel1);
//
//		Value value1 = sampledValue1.getValue();
//
//		System.out.println("Value of channel2: " + value1.getIntegerValue());
//
//		assertEquals(value1.getIntegerValue(), 3);
//		stop(); 
//	}

	@Test
	public void testChannelUpdate() throws ChannelAccessException, InterruptedException {

		TestDriver d = startMbus();
		List<ChannelConfiguration> configs = addAndConfigureWritableChannelsForDevice1(channelAccess, 120, 120);
		configs.add(addAndConfigureWritableOneChannelForDevice1(channelAccess, 100, "862b"));
		
		for (ChannelConfiguration cl: configs) {
			d.setExpectedChannelRead(cl, 1);
		}
		d.assertAllChannelsRead(3,TimeUnit.SECONDS);

		// the assertAllChannelsRead returns as soon as the third value has been processed by the driver.
		// the channelManager has had no chance yet to cache the supplied value, because the ReaderThread is switched away.
		// this causes the test to fail, because sampledValue2 is not yet set.
		Thread.sleep(10);
		
		ChannelConfiguration channel1 = configs.get(0);
		ChannelConfiguration channel2 = configs.get(1);
		ChannelConfiguration channel3 = configs.get(2);

		SampledValue sampledValue1 = channelAccess.getChannelValue(channel1);
		SampledValue sampledValue2 = channelAccess.getChannelValue(channel2);
		SampledValue sampledValue3 = channelAccess.getChannelValue(channel3);

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue();
		Value value3 = sampledValue3.getValue();
		
		assertEquals(value2.getIntegerValue(), value1.getIntegerValue(),1.2); 
		assertTrue("Expected value < 25, got " + value3.getIntegerValue(),value3.getIntegerValue() < 25);

		System.out.println("Value of channel1: " + value1.getIntegerValue() + ", Value of channel2: "
				+ value2.getIntegerValue() + ", Value of channel3: " + value3.getIntegerValue());
	}

	@Test
	public void testReadingInterval10() throws ChannelAccessException,
			IllegalConversionException, InterruptedException {

		TestDriver d = startMbus();
		d.setExpectedChannelAddeds(7);
		List<ChannelConfiguration> configs = new ArrayList<ChannelConfiguration>(7);
		
		configs.add(addAndConfigureWritableOneChannelForDevice1(channelAccess, 100, "02/862a"));
		configs.add(addAndConfigureWritableOneChannelForDevice1(channelAccess, 100, "02/823b"));
		configs.add(addAndConfigureWritableOneChannelForDevice1(channelAccess, 100, "824a"));
		configs.add(addAndConfigureWritableOneChannelForDevice1(channelAccess, 25, "832a"));
		configs.add(addAndConfigureWritableOneChannelForDevice1(channelAccess, 25, "862b"));
		configs.add(addAndConfigureWritableOneChannelForDevice1(channelAccess, 131, "862d"));
		
		for (ChannelConfiguration cl :configs) {
			d.setExpectedChannelRead(cl, 1);
		}
		configs.add(addAndConfigureWritableOneChannelForDevice1(channelAccess, ChannelConfiguration.LISTEN_FOR_UPDATE, "862c"));
		
		d.assertChannelsAdded();
		d.assertAllChannelsRead();

		ChannelConfiguration channel1 = configs.get(0);
		ChannelConfiguration channel2 = configs.get(1);
		ChannelConfiguration channel3 = configs.get(2);
		ChannelConfiguration channel4 = configs.get(3);
		//		ChannelLocator channel5 = channels.get(4);

		SampledValue sampledValue1 = channelAccess.getChannelValue(channel1);
		SampledValue sampledValue2 = channelAccess.getChannelValue(channel2);
		SampledValue sampledValue3 = channelAccess.getChannelValue(channel3);
		SampledValue sampledValue4 = channelAccess.getChannelValue(channel4);
		//		SampledValue sampledValue5 = channelAccess.getChannelValue(channel5);

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue();
		Value value3 = sampledValue3.getValue();
		Value value4 = sampledValue4.getValue();
		//		Value value5 = sampledValue5.getValue();

		System.out.println("channel1: " + value1.getIntegerValue() + ", channel2: " + value2.getIntegerValue());
		System.out.println("channel3: " + value3.getIntegerValue() + ", channel4: " + value4.getIntegerValue());
		//		System.out.println("channel5: " + value5.getIntegerValue());

		//assertTrue(value1.getIntegerValue() > 2 && value1.getIntegerValue() < 6);
		//assertTrue(value1.getIntegerValue() < value2.getIntegerValue());
		//assertTrue(value2.getIntegerValue() <= value3.getIntegerValue() + 2 || value3.getIntegerValue() <= value2.getIntegerValue() + 2);
		//assertEquals(value4.getIntegerValue(), 3);
	}

	@Test
	public void testReadingInterval11() throws ChannelAccessException,
			IllegalConversionException, InterruptedException {

		TestDriver d = startMbus();
		ChannelConfiguration config1 = addAndConfigureWritableOneChannelForDevice1(channelAccess, 100, "824a");
		ChannelConfiguration config2 =addAndConfigureWritableOneChannelForDevice1(channelAccess, 25, "832a");

		d.setExpectedChannelRead(config1,3);
		d.setExpectedChannelRead(config2,3);
		d.assertAllChannelsRead(); // wait for 3 values in each channel

		SampledValue sampledValue1 = channelAccess.getChannelValue(config1);
		SampledValue sampledValue2 = channelAccess.getChannelValue(config2);

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue();
		System.out.println("  Values: " + value1.getIntegerValue() + ", " + value2.getIntegerValue());

//		assertTrue(value1.getIntegerValue() > 4 && value1.getIntegerValue() < 12); // very dangerous...
//		assertTrue(value2.getIntegerValue() > 8 && value2.getIntegerValue() < 24);
	}

	@Test
	public void testReadingInterval12() throws ChannelAccessException,
			IllegalConversionException, InterruptedException {

		TestDriver d = startMbus();
		d.setExpectedChannelAddeds(3);

		addAndConfigureWritableOneChannelForDevice1(channelAccess, 100, "824a");
		ChannelConfiguration channel2 = addAndConfigureWritableOneChannelForDevice1(channelAccess, 25, "832a");
		addAndConfigureWritableOneChannelForDevice1(channelAccess, 131, "862d");
		d.assertChannelsAdded();

		d.setExpectedChannelRemoveds(1);
		channelAccess.deleteChannel(channel2);
		d.assertChannelsRemoved();
	}

	@Test
	public void testReadingIntervalSeveralDevices() throws ChannelAccessException,
			IllegalConversionException, InterruptedException {

		TestDriver d = startMbus();
		d.setExpectedChannelAddeds(1);
		
		ChannelConfiguration channelConfiguration = addAndConfigureWritableChannel("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), "02/862a", 100);
//		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
//		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);
//		ChannelConfiguration channelConfiguration = channelAccess.getChannelConfiguration(channelLocator);
//		channelConfiguration.setSamplingPeriod(100);
//		channelAccess.addChannel(channelConfiguration);
		
		d.assertChannelsAdded();
		d.setExpectedChannelAddeds(1);
		
		ChannelConfiguration channelConfiguration2 = addAndConfigureWritableChannel("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), "02/862b", 100);
//		DeviceLocator deviceLocator2 = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
//		ChannelLocator channelLocator2 = channelAccess.getChannelLocator("02/862b", deviceLocator2);
//		ChannelConfiguration channelConfiguration2 = channelAccess.getChannelConfiguration(channelLocator2);
//		channelConfiguration2.setSamplingPeriod(100);
//		channelAccess.addChannel(channelConfiguration2);
		
		d.assertChannelsAdded();
		d.setExpectedChannelRead(channelConfiguration, 3); // minimum number
		d.setExpectedChannelRead(channelConfiguration2, 3);
		d.assertChannelRead(channelConfiguration, 5, TimeUnit.SECONDS);
		d.assertChannelRead(channelConfiguration2, 5, TimeUnit.SECONDS);
		
		SampledValue sampledValue1 = channelAccess.getChannelValue(channelConfiguration); 
		SampledValue sampledValue2 = channelAccess.getChannelValue(channelConfiguration2);

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue(); 
		System.out.println("   values: " + value1.getIntegerValue() + ", " + value2.getIntegerValue());
//		assertTrue(value1.getIntegerValue() > 2 && value1.getIntegerValue() < 6); // this is extremely instable, not a good check... // replaced by latch-based method
//		assertTrue(value2.getIntegerValue() > 2 && value2.getIntegerValue() < 6);
	}

	/*  @Test
	public void testEventListener() throws ChannelConfigurationException{
		
		System.out.println("Test: testEventListener");
		
		registerMbusTestDriver();
		
		List<ChannelLocator> channelLocators = addAndConfigureWritableChannelsForDevice1(channelAccess, -1, -1);
		
		channelAccess.registerUpdateListener(channelLocators, new ChannelEventListener() {
			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels){
				System.out.println("Event! Channel: " + channels.get(0).getChannelLocator());
				
				if(type == EventType.UPDATED){
					System.out.println("Timestamp: " + channels.get(0).getSampledValue().getTimestamp());
				}
				
				if(type == EventType.VALUE_CHANGED){
					try {
						System.out.println("Value: " + channels.get(0).getSampledValue().getValue().getIntegerValue());
					} catch (IllegalConversionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}*/

	private ChannelConfiguration addAndConfigureWritableOneChannelForDevice1(ChannelAccess channelManagement,
			long samplingPeriod, String channelname) throws ChannelAccessException {

		return addAndConfigureWritableChannel("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), channelname, samplingPeriod);
	}

	private List<ChannelConfiguration> addAndConfigureWritableChannelsForDevice1(ChannelAccess channelManagement,
			long samplingPeriod1, long samplingPeriod2) throws ChannelAccessException {

		ChannelConfiguration channelConfiguration;
		DeviceLocator dl;
		ChannelLocator cl;
		List<ChannelConfiguration> channelLocators = new LinkedList<ChannelConfiguration>();

		dl = new DeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
		
		cl = new ChannelLocator("02/862a", dl);
		channelConfiguration = channelManagement.addChannel(cl, Direction.DIRECTION_INOUT, samplingPeriod1);
		channelLocators.add(channelConfiguration);

		cl = new ChannelLocator("02/823b", dl);
		channelConfiguration = channelManagement.addChannel(cl, Direction.DIRECTION_INOUT, samplingPeriod2);
		channelLocators.add(channelConfiguration);

		return channelLocators;
	}

	private boolean driverIsListed(List<String> drivers, String wantedDriver) {

		if (drivers == null || wantedDriver == null) {
			return false;
		}

		for (String driver : drivers) {

			if (driver.equals(wantedDriver)) {
				return true;
			}
		}

		return false;
	}

	private static <S> boolean channelIsListed(List<S> channels, S wantedChannel) {

		if (channels == null || wantedChannel == null) {
			return false;
		}

		for (S c : channels) {

			if (c.equals(wantedChannel)) {
				return true;
			}
		}

		return false;
	}

	//Ende Clara

	private void setupTestDevices(TestDriver mbusTestDriver) {

		DeviceLocator deviceLocator;

		deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
//		deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
		mbusTestDriver.addDevice(deviceLocator);

		deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
//		deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
		mbusTestDriver.addDevice(deviceLocator);

		deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
//		deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p" + counter.getAndIncrement(), null);
		mbusTestDriver.addDevice(deviceLocator);

		deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB1", "p" + counter.getAndIncrement(), null);
//		deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB1", "p" + counter.getAndIncrement(), null);
		mbusTestDriver.addDevice(deviceLocator);
	}

	// creates two channels
	private List<ChannelConfiguration> addChannelsToConfigurationForDevice1(ChannelAccess channelManagement, long samplingPeriod)
			throws ChannelAccessException {
		String address = "p" + counter.getAndIncrement();
		DeviceLocator deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB0", address, null);

		return configureChannelsForDevice(channelManagement, samplingPeriod, deviceLocator);
	}

//	private void addChannelsToConfigurationForDevice2(ChannelAccess channelManagement, int samplingPeriod)
//			throws ChannelConfigurationException {
//		DeviceLocator deviceLocator = channelManagement.getDeviceLocator("mbus", "/dev/ttyUSB0", "p2", null);
//
//		configureChannelsForDevice(channelManagement, samplingPeriod, deviceLocator);
//	}

	private List<ChannelConfiguration> configureChannelsForDevice(ChannelAccess channelManagement, long samplingPeriod,
			DeviceLocator deviceLocator) throws ChannelAccessException {
		ChannelLocator channelLocator;

		ChannelConfiguration channelConfiguration;
		List<ChannelConfiguration> list= new ArrayList<>();
		
		channelLocator = new ChannelLocator("02/862a", deviceLocator);
		channelConfiguration = channelManagement.addChannel(channelLocator,Direction.DIRECTION_INOUT, samplingPeriod);
		list.add(channelConfiguration);

		channelLocator = new ChannelLocator("02/823b", deviceLocator);
		channelConfiguration = channelManagement.addChannel(channelLocator, Direction.DIRECTION_INPUT, samplingPeriod);
		list.add(channelConfiguration);
		
		return list;
	}

	private boolean checkConditionWithTimeout(ConditionCheck condition, long timeoutInMs) {

		long startTime = System.currentTimeMillis();

		while (System.currentTimeMillis() < (startTime + timeoutInMs)) {
			if (condition.check()) {
				return true;
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}

		return false;
	}

	private void registerMbusTestDriver() {
		TestDriver testDriver = new TestDriver("mbus", "wired mbus test driver");
		mbusDriverService = ctx.registerService(ChannelDriver.class, testDriver, null);
	}

	private void unregisterMbusTestDriver() {
		mbusDriverService.unregister();
	}
	
	// FIXME This does not work...
//	private void startDriverBundle(Class<?> activator, String symbolicName) {
//		try {
//			InputStream inputStream = TinyBundles.bundle()
//					.add(activator.getName(),getFileUrl(activator))
//					.add(TestDriverActivator.class.getName(),getFileUrl(TestDriverActivator.class))
//					.add(TestDriver.class.getName(),getFileUrl(TestDriver.class))
//					.set(Constants.BUNDLE_SYMBOLICNAME, symbolicName)
//					.set(Constants.BUNDLE_ACTIVATOR, activator.getName())
//					.set(Constants.IMPORT_PACKAGE, "*")
//					.build(TinyBundles.withBnd());
//			ctx.installBundle(activator.getName(), inputStream).start();
//
//		} catch (BundleException |IllegalStateException | NullPointerException |MalformedURLException e) {
//			e.printStackTrace();
//			throw new AssertionError(e);
//		}
//	}

	private <T extends Object> T lookupService(Class<T> serviceClass) {
		ServiceReference<T> serviceRef = ctx.getServiceReference(serviceClass);

		if (serviceRef != null) {
			return ctx.getService(serviceRef);
		}

		return null;
	}

	// @formatter:on
}
