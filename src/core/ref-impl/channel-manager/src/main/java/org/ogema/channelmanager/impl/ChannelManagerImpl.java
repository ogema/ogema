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
package org.ogema.channelmanager.impl;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.channelmanager.impl.config.ChannelConfigurationImpl;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfigurationException;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.NoSuchDriverException;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.NoSuchChannelException;
import org.ogema.core.channelmanager.driverspi.NoSuchDeviceException;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.slf4j.LoggerFactory;

/**
 * 
 * The ChannelManager is a part of the OGEMA 2.0 Core Framework. The ChannelManager manage all Channels to external
 * Devices. You can get, set, add or delete Channels.
 * 
 */
@Component(immediate = true)
@Service(ChannelAccess.class)
@Reference(policy = ReferencePolicy.DYNAMIC, name = "drivers", referenceInterface = ChannelDriver.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, bind = "addDriver", unbind = "removeDriver")
public class ChannelManagerImpl implements ChannelAccess, ChannelUpdateListener {

	private final Map<String, WeakReference<ChannelDriver>> driverList;

	private final List<DeviceLocator> knownDeviceLocators;
	private final List<ChannelLocator> knownChannelLocators;

	private final List<Channel> asynchronChannels;
	private final HashMap<String, List<SampledValueContainer>> asynchronousSampledValueContainersByDriver = new HashMap<String, List<SampledValueContainer>>();
	private final Map<ChannelLocator, List<ChannelEventListener>> asycUpdateListeners = new HashMap<ChannelLocator, List<ChannelEventListener>>();

	private final List<Channel> channelList;

	private final List<CommDevice> devices;

	@Reference
	private PermissionManager permMan;

	/**
	 * The constructor create a "driver hashmap" , there is saved which driver has the Channel. Than will be create a
	 * channel, device, kownDeviceLocator and knownChannelLocator List.
	 * 
	 * @param pMan
	 *            *
	 */
	public ChannelManagerImpl() {
		driverList = new HashMap<String, WeakReference<ChannelDriver>>();
		channelList = new LinkedList<Channel>();
		asynchronChannels = new LinkedList<Channel>();
		devices = new LinkedList<CommDevice>();

		knownDeviceLocators = new LinkedList<DeviceLocator>();
		knownChannelLocators = new LinkedList<ChannelLocator>();
	}

	public ChannelManagerImpl(PermissionManager pMan) {
		this();
		this.permMan = pMan;
	}

	private Channel lookupChannel(ChannelLocator channelLocator) {
		for (Channel channel : channelList) {
			if (channel.getConfiguration().getChannelLocator().equals(channelLocator)) {
				return channel;
			}
		}

		return null;
	}

	private Channel lookupAsynchonChannel(ChannelLocator channelLocator) {
		for (Channel channel : asynchronChannels) {
			if (channel.getConfiguration().getChannelLocator().equals(channelLocator)) {
				return channel;
			}
		}

		return null;
	}

	@Override
	public void channelsUpdated(List<SampledValueContainer> channels) {

		if (asycUpdateListeners.get(channels.get(0).getChannelLocator()) != null) {
			// For Async

			for (SampledValueContainer channelData : channels) {

				for (Channel c : channelList) {
					if (channelData.getChannelLocator().equals(c.getSampledValueContainer().getChannelLocator())) {

						c.setValue(channelData.getSampledValue());

					}
				}

				List<SampledValueContainer> singleValueList = new LinkedList<SampledValueContainer>();

				singleValueList.add(channelData);

				ChannelLocator channelLocator = channelData.getChannelLocator();

				/* call update listeners */
				List<ChannelEventListener> updateEventListeners = asycUpdateListeners.get(channelLocator);

				if (updateEventListeners != null) {
					callEventListeners(singleValueList, updateEventListeners);
				}

			}

		}
		else {
			// For Sync
			for (SampledValueContainer svc : channels) {
				if (asycUpdateListeners.get(svc.getChannelLocator()) != null) {
					for (Channel c : asynchronChannels) {
						if (svc.getChannelLocator().equals(c.getSampledValueContainer().getChannelLocator())) {

							c.setValue(svc.getSampledValue());
							callEventListeners(channels, asycUpdateListeners.get(c));
						}
					}
				}
				for (Channel c : channelList) {
					if (svc.getChannelLocator().equals(c.getSampledValueContainer().getChannelLocator())) {

						c.setValue(svc.getSampledValue());

					}
				}
			}
		}
	}

