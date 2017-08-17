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
/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
package org.ogema.app.securityconsumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceListener;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an open connection to an TEA device. It reads the values from the device and updates the data
 * in its ElectricityMeter.
 *
 * When created it tries to connect to the device and starts to update the data in the resource.
 *
 * @author pau
 *
 */
public class TEADevice implements ResourceListener {
	private final static Logger logger = LoggerFactory.getLogger(TEADevice.class);

	// use the modbus-tcp driver
	private static final String DRIVER_ID = "modbus-combined";

	// TODO: Winfried, 14.09.2015: the described registers are for TEA controller.
	// must be adapted for other kind of meters!

	// TEA register 40001 == modbus register 0
	// the TEA registers start at 40001 (40001 - 40001 = 0) hence modbus register 0
	// there are 4 registers (2 x 16bit Sollwert, 2 x 16bit Istwert)
	// private static final String CHANNEL_ADDRESS_IV = "multi:4:0";
	private static final String CHANNEL_ADDRESS_IV = "1:HOLDING_REGISTERS:1:1";
	private static final String CHANNEL_ADDRESS_SV = "1:HOLDING_REGISTERS:2:1";
	private static final String CHANNEL_ADDRESS_1D = "0:HOLDING_REGISTERS:100:87";
	private static final String CHANNEL_ADDRESS_2D = "0:HOLDING_REGISTERS:200:87";
	private static final String CHANNEL_ADDRESS_3D = "0:HOLDING_REGISTERS:300:87";
	private static final String CHANNEL_ADDRESS_4D = "0:HOLDING_REGISTERS:400:87";
	private static final String CHANNEL_ADDRESS_5D = "0:HOLDING_REGISTERS:500:87";
	private static final String CHANNEL_ADDRESS_6D = "0:HOLDING_REGISTERS:600:87";
	private static final String CHANNEL_ADDRESS_7D = "0:HOLDING_REGISTERS:700:87";
	private static final String CHANNEL_ADDRESS_8D = "0:HOLDING_REGISTERS:800:87";
	private static final String CHANNEL_ADDRESS_9D = "0:HOLDING_REGISTERS:900:87";
	private static final String CHANNEL_ADDRESS_10D = "0:HOLDING_REGISTERS:1000:87";
	private static final String CHANNEL_ADDRESS_11D = "0:HOLDING_REGISTERS:1100:87";
	private static final String CHANNEL_ADDRESS_12D = "0:HOLDING_REGISTERS:1200:87";
	private static final String CHANNEL_ADDRESS_13D = "0:HOLDING_REGISTERS:1300:87";
	private static final String CHANNEL_ADDRESS_14D = "0:HOLDING_REGISTERS:1400:87";
	private static final String CHANNEL_ADDRESS_15D = "0:HOLDING_REGISTERS:1500:87";
	private static final String CHANNEL_ADDRESS_16D = "0:HOLDING_REGISTERS:1600:87";
	private static final String CHANNEL_ADDRESS_17D = "0:HOLDING_REGISTERS:1700:87";
	private static final String CHANNEL_ADDRESS_18D = "0:HOLDING_REGISTERS:1800:87";
	private static final String CHANNEL_ADDRESS_19D = "0:HOLDING_REGISTERS:1900:87";
	private static final String CHANNEL_ADDRESS_20D = "0:HOLDING_REGISTERS:2000:87";
	private static final String CHANNEL_ADDRESS_21D = "0:HOLDING_REGISTERS:2100:87";
	private static final String CHANNEL_ADDRESS_22D = "0:HOLDING_REGISTERS:2200:87";
	private static final String CHANNEL_ADDRESS_23D = "0:HOLDING_REGISTERS:2300:87";
	private static final String CHANNEL_ADDRESS_24D = "0:HOLDING_REGISTERS:2400:87";
	private static final String CHANNEL_ADDRESS_25D = "0:HOLDING_REGISTERS:2500:87";
	private static final String CHANNEL_ADDRESS_26D = "0:HOLDING_REGISTERS:2600:87";
	private static final String CHANNEL_ADDRESS_27D = "0:HOLDING_REGISTERS:2700:87";

