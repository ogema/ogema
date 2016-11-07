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

import java.io.IOException;
import java.util.List;

import org.ogema.core.channelmanager.measurements.Value;

/**
 * The ChannelDriver interface has to be implemented by each low-level protocol driver. The low-level driver has to
 * export an instance of this interface as an OSGi service.
 */
public interface ChannelDriver {

	/**
	 * Return the unique ID of this driver.
	 * 
	 * @return Unique ID of the driver as a human readable string
	 */
	public String getDriverId();

	/**
	 * Human readable description of the driver. Can be displayed by UI components.
	 * 
	 * @return a human readable description of the driver.
	 */
	public String getDescription();

	/**
	 * Discover devices connected to a communication interface.
	 * 
	 * This will start a device scan at the specified interface. The exact behavior shall be specified by the driver.
	 * Depending on interfaceId and filter the method may also just return devices already registered with the
	 * driver/network controller.
	 * 
	 * @param interfaceId
	 *            ID of the interface (e.g. /dev/ttyS0, eth1, IP network address ...)
	 * @param filter
	 *            set a filter to specify the scope of the search (driver specific - eg. address range, search method,
	 *            device type ...)
	 * @param listener
	 *            listener to notify when a device is found
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported by the driver
	 * @throws NoSuchInterfaceException
	 *             if the interface does not exist
	 * @throws IOException
	 *             if an error occurs during the device scan
	 */
	public void startDeviceScan(String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, IOException;

	/**
	 * Abort device scan
	 * 
	 * Abort the current device scan for the specified interface.
	 * 
	 * Note: This also implies that the listener specified by the startDeviceScan method must no longer be used.
	 * 
	 * @param interfaceId
	 *            the same interface ID as set when invoking the device scan
	 * @param filter
	 *            the same filter as set when invoking the device scan
	 **/
	public void abortDeviceScan(String interfaceId, String filter);

	/**
	 * Discover available channels of a communication device. Depending on the device the method may also just return
	 * channels already known to the driver or trigger communication to the device.
	 * 
	 * @param device
	 *            the device for which to scan for available channels
	 * @param listener
	 *            methods will be invoked to inform the client of the progress an results of the device scan.
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported by the driver
	 */
	public void startChannelScan(DeviceLocator device, ChannelScanListener listener)
			throws UnsupportedOperationException;

	/**
	 * Returns list of known channels in the device with the given device locator. This call does not initiate a channel
	 * scan.
	 * 
	 * @param device
	 *            device to get the known channels form
	 * @return Returns a list of known channels of the device
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported by the driver
	 */
	public List<ChannelLocator> getChannelList(DeviceLocator device) throws UnsupportedOperationException;

	/**
	 * Synchronous read. Results are written into the SampledValueContainers Returns when all data requested has been
	 * received and written into the channels object. If data cannot be acquired (e.g. due to communication time out)
	 * the respective value/timestamp is unchanged, but quality should be set to bad.<br>
	 * In case a device contains channels that are not found automatically by the driver channels may also be used with
	 * this method that are not known to the driver before.
	 * 
	 * @param channels
	 *            list of channels to read.
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported by the driver
	 * @throws IOException
	 *             if the read of a channel failed. When a IOException is thrown the ChannelManager automatically sets
	 *             the quality flag of the SampledValueContainers to bad. Optionally the driver might set the quality
	 *             flag to bad by itself to avoid a IOException and perform a driver specific error handling.
	 */
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException;

	/**
	 * Listen to channels who send data without request, but based on subscription. This method will subscribe to the
	 * channels specified. Each call to listenChannel resets the list of channels listened to. So to stop all
	 * subscriptions call this method with an empty list in channels. Management of subscription requests from different
	 * listeners shall be controlled by the ChannelManager.<br>
	 * When the driver is de-registered or the ChannelManager is in shutdown, 
	 * this method is called with an empty list and a <code>null</code> listener.
	 * 
	 * @param channels
	 *            the channels to listen for
	 * @param listener
	 *            will be invoked when at least one channel value is updated. The list given to the listener will only
	 *            contain the elements channels that received a new value. See asynchronous read regarding channels
	 *            during and after callback.
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported by the driver
	 * @throws NoSuchDeviceException
	 *             thrown when device doesn't exist
	 * @throws NoSuchChannelException
	 *             thrown when channel doesn't exist
	 * @throws IOException
	 *             thrown when an IO error occurred
	 */
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException, NoSuchDeviceException, NoSuchChannelException, IOException;

	/**
	 * synchronous write
	 * 
	 * @param channels
	 *            A list of ValueContainer objects containing the Values and ChannelLocators for each channel that
	 *            should be written.
	 * @throws NoSuchDeviceException
	 *             thrown when device doesn't exist
	 * @throws NoSuchChannelException
	 *             thrown when channel doesn't exist
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported by the driver
	 * @throws IOException
	 *             if the write of a channel failed
	 */
	public void writeChannels(List<ValueContainer> channels) throws UnsupportedOperationException, IOException,
			NoSuchDeviceException, NoSuchChannelException;

	/**
	 * Shutdown the driver.
	 * 
	 * Driver should release all previously allocated resources (e.g. network connections, serial interfaces), aborts
	 * any running scanning processes and deletes all references to listeners. Afterwards the driver bundle can be
	 * stopped
	 */
	public void shutdown();

	/**
	 * A new channel that should be managed by this driver has been added to the system.
	 * 
	 * This method can be used by the driver to allocate resources needed by this channel (e.g. serial interfaces,
	 * connection establishment for connection-oriented communication systems).
	 * 
	 * @param channel
	 *            the newly added channel
	 */
	public void channelAdded(ChannelLocator channel);

	/**
	 * A channel managed by this driver has been removed from the system.
	 * 
	 * This message is intended to be used by the driver to determine if previously allocated resources (like serial
	 * interfaces of established connections) can be released.
	 * 
	 * @param channel
	 *            the removed channel
	 */
	public void channelRemoved(ChannelLocator channel);

	/**
	 * Add a DeviceListener consuming asynchronous device found/lost events.
	 * 
	 * @param listener
	 *            The DeviceListener object.
	 */
	public void addDeviceListener(DeviceListener listener);

	/**
	 * Remove a previously added DeviceListener. From this time the listener receives no longer any device found/lost
	 * events.
	 * 
	 * @param listener
	 *            The DeviceListener object.
	 */
	public void removeDeviceListener(DeviceListener listener);

	public void writeChannel(ChannelLocator channelLocator, Value value) throws UnsupportedOperationException,
			IOException, NoSuchDeviceException, NoSuchChannelException;
}
