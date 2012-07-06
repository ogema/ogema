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

import java.util.List;

import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;

public class ListenHandle {

	private String channelAddress;
	private String deviceAddress;
	private ChannelUpdateListener listener;
	private List<SampledValueContainer> list;

	public ListenHandle(String channelAddress, String deviceAddress, ChannelUpdateListener listener,
			List<SampledValueContainer> channels) {
		setChannelAddress(channelAddress);
		setDeviceAddress(deviceAddress);
		setListener(listener);
		setList(channels);
	}

	public ChannelUpdateListener getListener() {
		return listener;
	}

	public void setListener(ChannelUpdateListener listener) {
		this.listener = listener;
	}

	public String getDeviceAddress() {
		return deviceAddress;
	}

	public void setDeviceAddress(String deviceAddress) {
		this.deviceAddress = deviceAddress;
	}

	public String getChannelAddress() {
		return channelAddress;
	}

	public void setChannelAddress(String channelAddress) {
		this.channelAddress = channelAddress;
	}

	public List<SampledValueContainer> getList() {
		return list;
	}

	public void setList(List<SampledValueContainer> list) {
		this.list = list;
	}

}