	private static final String TEST_HOLDING = "15:HOLDING_REGISTERS:0:1";
	private static final String TEST_INPUT = "15:INPUT_REGISTERS:0:1";
	private static final String TEST_COILS = "15:COILS:0:2";
	private static final String TEST_DISCRETE = "15:DISCRETE_INPUTS:0:8";

	private final ApplicationManager appManager;
	private final ChannelAccess channelAccess;
	private Timer timer;

	private TEAResource dataResource;
	private final TEAConfigurationModel configurationResource;

	private List<ChannelConfiguration> channelConfigurations;
	ChannelEventListener channelEventListener;

	public TEADevice(ApplicationManager appManager, TEAConfigurationModel configurationResource) {
		channelConfigurations = new ArrayList<>();
		String dataResourceName;

		this.appManager = appManager;
		this.channelAccess = appManager.getChannelAccess();
		ResourceManagement resourceManager = appManager.getResourceManagement();

		this.configurationResource = configurationResource;

		// add listener for updated configuration values
		// Are we also notified when a subelement changes?
		// -> TF: Yes, because it is registered recursively.
		// I strongly recommend against doing it that way, though. Can eat an arbitrary amount of performance.
		configurationResource.addResourceListener(this, true);

		List<ChannelLocator> channelLocators = createChannelLocators(configurationResource);
		dataResourceName = configurationResource.resourceName().getValue();

		try {
			for (ChannelLocator channel : channelLocators) {
				channelConfigurations.add(channelAccess.addChannel(channel, Direction.DIRECTION_INOUT, 5000));
			}
			channelEventListener = new ChannelEventListener() {

				@Override
				public void channelEvent(EventType type, List<SampledValueContainer> channels) {
					// for (SampledValueContainer c : channels) {
					// TODO implement processChannelValue
					// processChannelValue(c.getChannelLocator().getChannelAddress(),
					// c.getSampledValue().getValue());
					update(channels);
					// }
				}
			};
			channelAccess.registerUpdateListener(channelConfigurations, channelEventListener);
		} catch (ChannelAccessException e) {
			e.printStackTrace();
		}

		// create the named instance and sub elements
		try {
			dataResource = new TEAResource(resourceManager, dataResourceName);
		} catch (ResourceException e1) {
			e1.printStackTrace();
		}

		// update();

		// activate the now functional model
		dataResource.activate(true);

		// timer = appManager.createTimer(5000);
		// timer.addListener(this);
	}

	// private int count;
	//
	// private SampledValue getSimulationValue(ChannelLocator channelLocator) throws ChannelAccessException
	// {
	// int array[] = new int[82];
	//
	// array[54] = count++;
	//
	// return new SampledValue(new ObjectValue(array), System.currentTimeMillis(), Quality.GOOD);
	// }

	private void update(List<SampledValueContainer> channels) {

		SampledValue value;

		for (SampledValueContainer c : channels) {
			value = c.getSampledValue();
			// value = getSimulationValue(channelLocator);

			Value v = value.getValue();
			if (v == null) {
				logger.info("TEADevice.update: quality=" + value.getQuality());
			}
			else {
				int[] values = (int[]) v.getObjectValue();
				for (int i : values) {
					logger.info("TEADevice.update: value=" + i);
				}
			}
		}

	}

	private List<ChannelLocator> createChannelLocators(TEAConfigurationModel configuration) {

		DeviceLocator deviceLocator;
		List<ChannelLocator> channels = new ArrayList<ChannelLocator>();
		String interfaceId = configuration.interfaceId().getValue();
		String deviceAddress = configuration.deviceAddress().getValue();
		String deviceParameters = configuration.deviceParameters().getValue();

		deviceLocator = new DeviceLocator(DRIVER_ID, interfaceId, deviceAddress, deviceParameters);

		channels.add(new ChannelLocator(CHANNEL_ADDRESS_IV, deviceLocator));
		channels.add(new ChannelLocator(CHANNEL_ADDRESS_SV, deviceLocator));
		// deviceLocator = channelAccess.getDeviceLocator(DRIVER_ID, interfaceId, deviceAddress, deviceParameters);
		//
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_1D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_2D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_3D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_4D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_5D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_6D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_7D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_8D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_9D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_10D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_11D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_12D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_13D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_14D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_15D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_16D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_17D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_18D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_19D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_20D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_21D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_22D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_23D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_24D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_25D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_26D, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(CHANNEL_ADDRESS_27D, deviceLocator));

