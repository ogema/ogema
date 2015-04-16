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
package org.ogema.core.channelmanager;

import java.io.IOException;
import java.util.List;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * 
 * The ChannelAccess is the Interface between ChannelManager and Application You can get the ChannelAccess Object from
 * the Application Manager.
 * 
 */
public interface ChannelAccess {

	/**
	 * Get the list of active channels
	 * 
	 * @return list of active configured channels
	 */
	public List<ChannelLocator> getAllConfiguredChannels();

	/**
	 * Set the value of a single channel
	 * 
	 * This will cause a write access to the underlying low-level driver.
	 * 
	 * If the channel has not a direction property Direction.OUT or Direction.IN_OUT a ChannelAccessException will be
	 * thrown.
	 * 
	 * @param channelLocator
	 *            the ChannelLocator object describing the channel to access
	 * @param value
	 *            the value that is used to update the channel.
	 * 
	 * @throws ChannelAccessException
	 */
	public void setChannelValue(ChannelLocator channelLocator, Value value) throws ChannelAccessException;

	/**
	 * Set the values of multiple channels at once.
	 * 
	 * Same as setChannelValue but for multiple channels at once. The application (high-level driver) has to provide an
	 * instance of Value for each channel to write to. The ChannelLocator and Value instances are related by their
	 * positions in the list. Both lists have to be of the same size. Otherwise an IllegalArgumentException will be
	 * thrown.
	 * 
	 * @param channelLocators
	 *            a list of ChannelLocator objects describing the channels to access
	 * @param values
	 *            a list of
	 */
	public void setMultipleChannelValues(List<ChannelLocator> channelLocators, List<Value> values)
			throws ChannelAccessException;

	/**
	 * Get the last value received on a single channel. Note that polling / subscription is organized by the
	 * ChannelManger independently of read operations on the ChannelManager interface.
	 * 
	 * @param channelId
	 *            the unique name of the channel
	 * @return the value of the channel
	 */
	public SampledValue getChannelValue(ChannelLocator channelLocator) throws ChannelAccessException;

	/**
	 * Get the values of multiple channels
	 */
	public List<SampledValue> getMultipleChannelValues(List<ChannelLocator> channelLocators);

	/**
	 * Register a listener that will be called if the value is updated.
	 * 
	 * @param channelId
	 *            the unique name of the channel
	 * @param listener
	 */
	public void registerUpdateListener(List<ChannelLocator> channelLocator, ChannelEventListener listener);

	/**
	 * Register a listener that will be called when the value or quality of the channel changes.
	 * 
	 * @param channelId
	 *            the unique name of the channel
	 * @param listener
	 */
	public void registerChangedListener(List<ChannelLocator> channelLocator, ChannelEventListener listener);

	/**
	 * Get the configuration object for a channel. If the channel is already configured an existing configuration object
	 * will be returned. Otherwise a new ChannelConfiguration instance will be created.
	 * 
	 * @param channelLocator
	 * @return unique ChannelConfiguration object for the channel specified by ChannelLocator.
	 */
	public ChannelConfiguration getChannelConfiguration(ChannelLocator channelLocator);

	/**
	 * Add a new channel to the channel manager configuration. The value for the channel will be polled by the
	 * ChannelManager or a subscription to the value will be generated.
	 * 
	 * If the channel has already been configured a ChannelConfigurationException will be thrown.
	 * 
	 * @param channelName
	 * @param configuration
	 * @throws ChannelConfigurationException
	 */
	public void addChannel(ChannelConfiguration configuration) throws ChannelConfigurationException;

	/**
	 * Delete an existing (configured) channel.
	 * 
	 * If the channel is not configured then a ChannelConfigurationException will be thrown.
	 * 
	 * @param channelLocator
	 *            the ChannelLocator of the channel to delete.
	 */
	public void deleteChannel(ChannelLocator channelLocator) throws ChannelConfigurationException;

	/**
	 * One time readout of device data that has not yet been configured as a channel This is intended to be used by
	 * driver/device specific configuration to determine the exact configuration of a device.
	 * 
	 * @param channelLocator
	 * @return
	 */
	public SampledValueContainer readUnconfiguredChannel(ChannelLocator channelLocator);

	/**
	 * Get an instance of DeviceLocator with the specified properties. The framework ensures that there exists only one
	 * DeviceLocator instance with the same properties (
	 * 
	 * @param driverName
	 *            the unique name (id) of the driver
	 * @param interfaceName
	 *            the unique name (id) of the driver (from Hardware Manager?)
	 * @param deviceAddress
	 *            the address of the device. The syntax of the address is driver specific.
	 * @param parameter
	 *            communication parameter for the device. The syntax is driver specific.
	 * 
	 * @return
	 */
	public DeviceLocator getDeviceLocator(String driverId, String interfaceId, String deviceAddress, String parameters);

	/**
	 * Get an instance of ChannelLocator with the specified channelAddress for the device specified by deviceLocator.
	 * The framework enforces that there exists only a single instance of ChannelLocator with the specified address and
	 * DeviceLocator.
	 * 
	 * @param channelAddress
	 *            the address of the channel. The syntax is driver specific.
	 * @param deviceLocator
	 *            reference to the device that contains the channel.
	 * @return
	 */
	public ChannelLocator getChannelLocator(String channelAddress, DeviceLocator deviceLocator);

	/**
	 * Get the list of unique driver IDs of all registered drivers. A driver ID is a simple text string.
	 * 
	 * @return A list of the IDs of all known drivers.
	 */
	public List<String> getDriverIds();

	/**
	 * Discover devices connected to a local (or remote?) interface
	 * 
	 * @param driverId
	 *            The unique ID that identifies the driver to handle this interface (see getDriverIds)
	 * @param interfaceId
	 *            ID of the interface (e.g. /dev/ttyS0, eth1, IP network address ...)
	 * @param filer
	 *            set a filter to specify the scope of the search (driver specific - eg. address range, search method,
	 *            device type ...)
	 * 
	 * @throws
	 */
	public List<DeviceLocator> discoverDevices(String driverId, String interfaceId, String filter)
			throws UnsupportedOperationException, NoSuchInterfaceException, NoSuchDriverException, IOException;

	/** Asynchronous version of discoverDevices */
	public void discoverDevices(String driverId, String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, NoSuchDriverException;

	/**
	 * Discover available channels of a device. If the channels found shall be used for read/write they must be
	 * registered with addChannel.
	 * 
	 * @param device
	 *            the device locator of the device to discover
	 * @return List of ChannelLocator instances that describe the discovered channels
	 */
	public List<ChannelLocator> discoverChannels(DeviceLocator device) throws UnsupportedOperationException,
			NoSuchInterfaceException, NoSuchDriverException, IOException;

	/** Asynchronous version of discoverChannels */
	public void discoverChannels(DeviceLocator device, ChannelScanListener listener);
}
