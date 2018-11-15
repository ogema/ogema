/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ogema.channels;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpSession;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.applicationregistry.ApplicationListener;
import org.ogema.applicationregistry.ApplicationRegistry;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.NoSuchDriverException;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
import org.ogema.core.channelmanager.driverspi.DeviceListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.NoSuchChannelException;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

/**
 * 
 * The ChannelManager is a part of the OGEMA 2.0 Core Framework. 
 * The ChannelManager manages all Channels to external Devices. 
 * You can get, set, add or delete Channels.
 * 
 */
@Component(immediate = true)
@Service(ChannelAccess.class)
@Reference(policy = ReferencePolicy.DYNAMIC, name = "drivers", referenceInterface = ChannelDriver.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, bind = "addDriver", unbind = "removeDriver")
public class ChannelManagerImpl implements ChannelAccess {
 
	public final static String PROP_CHANNELMANAGER_READERTHREADFACTORY = "org.ogema.channels.readerthreadfactory";

	private static final String PROP_CHANNELMANAGER_LOG_TIMEOUT = "org.ogema.channels.loginterval";
	
	private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
	
	@Reference
	PermissionManager permMan;

	@Reference
	ApplicationRegistry appreg;

	/**	The map of known drivers */
	private final ConcurrentMap<String, Driver> drivers = new ConcurrentHashMap<String, Driver>();

	private ReaderThreadFactory readerThreadFactory = new ReaderThreadPerDeviceFactory(this);

	private final ApplicationListener appListener = new ApplicationListenerImpl();

	/**
	 * Add a driver to the driver list.
	 */
	synchronized protected void addDriver(ChannelDriver channelDriver) {
		
		if(channelDriver == null) {
			logger.warn("Rejected <null> driver.");
			return;
		}
		
		String driverId = channelDriver.getDriverId();

		if (driverId == null) {
			logger.warn("Rejected driver with <null> id string.");
			return;
		}


		ReaderThreadFactory factory = getReaderThreadFactory(driverId);
		
		if (drivers.putIfAbsent(driverId, new Driver(factory, channelDriver)) == null) {
			logger.info("Added driver {}", driverId);
		} else {
			logger.warn("Rejected already known driver {}", driverId);
		}
	}

	private ReaderThreadFactory getReaderThreadFactory(String driverId) {
		
		String factoryName;
		ReaderThreadFactory factory;
		
		// check if specific property for driver id is set
		factoryName = System.getProperty(PROP_CHANNELMANAGER_READERTHREADFACTORY + "." + driverId);
		
		// check if global property for factory is set
		if (factoryName == null) {
			factoryName = System.getProperty(PROP_CHANNELMANAGER_READERTHREADFACTORY);
		}
		
		try {
		if (factoryName != null) {
			// create factory
			// FIXME Class.forName is ugly... causes problems with OSGi class loaders, and should be generally avoided
			Class<?> factoryClass = Class.forName(factoryName);
			factory = (ReaderThreadFactory)factoryClass.newInstance();
			logger.info("Using ReaderThreadFactory {} for driver {} ", factoryName,  driverId);
		} else {
			factory = readerThreadFactory;
		}
		} catch (Exception e) {
			logger.warn("Use default ReaderThreadFactory for driver {} instead of {}", driverId, factoryName, e);
			factory = readerThreadFactory;
		}
		return factory;
	}

	/**
	 * Remove a driver from the driver list.
	 * Close and remove all channels for this driver.
	 *
	 */
	synchronized protected void removeDriver(ChannelDriver channelDriver) {
		
		if(channelDriver == null) {
			logger.warn("Tried to remove <void> driver.");
			return;
		}
		
		String driverId = channelDriver.getDriverId();

		logger.info("Removed driver {}", driverId);
		
		Driver removed = drivers.remove(driverId);
		
		if (removed != null)
			removed.close();
	}

	// this will be called at component activation
	// the appreg reference will be valid, as it is declared static
	protected void activate(ComponentContext context) {
		logger.info("Starting ChannelManager");
		appreg.registerAppListener(appListener);
		readLogTimeout();
	}
	
