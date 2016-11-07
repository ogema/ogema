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
package org.ogema.driver.acudc243;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfigurationException;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.tools.resource.util.ResourceUtils;

/**
 * This class represents an open connection to an AcuDC243 device. It reads the energy value from the device and updates
 * the data in its ElectricityMeter.
 * 
 * When created it tries to connect to the device and starts to update the data in the resource.
 * 
 * @author pau
 * 
 */
public class AcuDC243Device implements ChannelEventListener, ResourceValueListener<BooleanResource> {

	/** use the modbus-rtu driver */
	private static final String DRIVER_ID = "modbus-rtu";

	/** hard coded channel address. Read 3 16bit registers starting from address 0 */
	private static final String CHANNEL_ADDRESS = "multi:6:512";

	private final ApplicationManager appManager;
	private final ChannelAccess channelAccess;
	private final OgemaLogger logger;

	private MeterPattern dataResource;
	private final AcuDC243Configuration configurationResource;

	private ChannelLocator channelLocator;

	public AcuDC243Device(ApplicationManager appManager, AcuDC243Configuration configurationResource) {

		String dataResourceName;
		List<ChannelLocator> list = new ArrayList<ChannelLocator>();

		this.appManager = appManager;
		this.channelAccess = appManager.getChannelAccess();
		this.logger = appManager.getLogger();

		this.configurationResource = configurationResource;

		configurationResource.addValueListener(this, true);

		channelLocator = createChannelLocator(configurationResource);
		list.add(channelLocator);

		dataResourceName = configurationResource.resourceName().getValue();

		// create channel
		try {
			ChannelConfiguration chConf = channelAccess.getChannelConfiguration(channelLocator);
			chConf.setSamplingPeriod(1000);
			channelAccess.addChannel(chConf);
			channelAccess.registerUpdateListener(list, this);
		} catch (ChannelConfigurationException e) {
			logger.error(null, e);
		}

		// create the named ElectricityMeter instance and sub elements
		try {
			ResourcePatternAccess patAcc = appManager.getResourcePatternAccess();
			dataResource = patAcc.createResource(
					appManager.getResourceManagement().getUniqueResourceName(dataResourceName), MeterPattern.class);
			activate(dataResource); // does not activate value resources
		} catch (ResourceException e) {
			logger.error(null, e);
		}
	}

	private void activate(MeterPattern device) {
		// do not activate value resources, since they do not contain sensible values yet
		ResourceUtils.activateComplexResources(device.model, true, appManager.getResourceAccess());
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
		}catch (Throwable t) {
			logger.error(null, t);
		}
	}

	private ChannelLocator createChannelLocator(AcuDC243Configuration configuration) {

		DeviceLocator deviceLocator;

		String interfaceId = configuration.interfaceId().getValue();
		String deviceAddress = configuration.deviceAddress().getValue();
		String deviceParameters = configuration.deviceParameters().getValue();

		deviceLocator = channelAccess.getDeviceLocator(DRIVER_ID, interfaceId, deviceAddress, deviceParameters);

		return channelAccess.getChannelLocator(CHANNEL_ADDRESS, deviceLocator);
	}

	private void setModel(MeterPattern pattern, SampledValue value) {

		int[] values;
		float volt, ampere, kw;
		int intBits;

		// the response has one integer value per 16bit modbus register
		if (value == null)
			return;
		Value v = value.getValue();
		if (v == null)
			return;
		values = (int[]) v.getObjectValue();

		// calculate real voltage value
		intBits = (((values[0] << 16) & 0xffff0000) | ((values[1]) & 0x0000ffff));
		volt = Float.intBitsToFloat(intBits);

		intBits = (((values[2] << 16) & 0xffff0000) | ((values[3]) & 0x0000ffff));
		ampere = Float.intBitsToFloat(intBits);

		intBits = (((values[4] << 16) & 0xffff0000) | ((values[5]) & 0x0000ffff));
		kw = Float.intBitsToFloat(intBits);

		pattern.voltage.setValue(volt);
		pattern.current.setValue(ampere);
		pattern.power.setValue(kw);
	}

	public void close() {
		// what to do with the resources? Just drop them.
		// remove listeners
		try {
			configurationResource.removeValueListener(this);
		} catch (ResourceException e) {

		}

		try {
			channelAccess.deleteChannel(channelLocator);
		} catch (Throwable t) {
			appManager.getLogger().error("channelAccess.deleteChannel({})", channelLocator, t);
		}
	}

	@Override
	public void resourceChanged(BooleanResource resource) {

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
		// String newId = configurationResource.resourceName().getValue();
		//
		// if (dataResource.meter.getName() != newId) {
		// ResourceManagement resourceManager = appManager.getResourceManagement();
		//
		// // the resource is renamed, in this case delete the old and create a
		// // new one
		// try {
		// dataResource.delete();
		// dataResource = null;
		// } catch (ResourceException e) {
		// logger.error(null, e);
		// }
		//
		// try {
		// dataResource = new AcuDC243Resource(resourceManager, newId);
		// dataResource.activate(true);
		// } catch (ResourceException e) {
		// logger.error(null, e);
		// }
		// }
	}

	@Override
	public void channelEvent(EventType type, List<SampledValueContainer> channels) {
		// this listener is only registered for our channel, no need to check it
		update();
	}
}
