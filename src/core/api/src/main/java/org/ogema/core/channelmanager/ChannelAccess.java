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

import java.util.List;

import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
import org.ogema.core.channelmanager.driverspi.DeviceListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * 
 * ChannelAccess is the Interface between ChannelManager and Application.<br>
 * You can get the ChannelAccess Object from the Application Manager.
 * 
 */
public interface ChannelAccess {

	/**
	 * Get the list of active channels.
	 *
	 * @return list of active configured channels
	 */
	public List<ChannelLocator> getAllConfiguredChannels();

	/**
	 * Set the value of a single channel.<br>
	 * 
	 * This will cause a write access to the underlying low-level driver.<br>
	 * 
	 * If the channel has not a direction property Direction.DIRECTION_INOUT or Direction.DIRECTION_OUTPUT a
	 * ChannelAccessException will be thrown.<br>
	 * 
	 * Writing a channel is completely decoupled from reading. Values written to the channel will be visible for reading
	 * after the channelManager queries the driver. This might be instant if the samplingPeriod is set to
	 * NO_READ_NO_LISTEN, after the completion of the current samplingPeriodInMs has elapsed or when a new value is
	 * available if the sampling Period is set to LISTEN_FOR_UPDATE.
	 * 
	 * @param configuration
	 *            the ChannelConfiguration object describing the channel to access
	 * @param value
	 *            the value that is used to update the channel.
	 * 
	 * @throws ChannelAccessException
	 *             if set value failed (e.g. the channel does not exist)
	 */
	public void setChannelValue(ChannelConfiguration configuration, Value value) throws ChannelAccessException;

	/**
	 * Set the values of multiple channels at once.<br>
	 * 
	 * Same as setChannelValue but for multiple channels at once. The application (high-level driver) has to provide an
	 * instance of Value for each channel to write to. The ChannelLocator and Value instances are related by their
	 * positions in the list. Both lists have to be of the same size. Otherwise an IllegalArgumentException will be
	 * thrown.
	 * 
	 * @param configurations
	 *            a list of {@link ChannelConfiguration} objects describing the channels to access
	 * @param values
	 *            a list of Values
	 * @throws ChannelAccessException
	 *             if set value failed (e.g. the channel does not exist)
	 */
	public void setMultipleChannelValues(List<ChannelConfiguration> configurations, List<Value> values)
			throws ChannelAccessException;

	/**
	 * Get the last value received on a single channel. Note that polling / subscription is organized by the
	 * ChannelManger independently of read operations on the ChannelManager interface.<br>
	 * 
	 * Writing a channel is completely decoupled from reading. Values written to the channel will be visible for reading
	 * after the channelManager queries the driver. This might be instant if the samplingPeriod is set to
	 * NO_READ_NO_LISTEN, after the completion of the current samplingPeriodInMs has elapsed or when a new value is
	 * available if the sampling Period is set to LISTEN_FOR_UPDATE.<br>
	 * 
	 * If the channel value has not yet been updated after creation (i.e. for listening channels), the channel returns a
	 * SampledValue with no Value, Quality.BAD and the timestamp of channel creation.
	 * 
	 * @param configuration
	 *            the ChannelConfiguration object describing the channel to access
	 * @return the value of the channel
	 * @throws ChannelAccessException
	 *             if get value failed (e.g. the channel does not exist)
	 */
	public SampledValue getChannelValue(ChannelConfiguration configuration) throws ChannelAccessException;

	/**
	 * Get the values of multiple channels.
	 *
	 * @param configurations
	 *            a list of ChannelConfiguration objects describing the channels to access
	 * @return List of values
	 * @throws ChannelAccessException
	 *             the channel access exception
	 */
	public List<SampledValue> getMultipleChannelValues(List<ChannelConfiguration> configurations)
			throws ChannelAccessException;

	/**
	 * Register a listener that will be called if the value is updated.
	 *
	 * @param configuration
	 *            ChannelConfiguration of the channel to register listener to
	 * @param listener
	 *            listener will be called when the value was updated
	 * @throws ChannelAccessException
	 *             the channel access exception
	 */
	public void registerUpdateListener(List<ChannelConfiguration> configuration, ChannelEventListener listener)
			throws ChannelAccessException;

	/**
	 * Unregister update listener.
	 *
	 * @param configuration
	 *            the configuration
	 * @param listener
	 *            the listener
	 */
	public void unregisterUpdateListener(List<ChannelConfiguration> configuration, ChannelEventListener listener);

	/**
	 * Register a listener that will be called when the value or quality of the channel changes.
	 * 
	 * @param configuration
	 *            ChannelConfiguration of the channel to register listener to
	 * @param listener
	 *            listener will be called when the value or quality has changed
	 * @throws ChannelAccessException
	 *             the channel access exception
	 */
	public void registerChangedListener(List<ChannelConfiguration> configuration, ChannelEventListener listener)
			throws ChannelAccessException;

	/**
	 * Unregister changed listener.
	 *
	 * @param configuration
	 *            the configuration
	 * @param listener
	 *            the listener
	 */
	public void unregisterChangedListener(List<ChannelConfiguration> configuration, ChannelEventListener listener);

