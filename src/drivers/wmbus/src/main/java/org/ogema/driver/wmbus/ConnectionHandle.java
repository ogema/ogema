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
