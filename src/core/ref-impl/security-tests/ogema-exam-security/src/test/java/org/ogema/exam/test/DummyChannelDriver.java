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
package org.ogema.exam.test;

import java.io.IOException;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
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
import org.ogema.core.channelmanager.measurements.Value;

@Component(immediate = true)
@Service({ ChannelDriver.class })
public class DummyChannelDriver implements ChannelDriver {

	static final String DRIVER_ID = "ogema-exam-security-test-driver";

	@Override
	public String getDriverId() {
		return DRIVER_ID;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void startDeviceScan(String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void abortDeviceScan(String interfaceId, String filter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startChannelScan(DeviceLocator device, ChannelScanListener listener)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ChannelLocator> getChannelList(DeviceLocator device) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException, NoSuchDeviceException, NoSuchChannelException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeChannels(List<ValueContainer> channels)
			throws UnsupportedOperationException, IOException, NoSuchDeviceException, NoSuchChannelException {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void channelAdded(ChannelLocator channel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void channelRemoved(ChannelLocator channel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDeviceListener(DeviceListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeDeviceListener(DeviceListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeChannel(ChannelLocator channelLocator, Value value)
			throws UnsupportedOperationException, IOException, NoSuchDeviceException, NoSuchChannelException {
		// TODO Auto-generated method stub

	}

}