		deviceLocator = new DeviceLocator(DRIVER_ID, "", "127.0.0.1", "");

		// channels.add(channelAccess.getChannelLocator(TEST_INPUT, deviceLocator));
		// channels.add(channelAccess.getChannelLocator(TEST_HOLDING, deviceLocator));
		// channels.add(new ChannelLocator("15:COILS:0:8", deviceLocator));
		// channels.add(channelAccess.getChannelLocator(TEST_DISCRETE, deviceLocator));

		return channels;
	}

	// @Override
	// public void timerElapsed(Timer timer) {
	// logger.info("TEADevice: timer elapsed");
	// update();
	// }

	private int getUint32(int[] array, int offset) {
		return array[offset + 1] * 65536 + array[offset];
	}

	/** Configuration has been deleted, close the channel to the device. */
	public void close() {
		// what to do with the resources? Just drop them.
		// remove listeners
		try {
			configurationResource.removeResourceListener(this);
		} catch (ResourceException e) {

		}

		timer.stop();
		dataResource.deactivate(true);
		try {
			for (ChannelConfiguration channel : channelConfigurations) {
				channelAccess.deleteChannel(channel);
			}
		} catch (Throwable t) {
			appManager.getLogger().error("channelAccess.deleteChannel({})", channelConfigurations.toArray(), t);
		}
	}

	/** If the configuration changed, update the channel accordingly. */
	@Override
	public void resourceChanged(Resource resource) {
		logger.info("TEADevice.resourceChanged:" + resource.getName() + "/" + resource.getClass().getName());
		// did the channel layout change?
		List<ChannelLocator> newChan = createChannelLocators(configurationResource);

		List<ChannelLocator> tmpList = new ArrayList<ChannelLocator>();

		for (ChannelConfiguration configuration : channelConfigurations) {
			tmpList.add(configuration.getChannelLocator());
		}

		if (!tmpList.equals(newChan)) {

			for (ChannelConfiguration channel : channelConfigurations) {
				channelAccess.deleteChannel(channel);
			}

			try {
				channelConfigurations.clear();
				for (ChannelLocator locator : newChan) {
					channelConfigurations.add(channelAccess.addChannel(locator, Direction.DIRECTION_INOUT,
							configurationResource.timeout().getValue()));
				}
			} catch (ChannelAccessException e) {
				appManager.getLogger().error("channelAccess.addChannel({})", channelConfigurations, e);
			}
		}

		// did the timeout change?
		long newTimeout = configurationResource.timeout().getValue();
		if (timer.getTimingInterval() != newTimeout) {
			timer.setTimingInterval(newTimeout);
		}

		// did the name change?
		String newId = configurationResource.resourceName().getValue();

		if ("teaController"/* dataResource.controller.getName() */ != newId) {
			ResourceManagement resourceManager = appManager.getResourceManagement();

			// the resource is renamed, in this case delete the old and create a
			// new one
			try {
				resourceManager.deleteResource("teaController"/* dataResource.controller.getName() */);
			} catch (ResourceException e) {
				e.printStackTrace();
			}
			// dataResourceId = newId;
			try {
				dataResource = new TEAResource(resourceManager, newId);
				dataResource.activate(true);
			} catch (ResourceException e1) {
				e1.printStackTrace();
			}
		}

		if (resource instanceof IntegerResource) {
			if (resource.isActive() && resource.isWriteable()) {
				try {
					int intValue = (int) ((IntegerResource) resource).getValue();
					Value value = new IntegerValue(intValue);

					// ChannelLocator channel = newChan.get(1); // writable SV is index "1"
					channelAccess.setChannelValue(channelConfigurations.get(1), value);
				} catch (ChannelAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
