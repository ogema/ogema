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
package org.ogema.channelmapperv2.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.DeviceListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.NoSuchChannelException;
import org.ogema.core.channelmanager.driverspi.NoSuchDeviceException;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

public class TestDriver implements ChannelDriver {
	
	final static String DRIVER_ID = "testDriver";
	final static DeviceLocator DEVICE = new DeviceLocator(DRIVER_ID, "testIf", "testAddress", "param0:0");
	final static ChannelLocator CHANNEL_0 = new ChannelLocator("channel0", DEVICE);
	final static ChannelLocator CHANNEL_1 = new ChannelLocator("channel1", DEVICE);
	private final FrameworkClock clock;
	
	volatile float channel0Value = 0;
	volatile float channel1Value = 0;
	
	volatile CountDownLatch readLatch;
	volatile CountDownLatch writeLatch;
	volatile CountDownLatch channelAddedLatch;
	volatile CountDownLatch channelRemovedLatch;
	
	volatile List<SampledValueContainer> lastReadChannels;
	volatile ChannelLocator lastAddedChannel;
	volatile ChannelLocator lastRemovedChannel;
	
	public TestDriver(FrameworkClock clock) {
		reset(1);
		this.clock = clock;
	}
	
	public void reset(int expectedEvents) {
		this.readLatch = new CountDownLatch(expectedEvents);
		this.writeLatch = new CountDownLatch(expectedEvents);
		this.channelAddedLatch = new CountDownLatch(expectedEvents);
		this.channelRemovedLatch = new CountDownLatch(expectedEvents);
	}
	
	@Override
	public String getDriverId() {
		return DRIVER_ID;
	}

	@Override
	public String getDescription() {
		return DRIVER_ID;
	}

	@Override
	public void startDeviceScan(String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, IOException {
		listener.deviceFound(DEVICE);
	}

	@Override
	public void abortDeviceScan(String interfaceId, String filter) {
	}

	@Override
	public void startChannelScan(DeviceLocator device, ChannelScanListener listener)
			throws UnsupportedOperationException {
		listener.channelFound(CHANNEL_0);
		listener.channelFound(CHANNEL_1);
	}

	@Override
	public List<ChannelLocator> getChannelList(DeviceLocator device) throws UnsupportedOperationException {
		return Arrays.asList(CHANNEL_0,CHANNEL_1);
	}

	@Override
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {
		this.lastReadChannels = new ArrayList<>(channels);
		final long t = clock != null ? clock.getExecutionTime() : System.currentTimeMillis();
		for (SampledValueContainer svc : channels) {
			final ChannelLocator channel = svc.getChannelLocator();
			final Float value = channel.equals(CHANNEL_0) ? channel0Value : channel.equals(CHANNEL_1) ? channel1Value : null;
			if (value == null)
				continue;
			svc.setSampledValue(new SampledValue(new FloatValue(value), t, Quality.GOOD));
		}
		this.readLatch.countDown();
	}

	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException, NoSuchDeviceException, NoSuchChannelException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeChannels(List<ValueContainer> channels)
			throws UnsupportedOperationException, IOException, NoSuchDeviceException, NoSuchChannelException {
		for (ValueContainer vc : channels) {
			writeChannel(vc.getChannelLocator(), vc.getValue());
		}
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void channelAdded(ChannelLocator channel) {
		this.lastAddedChannel = channel;
		this.channelAddedLatch.countDown();
	}

	@Override
	public void channelRemoved(ChannelLocator channel) {
		this.lastRemovedChannel = channel;
		this.channelRemovedLatch.countDown();
	}

	@Override
	public void addDeviceListener(DeviceListener listener) {
		listener.deviceAdded(DEVICE);
	}

	@Override
	public void removeDeviceListener(DeviceListener listener) {
	}

	@Override
	public void writeChannel(final ChannelLocator channelLocator, final Value value)
			throws UnsupportedOperationException, IOException, NoSuchDeviceException, NoSuchChannelException {
		Objects.requireNonNull(channelLocator);
		final float val = value.getFloatValue();
		if (channelLocator.equals(CHANNEL_0))
			channel0Value = val;
		else if (channelLocator.equals(CHANNEL_1))
			channel1Value = val;
		else 
			throw new IllegalArgumentException("Unknown channel " + channelLocator);
		writeLatch.countDown();
	}

}
