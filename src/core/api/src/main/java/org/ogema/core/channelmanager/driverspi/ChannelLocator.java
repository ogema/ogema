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

/**
 * The unique description of a channel. This can be seen as the system wide unique address of a channel. It consists of
 * a low-level driver specific channel address and a DeviceLocator instance.
 */
public interface ChannelLocator {
	/**
	 * 
	 * @return String channelAdress
	 */
	String getChannelAddress();

	/**
	 * 
	 * @return {@link DeviceLocator}
	 */
	DeviceLocator getDeviceLocator();
}
