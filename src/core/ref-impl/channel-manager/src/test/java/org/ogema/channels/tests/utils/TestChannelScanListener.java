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
package org.ogema.channels.tests.utils;

import java.util.LinkedList;
import java.util.List;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;

public class TestChannelScanListener implements ChannelScanListener {

	public boolean finished = false;
	public boolean success = false;
	public float progress = 0.f;
	public List<ChannelLocator> foundChannels = null;

	@Override
	public void channelFound(ChannelLocator channel) {
		if (foundChannels == null) {
			foundChannels = new LinkedList<ChannelLocator>();
		}

		foundChannels.add(channel);
	}

	@Override
	public void finished(boolean success) {
		finished = true;
		this.success = success;
	}

	@Override
	public void progress(float ratio) {
		progress = ratio;
	}

}