	private void callEventListeners(List<SampledValueContainer> singleValueList, List<ChannelEventListener> listeners) {
		for (ChannelEventListener listenerReference : listeners) {
			ChannelEventListener listener = listenerReference;

			if (listener == null) {
				listeners.remove(listenerReference);
			}
			else {
				listener.channelEvent(EventType.UPDATED, singleValueList);
			}
		}
	}

	@Override
	public SampledValue getChannelValue(ChannelLocator channelLocator) throws ChannelAccessException {
		Channel channel = lookupChannel(channelLocator);

		if (channel != null) {
			if (channel.getValue() != null) {
				return channel.getValue();
			}
			else {
				throw new ChannelAccessException("Channel has no value.");
			}
		}
		else {
			throw new ChannelAccessException("Channel does not exist.");
		}
	}

	@Override
	public ChannelConfiguration getChannelConfiguration(ChannelLocator channelLocator) {

		Channel channel = lookupChannel(channelLocator);

		if (channel != null) {
			return channel.getConfiguration();
		}

		return new ChannelConfigurationImpl(channelLocator);
	}

	@Override
	public void addChannel(ChannelConfiguration configuration) throws ChannelConfigurationException {

		DeviceLocator deviceLocator = configuration.getDeviceLocator();
		/*
		 * Check permission
		 */
		if (System.getSecurityManager() != null && !permMan.checkAddChannel(configuration, deviceLocator)) {
			throw new SecurityException("Action not permitted.");
		}

		if (lookupChannel(configuration.getChannelLocator()) != null) {
			throw new ChannelConfigurationException("Channel already exists");
		}

		Channel channel = new Channel(configuration);

		CommDevice matchingDevice = lookupDevice(channel);

		ChannelDriver driver = lookupDriverByName(deviceLocator.getDriverName());

		if (driver == null) {
			throw new ChannelConfigurationException("driver \"" + deviceLocator.getDriverName() + "\" does not exist.");
		}
		// ** Only synchrony channels added to ComDevice ** //
		if (channel.getConfiguration().getSamplingPeriod() >= 0) {
			if (matchingDevice == null) {
				matchingDevice = new CommDevice(deviceLocator, driver);
				devices.add(matchingDevice);
			}

			matchingDevice.addChannel(channel);
			driver.channelAdded(channel.getConfiguration().getChannelLocator());
			channelList.add(channel);

		}// For asynchrony Communication
		else {
			if (lookupAsynchonChannel(configuration.getChannelLocator()) != null) {
				throw new ChannelConfigurationException("Asynchon Channel already exists");
			}

			driver.channelAdded(channel.getConfiguration().getChannelLocator());
			channelList.add(channel);
			asynchronChannels.add(channel);
			List<SampledValueContainer> containers = asynchronousSampledValueContainersByDriver.get(configuration
					.getDeviceLocator());

			if (containers == null) {
				containers = new ArrayList<SampledValueContainer>();
				asynchronousSampledValueContainersByDriver.put(deviceLocator.getDriverName(), containers);
			}

			containers.add(channel.getSampledValueContainer());
			try {
				driver.listenChannels(containers, this);
			} catch (Exception e) {
				throw new ChannelConfigurationException(e.getMessage());
			}

		}

	}

	private CommDevice lookupDevice(Channel channel) {
		CommDevice matchingDevice = null;

		for (CommDevice device : devices) {
			if (device.doesChannelMatch(channel)) {
				matchingDevice = device;
				break;
			}
		}
		return matchingDevice;
	}

	private ChannelDriver lookupDriverByName(String driverName) {
		WeakReference<ChannelDriver> driver = driverList.get(driverName);

		if (driver != null) {
			return driver.get();
		}

		return null;
	}

	/**
	 * add new driver and save them at the driverList Hashmap
	 */
	protected void addDriver(ChannelDriver driver) {
		String driverId = driver.getDriverId();

		LoggerFactory.getLogger(getClass()).info("Add new driver {}", driverId);

		if (lookupDriverByName(driverId) == null) {
			driverList.put(driverId, new WeakReference<ChannelDriver>(driver));
		}
	}

