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
	private static final String DRIVER_ID = "modbus-tcp";

	// TODO: Winfried, 14.09.2015: the described registers are for TEA controller.
	// must be adapted for other kind of meters!

	// TEA register 40001 == modbus register 0
	// the TEA registers start at 40001 (40001 - 40001 = 0) hence modbus register 0
	// there are 4 registers (2 x 16bit Sollwert, 2 x 16bit Istwert)
	// private static final String CHANNEL_ADDRESS_IV = "multi:4:0";
	private static final String CHANNEL_ADDRESS_IV = "0:HOLDING_REGISTERS:0:int";
	private static final String CHANNEL_ADDRESS_SV = "0:HOLDING_REGISTERS:2:int";

	private final ApplicationManager appManager;
	private final ChannelAccess channelAccess;
	private Timer timer;

	private TEAResource dataResource;
	private final TEAConfigurationModel configurationResource;

	private List<ChannelConfiguration> channelConfigurations;
	ChannelEventListener channelEventListener;

	public TEADevice(ApplicationManager appManager, TEAConfigurationModel configurationResource) {

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

			// write received value to model
			// TODO: DataResource noch festlegen
			setModel(dataResource, value);

			logger.info("TEADevice.update: value=" + value.getValue().getIntegerValue());
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

	/** Parse TEA answer and put the values into the OGEMA resource. */
	private void setModel(TEAResource model, SampledValue value) {

		// the response has one integer value per 16bit modbus register
		// int[] values = (int[]) value.getValue().getObjectValue();

		// 32bit values are distributed little endian across the 16bit register
		// the lower 16 bit reside at the lower address

		// appManager.getResourceAccessManager().startTransaction();

		// by calling setValue() the Resource Manager is notified that the value has changed

		// TODO: Winfried, 14.09.2015: the registers here are specific for ION7550 meter.
		// must be adapted for general TEA meters (configurable)
		// int istlow = values[0] & 0x0000ffff;
		// int isthigh = values[1] & 0x0000ffff;
		// int solllow = values[2] & 0x0000ffff;
		// int sollhigh = values[3] & 0x0000ffff;
		// int istwert = (int) ((isthigh << 16) | istlow);
		// int sollwert = (int) ((sollhigh << 16) | solllow);

		// model.sollwert.setValue(istwert);
		// model.istwert.setValue(sollwert);

		logger.info("TEADevice.setModel: value=" + value.getValue().getIntegerValue());

		// appManager.getResourceAccessManager().commitTransaction();
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

					//ChannelLocator channel = newChan.get(1); // writable SV is index "1"
					channelAccess.setChannelValue(channelConfigurations.get(1), value);
				} catch (ChannelAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
