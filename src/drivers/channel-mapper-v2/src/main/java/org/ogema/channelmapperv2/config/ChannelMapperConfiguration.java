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
package org.ogema.channelmapperv2.config;

import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.prototypes.Configuration;

public interface ChannelMapperConfiguration extends Configuration {
	
	/**
	 * Must be a reference
	 * @return
	 */
	SingleValueResource target(); 
	
	/**
	 * The channel identifier.
	 * 
	 * @see ChannelLocator
	 * @return
	 */
	PersistentChannelLocator channelLocator();
	
	/**
	 * Admissible values:
	 * <ul>
	 * 	<li>INPUT read channel value and write it to the target resource
	 * 	<li>OUTPUT write resource value to channel
	 * 	<li>INOUT read from and write to channel
	 * </ul>
	 * 
	 * @see Direction
	 * @return
	 */
	StringResource direction();
	
	/**
	 * In ms. Only relevant for direction INPUT and INOUT.
	 * @return
	 */
	TimeResource samplingInterval();
	
	/**
	 * Scaling factor between channel value and resource value (resource value = channel value * scalingFactor + valueOffset)
	 * @return
	 */
	FloatResource scalingFactor();
	
	/**
	 * Offset between channel value and resource value (resource value = channel value * scalingFactor + valueOffset)
	 * @return
	 */
	FloatResource valueOffset();
	
	/**
	 * Optional. A human-readable description of the value.
	 * @return
	 */
	StringResource description();
	
	/**
	 * Indicates whether the channel mapping has been successful, or is still pending.
	 * @return
	 */
	BooleanResource registrationSuccessful();

}
