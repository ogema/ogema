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

import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
import org.ogema.core.channelmanager.driverspi.DeviceListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
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
	 *             if set value failed (e.g. the channel does not exist)
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
	 *            a list of Values
	 * @throws ChannelAccessException
	 *             if set value failed (e.g. the channel does not exist)
	 */
	public void setMultipleChannelValues(List<ChannelLocator> channelLocators, List<Value> values)
			throws ChannelAccessException;

	/**
	 * Get the last value received on a single channel. Note that polling / subscription is organized by the
	 * ChannelManger independently of read operations on the ChannelManager interface.
	 * 
	 * @param channelLocator
	 *            the ChannelLocator object describing the channel to access
	 * @return the value of the channel
	 * @throws ChannelAccessException
	 *             if get value failed (e.g. the channel does not exist)
	 */
	public SampledValue getChannelValue(ChannelLocator channelLocator) throws ChannelAccessException;

	/**
	 * Get the values of multiple channels
	 * 
	 * @param channelLocators
	 *            a list of ChannelLocator objects describing the channels to access
	 * @return List of values
	 */
	public List<SampledValue> getMultipleChannelValues(List<ChannelLocator> channelLocators);

	/**
	 * Register a listener that will be called if the value is updated.
	 * 
	 * @param channelLocator
	 *            channelLocator of the channel to register listener to
	 * @param listener
	 *            listener will be called when the value was updated
	 */
	public void registerUpdateListener(List<ChannelLocator> channelLocator, ChannelEventListener listener);

	/**
	 * Register a listener that will be called when the value or quality of the channel changes.
	 * 
	 * @param channelLocator
	 *            channelLocator of the channel to register listener to
	 * @param listener
	 *            listener will be called when the value or quality has changed
	 */
	public void registerChangedListener(List<ChannelLocator> channelLocator, ChannelEventListener listener);

	/**
	 * Get the configuration object for a channel. If the channel is already configured an existing configuration object
	 * will be returned. Otherwise a new ChannelConfiguration instance will be created.
	 * 
	 * @param channelLocator
	 *            channelLocator of the channel to get configuration from
	 * @return unique ChannelConfiguration object for the channel specified by ChannelLocator.
	 */
	public ChannelConfiguration getChannelConfiguration(ChannelLocator channelLocator);

	/**
	 * Add a new channel to the channel manager configuration. The value for the channel will be polled by the
	 * ChannelManager or a subscription to the value will be generated.
	 * 
	 * @param configuration
	 *            configuration of the channel to be added
	 * @throws ChannelConfigurationException
	 *             if the channel has already been configured
	 */
	public void addChannel(ChannelConfiguration configuration) throws ChannelConfigurationException;

	/**
	 * Delete an existing (configured) channel.
	 * 
	 * @param channelLocator
	 *            channelLocator of the channel to delete.
	 * @throws ChannelConfigurationException
	 *             if the channel is not configured
	 */
	public void deleteChannel(ChannelLocator channelLocator) throws ChannelConfigurationException;

	/**
	 * One time readout of device data that has not yet been configured as a channel. This is intended to be used by
	 * driver/device specific configuration to determine the exact configuration of a device.
	 * 
	 * @param channelList
	 *            List of SampledValueContainer addressing channels to read. All channels must target the same driver.
	 */
	public void readUnconfiguredChannels(List<SampledValueContainer> channelList);

	/**
	 * One time write of device data that has not yet been configured as a channel. This is intended to be used by
	 * driver/device specific configuration to change the exact configuration of a device.
	 * 
	 * @param channelList
	 *            List of SampledValueContainer addressing channels to write. All channels must target the same driver.

	 * @throws ChannelAccessException
	 *             if set value failed (e.g. the channel does not exist)
	 */
	public void writeUnconfiguredChannels(List<ValueContainer> channelList) throws ChannelAccessException;

	/**
	 * Get an instance of DeviceLocator with the specified properties. The framework ensures that there exists only one
	 * DeviceLocator instance with the same properties (
	 * 
	 * @param driverId
	 *            the unique name (id) of the driver
	 * @param interfaceId
	 *            the unique name (id) of the driver (from Hardware Manager?)
	 * @param deviceAddress
	 *            the address of the device. The syntax of the address is driver specific.
	 * @param parameters
	 *            communication parameter for the device. The syntax is driver specific.
	 * 
	 * @return Instance of DeviceLocator
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
	 * @return Instance of ChannelLocator
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
	 * @param filter
	 *            set a filter to specify the scope of the search (driver specific - eg. address range, search method,
	 *            device type ...)
	 * @return list of connected devices.
	 * 
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported by the driver
	 * @throws NoSuchInterfaceException
	 *             if the interface does not exist
	 * @throws NoSuchDriverException
	 *             thrown when the driverId doesn't exist
	 * @throws IOException
	 *             if an error occurs during the device scan
	 */
	public List<DeviceLocator> discoverDevices(String driverId, String interfaceId, String filter)
			throws UnsupportedOperationException, NoSuchInterfaceException, NoSuchDriverException, IOException;

	/**
	 * Asynchronous version of discoverDevices
	 * 
	 * @param driverId
	 *            The unique ID that identifies the driver to handle this interface (see getDriverIds)
	 * @param interfaceId
	 *            ID of the interface (e.g. /dev/ttyS0, eth1, IP network address ...)
	 * @param filter
	 *            set a filter to specify the scope of the search (driver specific - eg. address range, search method,
	 *            device type ...)
	 * @param listener
	 *            is called when a device was found or the scan has finished
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported by the driver
	 * @throws NoSuchInterfaceException
	 *             if the interface does not exist
	 * @throws NoSuchDriverException
	 *             thrown when the driverId doesn't exist
	 */
	public void discoverDevices(String driverId, String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, NoSuchDriverException;

	/**
	 * Discover available channels of a device. If the channels found shall be used for read/write they must be
	 * registered with addChannel.
	 * 
	 * @param device
	 *            the device locator of the device to discover
	 * @return List of ChannelLocator instances that describe the discovered channels
	 * 
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported by the driver
	 * @throws NoSuchInterfaceException
	 *             if the interface does not exist
	 * @throws NoSuchDriverException
	 *             thrown when the driverId doesn't exist
	 * @throws IOException
	 *             if an error occurs during the device scan
	 */
	public List<ChannelLocator> discoverChannels(DeviceLocator device) throws UnsupportedOperationException,
			NoSuchInterfaceException, NoSuchDriverException, IOException;

	/**
	 * Asynchronous version of discoverChannels
	 * 
	 * @param device
	 *            the device locator of the device to discover
	 * @param listener
	 *            is called when a channel was found or the scan has finished
	 */
	public void discoverChannels(DeviceLocator device, ChannelScanListener listener);

	/**
	 * Returns the description of the specified driver
	 * 
	 * @param driverId
	 *            Id of the driver
	 * 
	 * @return Description of driver on success, empty string else
	 */
	public String getDriverDescription(String driverId);

	/**
	 * Returns list of known channels in the device with the given device locator.
	 * 
	 * @param deviceLocator
	 *            device to get the known channels form
	 * @return Returns a list of known channels of the device
	 * 
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported by the driver
	 */
	public List<ChannelLocator> getChannelList(DeviceLocator deviceLocator) throws UnsupportedOperationException;

	/**
	 * Wrapper method that calls the driver method with same name (see
	 * {@link ChannelDriver#addDeviceListener(DeviceListener) }).
	 *
	 * @param driverId
	 *            The driver id string
	 * @param listener
	 *            The DeviceListener object.
	 */
	public void addDeviceListener(String driverId, DeviceListener listener);

	/**
	 * Wrapper method that calls the driver method with same name (see
	 * {@link ChannelDriver#removeDeviceListener(DeviceListener)}).
	 * 
	 * @param driverId
	 *            The driver id string
	 * @param listener
	 *            The DeviceListener object.
	 */
	public void removeDeviceListener(String driverId, DeviceListener listener);
}
