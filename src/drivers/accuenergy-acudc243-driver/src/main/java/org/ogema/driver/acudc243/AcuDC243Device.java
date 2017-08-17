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
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
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
import org.ogema.core.model.units.ElectricCurrentResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.VoltageResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.core.security.WebAccessManager;
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
	private static final String DRIVER_ID = "modbus-combined";

	/** hard coded channel address. Read 3 16bit registers starting from address 0 */
	private static final String CHANNEL_ADDRESS = "2:HOLDING_REGISTERS:512:6";

	private final ApplicationManager appManager;
	private final ChannelAccess channelAccess;
	private final OgemaLogger logger;

	MeterPattern dataResource;
	final AcuDC243Configuration configurationResource;

	private ChannelConfiguration channelConf;

	ElectricCurrentResource current;

	VoltageResource voltage;

	PowerResource power;

	public AcuDC243Device(ApplicationManager appManager, AcuDC243Configuration configurationResource) {

		String dataResourceName;
		List<ChannelConfiguration> list = new ArrayList<ChannelConfiguration>();

		this.appManager = appManager;
		this.channelAccess = appManager.getChannelAccess();
		this.logger = appManager.getLogger();

		this.configurationResource = configurationResource;

		configurationResource.addValueListener(this, true);

		ChannelLocator channelLocator = createChannelLocator(configurationResource);

		dataResourceName = configurationResource.resourceName().getValue();

		// create the named ElectricityMeter instance and sub elements
		try {
			ResourcePatternAccess patAcc = appManager.getResourcePatternAccess();
			dataResource = patAcc.createResource(
					appManager.getResourceManagement().getUniqueResourceName(dataResourceName), MeterPattern.class);
			activate(dataResource); // does not activate value resources
			initModel(dataResource);
		} catch (ResourceException e) {
			logger.error(null, e);
		}
		// create channel
		try {
			channelConf = channelAccess.addChannel(channelLocator, Direction.DIRECTION_INOUT, 1000);
			list.add(channelConf);
			channelAccess.registerUpdateListener(list, this);
		} catch (ChannelAccessException e) {
			logger.error(null, e);
		}

		Servlet s = new Servlet(this);
		WebAccessManager wam = appManager.getWebAccessManager();
		wam.registerWebResource("/acudc243/service", s); // TODO make alias generic for multiple devices
	}

	/**
	 * Initialize the field with defaults and request exclusive write access to all fields read from device.
	 */
	public void initModel(MeterPattern p) {
		this.current = p.current;
		this.current.activate(false);
		this.current.setValue(0.f);
		this.voltage = p.voltage;
		this.voltage.setValue(0);
		this.power = p.power;
		this.power.setValue(0);

		this.current.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		this.voltage.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		this.power.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		RecordedData rd = p.current.getHistoricalData();

		RecordedDataConfiguration configuration = new RecordedDataConfiguration();
		configuration.setStorageType(RecordedDataConfiguration.StorageType.ON_VALUE_CHANGED);
		// configuration.setFixedInterval(3000);
		rd.setConfiguration(configuration);
	}

	private void activate(MeterPattern device) {
		// do not activate value resources, since they do not contain sensible values yet
		ResourceUtils.activateComplexResources(device.model, true, appManager.getResourceAccess());
	}

	private void update() {
		try {
			SampledValue value;

			// read latest cached value from channel
			value = channelAccess.getChannelValue(channelConf);

			// write received value to model
			setModel(dataResource, value);

		} catch (ChannelAccessException e) {
			logger.error(null, e);
		} catch (IllegalConversionException e) {
			logger.error(null, e);
		} catch (Throwable t) {
			logger.error(null, t);
		}
	}

	private ChannelLocator createChannelLocator(AcuDC243Configuration configuration) {

		DeviceLocator deviceLocator;

		String interfaceId = configuration.interfaceId().getValue();
		String deviceAddress = configuration.deviceAddress().getValue();
		String deviceParameters = configuration.deviceParameters().getValue();

		deviceLocator = new DeviceLocator(DRIVER_ID, interfaceId, deviceAddress, deviceParameters);

		return new ChannelLocator(CHANNEL_ADDRESS, deviceLocator);
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
			channelAccess.deleteChannel(channelConf);
		} catch (Throwable t) {
			appManager.getLogger().error("channelAccess.deleteChannel({})", channelConf, t);
		}
	}

	@Override
	public void resourceChanged(BooleanResource resource) {

		// did the channel layout change?
		ChannelLocator newChan = createChannelLocator(configurationResource);
		if (!channelConf.getChannelLocator().equals(newChan)) {

			channelAccess.deleteChannel(channelConf);

			try {
				channelAccess.addChannel(newChan, Direction.DIRECTION_INOUT,
						configurationResource.timeout().getValue());
			} catch (ChannelAccessException e) {
				appManager.getLogger().error("channelAccess.addChannel({})", newChan, e);
			}
		}
		else {
			String currentParameter = channelConf.getDeviceLocator().getParameters();
			String newParameter = configurationResource.deviceParameters().getValue();

			if (!currentParameter.equals(newParameter)) {

				channelAccess.deleteChannel(channelConf);

				try {
					channelAccess.addChannel(newChan, Direction.DIRECTION_INOUT,
							configurationResource.timeout().getValue());
				} catch (ChannelAccessException e) {
					appManager.getLogger().error("channelAccess.addChannel({})", newChan, e);
				}
			}
		}
	}

	@Override
	public void channelEvent(EventType type, List<SampledValueContainer> channels) {
		// this listener is only registered for our channel, no need to check it
		update();
	}

	public String getName() {
		return configurationResource.resourceName().getValue();
	}
}
