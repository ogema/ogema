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
package org.ogema.driver.generic_xbee;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.slf4j.Logger;

public abstract class GenericXbeeZbDevice implements ChannelEventListener {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("generic_xbee_hl");

	protected final GenericXbeeZbConfig configurationResource;
	protected final ApplicationManager appManager;

	protected ChannelLocator channelLocator;
	protected long timeout;
	protected DeviceLocator deviceLocator;
	protected ResourceManagement resourceManager;

	protected GenericXbeeZbDriver driver;

	protected final ChannelAccess channelAccess;
	private List<ChannelLocator> channelList;

	public GenericXbeeZbDevice(GenericXbeeZbDriver driver, ApplicationManager appManager, GenericXbeeZbConfig config) {
		this.appManager = appManager;
		channelList = new ArrayList<ChannelLocator>();
		channelAccess = appManager.getChannelAccess();
		resourceManager = appManager.getResourceManagement();
		configurationResource = config;
		configurationResource.resourceName = configurationResource.resourceName.replace("-", "");
		this.driver = driver;

		deviceLocator = channelAccess.getDeviceLocator(config.driverId, config.interfaceId, config.deviceAddress,
				config.deviceParameters);
		unifyResourceName(configurationResource);
	}

	public GenericXbeeZbDevice(GenericXbeeZbDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		this.appManager = appManager;
		this.channelAccess = appManager.getChannelAccess();
		channelList = new ArrayList<ChannelLocator>();
		this.resourceManager = appManager.getResourceManagement();
		this.configurationResource = new GenericXbeeZbConfig();
		this.driver = driver;

		configurationResource.interfaceId = deviceLocator.getInterfaceName();
		configurationResource.deviceAddress = deviceLocator.getDeviceAddress();
		configurationResource.driverId = deviceLocator.getDriverName();
		configurationResource.deviceParameters = deviceLocator.getParameters();
		String[] splitStringArray = deviceLocator.getParameters().split(":");
		configurationResource.resourceName = splitStringArray[1];

		this.deviceLocator = deviceLocator;
		unifyResourceName(configurationResource);
	}

	@Override
	public void channelEvent(EventType type, List<SampledValueContainer> channels) {
		for (SampledValueContainer c : channels) {
			try {
				updateValues(c.getSampledValue().getValue());
			} catch (Throwable e) {
				logger.error("Changed channel value could not be read");
				e.printStackTrace();
			}
		}
	}

	protected ChannelLocator createChannelLocator(String channelAddress) {
		return channelAccess.getChannelLocator(channelAddress, deviceLocator);
	}

	public abstract void addChannel(GenericXbeeZbConfig config);

	public abstract void updateValues(Value value);

	protected void addToUpdateListener(ChannelLocator channelLocator) {
		channelList.add(channelLocator);
		channelAccess.registerUpdateListener(channelList, this);
	}

	protected void removeFromUpdateListener(ChannelLocator channelLocator) {
		channelList.remove(channelLocator);
		logger.debug("channelList size: " + channelList.size());
		channelAccess.registerUpdateListener(channelList, this);
	}

	public abstract void unifyResourceName(GenericXbeeZbConfig xbeeConfig);

	public abstract JSONObject packValuesAsJSON();

}
