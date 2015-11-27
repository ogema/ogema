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
package org.ogema.core.channelmanager.driverspi;

/**
 * Monitor available Channels
 * 
 */
public interface ChannelScanListener {
	/**
	 * 
	 * @param channel
	 */
	public void channelFound(ChannelLocator channel);

	/**
	 * 
	 * @param success
	 */
	public void finished(boolean success);

	/** 0.0:not started, 1.0:finished */
	public void progress(float ratio);
}
