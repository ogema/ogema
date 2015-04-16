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
package org.ogema.driver.DRS485DE;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfigurationException;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceListener;
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
public class DRS485DEDevice implements ChannelEventListener, ResourceListener {

	/** use the modbus-rtu driver */
	private static final String DRIVER_ID = "modbus-rtu";

	/** hard coded channel address. Read 3 16bit registers starting from address 0 */
	private static final String CHANNEL_ADDRESS = "multi:3:0";

	private final ApplicationManager appManager;
	private final ChannelAccess channelAccess;
	private final OgemaLogger logger;

	private DRS485DEResource dataResource;
	private final DRS485DEConfigurationModel configurationResource;

	private ChannelLocator channelLocator;

	public DRS485DEDevice(ApplicationManager appManager, DRS485DEConfigurationModel configurationResource) {

		String dataResourceName;
		List<ChannelLocator> list = new ArrayList<ChannelLocator>();

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

		channelLocator = createChannelLocator(configurationResource);
		list.add(channelLocator);

		dataResourceName = configurationResource.resourceName().getValue();

		// create channel
		try {
			channelAccess.addChannel(channelAccess.getChannelConfiguration(channelLocator));
			channelAccess.registerUpdateListener(list, this);
		} catch (ChannelConfigurationException e) {
			logger.error(null, e);
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
			value = channelAccess.getChannelValue(channelLocator);

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

		deviceLocator = channelAccess.getDeviceLocator(DRIVER_ID, interfaceId, deviceAddress, deviceParameters);

		return channelAccess.getChannelLocator(CHANNEL_ADDRESS, deviceLocator);
	}

	private void setModel(DRS485DEResource model, SampledValue value) {

		int[] values;
		long result;

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

	public void close() {
		// what to do with the resources? Just drop them.
		// remove listeners
		try {
			configurationResource.removeResourceListener(this);
		} catch (ResourceException e) {

		}

		dataResource.deactivate(true);
		try {
			channelAccess.deleteChannel(channelLocator);
		} catch (Throwable t) {
			appManager.getLogger().error("channelAccess.deleteChannel({})", channelLocator, t);
		}
	}

	@Override
	public void resourceChanged(Resource resource) {

		// did the channel layout change?
		ChannelLocator newChan = createChannelLocator(configurationResource);
		if (!channelLocator.equals(newChan)) {

			try {
				channelAccess.deleteChannel(channelLocator);
			} catch (ChannelConfigurationException e1) {
				// ignore if channel does not exist
			}

			channelLocator = newChan;
			try {
				channelAccess.addChannel(channelAccess.getChannelConfiguration(channelLocator));
			} catch (ChannelConfigurationException e) {
				appManager.getLogger().error("channelAccess.addChannel({})", channelLocator, e);
			}
		}
		else {
			String currentParameter = channelLocator.getDeviceLocator().getParameters();
			String newParameter = configurationResource.deviceParameters().getValue();

			if (!currentParameter.equals(newParameter)) {
				channelLocator.getDeviceLocator().setParameters(newParameter);

				try {
					channelAccess.deleteChannel(channelLocator);
				} catch (ChannelConfigurationException e1) {
					// ignore if channel does not exist
				}

				try {
					channelAccess.addChannel(channelAccess.getChannelConfiguration(channelLocator));
				} catch (ChannelConfigurationException e) {
					appManager.getLogger().error("channelAccess.addChannel({})", channelLocator, e);
				}
			}
		}

		// did the timeout change?
		long newTimeout = configurationResource.timeout().getValue();
		long oldTimeout = channelAccess.getChannelConfiguration(channelLocator).getSamplingPeriod();
		if (oldTimeout != newTimeout) {
			channelAccess.getChannelConfiguration(channelLocator).setSamplingPeriod(newTimeout);
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
