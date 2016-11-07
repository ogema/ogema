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
 * 
 * An Exception thrown if channel doesn't exist
 */
public class NoSuchChannelException extends Exception {

	private static final long serialVersionUID = 3367332317411774073L;

	private ChannelLocator channelLocator = null;

	/**
	 * Constructor
	 * 
	 * @param channelLocator
	 */
	public NoSuchChannelException(ChannelLocator channelLocator) {
		this.channelLocator = channelLocator;
	}

	/**
	 * 
	 * @return channelLocator
	 */
	public ChannelLocator getChannelLocator() {
		return channelLocator;
	}
}
