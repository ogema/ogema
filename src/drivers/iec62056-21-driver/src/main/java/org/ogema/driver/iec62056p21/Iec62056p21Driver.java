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
package org.ogema.driver.iec62056p21;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.openmuc.j62056.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = false)
@Service(ChannelDriver.class)
public class Iec62056p21Driver implements ChannelDriver {

	private final static Logger logger = LoggerFactory.getLogger(Iec62056p21Driver.class);

	private final Map<String, Iec62056p21Connection> connections = new HashMap<String, Iec62056p21Connection>();

	@Override
	public String getDriverId() {
		return "iec62056p21";
	}

	@Override
	public String getDescription() {
		return "This is a driver to communicate with IEC 62056-21 Devices";
	}

	@Override
	public void startDeviceScan(String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, IOException {

		throw new UnsupportedOperationException();
	}

	@Override
	public void abortDeviceScan(String interfaceId, String filter) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void startChannelScan(DeviceLocator device, ChannelScanListener listener)
			throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	@Override
	public List<ChannelLocator> getChannelList(DeviceLocator device) throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	@Override
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {

		long now = System.currentTimeMillis();
		List<DataSet> dataSets;
		Iec62056p21Connection connection = getConnection(channels);

		try {
			dataSets = connection.read();
			for (SampledValueContainer channel : channels) {
				assignDataToChannel(now, dataSets, channel);
			}
		} catch (Exception e) {
			// catch all exceptions here. possible handling is either throwing an IOException or set Quality to BAD
			throw new IOException("Unable to perform readChannels due to: " + e.getMessage());
		}

	}

	/**
	 * Assings the received data to the corresponding channels
	 */
	private void assignDataToChannel(long now, List<DataSet> dataSets, SampledValueContainer channel) {

		boolean assigned = false;

		// iterate over all dataSets and check if a DataSet Id matches with a channel address
		for (DataSet dataSet : dataSets) {

			if (dataSet.getId().equals(channel.getChannelLocator().getChannelAddress())) {
				Value value = convertDataSetToValue(dataSet);
				if (value != null) {

					if (logger.isTraceEnabled()) {
						logger.trace("dataSet id: " + dataSet.getId() + "\t value " + dataSet.getValue() + "\t unit "
								+ dataSet.getUnit());
					}

					channel.setSampledValue(new SampledValue(value, now, Quality.GOOD));
				}
				else {
					logger.warn("DataSet value of Id " + dataSet.getId() + " is null. Set quality to BAD.");
					channel.setSampledValue(new SampledValue(null, now, Quality.BAD));
				}
				assigned = true;
				break;
			}
		}

		// check the case where channelAddress doesn't match with any of the dataSet ids (e.g. dataSets List is empty).
		// So setSampledValue wouldn't be
		// called for this channel. The cause of this case is most probably a wrong configuration of the channel
		if (!assigned) {
			logger.warn("No matching dataSet ID found for channel " + channel.getChannelLocator().getChannelAddress()
					+ " Set quality to BAD.");
			channel.setSampledValue(new SampledValue(null, now, Quality.BAD));
		}
	}

	private Value convertDataSetToValue(DataSet dataSet) {

		Value value = null;

		String valueStr = dataSet.getValue();
		if (valueStr != null) {
			try {
				value = new DoubleValue(Double.parseDouble(valueStr));
			} catch (NumberFormatException e) {
				value = new StringValue(valueStr);
			}
		}
		return value;
	}

	/**
	 * Looks up a previous created connection for the channels which should be sampled
	 */
	private Iec62056p21Connection getConnection(List<SampledValueContainer> channels) throws IOException {

		Iec62056p21Connection connection = null;

		if (channels.size() != 0) {
			// read channels method is called separately for each device. so we just get the device address of the first
			// channel in the list. the other device addresses should be the same
			String deviceAddress = channels.get(0).getChannelLocator().getDeviceLocator().getDeviceAddress();
			connection = connections.get(deviceAddress);
			if (connection == null) {
				throw new IOException("Read channels failed. Unable to find deviceAddress: " + deviceAddress
						+ " in connection list. Make sure channelAdded method is called before "
						+ "accessing channel via readChannels method");
			}
		}
		else {
			throw new IOException("This shouldn't happen. Read channels failed since channels size = 0");
		}

		return connection;
	}

	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException, NoSuchDeviceException, NoSuchChannelException, IOException {

		throw new UnsupportedOperationException();
	}

	@Override
	public void writeChannels(List<ValueContainer> channels) throws UnsupportedOperationException, IOException,
			NoSuchDeviceException, NoSuchChannelException {

		throw new UnsupportedOperationException();
	}

	@Override
	public void shutdown() {
		// disconnect all connections
		Iterator<Entry<String, Iec62056p21Connection>> it = connections.entrySet().iterator();
		while (it.hasNext()) {
			Iec62056p21Connection connection = (Iec62056p21Connection) it.next();
			connection.disconnect();
		}
		// clear connection list
		connections.clear();
	}

	/**
	 * Adds the channel to an existing connection. If no matching connection exists, it first creates the connection for
	 * the device the channel belongs to.
	 */
	@Override
	public void channelAdded(ChannelLocator channel) {

		DeviceLocator deviceLocator = channel.getDeviceLocator();
		String deviceAddress = deviceLocator.getDeviceAddress();
		Iec62056p21Connection connection = connections.get(deviceAddress);

		if (connection == null) {
			try {
				connection = new Iec62056p21Connection(deviceAddress, deviceLocator.getParameters());
				connections.put(deviceAddress, connection);
			} catch (ConnectionException e) {
				e.printStackTrace();
			}
		}
		else {
			connection.addChannel(channel);
		}
	}

	@Override
	public void channelRemoved(ChannelLocator channel) {

		Iec62056p21Connection connection = connections.get(channel.getDeviceLocator().getDeviceAddress());
		connection.removeChannel(channel);

		// TODO: remove connection if no channel is assigned to it anymore?
	}

	@Override
	public void addDeviceListener(DeviceListener listener) {
		throw new UnsupportedOperationException("Driver does not support device listener");
	}

	@Override
	public void removeDeviceListener(DeviceListener listener) {
		throw new UnsupportedOperationException("Driver does not support device listener");
	}

	@Override
	public void writeChannel(ChannelLocator channelLocator, Value value) throws UnsupportedOperationException, IOException,
			NoSuchDeviceException, NoSuchChannelException {
		throw new UnsupportedOperationException("Driver does not support writeChannel method");
	}

}
