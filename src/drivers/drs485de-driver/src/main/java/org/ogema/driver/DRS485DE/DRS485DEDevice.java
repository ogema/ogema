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
package org.ogema.driver.DRS485DE;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceManagement;

/**
 * This class represents an open connection to an DRS485DE device. It reads the energy value from the device and updates
 * the data in its ElectricityMeter.
 * 
 * When created it tries to connect to the device and starts to update the data in the resource.
 * 
 * @author pau
 * 
 */
@SuppressWarnings("deprecation")
public class DRS485DEDevice implements ChannelEventListener, org.ogema.core.resourcemanager.ResourceListener {

	/** use the modbus-rtu driver */
	private static final String DRIVER_ID = "modbus-combined";

	/** hard coded channel address. Read 3 16bit registers starting from address 0 */
	private static final String CHANNEL_ADDRESS = "HOLDING_REGISTERS:0:3";

	private final ApplicationManager appManager;
	private final ChannelAccess channelAccess;
	private final OgemaLogger logger;

	private DRS485DEResource dataResource;
	private final DRS485DEConfigurationModel configurationResource;

	//private ChannelLocator channelLocator;
	private ChannelConfiguration channelConfiguration;
	
	public DRS485DEDevice(ApplicationManager appManager, DRS485DEConfigurationModel configurationResource) {

		String dataResourceName;
		

		this.appManager = appManager;
		this.channelAccess = appManager.getChannelAccess();
		this.logger = appManager.getLogger();

		ResourceManagement resourceManager = appManager.getResourceManagement();

		this.configurationResource = configurationResource;

		// add listener for updated configuration values
		// Are we also notified when a subelement changes?
		// -> TF: Yes, because it is registered recursively.
		// I strongly recommend against doing it that way, though. Can eat an
		// arbitrary amount of performance.
		configurationResource.addResourceListener(this, true);

		ChannelLocator channelLocator = createChannelLocator(configurationResource);

		dataResourceName = configurationResource.resourceName().getValue();

		// create channel
		try {
			List<ChannelConfiguration> list = new ArrayList<ChannelConfiguration>();
			
			channelConfiguration = channelAccess.addChannel(channelLocator, Direction.DIRECTION_INPUT, 1000);
			
			list.add(channelConfiguration);			
			channelAccess.registerUpdateListener(list, this);
		} catch (ChannelAccessException e) {
			e.printStackTrace();
		}

		// create the named ElectricityMeter instance and sub elements
		try {
			dataResource = new DRS485DEResource(resourceManager, dataResourceName);
		} catch (ResourceException e) {
			logger.error(null, e);
		}

		// activate the model
		dataResource.activate(true);
	}

	private void update() {
		try {
			SampledValue value;

			// read latest cached value from channel
			value = channelAccess.getChannelValue(channelConfiguration);

			// write received value to model
			setModel(dataResource, value);

		} catch (ChannelAccessException e) {
			logger.error(null, e);
		} catch (IllegalConversionException e) {
			logger.error(null, e);
		}
	}

	private ChannelLocator createChannelLocator(DRS485DEConfigurationModel configuration) {

		DeviceLocator deviceLocator;

		String interfaceId = configuration.interfaceId().getValue();
		String deviceAddress = configuration.deviceAddress().getValue();
		String deviceParameters = configuration.deviceParameters().getValue();

		deviceLocator = new DeviceLocator(DRIVER_ID, interfaceId, "", deviceParameters);

		return new ChannelLocator(deviceAddress + ":" + CHANNEL_ADDRESS, deviceLocator);
	}

	private void setModel(DRS485DEResource model, SampledValue value) {

		int[] values;
		long result;

		if (value.getQuality() == Quality.GOOD && value.getValue() != null)
		{
			// the response has one integer value per 16bit modbus register
			values = (int[]) value.getValue().getObjectValue();
	
			// result is in Wh * 10 (two fractional digits)
	
			// upper 8bit of the result registers are unused
			result = ((long) values[0] & 0xFF) << 16;
			result += ((long) values[1] & 0xFF) << 8;
			result += (long) values[2] & 0xFF;
	
			result *= 10; // Wh
			result *= 3600; // Joule
	
			model.totalEnergy.setValue((float) result);
		}
	}

	public void close() {
		// what to do with the resources? Just drop them.
		// remove listeners
		try {
			configurationResource.removeResourceListener(this);
		} catch (ResourceException e) {

		}

		dataResource.deactivate(true);
		channelAccess.deleteChannel(channelConfiguration);
	}

	@Override
	public void resourceChanged(Resource resource) {

		boolean update = false;
		
		// did the channel layout change?
		ChannelLocator newChan = createChannelLocator(configurationResource);
		
		update = channelConfiguration == null;
		
		if (update == false)
			update = !channelConfiguration.getChannelLocator().equals(newChan);
		
		if (update == false)
			update = channelConfiguration.getSamplingPeriod() != configurationResource.timeout().getValue();
		
		if (update) {
			channelAccess.deleteChannel(channelConfiguration);

			try {
				channelConfiguration = channelAccess.addChannel(newChan, Direction.DIRECTION_INPUT, configurationResource.timeout().getValue());
			} catch (ChannelAccessException e) {
				appManager.getLogger().error("channelAccess.addChannel({})", newChan, e);
			}
		}

		// did the name change?
		String newId = configurationResource.resourceName().getValue();

		if (dataResource.meter.getName() != newId) {
			ResourceManagement resourceManager = appManager.getResourceManagement();

			// the resource is renamed, in this case delete the old and create a
			// new one
			try {
				dataResource.delete();
				dataResource = null;
			} catch (ResourceException e) {
				logger.error(null, e);
			}

			try {
				dataResource = new DRS485DEResource(resourceManager, newId);
				dataResource.activate(true);
			} catch (ResourceException e) {
				logger.error(null, e);
			}
		}
	}

	@Override
	public void channelEvent(EventType type, List<SampledValueContainer> channels) {
		// this listener is only registered for our channel, no need to check it
		update();
	}
}