	protected void removeDriver(ChannelDriver driver) {
		String driverId = driver.getDriverId();

		driverList.remove(driverId);
	}

	@Override
	public DeviceLocator getDeviceLocator(String driverName, String interfaceName, String deviceAddress,
			String parameters) {

		DeviceLocator deviceLocator = new DefaultDeviceLocator(driverName, interfaceName, deviceAddress, parameters);

		synchronized (knownDeviceLocators) {

			for (DeviceLocator knownDeviceLocator : knownDeviceLocators) {
				if (deviceLocator.equals(knownDeviceLocator)) {
					return knownDeviceLocator;
				}

			}

			knownDeviceLocators.add(deviceLocator);

		}

		return deviceLocator;
	}

	@Override
	public ChannelLocator getChannelLocator(String channelAddress, DeviceLocator deviceLocator) {
		ChannelLocator channelLocator = new DefaultChannelLocator(deviceLocator, channelAddress);

		synchronized (knownChannelLocators) {

			for (ChannelLocator knownChannelLocator : knownChannelLocators) {
				if (channelLocator.equals(knownChannelLocator)) {
					return knownChannelLocator;
				}
			}

			knownChannelLocators.add(channelLocator);

		}

		return channelLocator;
	}

	@Override
	public void deleteChannel(ChannelLocator channelLocator) throws ChannelConfigurationException {
		Channel channel = lookupChannel(channelLocator);
		if (channel == null)
			throw new ChannelConfigurationException("Channel \"" + channelLocator + "\" is not configured.");
		ChannelConfiguration config = channel.getConfiguration();
		CommDevice device = lookupDevice(channel);
		/*
		 * Check Permission to delete a channel
		 */

		if (permMan.checkDeleteChannel(config, config.getDeviceLocator())) {
			if (device != null)
				device.removeChannel(channel);
		}

		channelList.remove(channel);
		if (channel.getConfiguration().getSamplingPeriod() < 0) {
			asynchronChannels.remove(channel);
			asynchronousSampledValueContainersByDriver.remove(channel.getSampledValueContainer());
			lookupDriver(channel).channelRemoved(channel.getSampledValueContainer().getChannelLocator());
		}
	}

	@Override
	public List<String> getDriverIds() {
		List<String> driverIdList = new LinkedList<String>();

		Set<String> driverIds = driverList.keySet();

		for (String driverId : driverIds) {
			driverIdList.add(driverId);
		}

		return driverIdList;
	}

	@Override
	public List<ChannelLocator> getAllConfiguredChannels() {
		List<ChannelLocator> channels = new LinkedList<ChannelLocator>();

		for (Channel channel : channelList) {
			/*
			 * Check permission: In order to perform the action to get a configured channel the caller has to be
			 * permitted to add the channel itself.
			 */
			ChannelConfiguration conf = channel.getConfiguration();
			if (permMan.checkAddChannel(conf, conf.getDeviceLocator())) {
				channels.add(channel.getConfiguration().getChannelLocator());
			}
		}

		return channels;
	}

	@Override
	public void setChannelValue(ChannelLocator channelLocator, Value value) throws ChannelAccessException {
		Channel channel = lookupChannel(channelLocator);

		if (channel != null) {

			if (!channel.isWritable()) {
				throw new ChannelAccessException("Channel \"" + channelLocator + "\" is not writable.");
			}

			ChannelDriver driver = lookupDriver(channel);

			if (driver == null) {
				throw new ChannelAccessException("low-level driver not available.");
			}

			ValueContainer valueContainer = new ValueContainer(channel.getConfiguration().getChannelLocator(), value);

			List<ValueContainer> containerList = new LinkedList<ValueContainer>();

			containerList.add(valueContainer);

			try {
				driver.writeChannels(containerList);
			} catch (UnsupportedOperationException | NoSuchDeviceException | IOException | NoSuchChannelException e) {

				// TODO Zwischen einzelnen Exceptions differenzieren

				throw new ChannelAccessException(e);
			}
		}
		else {
			throw new ChannelAccessException("Channel does not exist");
		}
	}

