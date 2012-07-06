/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.channelmanager.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogema.channelmanager.impl.testdriver.MBusTestDriverActivator;
import org.ogema.channelmanager.impl.testdriver.ModbusTestDriverActivator;
import org.ogema.channelmanager.impl.testdriver.TestDriver;
import org.ogema.channelmanager.impl.testdriver.TestDriver.Device;
import org.ogema.channelmanager.impl.testdriver.TestDriverActivator;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.ChannelConfigurationException;
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
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import aQute.lib.osgi.Constants;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class ChannelManagerIntegrationTest {

	// @formatter:off

	@Inject
	BundleContext ctx;

	private ChannelDriver testDriver;

	private ServiceRegistration<ChannelDriver> mbusDriverService;

	@Inject
	private ChannelAccess channelAccess;

	@Before
	public void setup() {

	}

	@Configuration
	public Option[] config() {
		return new Option[] { CoreOptions.systemProperty("org.ogema.security").value("off") };
	}

	@Configuration
	public Option[] configure() {
		String ogemaVersion = MavenUtils.asInProject().getVersion("org.ogema.core", "api");
		return options(
				//excludeDefaultRepositories(),
				//repositories("file:.m2/"),
				junitBundles(), mavenBundle("org.ops4j.pax.tinybundles", "tinybundles", "1.0.0"), mavenBundle(
						"ch.qos.logback", "logback-core", "1.0.0"), mavenBundle("ch.qos.logback", "logback-classic",
						"1.0.0"), bundle("file:../../../../osgi/bundles/org.knopflerfish.bundle.jsdk-API_2.5.jar"),
				mavenBundle("org.slf4j", "slf4j-api", "1.7.2"),
				bundle("file:../../../../osgi/bundles/org.apache.felix.framework.security-2.2.0.jar"), mavenBundle(
						"org.apache.wicket", "wicket-request", "6.9.1"), mavenBundle("org.apache.wicket",
						"wicket-util", "6.9.1"), mavenBundle("org.apache.wicket", "wicket-core", "6.9.1"), mavenBundle(
						"org.ogema.core", "api").versionAsInProject(), mavenBundle("org.ogema.driver.coap",
						"coap-driver", ogemaVersion), bundle("reference:file:target/classes"));

	}

	@Test
	public void testDriverInstallation() {

		startDriverBundle(ModbusTestDriverActivator.class, "org.ogema.driver.modbus-driver");
		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		List<String> drivers = channelAccess.getDriverIds();

		assertTrue(driverIsListed(drivers, "modbus"));
		assertTrue(driverIsListed(drivers, "mbus"));
		assertTrue(driverIsListed(drivers, "coap"));

		assertEquals(3, drivers.size());
	}

	@Test(expected = NoSuchDriverException.class)
	public void testRequestDeviceListFromUnknownDriver() throws UnsupportedOperationException,
			NoSuchInterfaceException, IOException, NoSuchDriverException {

		startDriverBundle(ModbusTestDriverActivator.class, "org.ogema.driver.modbus-driver");

		channelAccess.discoverDevices("mbus", "/dev/ttyUSB0", null);
	}

	@Test
	public void testChannelAccessServiceLookup() {

		ChannelAccess accessService = lookupService(ChannelAccess.class);

		assertNotNull(accessService);

		List<ChannelLocator> channels = accessService.getAllConfiguredChannels();

		assertNotNull(channels);

		assertEquals(0, channels.size());
	}

	@Test
	public void testChannelConfiguration() throws ChannelConfigurationException {

		startDriverBundle(ModbusTestDriverActivator.class, "org.ogema.driver.modbus-driver");
		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 1000);

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(2, channels.size());

		ChannelLocator channel1 = channels.get(0);

		assertEquals("02/862a", channel1.getChannelAddress());
	}

	@Test
	public void testDeleteChannelFromConfiguration() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 1000);

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		channelAccess.deleteChannel(channelLocator);

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(1, channels.size());

		ChannelLocator channel1 = channels.get(0);

		assertEquals("02/823b", channel1.getChannelAddress());
	}

	@Test(expected = ChannelConfigurationException.class)
	public void testDeletionOfUnconfiguredChannel() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		channelAccess.deleteChannel(channelLocator);
	}

	@Test
	public void testChannelDirectionConfiguration() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 1000);

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		ChannelLocator channel1 = channels.get(0);

		ChannelConfiguration channelConfig1 = channelAccess.getChannelConfiguration(channel1);

		System.out.println(channelConfig1.getChannelLocator() + " direction: " + channelConfig1.getDirection());

		assertEquals(Direction.DIRECTION_INOUT, channelConfig1.getDirection());

		ChannelLocator channel2 = channels.get(1);

		ChannelConfiguration channelConfig2 = channelAccess.getChannelConfiguration(channel2);

		System.out.println(channelConfig2.getChannelLocator() + " direction: " + channelConfig2.getDirection());

		assertEquals(Direction.DIRECTION_INPUT, channelConfig2.getDirection());
	}

	@Test(expected = ChannelConfigurationException.class)
	public void testAddChannelsTwice() throws ChannelConfigurationException {

		startDriverBundle(ModbusTestDriverActivator.class, "org.ogema.driver.modbus-driver");
		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 1000);
		addChannelsToConfigurationForDevice1(channelAccess, 1000);
	}

	@Test
	public void testChannelRead1000ms() throws ChannelConfigurationException {

		startDriverBundle(ModbusTestDriverActivator.class, "org.ogema.driver.modbus-driver");
		registerMbusTestDriver();

		addChannelsToConfigurationForDevice1(channelAccess, 1000);

		assertTrue(checkConditionWithTimeout(new ConditionCheck(this.testDriver) {
			@Override
			public boolean check() {
				return (((TestDriver) testObject).channelAddedCalled == 2);
			}
		}, 500));

		assertTrue(checkConditionWithTimeout(new ConditionCheck(this.testDriver) {
			@Override
			public boolean check() {
				return (((TestDriver) testObject).readChannelsCalled >= 1);
			}
		}, 2000));

	}

	@Test
	public void testChannelRead100ms() throws ChannelConfigurationException {

		registerMbusTestDriver();

		addChannelsToConfigurationForDevice1(channelAccess, 100);

		assertTrue(checkConditionWithTimeout(new ConditionCheck(this.testDriver) {
			@Override
			public boolean check() {
				return (((TestDriver) testObject).channelAddedCalled == 2);
			}
		}, 500));

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertThat(((TestDriver) this.testDriver).readChannelsCalled, RangeMatcher.inRange(9, 14));
	}

	// Test deaktiviert weil er anscheinend auf CI-Server sparadisch fehlschlägt.
	@Ignore
	@Test
	public void testChannelReadMultipleDevices() throws ChannelConfigurationException {

		registerMbusTestDriver();

		addChannelsToConfigurationForDevice1(channelAccess, 100);
		addChannelsToConfigurationForDevice2(channelAccess, 100);

		assertTrue(checkConditionWithTimeout(new ConditionCheck(this.testDriver) {
			@Override
			public boolean check() {
				return (((TestDriver) testObject).channelAddedCalled == 4);
			}
		}, 500));

		assertTrue(checkConditionWithTimeout(new ConditionCheck(this.testDriver) {
			@Override
			public boolean check() {
				return (((TestDriver) testObject).readChannelsCalled >= 20);
			}
		}, 1600));

	}

	@Test
	public void testChannelAccess() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 100);
		addChannelsToConfigurationForDevice2(channelAccess, 100);

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(4, channels.size());

		ChannelLocator channel1 = channels.get(0);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		SampledValue sampledValue = channelAccess.getChannelValue(channel1);

		Value value = sampledValue.getValue();

		assertTrue(value.getIntegerValue() > 2 && value.getIntegerValue() < 6);
	}

	@Test(expected = ChannelAccessException.class)
	public void testReadSingleChannelWithMissingValue() throws ChannelAccessException, ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 100);

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		channelAccess.getChannelValue(channelLocator);
	}

	@Test
	public void testWriteSingleChannel() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException, NoSuchDeviceException, NoSuchChannelException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 100);

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		Value value = new LongValue(1012);

		channelAccess.setChannelValue(channelLocator, value);

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}

		SampledValue sampledValue = channelAccess.getChannelValue(channelLocator);

		assertEquals(1012, sampledValue.getValue().getIntegerValue());

	}

	@Test(expected = ChannelAccessException.class)
	public void testWriteReadOnlyChannel() throws ChannelAccessException, ChannelConfigurationException,
			NoSuchDeviceException, NoSuchChannelException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 100);

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862b", deviceLocator);

		Value value = new IntegerValue(100);

		channelAccess.setChannelValue(channelLocator, value);
	}

	@Test(expected = ChannelAccessException.class)
	public void testWriteChannelWithDriverNotAvailable() throws ChannelConfigurationException, ChannelAccessException,
			NoSuchDeviceException, NoSuchChannelException {

		registerMbusTestDriver();

		addChannelsToConfigurationForDevice1(channelAccess, 100);

		unregisterMbusTestDriver();

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		Value value = new LongValue(1012);

		channelAccess.setChannelValue(channelLocator, value);
	}

	@Test
	public void testDiscoverDevices() throws UnsupportedOperationException, NoSuchInterfaceException,
			NoSuchDriverException, IOException {

		registerMbusTestDriver();

		setupTestDevices();

		List<DeviceLocator> devices = channelAccess.discoverDevices("mbus", "/dev/ttyUSB0", null);

		assertNotNull(devices);

		assertEquals(3, devices.size());

		devices = channelAccess.discoverDevices("mbus", "/dev/ttyUSB1", null);

		assertEquals(1, devices.size());
	}

	@Test
	public void testDiscoverDevicesAsnyc() throws UnsupportedOperationException, NoSuchInterfaceException,
			NoSuchDriverException, IOException {

		registerMbusTestDriver();

		setupTestDevices();

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

	@Test(expected = NoSuchDriverException.class)
	public void testDiscoverDevicesNoSuchDriver() throws UnsupportedOperationException, NoSuchInterfaceException,
			NoSuchDriverException, IOException {

		channelAccess.discoverDevices("mbus", "/dev/ttyUSB0", null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testDiscoverDevicesOperationNotSupported() throws UnsupportedOperationException,
			NoSuchInterfaceException, NoSuchDriverException, IOException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		channelAccess.discoverDevices("mbus", "/dev/ttyUSB0", null);
	}

	@Test(expected = NoSuchInterfaceException.class)
	public void testDiscoverDevicesUnknownInterface() throws UnsupportedOperationException, NoSuchInterfaceException,
			NoSuchDriverException, IOException {

		registerMbusTestDriver();

		setupTestDevices();

		channelAccess.discoverDevices("mbus", "/dev/ttyUSB2", null);
	}

	//Anfang Clara

	@Test
	public void testsetMultipleChannelValues() throws ChannelAccessException, ChannelConfigurationException,
			IllegalConversionException, NoSuchDeviceException, NoSuchChannelException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		List<ChannelLocator> channelLocators = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100);

		List<Value> values = new LinkedList<Value>();

		values.add(new LongValue(1012));
		values.add(new LongValue(700));

		channelAccess.setMultipleChannelValues(channelLocators, values);

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}

		SampledValue sampledValue = channelAccess.getChannelValue(channelLocators.get(0));

		assertEquals(1012, sampledValue.getValue().getIntegerValue());

		sampledValue = channelAccess.getChannelValue(channelLocators.get(1));

		assertEquals(700, sampledValue.getValue().getIntegerValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testsetMultipleChannelValuesMissingValues() throws ChannelAccessException,
			ChannelConfigurationException, IllegalConversionException, NoSuchDeviceException, NoSuchChannelException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		List<ChannelLocator> channelLocators = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100);

		List<Value> values = new LinkedList<Value>();

		values.add(new LongValue(1012));

		channelAccess.setMultipleChannelValues(channelLocators, values);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullsetMultipleChannelValues() throws ChannelAccessException, NoSuchDeviceException,
			NoSuchChannelException {

		channelAccess.setMultipleChannelValues(null, null);

	}

	@Test
	public void testGetMultipleChannelValues() throws ChannelAccessException, ChannelConfigurationException,
			IllegalConversionException, NoSuchDeviceException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		List<ChannelLocator> channelLocators = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100);

		List<Value> values = new LinkedList<Value>();

		values.add(new LongValue(1012));
		values.add(new LongValue(700));

		channelAccess.setMultipleChannelValues(channelLocators, values);

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}

		List<SampledValue> sampledValues = channelAccess.getMultipleChannelValues(channelLocators);

		assertEquals(1012, sampledValues.get(0).getValue().getIntegerValue());

		assertEquals(700, sampledValues.get(1).getValue().getIntegerValue());
	}

	@Test(expected = NullPointerException.class)
	public void testNullMultipleChannelValues() {

		channelAccess.getMultipleChannelValues(null);
	}

	@Test
	public void testNullMultipleChannelValues2() {

		List<SampledValue> sampledValues = channelAccess.getMultipleChannelValues(new LinkedList<ChannelLocator>());
		assertEquals(sampledValues.size(), 0);
	}

	@Test(expected = NoSuchDriverException.class)
	public void testNoChannelFoundForDevice() throws UnsupportedOperationException, NoSuchInterfaceException,
			NoSuchDriverException, NoSuchDeviceException, IOException {

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);
		channelAccess.discoverChannels(deviceLocator);
	}

	@Test
	public void testDiscoverChannels() throws ChannelConfigurationException, UnsupportedOperationException,
			NoSuchInterfaceException, NoSuchDriverException, NoSuchDeviceException, IOException {

		registerMbusTestDriver();

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		Device device = ((TestDriver) testDriver).addDevice(deviceLocator);

		device.addChannel(channelAccess.getChannelLocator("02/862a", deviceLocator));
		device.addChannel(channelAccess.getChannelLocator("02/823b", deviceLocator));

		List<ChannelLocator> channels = channelAccess.discoverChannels(deviceLocator);

		assertEquals(2, channels.size());

		assertEquals("02/862a", channels.get(0).getChannelAddress());
		assertEquals("02/823b", channels.get(1).getChannelAddress());
	}

	@Test
	public void testDiscoverChannelsAsync() throws UnsupportedOperationException, NoSuchInterfaceException,
			NoSuchDriverException, IOException {

		registerMbusTestDriver();

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		Device device = ((TestDriver) testDriver).addDevice(deviceLocator);

		device.addChannel(channelAccess.getChannelLocator("02/862a", deviceLocator));
		device.addChannel(channelAccess.getChannelLocator("02/823b", deviceLocator));
		device.addChannel(channelAccess.getChannelLocator("04/822f", deviceLocator));

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
	}

	@Test(expected = ChannelConfigurationException.class)
	public void testDeletionOfDeletedChannel() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		addChannelsToConfigurationForDevice1(channelAccess, 100);

		channelAccess.deleteChannel(channelLocator);
		channelAccess.deleteChannel(channelLocator);
	}

	@Test(expected = ChannelAccessException.class)
	public void testWriteChannelWithChannelNotAvailable() throws ChannelConfigurationException, ChannelAccessException,
			NoSuchDeviceException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 100);

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		channelAccess.deleteChannel(channelLocator);

		Value value = new LongValue(1012);

		channelAccess.setChannelValue(channelLocator, value);
	}

	@Test
	public void testDeleteChannelFromConfiguration2() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 1000);

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		channelAccess.deleteChannel(channelLocator);

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(1, channels.size());

		channelLocator = channelAccess.getChannelLocator("02/823b", deviceLocator);

		channelAccess.deleteChannel(channelLocator);

		channels = channelAccess.getAllConfiguredChannels();

		assertEquals(0, channels.size());
	}

	@Test
	public void testDeleteChannelFromConfiguration3() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 1000);

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		channelAccess.deleteChannel(channelLocator);

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(1, channels.size());

		channelLocator = channelAccess.getChannelLocator("02/823b", deviceLocator);

		addChannelsToConfigurationForDevice2(channelAccess, 1000);

		channels = channelAccess.getAllConfiguredChannels();

		assertEquals(3, channels.size());

		deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p2", null);

		channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		channelAccess.deleteChannel(channelLocator);

		channels = channelAccess.getAllConfiguredChannels();

		assertEquals(2, channels.size());
	}

	@Test
	public void testAddChannelsAfterDeleteFromConfiguration() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 1000);

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		channelAccess.deleteChannel(channelLocator);

		channelLocator = channelAccess.getChannelLocator("02/823b", deviceLocator);

		channelAccess.deleteChannel(channelLocator);

		addChannelsToConfigurationForDevice1(channelAccess, 1000);
	}

	@Test
	public void testWriteSingleChannelNegativeValue() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException, NoSuchDeviceException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 100);

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		Value value = new LongValue(-1012);

		channelAccess.setChannelValue(channelLocator, value);

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}

		SampledValue sampledValue = channelAccess.getChannelValue(channelLocator);

		assertEquals(-1012, sampledValue.getValue().getIntegerValue());

	}

	@Test
	public void testWriteSingleChannelTwice() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException, NoSuchDeviceException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addChannelsToConfigurationForDevice1(channelAccess, 100);

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		Value value = new LongValue(1012);

		channelAccess.setChannelValue(channelLocator, value);

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}

		SampledValue sampledValue = channelAccess.getChannelValue(channelLocator);

		assertEquals(1012, sampledValue.getValue().getIntegerValue());

		value = new LongValue(700);

		channelAccess.setChannelValue(channelLocator, value);

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}

		sampledValue = channelAccess.getChannelValue(channelLocator);

		assertEquals(700, sampledValue.getValue().getIntegerValue());

	}

	@Test(expected = ChannelAccessException.class)
	public void testWriteNonExistingChannel() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException, NoSuchDeviceException {

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		Value value = new LongValue(1012);

		channelAccess.setChannelValue(channelLocator, value);
	}

	@Test(expected = ChannelAccessException.class)
	public void testWriteNullChannel() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException, NoSuchDeviceException {

		ChannelLocator channelLocator = null;

		Value value = new LongValue(1012);

		channelAccess.setChannelValue(channelLocator, value);
	}

	@Test(expected = ChannelAccessException.class)
	public void testReadNullChannel() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		ChannelLocator channelLocator = null;

		channelAccess.getChannelValue(channelLocator);
	}

	@Test
	public void testDriverInstallation2() {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		List<String> drivers = channelAccess.getDriverIds();

		assertEquals(2, drivers.size()); /* assume coap-driver + mbus-test-driver */
	}

	@Test
	public void testExamineExistingConfig() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);
		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/823b", deviceLocator);

		ChannelConfiguration channelConfiguration = channelAccess.getChannelConfiguration(channelLocator);
		channelConfiguration.setDirection(Direction.DIRECTION_INPUT); /* read-only channel */
		channelConfiguration.setSamplingPeriod(100);

		channelAccess.addChannel(channelConfiguration);

		ChannelConfiguration channelConfig2 = channelAccess.getChannelConfiguration(channelLocator);

		assertEquals(channelConfig2.getDirection(), channelConfiguration.getDirection());
		assertEquals(channelConfig2.getSamplingPeriod(), channelConfiguration.getSamplingPeriod());
		assertEquals(channelConfig2.getSamplingPeriod(), 100);

	}

	@Test
	public void testExamineNonExistingConfig() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		ChannelLocator channelLocator = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100).get(1);

		channelAccess.deleteChannel(channelLocator);

		ChannelConfiguration channelConfig2 = channelAccess.getChannelConfiguration(channelLocator);

		assertEquals(channelConfig2.getDirection(), Direction.DIRECTION_INOUT); // Default direction
		assertEquals(channelConfig2.getSamplingPeriod(), 1000); //Default SamplingPeriod
	}

	@Test
	public void testExamineAllConfiguredChannels() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		ChannelLocator channelLocator = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100).get(1);

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertTrue(channelIsListed(channels, channelLocator));
	}

	@Test
	public void testChannelListener() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		List<ChannelLocator> channels = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100);

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

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testChannelListenerReadingInterval() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		List<ChannelLocator> channels = addAndConfigureWritableChannelsForDevice1(channelAccess, 25, 100);

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

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testUpdateListener() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		List<ChannelLocator> channels = addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 100);

		channelAccess.registerUpdateListener(channels, new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				for (SampledValueContainer c : channels) {
					System.out.println("Channel: " + c.getChannelLocator().toString() + ", timestamp: "
							+ c.getSampledValue().getTimestamp());
				}
			}
		});

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testUpdateListenerReadingInterval() throws ChannelConfigurationException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		List<ChannelLocator> channels = addAndConfigureWritableChannelsForDevice1(channelAccess, 25, 100);

		channelAccess.registerUpdateListener(channels, new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				for (SampledValueContainer c : channels) {
					System.out.println("Channel: " + c.getChannelLocator().toString() + ", timestamp: "
							+ c.getSampledValue().getTimestamp());
				}
			}
		});

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReadingInterval() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addAndConfigureWritableChannelsForDevice1(channelAccess, 25, 100);

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(channels.size(), 2);

		ChannelLocator channel1 = channels.get(0);
		ChannelLocator channel2 = channels.get(1);

		SampledValue sampledValue1 = channelAccess.getChannelValue(channel1);
		SampledValue sampledValue2 = channelAccess.getChannelValue(channel2);

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue();

		System.out.println("Value of channel1: " + value1.getIntegerValue() + ", Value of channel2: "
				+ value2.getIntegerValue());

		assertTrue(value1.getIntegerValue() > value2.getIntegerValue());
	}

	@Test
	public void testChannelListeners() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		startDriverBundle(MBusTestDriverActivator.class, "org.ogema.driver.mbus-driver");

		addAndConfigureWritableChannelsForDevice1(channelAccess, 200, 100);

		final List<ChannelLocator> configuredChannels = channelAccess.getAllConfiguredChannels();

		final int[] channelEventOccured = new int[] { 0, 0 };

		channelAccess.registerUpdateListener(configuredChannels, new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {

				if (channels.get(0).getChannelLocator().equals(configuredChannels.get(0))) {
					channelEventOccured[0]++;
				}

				if (channels.get(0).getChannelLocator().equals(configuredChannels.get(1))) {
					channelEventOccured[1]++;
				}
			}
		});

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
		}

		float ratio = (float) channelEventOccured[1] / (float) channelEventOccured[0];

		System.out.println("channelEventOccured[0]:" + channelEventOccured[0] + " channelEventOccured[1]:"
				+ channelEventOccured[1]);

		assertEquals(2.0, ratio, 0.5);

		assertTrue(channelEventOccured[0] > 4);
		assertTrue(channelEventOccured[1] > 8);
	}

	@Test
	public void testReadingInterval2() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		registerMbusTestDriver();

		addAndConfigureWritableChannelsForDevice1(channelAccess, 100, -1);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(channels.size(), 2);

		ChannelLocator channel1 = channels.get(0);
		ChannelLocator channel2 = channels.get(1);

		SampledValue sampledValue1 = channelAccess.getChannelValue(channel1);
		SampledValue sampledValue2 = channelAccess.getChannelValue(channel2);

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue();

		System.out.println("Value of channel1: " + value1.getIntegerValue() + ", Value of channel2: "
				+ value2.getIntegerValue());

		assertTrue(value1.getIntegerValue() > 2 && value1.getIntegerValue() < 6);

		assertEquals(value2.getIntegerValue(), 3);
	}

	@Test
	public void testReadingInterval3() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		registerMbusTestDriver();

		addAndConfigureWritableChannelsForDevice1(channelAccess, -1, 100);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(channels.size(), 2);

		ChannelLocator channel1 = channels.get(0);
		ChannelLocator channel2 = channels.get(1);

		SampledValue sampledValue1 = channelAccess.getChannelValue(channel1);
		SampledValue sampledValue2 = channelAccess.getChannelValue(channel2);

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue();

		System.out.println("Value of channel1: " + value1.getIntegerValue() + ", Value of channel2: "
				+ value2.getIntegerValue());

		assertEquals(value1.getIntegerValue(), 3);

		assertTrue(value2.getIntegerValue() > 2 && value2.getIntegerValue() < 6);
	}

	@Test
	public void testReadingInterval4() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		registerMbusTestDriver();

		addAndConfigureWritableChannelsForDevice1(channelAccess, 0, 100);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(channels.size(), 2);

		ChannelLocator channel2 = channels.get(1);

		SampledValue sampledValue2 = channelAccess.getChannelValue(channel2);

		Value value2 = sampledValue2.getValue();

		System.out.println("Value of channel2: " + value2.getIntegerValue());

		assertTrue(value2.getIntegerValue() > 2 && value2.getIntegerValue() < 6);
	}

	@Test
	public void testReadingInterval5() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		registerMbusTestDriver();

		addAndConfigureWritableChannelsForDevice1(channelAccess, 100, 0);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(channels.size(), 2);

		ChannelLocator channel1 = channels.get(0);

		SampledValue sampledValue1 = channelAccess.getChannelValue(channel1);

		Value value1 = sampledValue1.getValue();

		System.out.println("Value of channel1: " + value1.getIntegerValue());

		assertTrue(value1.getIntegerValue() > 2 && value1.getIntegerValue() < 6);
	}

	@Test(expected = ChannelAccessException.class)
	public void testReadingInterval6() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		registerMbusTestDriver();

		addAndConfigureWritableChannelsForDevice1(channelAccess, 0, 0);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(channels.size(), 2);

		ChannelLocator channel1 = channels.get(0);

		SampledValue sampledValue1 = channelAccess.getChannelValue(channel1);

		sampledValue1.getValue();
	}

	@Test
	public void testReadingInterval8() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		registerMbusTestDriver();

		addAndConfigureWritableChannelsForDevice1(channelAccess, 0, -1);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(channels.size(), 2);

		ChannelLocator channel2 = channels.get(1);

		SampledValue sampledValue2 = channelAccess.getChannelValue(channel2);

		Value value2 = sampledValue2.getValue();

		System.out.println("Value of channel2: " + value2.getIntegerValue());

		assertEquals(value2.getIntegerValue(), 3);
	}

	@Test
	public void testReadingInterval9() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		registerMbusTestDriver();

		addAndConfigureWritableChannelsForDevice1(channelAccess, -1, 0);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		assertEquals(channels.size(), 2);

		ChannelLocator channel1 = channels.get(0);

		SampledValue sampledValue1 = channelAccess.getChannelValue(channel1);

		Value value1 = sampledValue1.getValue();

		System.out.println("Value of channel2: " + value1.getIntegerValue());

		assertEquals(value1.getIntegerValue(), 3);
	}

	@Test
	public void testChannelUpdate() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		registerMbusTestDriver();

		addAndConfigureWritableChannelsForDevice1(channelAccess, 120, 120);

		addAndConfigureWritableOneChannelForDevice1(channelAccess, 100, "862b");

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		ChannelLocator channel1 = channels.get(0);
		ChannelLocator channel2 = channels.get(1);
		ChannelLocator channel3 = channels.get(2);

		SampledValue sampledValue1 = channelAccess.getChannelValue(channel1);
		SampledValue sampledValue2 = channelAccess.getChannelValue(channel2);
		SampledValue sampledValue3 = channelAccess.getChannelValue(channel3);

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue();
		Value value3 = sampledValue3.getValue();

		assertEquals(value2.getIntegerValue(), value1.getIntegerValue());
		assertTrue(value1.getIntegerValue() <= value3.getIntegerValue());
		assertTrue(value3.getIntegerValue() > 2 && value3.getIntegerValue() < 6);

		System.out.println("Value of channel1: " + value1.getIntegerValue() + ", Value of channel2: "
				+ value2.getIntegerValue() + ", Value of channel3: " + value3.getIntegerValue());

	}

	@Test
	public void testReadingInterval10() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		registerMbusTestDriver();

		addAndConfigureWritableOneChannelForDevice1(channelAccess, 100, "824a");
		addAndConfigureWritableOneChannelForDevice1(channelAccess, 25, "832a");
		addAndConfigureWritableOneChannelForDevice1(channelAccess, 25, "862b");
		addAndConfigureWritableOneChannelForDevice1(channelAccess, -1, "862c");
		addAndConfigureWritableOneChannelForDevice1(channelAccess, 131, "862d");
		addAndConfigureWritableOneChannelForDevice1(channelAccess, 0, "862e");

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		ChannelLocator channel1 = channels.get(0);
		ChannelLocator channel2 = channels.get(1);
		ChannelLocator channel3 = channels.get(2);
		ChannelLocator channel4 = channels.get(3);
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
	public void testReadingInterval11() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		registerMbusTestDriver();

		addAndConfigureWritableOneChannelForDevice1(channelAccess, 100, "824a");

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		addAndConfigureWritableOneChannelForDevice1(channelAccess, 25, "832a");

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		ChannelLocator channel1 = channels.get(0);
		ChannelLocator channel2 = channels.get(1);

		SampledValue sampledValue1 = channelAccess.getChannelValue(channel1);
		SampledValue sampledValue2 = channelAccess.getChannelValue(channel2);

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue();

		assertTrue(value1.getIntegerValue() > 4 && value1.getIntegerValue() < 12);
		assertTrue(value2.getIntegerValue() > 8 && value2.getIntegerValue() < 24);
	}

	@Test
	public void testReadingInterval12() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		registerMbusTestDriver();

		addAndConfigureWritableOneChannelForDevice1(channelAccess, 100, "824a");
		addAndConfigureWritableOneChannelForDevice1(channelAccess, 25, "832a");
		addAndConfigureWritableOneChannelForDevice1(channelAccess, 131, "862d");

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		List<ChannelLocator> channels = channelAccess.getAllConfiguredChannels();

		channels.get(0);
		ChannelLocator channel2 = channels.get(1);
		channels.get(2);

		channelAccess.deleteChannel(channel2);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
	}

	@Test
	public void testReadingIntervalSeveralDevices() throws ChannelConfigurationException, ChannelAccessException,
			IllegalConversionException {

		registerMbusTestDriver();

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);
		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		ChannelConfiguration channelConfiguration = channelAccess.getChannelConfiguration(channelLocator);
		channelConfiguration.setSamplingPeriod(100);

		channelAccess.addChannel(channelConfiguration);

		DeviceLocator deviceLocator2 = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p2", null);
		ChannelLocator channelLocator2 = channelAccess.getChannelLocator("02/862b", deviceLocator2);

		ChannelConfiguration channelConfiguration2 = channelAccess.getChannelConfiguration(channelLocator2);
		channelConfiguration2.setSamplingPeriod(100);

		channelAccess.addChannel(channelConfiguration2);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		SampledValue sampledValue1 = channelAccess.getChannelValue(channelLocator);
		SampledValue sampledValue2 = channelAccess.getChannelValue(channelLocator2);

		Value value1 = sampledValue1.getValue();
		Value value2 = sampledValue2.getValue();

		assertTrue(value1.getIntegerValue() > 2 && value1.getIntegerValue() < 6);
		assertTrue(value2.getIntegerValue() > 2 && value2.getIntegerValue() < 6);

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

	private ChannelLocator addAndConfigureWritableOneChannelForDevice1(ChannelAccess channelManagement,
			int samplingPeriod, String channelname) throws ChannelConfigurationException {

		DeviceLocator deviceLocator = channelManagement.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);
		ChannelLocator channelLocator = channelManagement.getChannelLocator("02/" + channelname, deviceLocator);

		ChannelConfiguration channelConfiguration = channelManagement.getChannelConfiguration(channelLocator);
		channelConfiguration.setSamplingPeriod(samplingPeriod);

		channelManagement.addChannel(channelConfiguration);

		return channelLocator;
	}

	private List<ChannelLocator> addAndConfigureWritableChannelsForDevice1(ChannelAccess channelManagement,
			int samplingPeriod1, int samplingPeriod2) throws ChannelConfigurationException {

		List<ChannelLocator> channelLocators = new LinkedList<ChannelLocator>();

		DeviceLocator deviceLocator = channelManagement.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);
		ChannelLocator channelLocator = channelManagement.getChannelLocator("02/862a", deviceLocator);

		ChannelConfiguration channelConfiguration = channelManagement.getChannelConfiguration(channelLocator);
		channelConfiguration.setSamplingPeriod(samplingPeriod1);

		channelManagement.addChannel(channelConfiguration);

		channelLocators.add(channelLocator);

		channelLocator = channelManagement.getChannelLocator("02/823b", deviceLocator);

		channelConfiguration = channelManagement.getChannelConfiguration(channelLocator);
		channelConfiguration.setSamplingPeriod(samplingPeriod2);

		channelManagement.addChannel(channelConfiguration);
		channelLocators.add(channelLocator);

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

	private boolean channelIsListed(List<ChannelLocator> channels, ChannelLocator wantedChannel) {

		if (channels == null || wantedChannel == null) {
			return false;
		}

		for (ChannelLocator c : channels) {

			if (c.equals(wantedChannel)) {
				return true;
			}
		}

		return false;
	}

	//Ende Clara

	private void setupTestDevices() {
		TestDriver mbusTestDriver = (TestDriver) this.testDriver;

		DeviceLocator deviceLocator;

		deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);
		mbusTestDriver.addDevice(deviceLocator);

		deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p4", null);
		mbusTestDriver.addDevice(deviceLocator);

		deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p10", null);
		mbusTestDriver.addDevice(deviceLocator);

		deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB1", "p2", null);
		mbusTestDriver.addDevice(deviceLocator);
	}

	private void addChannelsToConfigurationForDevice1(ChannelAccess channelManagement, int samplingPeriod)
			throws ChannelConfigurationException {
		DeviceLocator deviceLocator = channelManagement.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		configureChannelsForDevice(channelManagement, samplingPeriod, deviceLocator);
	}

	private void addChannelsToConfigurationForDevice2(ChannelAccess channelManagement, int samplingPeriod)
			throws ChannelConfigurationException {
		DeviceLocator deviceLocator = channelManagement.getDeviceLocator("mbus", "/dev/ttyUSB0", "p2", null);

		configureChannelsForDevice(channelManagement, samplingPeriod, deviceLocator);
	}

	private void configureChannelsForDevice(ChannelAccess channelManagement, int samplingPeriod,
			DeviceLocator deviceLocator) throws ChannelConfigurationException {
		ChannelLocator channelLocator;

		ChannelConfiguration channelConfiguration;

		channelLocator = channelManagement.getChannelLocator("02/862a", deviceLocator);

		channelConfiguration = channelManagement.getChannelConfiguration(channelLocator);
		channelConfiguration.setSamplingPeriod(samplingPeriod);

		channelManagement.addChannel(channelConfiguration);

		channelLocator = channelManagement.getChannelLocator("02/823b", deviceLocator);

		channelConfiguration = channelManagement.getChannelConfiguration(channelLocator);
		channelConfiguration.setSamplingPeriod(samplingPeriod);
		channelConfiguration.setDirection(Direction.DIRECTION_INPUT); /* read-only channel */

		channelManagement.addChannel(channelConfiguration);

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
		testDriver = new TestDriver("mbus", "wired mbus test driver");

		mbusDriverService = ctx.registerService(ChannelDriver.class, testDriver, null);
	}

	private void unregisterMbusTestDriver() {
		mbusDriverService.unregister();
	}

	private void startDriverBundle(Class<?> activator, String symbolicName) {
		InputStream inputStream = TinyBundles.bundle().add(activator).add(TestDriverActivator.class).add(
				TestDriver.class).set(Constants.BUNDLE_SYMBOLICNAME, symbolicName).set(Constants.BUNDLE_ACTIVATOR,
				activator.getName()).set(Constants.IMPORT_PACKAGE, "*").build(TinyBundles.withBnd());
		try {
			ctx.installBundle(activator.getName(), inputStream).start();

		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

	private <T extends Object> T lookupService(Class<T> serviceClass) {
		ServiceReference<T> serviceRef = ctx.getServiceReference(serviceClass);

		if (serviceRef != null) {
			return ctx.getService(serviceRef);
		}

		return null;
	}

	// @formatter:on
}
