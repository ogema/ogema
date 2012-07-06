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
package org.ogema.driver.wmbus;

import java.util.LinkedList;
import java.util.List;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.driver.wmbus.WirelessMBusDriver.TRANCECIVER;

/**
 * Class representing an MBus Connection.<br>
 * This class will bind to the local com-interface.<br>
 * 
 */
public class ConnectionHandle {
	private ChannelUpdateListener listener;
	private int deviceCounter = 1;
	private final Object mBusSap;
	TRANCECIVER tranceciver;
	private boolean open;
	private final List<ChannelLocator> channelList = new LinkedList<ChannelLocator>();

	public ConnectionHandle(Object mBusSap, ChannelLocator channelLocator, TRANCECIVER tranceciver) {
		this.mBusSap = mBusSap;
		this.tranceciver = tranceciver;
		this.channelList.add(channelLocator);

	}

	public ChannelUpdateListener getListener() {
		return listener;
	}

	public void setListener(ChannelUpdateListener listener) {
		this.listener = listener;
	}

	public TRANCECIVER getTranceciver() {
		return tranceciver;
	}

	public Object getMBusSap() {
		return mBusSap;
	}

	public void increaseDeviceCounter() {
		deviceCounter++;
	}

	public void decreaseDeviceCounter() {
		deviceCounter--;
	}

	public int getDeviceCounter() {
		return deviceCounter;
	}

	public boolean isOpen() {
		return open;
	}

	public void open() {
		open = true;
	}

	public void close() {
		open = false;
	}

	public List<ChannelLocator> getChannels() {
		return channelList;
	}

}