	// this will be called at component deactivation
	synchronized  protected void deactivate(ComponentContext context) {
		logger.info("Stopping ChannelManager");
		appreg.unregisterAppListener(appListener);
		
		for (Driver driver : drivers.values()) {
			driver.close();
		}
		
		// remove closed drivers
		drivers.clear();
	}
	
	private void readLogTimeout() {
		String logTimeoutString = System.getProperty(PROP_CHANNELMANAGER_LOG_TIMEOUT);
		long logTimeout;

		try {
			if (logTimeoutString != null) {
				logTimeout = Long.parseLong(logTimeoutString);
				LogLimiter.LOG_SUPPRESSION_INTERVAL = logTimeout;
				logger.info("Setting log timeout to {} ms.", logTimeout);
			}
		} catch (NumberFormatException e) {
			logger.info("Could not parse Property {}.", PROP_CHANNELMANAGER_LOG_TIMEOUT, e);
		}
	}
	
	Driver getDriver(String driverName) throws NoSuchDriverException {
		
		if (driverName == null)
			throw new NullPointerException();
		
		Driver result = drivers.get(driverName);
		
		if (result == null)
			throw new NoSuchDriverException(driverName);
		
		return result;
	}
	
	@Override
	public ChannelConfiguration addChannel(ChannelLocator channelLocator, Direction direction, long samplingTimeInMs) throws ChannelAccessException {
		AppID appID = appreg.getContextApp(getClass());
		if (appID == null)
			throw new NullPointerException("Could not determine app");
		return addChannel(channelLocator, direction, samplingTimeInMs, appID);
	}
	
	private ChannelConfiguration addChannel(ChannelLocator channelLocator, Direction direction, long samplingTimeInMs, AppID appID) throws ChannelAccessException {
		ChannelConfigurationImpl configuration = new ChannelConfigurationImpl(channelLocator, samplingTimeInMs, direction, appID);
		DeviceLocator deviceLocator = channelLocator.getDeviceLocator();
		Driver driver;
		
		// Check permission
		if (!permMan.checkAddChannel(configuration, deviceLocator)) {
			throw new SecurityException("Action not permitted.");
		}

		// get driver
		try {
			driver = getDriver(deviceLocator.getDriverName());
		} catch (NoSuchDriverException e) {
			throw new ChannelAccessException(e);
		}
		
		// get channel
		return driver.addConfiguration(configuration);
	}
	
	@Override
	public boolean deleteChannel(ChannelConfiguration configuration) {
		
		Driver driver;
		
		try {
			driver = getDriver(configuration.getDeviceLocator().getDriverName());
		} catch (NoSuchDriverException e1) {
			return false;
		}
		
		// No permission check necessary. 
		// The app had the right to open the channel, 
		// it is allowed to close it for itself.
		
		// remove the channel from the device
		try {
			return driver.removeConfiguration(configuration);
		} catch (NoSuchChannelException e) { 
			return false;
		}
	}

	@Override
	public List<String> getDriverIds() {
		return new ArrayList<String>(drivers.keySet());
	}

	@Override
	public List<ChannelLocator> getAllConfiguredChannels() {
		List<ChannelLocator> channels = new LinkedList<ChannelLocator>();

		// no need to protect from concurrent access
		// iterator is weakly-consistent
		// never throws an ConcurrentModificationException
		for (Driver driver: drivers.values())
		{
			driver.getChannels(channels);
		}
		
		return channels;
	}

	private Configuration getConfiguration(ChannelConfiguration configuration) throws ChannelAccessException {
		
		Configuration result = null;
		Driver driver = null;
		try {
			driver = getDriver(configuration.getDeviceLocator().getDriverName());
			result = driver.getConfiguration(configuration);
		} catch (Exception e) {
			throw new ChannelAccessException(e);
		}
		
		return result;
	}
	
	@Override
	public void setChannelValue(ChannelConfiguration configuration, Value value) throws ChannelAccessException {
		Configuration conf = getConfiguration(configuration);
		
		try {
			conf.setChannelValue(value);
		} catch (Exception e) {
			throw new ChannelAccessException(e);
		}	
	}

	@Override
	public SampledValue getChannelValue(ChannelConfiguration configuration) throws ChannelAccessException {
		Configuration conf = getConfiguration(configuration);
		
		try {
			return conf.getChannelValue();
		} catch (Exception e) {
			throw new ChannelAccessException(e);
		}
	}