	private ChannelDriver lookupDriver(Channel channel) {
		return lookupDriverByName(channel.getConfiguration().getDeviceLocator().getDriverName());
	}

	@Override
	public SampledValueContainer readUnconfiguredChannel(ChannelLocator channelLocator) {

		/*
		 * Check permission to add a channel.
		 */
		ChannelConfiguration config = lookupChannel(channelLocator).getConfiguration();
		if (!permMan.checkAddChannel(config, config.getDeviceLocator())) {
			return null;
		}

		ChannelDriver driver = lookupDriverByName(channelLocator.getDeviceLocator().getDriverName());

		SampledValueContainer container = new SampledValueContainer(channelLocator);

		List<SampledValueContainer> channelList = new ArrayList<SampledValueContainer>(1);

		channelList.add(container);

		try {
			driver.readChannels(channelList);
			return container;
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// Value is set to null here. If an other default value is needed than the driver should handle the
			// IOException itself.
			container.setSampledValue(new SampledValue(null, System.currentTimeMillis(), Quality.BAD));
		}

		return null;
	}

	@Override
	public void setMultipleChannelValues(List<ChannelLocator> channelLocators, List<Value> values)
			throws ChannelAccessException {

		if (channelLocators == null || values == null) {
			throw new IllegalArgumentException("Parameter was not initialized.");
		}

		if (channelLocators.size() != values.size()) {
			throw new IllegalArgumentException("Non-compatible list sizes");
		}

		for (int i = 0; i < channelLocators.size(); i++) {
			setChannelValue(channelLocators.get(i), values.get(i));
		}
	}

	@Override
	public List<SampledValue> getMultipleChannelValues(List<ChannelLocator> channelLocators) {

		if (channelLocators != null) {

			List<SampledValue> values = new LinkedList<SampledValue>();
			SampledValue value;

			for (ChannelLocator cl : channelLocators) {
				try {
					value = getChannelValue(cl);
				} catch (ChannelAccessException e) {
					value = new SampledValue(null, 0, Quality.BAD);
				}
				values.add(value);
			}

			return values;

		}
		else {
			throw new NullPointerException();
		}
	}

	@Override
	public void registerUpdateListener(List<ChannelLocator> channelLocators, ChannelEventListener listener) {
		for (ChannelLocator channelLocator : channelLocators) {

			Channel channel = lookupChannel(channelLocator);
			CommDevice matchingDevice = lookupDevice(channel);
			boolean listenerExist = false;
			if (channel.getConfiguration().getSamplingPeriod() < 0) {
				// FOR ASYNCHONY CHANNELS
				List<ChannelEventListener> channelListeners = asycUpdateListeners.get(channelLocator);

				if (channelListeners == null) {

					channelListeners = new LinkedList<ChannelEventListener>();

					asycUpdateListeners.put(channelLocator, channelListeners);

				}
				else {
					for (ChannelEventListener selectListener : channelListeners) {
						if (selectListener == listener) {
							listenerExist = true;
							break;
						}
					}
				}

				if (!listenerExist) {

					channelListeners.add(listener);
					asycUpdateListeners.put(channelLocator, channelListeners);
				}

			}
			else {// FOR SYNCHONY CHANNELS
				matchingDevice.addChannelUpdateListener(channel, listener);
			}

		}
	}

	@Override
	public void registerChangedListener(List<ChannelLocator> channelLocators, ChannelEventListener listener) {

		for (ChannelLocator channelLocator : channelLocators) {

			Channel channel = lookupChannel(channelLocator);
			CommDevice matchingDevice = lookupDevice(channel);

			matchingDevice.addChannelChangedListener(channel, listener);
		}
	}

