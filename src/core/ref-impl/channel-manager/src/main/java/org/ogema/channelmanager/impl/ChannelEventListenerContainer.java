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
package org.ogema.channelmanager.impl;

import java.util.LinkedList;
import java.util.List;

import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;

public class ChannelEventListenerContainer {

	private final ChannelEventListener listener;
	private List<SampledValueContainer> channels = null;
	private List<SampledValueContainer> changedChannels = null;

	public ChannelEventListenerContainer(ChannelEventListener listener) {

		this.listener = listener;
		this.channels = new LinkedList<SampledValueContainer>();
	}

	public void addChannel(SampledValueContainer channel) {

		boolean alreadyAdded = false;

		for (SampledValueContainer c : channels) {

			if (c.equals(channel)) {

				alreadyAdded = true;
			}
		}

		if (!alreadyAdded) {
			this.channels.add(channel);
		}
	}

	public void removeChannel(SampledValueContainer channel) {
		this.channels.remove(channel);
	}

	public void resetChangedChannels() {
		this.changedChannels = new LinkedList<SampledValueContainer>();
	}

	public void addChangedChannel(SampledValueContainer channel) {

		boolean alreadyAdded = false;

		for (SampledValueContainer c : changedChannels) {

			if (c.equals(channel)) {

				alreadyAdded = true;
			}
		}

		if (!alreadyAdded) {
			this.changedChannels.add(channel);
		}
	}

	public List<SampledValueContainer> getChannels() {
		return this.channels;
	}

	public List<SampledValueContainer> getChangedChannels() {
		return this.changedChannels;
	}

	public ChannelEventListener getListener() {
		return this.listener;
	}
}
