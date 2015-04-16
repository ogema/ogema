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
package org.ogema.drivers.lemoneg;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfigurationException;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceListener;
import org.ogema.core.resourcemanager.ResourceManagement;

/**
 * This class represents an open connection to a lemoneg device. It reads the values from the device and updates the
 * data in its LemonegDataResource.
 * 
 * When created it tries to connect to the device and starts to update the data in the resource.
 * 
 * @author pau
 * 
 */
public class LemonegDevice implements TimerListener, ResourceListener {

	private final ApplicationManager appManager;
	private final ChannelAccess channelAccess;
	private Timer timer;

	private LemonegDataModel dataResource;
	private final LemonegConfigurationModel configurationResource;

	private ChannelLocator channelLocator;
	private long timeout;
	private String dataResourceId;

	public LemonegDevice(ApplicationManager appManager, LemonegConfigurationModel configurationResource) {

		this.appManager = appManager;
		this.channelAccess = appManager.getChannelAccess();
		ResourceManagement resourceManager = appManager.getResourceManagement();

		this.configurationResource = configurationResource;

		// add listener for updated configuration values
		// Are we also notified when a subelement changes? -> TF: Yes, because it is registered recursively. I strongly recommend against doing it that way, though. Can eat an arbitrary amount of performance.
		configurationResource.addResourceListener(this, true);

		channelLocator = createChannelLocator(configurationResource);
		timeout = configurationResource.timeout().getValue();
		dataResourceId = configurationResource.resourceName().getValue();

		try {
			channelAccess.addChannel(channelAccess.getChannelConfiguration(channelLocator));
		} catch (ChannelConfigurationException e) {
			e.printStackTrace();
		}

		// create the named LemonegDataModel instance and request control
		// access.
		try {
			dataResource = resourceManager.createResource(dataResourceId, LemonegDataModel.class);
			dataResource.addOptionalElement("voltage");
			dataResource.voltage().addOptionalElement("mmxVoltage");

			dataResource.addOptionalElement("current");
			dataResource.current().addOptionalElement("mmxCurrent");

			dataResource.addOptionalElement("activePower");
			dataResource.activePower().addOptionalElement("mmx");

			dataResource.addOptionalElement("phaseFrequency");
			dataResource.addOptionalElement("timeStamp");

			// resourceManager.controlResource(dataResourceId);
		} catch (ResourceException e1) {
			e1.printStackTrace();
		}

		// update();

		// activate the now functional and populated model
		dataResource.activate(true);

		timer = appManager.createTimer(timeout);
		timer.addListener(this);
	}

	private void update() {
		// now read the first data from the device to populate the model
		try {
			SampledValue value = channelAccess.getChannelValue(channelLocator);

			byte[] array = value.getValue().getByteArrayValue();
			LemonegValues lval = new LemonegValues();
			parseArray(array, lval);

			// write received value to model
			setModel(dataResource, lval);

		} catch (ChannelAccessException e) {
			e.printStackTrace();
		} catch (IllegalConversionException e) {
			e.printStackTrace();
		}
	}

	private ChannelLocator createChannelLocator(LemonegConfigurationModel configuration) {

		String interfaceId = configuration.interfaceId().getValue();
		String driverId = configuration.driverId().getValue();
		String deviceAddress = configuration.deviceAddress().getValue();
		String deviceParameters = configuration.deviceParameters().getValue();
		String channelAddress = configuration.channelAddress().getValue();

		return channelAccess.getChannelLocator(channelAddress, channelAccess.getDeviceLocator(driverId, interfaceId,
				deviceAddress, deviceParameters));
	}

	@Override
	public void timerElapsed(Timer timer) {
		update();
	}

	void setModel(LemonegDataModel model, LemonegValues values) {
		// appManager.getResourceAccessManager().startTransaction();

		// by calling setValue() the Resource Manager is notified that the value has changed
		model.voltage().reading().setValue(values.ueff);
		model.current().reading().setValue(values.ieff);
		model.activePower().reading().setValue(values.p);
		model.phaseFrequency().setValue(values.hz);
		model.timeStamp().setValue(values.timestamp);

		// appManager.getResourceAccessManager().commitTransaction();
	}