	private class AppIDbyThread implements AppID {

		Thread thread;
		
		AppIDbyThread(Thread thread) {
			this.thread = thread;
		}
		
		@Override
		public String getIDString() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getLocation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle getBundle() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Application getApplication() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getOwnerUser() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getOwnerGroup() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getVersion() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean isActive() {
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((thread == null) ? 0 : thread.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AppIDbyThread other = (AppIDbyThread) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (thread == null) {
				if (other.thread != null)
					return false;
			} else if (!thread.equals(other.thread))
				return false;
			return true;
		}

		private ChannelManagerImpl getOuterType() {
			return ChannelManagerImpl.this;
		}

		@Override
		public URL getOneTimePasswordInjector(String path, HttpSession ses) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	@Override
	public void readUnconfiguredChannels(List<SampledValueContainer> channelList) throws ChannelAccessException {

		// implement readUnconfigured by opening the channels internally and closing them at the end of the call.
		// to be able to distinguish normally opened channels from the same app a new AppID implentation is used. 
		// This AppID is based on the thread hash code. This is unique because the thread is used for the call.

		AppID appID = new AppIDbyThread(Thread.currentThread());
		Driver driver = null;
		List<ChannelConfiguration> openChannels = new ArrayList<ChannelConfiguration>();

		try {
			for (SampledValueContainer vCont : channelList) {
				Driver driverTmp;
				ChannelLocator channelLocator = vCont.getChannelLocator();

				driverTmp = drivers.get(channelLocator.getDeviceLocator().getDriverName());

				if (driver == null)
					driver = driverTmp;

				// We have to ensure that all Values are directed to the same
				// driver
				if (driverTmp == null || (driverTmp != driver))
					throw new UnsupportedOperationException("All requests must be directed to the same driver.");
				
				// A channel is temporarily created for each request. 
				// This is done to avoid special handling of ChannelDriver.channelAdded().
				openChannels.add(addChannel(channelLocator, Direction.DIRECTION_INPUT,
						ChannelConfiguration.NO_READ_NO_LISTEN, appID));
			}

			driver.readChannels(channelList);
		} catch (Exception e) {
			throw new ChannelAccessException(e);
		} finally {
			for (ChannelConfiguration conf : openChannels) {
				deleteChannel(conf);
			}
		}
	}

	@Override
	public void writeUnconfiguredChannels(List<ValueContainer> channelList) throws ChannelAccessException {

		// implement writeUnconfigured by opening the channels internally and closing them at the end of the call.
		// to be able to distinguish normally opened channels from the same app a new AppID implementation is used. 
		// This AppID is based on the thread hash code. This is unique because the thread is used for the call.

		AppID appID = new AppIDbyThread(Thread.currentThread());
		Driver driver = null;
		List<ChannelConfiguration> openChannels = new ArrayList<ChannelConfiguration>();

		try {
			for (ValueContainer vCont : channelList) {
				Driver driverTmp;
				ChannelLocator channelLocator = vCont.getChannelLocator();

				driverTmp = drivers.get(channelLocator.getDeviceLocator().getDriverName());

				if (driver == null)
					driver = driverTmp;

				// We have to ensure that all Values are directed to the same
				// driver
				if (driverTmp == null || (driverTmp != driver))
					throw new UnsupportedOperationException("All requests must be directed to the same driver.");
				
				openChannels.add(addChannel(channelLocator, Direction.DIRECTION_OUTPUT,
						ChannelConfiguration.NO_READ_NO_LISTEN, appID));
			}

			driver.writeChannels(channelList);
		} catch (Exception e) {
			throw new ChannelAccessException(e);
		} finally {
			for (ChannelConfiguration conf : openChannels) {
				deleteChannel(conf);
			}
		}
	}
	
	@Override
	public void setMultipleChannelValues(List<ChannelConfiguration> configurations, List<Value> values)
			throws ChannelAccessException {

		if (configurations.size() != values.size()) {
			throw new IllegalArgumentException("Non-matching list sizes");
		}

		for (int i = 0; i < configurations.size(); i++) {
			setChannelValue(configurations.get(i), values.get(i));
		}
	}

	@Override
	public List<SampledValue> getMultipleChannelValues(List<ChannelConfiguration> configurations) {

		List<SampledValue> values = new LinkedList<SampledValue>();
		SampledValue value;

		for (ChannelConfiguration cl : configurations) {
			try {
				value = getChannelValue(cl);
			} catch (ChannelAccessException e) {
				value = new SampledValue(null, 0, Quality.BAD);
			}
			values.add(value);
		}

		return values;

	}

	@Override
	public void registerUpdateListener(
			List<ChannelConfiguration> configurations,
			ChannelEventListener listener) {
		for (ChannelConfiguration configuration : configurations) {
			Configuration conf;
			try {
				conf = getConfiguration(configuration);
				conf.addUpdateListener(listener);
			} catch (ChannelAccessException e) {
				// this is an internal error
				e.printStackTrace();
			}
		}
	}

	@Override
	public void registerChangedListener(
			List<ChannelConfiguration> configurations,
			ChannelEventListener listener) {
		for (ChannelConfiguration configuration : configurations) {
			Configuration conf;
			try {
				conf = getConfiguration(configuration);
				conf.addChangedListener(listener);
			} catch (ChannelAccessException e) {
				// this is an internal error
				e.printStackTrace();
			}
		}
	}

	@Override
	public void unregisterUpdateListener(
			List<ChannelConfiguration> configurations,
			ChannelEventListener listener) {
		for (ChannelConfiguration configuration : configurations) {
			Configuration conf;
			try {
				conf = getConfiguration(configuration);
				conf.removeUpdateListener(listener);
			} catch (ChannelAccessException e) {
				// this is an internal error
				e.printStackTrace();
			}
		}
	}

	@Override
	public void unregisterChangedListener(
			List<ChannelConfiguration> configurations,
			ChannelEventListener listener) {
		for (ChannelConfiguration configuration : configurations) {
			Configuration conf;
			try {
				conf = getConfiguration(configuration);
				conf.removeChangedListener(listener);
			} catch (ChannelAccessException e) {
				// this is an internal error
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<ChannelLocator> discoverChannels(DeviceLocator device) throws ChannelAccessException {

		final List<ChannelLocator> channels = new LinkedList<ChannelLocator>();
		final AtomicBoolean finished = new AtomicBoolean(false);

		try {
			Driver driver = getDriver(device.getDriverName());

			ChannelScanListener listener = new ChannelScanListener() {

				@Override
				public void progress(float ratio) {
					// ignored
				}

				@Override
				public synchronized void finished(boolean success) {
					finished.set(true);
					this.notify();
				}

				@Override
				public void channelFound(ChannelLocator channel) {
					channels.add(channel);
				}
			};

			driver.startChannelScan(device, listener);

			synchronized (listener) {
				while (finished.get() == false)
					try {
						listener.wait();
					} catch (InterruptedException e) {
						// don't care
					}
			}

			return channels;
		} catch (Exception e) {
			throw new ChannelAccessException(e);
		}
	}

	@Override
	public void discoverChannels(DeviceLocator device, ChannelScanListener listener) throws ChannelAccessException {

		try {
			Driver driver = getDriver(device.getDriverName());

			final ChannelScanListener client = listener;
			listener = new ChannelScanListener() {

				@Override
				public void progress(float ratio) {
					try {
						client.progress(ratio);
					} catch (Throwable t) {
						logger.warn("caught application exception in ChannelScanListener callback", t);
					}
				}

				@Override
				public void finished(boolean success) {
					try {
						client.finished(success);
					} catch (Throwable t) {
						logger.warn("caught application exception in ChannelScanListener callback", t);
					}
				}

				@Override
				public void channelFound(ChannelLocator channel) {
					try {
						client.channelFound(channel);
					} catch (Throwable t) {
						logger.warn("caught application exception in ChannelScanListener callback", t);
					}
				}
			};

			driver.startChannelScan(device, listener);
		} catch (Exception e) {
			throw new ChannelAccessException(e);
		}
	}

	@Override
	public List<DeviceLocator> discoverDevices(String driverId, String interfaceId, String filter)
			throws ChannelAccessException {
		// convert device scan to synchronous behavior

		AppID appID = appreg.getContextApp(getClass());
		
		try {
			Driver driver = getDriver(driverId);

			DeviceScanListenerSyncImpl listener = new DeviceScanListenerSyncImpl();

			DeviceScanner scanner = driver.startDeviceScan(interfaceId, filter, listener, appID);
			scanner.waitUntilFinished();

			if (listener.exceptionResult != null)
				throw listener.exceptionResult;
			
			return listener.deviceList;
			
		} catch (Exception e) {
			throw new ChannelAccessException(e);
		}
	}

	/**
	 * DeviceScanListener implementation for the synchronous invocation of discoverDevices.
	 * 
	 * @author pau
	 *
	 */
	private class DeviceScanListenerSyncImpl implements DeviceScanListener {

		List<DeviceLocator> deviceList = new LinkedList<DeviceLocator>();
		Exception exceptionResult;
		
		@Override
		public void deviceFound(DeviceLocator device) {
			deviceList.add(device);
		}

		@Override
		public void finished(boolean success, Exception e) {
			// DeviceScanner wakes up by itself from waitUntilFinished
			exceptionResult = e;
		}

		@Override
		public void progress(float ratio) {
			// ignore it
		}
	}
	
	@Override
	public void discoverDevices(String driverId, String interfaceId,
			String filter, DeviceScanListener listener)
			throws ChannelAccessException {
		
		AppID appID = appreg.getContextApp(getClass());
		Driver driver = null;
		
		try {
			driver = getDriver(driverId);
			driver.startDeviceScan(interfaceId, filter, listener, appID);
		} catch (Exception e) {
			throw new ChannelAccessException(e);
		}
	}

	// cleanup functions should not throw an exception
	@Override
	public boolean abortDiscoverDevices(String driverId, String interfaceId, String filter) 
	{
		AppID appID = appreg.getContextApp(getClass());
		Driver driver;
		boolean success = false;
		
		try {
			driver = getDriver(driverId);
			success = driver.abortDeviceScan(interfaceId, filter, appID);
		} catch (NoSuchDriverException | NullPointerException e) {
			logger.warn("could not abort device scan.", e);
		}
		
		return success;
	}

	@Override
	public String getDriverDescription(String driverId) {
		String result;
		Driver driver;
		
		try {
			driver = getDriver(driverId);
			result = driver.getDescription();
		} catch (NoSuchDriverException | NullPointerException e) {
			result = null;
		}
		
		return result;
	}

	@Override
	public List<ChannelLocator> getChannelList(DeviceLocator deviceLocator) throws ChannelAccessException {
		Driver driver;

		try {
			driver = getDriver(deviceLocator.getDriverName());
		} catch (NoSuchDriverException e) {
			throw new ChannelAccessException(e);
		}
		
		return driver.getChannelList(deviceLocator);
	}

	@Override
	public void addDeviceListener(String driverId, DeviceListener listener) throws ChannelAccessException {
		AppID appId = appreg.getContextApp(getClass());
		Driver driver = null;
		
		try {
			driver = getDriver(driverId);
		} catch (NoSuchDriverException e) {
			throw new ChannelAccessException(e);
		}
		
		driver.addDeviceListener(appId, listener);
	}

	// should not throw an exception. Application clean-up code might not be very robust.
	@Override
	public void removeDeviceListener(String driverId, DeviceListener listener) {
		AppID appId = appreg.getContextApp(getClass());
		Driver driver = null;
		
		try {
			driver = getDriver(driverId);
		} catch (NoSuchDriverException | NullPointerException e) {
			return;
		}
		
		try {
			driver.removeDeviceListener(appId, listener);
		} catch (NullPointerException e) {
			; // do nothing
		}
	}		

	ReaderThread getReaderThread(Channel channel) throws ChannelAccessException {
		return readerThreadFactory.getReaderThread(channel);
	}
	
	private class ApplicationListenerImpl implements ApplicationListener {

		@Override
		public void appInstalled(AppID app) {
			// don't care
		}

		@Override
		public void appRemoved(AppID app) {
			removeAppID(app);
		}
		
	}

	private void removeAppID(AppID app) {
		for (Driver driver : drivers.values()) {
			driver.removeAppID(app);
		}
	}
}
