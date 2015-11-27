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
package org.ogema.driver.zwave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.zwave.manager.NodeValue;

/**
 * 
 * @author baerthbn
 * 
 */
public class Channel {

	private NodeValue nodeValue;
	private SampledValueContainer sampledValueContainer;
	private ChannelUpdateListener channelEventListener;
	private List<SampledValueContainer> sampledValueContainerList = new ArrayList<SampledValueContainer>();
	private ChannelLocator locator;

	// private final Logger logger = org.slf4j.LoggerFactory.getLogger("zwave-driver");

	public Channel(ChannelLocator locator, Device dev) {
		this.locator = locator;
		String[] splitAddress = locator.getChannelAddress().split(":");
		String channelAddress = splitAddress[0] + ":" + splitAddress[1] + ":" + splitAddress[2];
		nodeValue = dev.getNode().getValues().get(channelAddress);
	}

	/**
	 * Implementation for synchronous read from LL to OGEMA
	 * 
	 * @param connection
	 * @return SampledValue
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public SampledValue readValue(Connection connection) throws IOException, UnsupportedOperationException {
		return new SampledValue(nodeValue.getOGEMAValue(), System.currentTimeMillis(), Quality.GOOD);
	}

	/**
	 * Implementation for synchronous & asynchronous write from OGEMA to LL. In this case a asynchronous version is used
	 * 
	 * @param connection
	 * @param value
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public void writeValue(Connection connection, Value value) throws IOException, UnsupportedOperationException {
		if (nodeValue.readOnly())
			throw new UnsupportedOperationException();
		nodeValue.setOGEMAValue(value);
	}

	/**
	 * Sets listener for asynchronous channel. Listens on LL side an notifies OGEMA
	 * 
	 * @param container
	 * @param listener
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public void setEventListener(SampledValueContainer container, ChannelUpdateListener listener) throws IOException,
			UnsupportedOperationException {
		nodeValue.setChannel(this);
		nodeValue.setListener(true);
		sampledValueContainer = container;
		channelEventListener = listener;
		sampledValueContainerList.add(container);
	}

	/**
	 * removes listener for asynchronous channel. Listens on LL side an notifies OGEMA
	 */
	public void removeUpdateListener() {
		nodeValue.setListener(false);
		channelEventListener = null;
		sampledValueContainerList.clear();
		sampledValueContainer = null;
	}

	/**
	 * updates listener for asynchronous channel. Listens on LL side an notifies OGEMA
	 */
	public void updateListener() {
		if (!nodeValue.writeOnly()) {
			sampledValueContainer.setSampledValue(new SampledValue(nodeValue.getOGEMAValue(), System
					.currentTimeMillis(), Quality.GOOD));
			channelEventListener.channelsUpdated(sampledValueContainerList);
		}
	}

	public ChannelLocator getChannelLocator() {
		return locator;
	}
}