	void parseArray(byte[] array, LemonegValues values) {

		long tmp;
		int i;

		// build ueff
		i = 0;
		tmp = ((long) array[i + 0] & 0xFF) << 16;
		tmp |= ((long) array[i + 1] & 0xFF) << 24;
		tmp |= ((long) array[i + 2] & 0xFF) << 0;
		tmp |= ((long) array[i + 3] & 0xFF) << 8;

		// ueff is in milli volts
		values.ueff = tmp * 1e-3f;

		// build ieff
		i = 4;
		tmp = ((long) array[i + 0] & 0xFF) << 16;
		tmp |= ((long) array[i + 1] & 0xFF) << 24;
		tmp |= ((long) array[i + 2] & 0xFF) << 0;
		tmp |= ((long) array[i + 3] & 0xFF) << 8;

		// ieff is in milli volts
		values.ieff = tmp * 1e-3f;

		// build p
		i = 8;
		tmp = ((long) array[i + 0] & 0xFF) << 48;
		tmp |= ((long) array[i + 1] & 0xFF) << 56;
		tmp |= ((long) array[i + 2] & 0xFF) << 32;
		tmp |= ((long) array[i + 3] & 0xFF) << 40;
		tmp |= ((long) array[i + 4] & 0xFF) << 16;
		tmp |= ((long) array[i + 5] & 0xFF) << 24;
		tmp |= ((long) array[i + 6] & 0xFF) << 0;
		tmp |= ((long) array[i + 7] & 0xFF) << 8;

		// p is in micro watts
		values.p = tmp * 1e-6f;

		// build timestamp
		i = 16;
		tmp = ((long) array[i + 0] & 0xFF) << 32;
		tmp |= ((long) array[i + 1] & 0xFF) << 40;
		tmp |= ((long) array[i + 2] & 0xFF) << 16;
		tmp |= ((long) array[i + 3] & 0xFF) << 24;
		tmp |= ((long) array[i + 4] & 0xFF) << 0;
		tmp |= ((long) array[i + 5] & 0xFF) << 8;

		// timestamp is in milli seconds
		values.timestamp = tmp;

		// build hz
		i = 22;
		tmp = ((long) array[i + 0] & 0xFF) << 8;
		tmp |= ((long) array[i + 1] & 0xFF) << 0;

		values.hz = (short) tmp;
	}

	public void close() {
		// what to do with the resources? Just drop them.
		// remove listeners
		configurationResource.removeResourceListener(this);
		timer.stop();
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
		ChannelLocator newChan = createChannelLocator((LemonegConfigurationModel) resource);
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
				e.printStackTrace();
			}
		}

		// did the timeout change?
		long newTimeout = ((LemonegConfigurationModel) resource).timeout().getValue();
		if (timeout != newTimeout) {
			timeout = newTimeout;
			timer.stop();
			timer = appManager.createTimer(timeout);
			timer.addListener(this);
		}

		// did the dataResource change?
		String newId = configurationResource.resourceName().getValue();

		if (dataResourceId != newId) {
			ResourceManagement resourceManager = appManager.getResourceManagement();

			// the resource is renamed, in this case delete the old and create a
			// new one
			try {
				resourceManager.deleteResource(dataResourceId);
			} catch (ResourceException e) {
				e.printStackTrace();
			}
			dataResourceId = newId;
			try {
				dataResource = resourceManager.createResource(dataResourceId, LemonegDataModel.class);
				//				resourceManager.controlResource(dataResource, AccessMode.SHARED, AccessPriority.PRIO_LOWEST);
				dataResource.activate(true);
			} catch (ResourceException e1) {
				e1.printStackTrace();
			}
		}

	}

	LemonegDataModel getDataResource() {
		return dataResource;
	}

	String getDataResourceId() {
		return dataResourceId;
	}
}