	/**
	 * Add a new channel to the channel manager configuration. <br>
	 * 
	 * Adding a channel twice will return the previously created configuration. <br>
	 * 
	 * A separate ChannelConfiguration will be created for each different OGEMA Application. <br>
	 * 
	 * The value for the channel will be polled by the ChannelManager or a subscription to the value will be generated.
	 *
	 * @param locator
	 *            configuration of the channel to be added
	 * @param direction
	 *            the direction
	 * @param samplingPeriodInMs
	 *            the sampling period in ms
	 * @return the channel configuration
	 * @throws ChannelAccessException
	 *             if anything goes wrong
	 */
	public ChannelConfiguration addChannel(ChannelLocator locator, Direction direction, long samplingPeriodInMs)
			throws ChannelAccessException;

	/**
	 * Delete an existing (configured) channel.
	 * <p>
	 * 
	 * As clean-up code is often unreliable, this method does not throw exceptions. An error is only reported through
	 * the return value.
	 * 
	 * @param configuration
	 *            ChannelConfiguration of the channel to delete.
	 * @return true if the delete call succeeded.
	 */
	public boolean deleteChannel(ChannelConfiguration configuration);

	/**
	 * One time readout of device data that has not yet been configured as a channel. This is intended to be used by
	 * driver/device specific configuration to determine the exact configuration of a device.
	 * 
	 * @param channelList
	 *            List of SampledValueContainer addressing channels to read. All channels must target the same driver.
	 * 
	 * @throws ChannelAccessException
	 *             if get value failed (e.g. the channel does not exist)
	 */
	public void readUnconfiguredChannels(List<SampledValueContainer> channelList) throws ChannelAccessException;

	/**
	 * One time write of device data that has not yet been configured as a channel. This is intended to be used by
	 * driver/device specific configuration to change the exact configuration of a device.
	 * 
	 * @param channelList
	 *            List of SampledValueContainer addressing channels to write. All channels must target the same driver.
	 * 
	 * @throws ChannelAccessException
	 *             if set value failed (e.g. the channel does not exist)
	 */
	public void writeUnconfiguredChannels(List<ValueContainer> channelList) throws ChannelAccessException;

	/**
	 * Get the list of unique driver IDs of all registered drivers. A driver ID is a simple text string.
	 * 
	 * @return A list of the IDs of all known drivers.
	 */
	public List<String> getDriverIds();

	/**
	 * Discover devices connected to a local (or remote?) interface.
	 *
	 * @param driverId
	 *            The unique ID that identifies the driver to handle this interface (see getDriverIds)
	 * @param interfaceId
	 *            ID of the interface (e.g. /dev/ttyS0, eth1, IP network address ...)
	 * @param filter
	 *            set a filter to specify the scope of the search (driver specific - eg. address range, search method,
	 *            device type ...)
	 * @return list of connected devices.
	 * @throws ChannelAccessException
	 *             wraps all lower level errors (see Throwable.getCause())
	 */
	public List<DeviceLocator> discoverDevices(String driverId, String interfaceId, String filter)
			throws ChannelAccessException;

	/**
	 * Asynchronous version of discoverDevices.
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
	 * @throws ChannelAccessException
	 *             wraps all lower level errors (see Throwable.getCause())
	 */
	public void discoverDevices(String driverId, String interfaceId, String filter, DeviceScanListener listener)
			throws ChannelAccessException;

	/**
	 * Abort running discoverDevices. Terminates the synchronous and asynchronous version. Supply the same parameters as
	 * the to-be-terminated scan.
	 *
	 * @param driverId
	 *            the driver id
	 * @param interfaceId
	 *            the interface id
	 * @param filter
	 *            the filter
	 * @return true, if successful
	 */
	public boolean abortDiscoverDevices(String driverId, String interfaceId, String filter);

	/**
	 * Discover available channels of a device. If the channels found shall be used for read/write they must be
	 * registered with addChannel.
	 * 
	 * @param device
	 *            the device locator of the device to discover
	 * @return List of ChannelLocator instances that describe the discovered channels
	 * 
	 * @throws ChannelAccessException
	 *             wraps all lower level errors (see Throwable.getCause())
	 */
	public List<ChannelLocator> discoverChannels(DeviceLocator device) throws ChannelAccessException;

	/**
	 * Asynchronous version of discoverChannels.
	 *
	 * @param device
	 *            the device locator of the device to discover
	 * @param listener
	 *            is called when a channel was found or the scan has finished
	 * @throws ChannelAccessException
	 *             wraps all lower level errors (see Throwable.getCause())
	 */
	public void discoverChannels(DeviceLocator device, ChannelScanListener listener) throws ChannelAccessException;

	/**
	 * Returns the description of the specified driver.
	 *
	 * @param driverId
	 *            Id of the driver
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
	 * @throws ChannelAccessException
	 *             wraps all lower level errors (see Throwable.getCause())
	 */
	public List<ChannelLocator> getChannelList(DeviceLocator deviceLocator) throws ChannelAccessException;

	/**
	 * Wrapper method that calls the driver method with same name (see
	 * {@link ChannelDriver#addDeviceListener(DeviceListener) }).
	 *
	 * @param driverId
	 *            The driver id string
	 * @param listener
	 *            The DeviceListener object.
	 * @throws ChannelAccessException
	 *             wraps all lower level errors (see Throwable.getCause())
	 */
	public void addDeviceListener(String driverId, DeviceListener listener) throws ChannelAccessException;

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
