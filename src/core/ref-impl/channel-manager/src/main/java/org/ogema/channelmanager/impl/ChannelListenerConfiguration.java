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
package org.ogema.channelmanager.impl;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;

public class ChannelListenerConfiguration {

	private ChannelLocator channelLocator;
	private boolean updateListener;
	private boolean valueListener;

	public ChannelListenerConfiguration(ChannelLocator channelLocator, boolean updateListener, boolean valueListener) {
		this.channelLocator = channelLocator;
		this.updateListener = updateListener;
		this.valueListener = valueListener;
	}

	public ChannelLocator getChannelLocator() {
		return this.channelLocator;
	}

	public void setChannelLocator(ChannelLocator channelLocator) {
		this.channelLocator = channelLocator;
	}

	public boolean getUpdateListener() {
		return this.updateListener;
	}

	public void setUpdateListener(boolean updateListener) {
		this.updateListener = updateListener;
	}

	public boolean getValueListener() {
		return this.valueListener;
	}

	public void setValueListener(boolean valueListener) {
		this.valueListener = valueListener;
	}
}