	@Override
	public List<ChannelLocator> discoverChannels(DeviceLocator device) throws NoSuchDriverException,
			UnsupportedOperationException {

		if (device != null) {

			WeakReference<ChannelDriver> driverRef = driverList.get(device.getDriverName());

			if (driverRef != null) {

				ChannelDriver driver = driverRef.get();

				if (driver != null) {

					final List<ChannelLocator> channels = new LinkedList<ChannelLocator>();

					final boolean finished[] = new boolean[1];
					finished[0] = false;

					final boolean scanSuccess[] = new boolean[1];

					driver.startChannelScan(device, new ChannelScanListener() {

						@Override
						public void progress(float ratio) {
							// TODO
							// System.out.println("scan progress: " + ratio);
						}

						@Override
						public void finished(boolean success) {
							finished[0] = true;
							scanSuccess[0] = success;
						}

						@Override
						public void channelFound(ChannelLocator channel) {
							/*
							 * Check permission to add a channel.
							 */
							ChannelConfiguration config = lookupChannel(channel).getConfiguration();
							if (permMan.checkAddChannel(config, config.getDeviceLocator())) {
								channels.add(channel);
							}
						}
					});

					do {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
						}
					} while (finished[0] == false);

					if (scanSuccess[0] == false) {
						return null;
					}
					else {
						return channels;
					}

				}
				else {
					throw new NoSuchDriverException("");
				}
			}
			else {
				throw new NoSuchDriverException("");
			}

		}

		return null;
	}

	@Override
	public void discoverChannels(DeviceLocator device, ChannelScanListener listener) {
		if (device != null) {
			WeakReference<ChannelDriver> driverRef = driverList.get(device.getDriverName());

			if (driverRef != null) {

				ChannelDriver driver = driverRef.get();

				final ChannelScanListener clientListener = listener;

				if (driver != null) {
					driver.startChannelScan(device, new ChannelScanListener() {

						@Override
						public void channelFound(ChannelLocator channel) {
							/*
							 * Check permission to add a channel
							 */
							ChannelConfiguration config = lookupChannel(channel).getConfiguration();
							if (permMan.checkAddChannel(config, config.getDeviceLocator())) {
								clientListener.channelFound(channel);
							}
						}

						@Override
						public void finished(boolean success) {
							clientListener.finished(success);
						}

						@Override
						public void progress(float ratio) {
							clientListener.progress(ratio);
						}

					});

				}
				else {
					listener.finished(false);
				}
			}
			else {
				listener.finished(false);
			}

		}
		else {
			listener.finished(false);
		}

	}

	@Override
	public List<DeviceLocator> discoverDevices(String driverId, String interfaceId, String filter)
			throws UnsupportedOperationException, NoSuchDriverException, NoSuchInterfaceException, IOException {
		// convert device scan to synchronous behavior

		ChannelDriver driver = lookupDriverByName(driverId);

		if (driver == null) {
			throw new NoSuchDriverException(driverId);
		}

		final List<DeviceLocator> deviceList = new LinkedList<DeviceLocator>();

		final boolean finished[] = new boolean[1];
		finished[0] = false;

		final boolean scanSuccess[] = new boolean[1];

		driver.startDeviceScan(interfaceId, filter, new DeviceScanListener() {

			@Override
			public void progress(float ratio) {
			}

			@Override
			public void finished(boolean success, Exception e) {
				scanSuccess[0] = success;
				finished[0] = true;
			}

			@Override
			public void deviceFound(DeviceLocator device) {
				deviceList.add(device);
			}
		});

		do {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (finished[0] = false);

		if (scanSuccess[0] == false) {
			throw new IOException();
		}

		return deviceList;
	}

	@Override
	public void discoverDevices(String driverId, String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, NoSuchDriverException {
		ChannelDriver driver = lookupDriverByName(driverId);

		if (driver == null) {
			throw new NoSuchDriverException(driverId);
		}

		new LinkedList<DeviceLocator>();

		final DeviceScanListener clientListener = listener;

		try {
			driver.startDeviceScan(interfaceId, filter, new DeviceScanListener() {

				@Override
				public void progress(float ratio) {
					clientListener.progress(ratio);
				}

				@Override
				public void finished(boolean success, Exception e) {
					clientListener.finished(success, e);
				}

				@Override
				public void deviceFound(DeviceLocator device) {
					clientListener.deviceFound(device);
				}
			});
		} catch (IOException e) {
			// TODO check if this is the correct exception!
			throw new NoSuchInterfaceException(interfaceId);
		}
	}

	@Override
	public void exceptionOccured(Exception e) {
		// TODO Auto-generated method stub

	}

}
