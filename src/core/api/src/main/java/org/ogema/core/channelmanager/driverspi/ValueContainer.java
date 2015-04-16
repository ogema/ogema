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

import org.ogema.core.channelmanager.measurements.Value;

/**
 * A Container for a Value and a ChannelLocator,
 * 
 */
public class ValueContainer {

	private Value value;
	private final ChannelLocator channelLocator;

	/**
	 * Constructor for ValueContainer
	 * 
	 * @param channelLocator
	 * @param value
	 */
	public ValueContainer(ChannelLocator channelLocator, Value value) {
		super();
		this.value = value;
		this.channelLocator = channelLocator;
	}

	/**
	 * 
	 * @return {@link ChannelLocator}
	 */
	public ChannelLocator getChannelLocator() {
		return channelLocator;
	}

	/**
	 * 
	 * @return {@link Value}
	 */
	public Value getValue() {
		return value;
	}

	/**
	 * 
	 * @param value
	 */
	public void setValue(Value value) {
		this.value = value;
	}
}
