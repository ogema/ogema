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
package org.ogema.driver.xbee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.xbee.frames.TransmitRequest;
import org.ogema.driver.xbee.manager.Endpoint;
import org.ogema.driver.xbee.manager.RemoteDevice;
import org.ogema.driver.xbee.manager.XBeeDevice;
import org.slf4j.Logger;

/**
 * This channel represents a XBee device/endpoint instead of a cluster. In this case the XBee device is expected to
 * serve only one kind of data without the use of clusters.
 * 
 * @author puschas
 * 
 */
public class XBeeChannel extends Channel {

	private XBeeDevice xBeeDevice;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");
	private SampledValueContainer sampledValueContainer;
	private ChannelUpdateListener channelUpdateListener;
	private List<SampledValueContainer> sampledValueContainerList = new ArrayList<SampledValueContainer>();

	protected XBeeChannel(ChannelLocator locator, String[] splitAddress, Device dev) {
		super(locator);
		this.device = dev;
		Endpoint endpoint = dev.getEndpoint();
		RemoteDevice remoteDevice = endpoint.getDevice();
		if (remoteDevice instanceof XBeeDevice) {
			xBeeDevice = (XBeeDevice) remoteDevice;
		} // TODO what else? Should not happen...
		else {
			logger.info("Not instanceof XBeeDevice");
			// TODO return nullpointer exception?
		}
	}

	@Override
	public synchronized SampledValue readValue(Connection connection) throws IOException, UnsupportedOperationException {
		Value value = xBeeDevice.getValue();
		return new SampledValue(value, System.currentTimeMillis(), Quality.GOOD); // There is no quality...
	}

	/**
	 * Use this method to send transmit request frames. The payload passed as byte array value will be built into frames
	 * by the FrameFactory and then sent.
	 */
	@Override
	public void writeValue(Connection connection, Value value) throws IOException, UnsupportedOperationException {
		TransmitRequest transmitRequest = new TransmitRequest(xBeeDevice.getAddress64Bit(), xBeeDevice
				.getAddress16Bit(), value.getByteArrayValue());
		connection.localDevice.sendMessage(transmitRequest);
	}

	@Override
	public void setUpdateListener(SampledValueContainer container, ChannelUpdateListener listener) throws IOException,
			UnsupportedOperationException {
		xBeeDevice.setChannel(this);
		xBeeDevice.setListener(true);
		sampledValueContainer = container;
		channelUpdateListener = listener;
		sampledValueContainerList.add(container); // TODO the whole solution with a list for one container is ugly...

	}

	@Override
	public void removeUpdateListener() {
		xBeeDevice.setListener(false);
		channelUpdateListener = null;
		sampledValueContainerList.clear();
		sampledValueContainer = null;
	}

	public void updateListener() {
		// TODO adjust quality
		sampledValueContainer.setSampledValue(new SampledValue(xBeeDevice.getValue(), System.currentTimeMillis(),
				Quality.GOOD));
		channelUpdateListener.channelsUpdated(sampledValueContainerList); // TODO the whole solution
		// with a list for one
		// container is ugly...
	}

}
