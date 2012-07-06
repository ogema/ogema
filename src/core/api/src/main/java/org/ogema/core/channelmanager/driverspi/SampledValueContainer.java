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
package org.ogema.core.channelmanager.driverspi;

import org.ogema.core.channelmanager.measurements.SampledValue;

/**
 * 
 * A Container for Sampledvalues. The SampeldValueContainer contains a {@link SampledValue} and a {@link ChannelLocator}
 * .
 * 
 * 
 */
public class SampledValueContainer {
	private SampledValue sampledValue = null;
	private ChannelLocator channelLocator;

	/**
	 * Contructor
	 * 
	 * @param channelLocator
	 */
	public SampledValueContainer(ChannelLocator channelLocator) {
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
	 * @return {@link SampledValue}
	 */
	public SampledValue getSampledValue() {
		return sampledValue;
	}

	/**
	 * 
	 * @param value
	 */
	public void setSampledValue(SampledValue value) {
		this.sampledValue = value;
	}
}
