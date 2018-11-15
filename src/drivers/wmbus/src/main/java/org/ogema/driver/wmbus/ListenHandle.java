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
