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
package org.ogema.channelmapper.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.channelmapper.ChannelMapper;
import org.ogema.channelmapper.ResourceMappingException;
import org.ogema.core.application.Application;
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
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;

/**
 * By means of the ChannelMapper, it is possible to map channels to resources without any HighLevel-Driver.
 */
@Component(immediate = true)
@Service( { Application.class, ChannelMapper.class })
@SuppressWarnings("deprecation")
public class ChannelMapperImpl implements Application, ChannelMapper, ChannelEventListener, org.ogema.core.resourcemanager.ResourceListener {

	private static final Map<ChannelLocator, ResourceAndMore> resourceMapping = new HashMap<ChannelLocator, ResourceAndMore>();
	private static final Map<Resource, ChannelAndMore> channelMapping = new HashMap<Resource, ChannelAndMore>();

	protected OgemaLogger logger;
	private boolean available = false;
	private ChannelAccess channelAccess;

	private ResourceAccess resourceAccess;

	private ResourceManagement resourceManagement;

	private PersistentConfiguration configuration = null;
	private MappingConfiguration mappingConfiguration;
	Timer timer = new Timer();

	public class Task extends TimerTask {

		MappedElement mappedElemente;
		Resource resource;
		List<ChannelConfiguration> channels = new LinkedList<ChannelConfiguration>();

		public Task(MappedElement mappedElemente, Resource resource) {
			this.mappedElemente = mappedElemente;
			this.resource = resource;

		}

		@Override
		public void run() {

			try {
				addMappedElements(mappedElemente, resource, channels);
			} catch (ChannelAccessException e) {
				// TODO Auto-generated catch block
				logger.info(" Driver doesn't exist will try again in 5min");
				timer.schedule(new Task(mappedElemente, resource), 300000);
			}

			// TODO Auto-generated method stub

		}

	}

	public void addMappedElements(MappedElement mappedElement, Resource resource, List<ChannelConfiguration> channels)
			throws ChannelAccessException {

		ChannelDescription channelDescription = mappedElement.getChannelDescription();
		DeviceLocator deviceLocator = new DeviceLocator(channelDescription.getDriverId(),
				channelDescription.getInterfaceId(), channelDescription.getDeviceAddress(), channelDescription
						.getParameters());
		ChannelLocator channelLocator = new ChannelLocator(channelDescription.getChannelAddress(),
				deviceLocator);
		
		long samplingPeriod = channelDescription.getSamplingPeriod() == null ? 1000 : channelDescription.getSamplingPeriod();
		
			ChannelConfiguration channelConfiguration = channelAccess.addChannel(channelLocator, channelDescription.getDirection(), samplingPeriod);
			
			if (channelDescription.getDirection() == Direction.DIRECTION_INOUT
					|| channelDescription.getDirection() == Direction.DIRECTION_INPUT) {
				channels.add(channelConfiguration);
			}

		if (resource != null) {
			int count = 0;
			int pos[] = new int[20];
			String atributePath = mappedElement.getAttributePath();
			Resource subResource = resource;
			for (int i = 0; i < atributePath.length(); i++) {
				if (atributePath.charAt(i) == '.') {
					pos[count] = i;
					count++;
				}
			}
			for (int i = 0; i <= count; i++) {
				if (i == 0) {
					if (count == 0) {
						subResource.addOptionalElement(atributePath.substring(0, atributePath.length()));
						subResource = subResource.getSubResource(atributePath.substring(0, atributePath.length()));
						if (channelDescription.getDirection() == Direction.DIRECTION_OUTPUT
								|| channelDescription.getDirection() == Direction.DIRECTION_INOUT) {
							subResource.addResourceListener(this, false);
						}
					}
					else {
						subResource.addOptionalElement(atributePath.substring(0, pos[0]));
						subResource = subResource.getSubResource(atributePath.substring(0, pos[0]));
					}
				}
				else if (i + 1 >= count) {
					subResource.addOptionalElement(atributePath.substring(pos[count - 1] + 1, atributePath.length()));
					subResource = subResource.getSubResource(atributePath.substring(pos[count - 1] + 1, atributePath
							.length()));
					if (channelDescription.getDirection() == Direction.DIRECTION_OUTPUT
							|| channelDescription.getDirection() == Direction.DIRECTION_INOUT) {
						subResource.addResourceListener(this, false);
					}
				}
				else {
					subResource.addOptionalElement(atributePath.substring(pos[i] + 1, pos[i + 1]));
					subResource = subResource.getSubResource(atributePath.substring(pos[i] + 1, pos[i + 1]));
				}
				if (channelLocator != null && subResource != null) {
					ResourceAndMore resourceAndMore = new ResourceAndMore(subResource, channelDescription
							.getValueOffset(), channelDescription.getScalingFactor());
					resourceMapping.put(channelLocator, resourceAndMore);
					ChannelAndMore channelAndMore = new ChannelAndMore(channelConfiguration, channelDescription
							.getValueOffset(), channelDescription.getScalingFactor());
					channelMapping.put(subResource, channelAndMore);
				}

			}
			channelAccess.registerUpdateListener(channels, this);
		}
	}

	private Resource lookupMappedResourceByName(String resourceName) {

		MappedResource mappedResource = mappingConfiguration.getMappedResource(resourceName);

		if (mappedResource == null) {
			return null;
		}

		if (mappedResource.getResourceName().equals(resourceName)) {
			return resourceAccess.getResource(resourceName);
		}

		return null;
	}

	@Override
	public boolean getAvailableFlag() {
		return available;
	}

	/*********************************
	 * 
	 * ChannelMapper implementation
	 * 
	 *********************************/

	@Override
	public void mapChannelToResource(ChannelLocator channel, String resourceElementPath, Direction direction,
			long samplingPeriod, double scalingFactor, double valueOffset) throws ResourceMappingException {

		StringTokenizer tokenizer = new StringTokenizer(resourceElementPath, ".");
		List<ChannelConfiguration> channels = new LinkedList<ChannelConfiguration>();
		if (tokenizer.countTokens() < 1) {
			throw new ResourceMappingException("Channel cannot be mapped to model root or top level resource!");
		}

		String topLevelResourceName = tokenizer.nextToken();

		Resource topLevelResource = resourceAccess.getResource(topLevelResourceName);

		if (topLevelResource == null) {
			throw new ResourceMappingException("Top level resource \"" + topLevelResourceName + "\" does not exist!");
		}

		int count = tokenizer.countTokens();
		Resource subResource = topLevelResource;
		boolean exist = true;
		String atributePath = "";
		for (int i = 0; i < count; i++) {

			String elementName = tokenizer.nextToken();
			if (subResource.getSubResource(elementName) == null) {
				subResource.addOptionalElement(elementName);
				exist = false;
			}
			else {
				logger.info("Channel " + elementName + " cannot be mapped, because subresource still exists");
			}
			if (i < count - 1) {
				atributePath = atributePath + elementName + ".";
			}
			else {
				atributePath = atributePath + elementName;
			}
			subResource = subResource.getSubResource(elementName);

		}
		if (!exist) {
			DeviceLocator deviceLocator = channel.getDeviceLocator();
			ChannelConfiguration chConf = null;
			ChannelDescription channelDescription = new ChannelDescription(deviceLocator.getDriverName(), deviceLocator
					.getInterfaceName(), deviceLocator.getDeviceAddress(), deviceLocator.getParameters(), channel
					.getChannelAddress(), samplingPeriod, scalingFactor, valueOffset);
			channelDescription.setDirection(direction);

			MappedElement mappedElement = new MappedElement(atributePath, channelDescription);

			mappingConfiguration.getMappedResource(topLevelResourceName).addMappedChannel(mappedElement);

			try {
				chConf = channelAccess.addChannel(channel, direction, samplingPeriod);
				channels.add(chConf);
			} catch (ChannelAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (channelDescription.getDirection() == Direction.DIRECTION_OUTPUT
					|| channelDescription.getDirection() == Direction.DIRECTION_INOUT) {
				subResource.addResourceListener(this, false);
			}

			ResourceAndMore resourceAndMore = new ResourceAndMore(subResource, channelDescription.getValueOffset(),
					channelDescription.getScalingFactor());
			resourceMapping.put(channel, resourceAndMore);
			ChannelAndMore channelAndMore = new ChannelAndMore(chConf, channelDescription.getValueOffset(),
					channelDescription.getScalingFactor());
			channelMapping.put(subResource, channelAndMore);
			try {
				channelAccess.registerUpdateListener(channels, this);
			} catch (ChannelAccessException e) {
				e.printStackTrace();
			}
			configuration.write();
		}

	}

	@Override
	public void addMappedResource(Class<? extends Resource> resourceType, String resourceName)
			throws ResourceMappingException {

		Resource resourceToMap = resourceAccess.getResource(resourceName);
		if (resourceToMap == null) {

			if (mappingConfiguration.getMappedResource(resourceName) == null) {

				resourceToMap = resourceManagement.createResource(resourceName, resourceType);

				if (resourceToMap == null) {
					throw new ResourceMappingException("Creation of resource \"" + resourceName + "\" failed");
				}

				MappedResource newMappedResource = new MappedResource(resourceType.getName(), resourceName);

				mappingConfiguration.addMappedResource(newMappedResource);
			}
		}

	}

	@Override
	public void deleteMappedResource(String resourceName) throws ResourceMappingException {

		Resource resourceToDelete = lookupMappedResourceByName(resourceName);
		MappedResource mappedResource = mappingConfiguration.getMappedResource(resourceName);
		List<MappedElement> list = mappedResource.getMappedElements();
		for (MappedElement mappedElement : list) {
			unmapChannel(new ChannelLocator(mappedElement.getChannelDescription().getChannelAddress(),
					new DeviceLocator(mappedElement.getChannelDescription().getDriverId(), mappedElement
							.getChannelDescription().getInterfaceId(), mappedElement.getChannelDescription()
							.getDeviceAddress(), mappedElement.getChannelDescription().getParameters())));
		}
		if (resourceToDelete == null) {
			throw new ResourceMappingException("Resource \"" + resourceName + "\" not mapped");
		}

		mappingConfiguration.removeMappedResource(resourceName);
		configuration.write();
	}

	@Override
	public void unmapChannel(ChannelLocator channel) {
		ResourceAndMore resourceAndMore = resourceMapping.get(channel);
		Resource resource = resourceAndMore.resource;

		MappedResource mappedRes = findMappedResource(resource);

		if (mappedRes != null) {
			channelMapping.remove(resource);
			List<MappedElement> list = mappedRes.getMappedElements();
			for (MappedElement mappedElement : list) {
				if (mappedElement.getChannelDescription().getChannelAddress() == channel.getChannelAddress()) {
					mappedRes.deleteMappedChannel(mappedElement);
					logger.info(mappedElement.getAttributePath());
				}
				else {
					logger.info(mappedElement.getAttributePath() + "not deleted because: \n"
							+ mappedElement.getChannelDescription().getChannelAddress() + "!="
							+ channel.getChannelAddress() + "or \n "
							+ mappedElement.getChannelDescription().getDeviceAddress() + "!="
							+ channel.getDeviceLocator().getDeviceAddress());
				}
			}

			resourceMapping.remove(channel);

			configuration.write();
		}
		else {
			logger.info("Failed to unmap channel, no resource is mapped on chosen channel!");
		}

	}

	public MappedResource findMappedResource(Resource resource) {
		if (resource != null) {
			if (mappingConfiguration.getMappedResource(resource.getName()) != null) {
				logger.info("Unmapping Resource: " + resource.getName());
				return mappingConfiguration.getMappedResource(resource.getName());
			}
			else {
				return findMappedResource(resource.getParent());

			}
		}

		return null;
	}

	@Override
	public void unmapChannel(ChannelLocator channel, String resourceElementPath) {
		unmapChannel(channel);

	}

	/*********************************
	 * Start function of ChannelMapper. Loading configuration from the configuration file "channelmapper.config".
	 * Creating Resources and Subresources. Mapping Subresources/Optional Elements to Channels and save them in a
	 * Hashmap for identification in ChannelEvent function. Register an UpdateListener with the MappedChannels
	 *********************************/

	@Override
	public void start(ApplicationManager appManager) {

		logger = appManager.getLogger();

		logger.info("ChannelMapper started");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
		}
		channelAccess = appManager.getChannelAccess();
		resourceAccess = appManager.getResourceAccess();
		resourceManagement = appManager.getResourceManagement();

		// load configuration from persistent storage

		configuration = new PersistentConfiguration();

		configuration.read();

		mappingConfiguration = configuration.getMappingConfiguration();

		List<MappedResource> mappedResources = mappingConfiguration.getMappedResources();
		logger.info("Amount of resources:  " + Integer.toString(mappedResources.size()) + "\n");

		List<ChannelConfiguration> channels = new LinkedList<ChannelConfiguration>();

		for (MappedResource mappedResource : mappedResources) {
			Resource resource = resourceAccess.getResource(mappedResource.getResourceName());

			if (resource == null) {
				logger.info("Configured resource %s does not exit. Resource will be created.", mappedResource
						.getResourceName());

				Class<? extends Resource> resourceType;
				try {
					@SuppressWarnings("unchecked")
					Class<? extends Resource> ogemaType = (Class<? extends Resource>) Class.forName(mappedResource
							.getResourceType());
					resourceType = ogemaType;
					logger.info("Resouce:=   " + resourceType.getName());

					resource = resourceManagement.createResource(mappedResource.getResourceName(), resourceType);

					if (resource == null) {
						logger.error("Failed to create resource!");
					}

				} catch (ClassNotFoundException e) {
					logger.error("Resource type not known!", e);
				}

				List<MappedElement> mappedElements = mappedResource.getMappedElements();
				for (MappedElement mappedElemente : mappedElements) {
					try {
						addMappedElements(mappedElemente, resource, channels);
					} catch (ChannelAccessException e) {
						logger.info("Driver doesn't exist, wait 30 sec", e);
						timer.schedule(new Task(mappedElemente, resource), 30000);
					}
				}

			}
			else {

				List<MappedElement> mappedElements = mappedResource.getMappedElements();
				for (MappedElement mappedElemente : mappedElements) {
					try {
						addMappedElements(mappedElemente, resource, channels);
					} catch (ChannelAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.info("Driver doesn't exist, wait 30 sec");
						timer.schedule(new Task(mappedElemente, resource), 30000);
					}
				}

			}
		}

		available = true;
	}

	@Override
	public void stop(AppStopReason reason) {
		configuration.write();

		logger.info("ChannelMapper stopped!");
	}

	/***************************************
	 * 
	 * Channelevent Listener write sampled values to the resource.
	 * 
	 ***************************************/

	@Override
	public void channelEvent(EventType type, List<SampledValueContainer> channels) {
		for (SampledValueContainer container : channels) {
			ChannelLocator channel = container.getChannelLocator();
			ResourceAndMore resourceAndMore = resourceMapping.get(channel);
			Resource resource = resourceAndMore.resource;

			if (resource != null) {

				try {

					Value value = container.getSampledValue().getValue();

					if (container.getSampledValue().getQuality() == Quality.BAD) {
						logger.info(container.getChannelLocator() + ": Quality.BAD");

					}
					else {
						logger.info(container.getChannelLocator() + ": Quality.GOOD");

						if (resource instanceof FloatResource) {

							float floatValue = (float) ((value.getFloatValue() * resourceAndMore.scalingFactor) + resourceAndMore.valueOffset);
							((FloatResource) resource).setValue(floatValue);

						}
						else if (resource instanceof IntegerResource) {
							int intValue = (int) ((value.getIntegerValue() * resourceAndMore.scalingFactor) + resourceAndMore.valueOffset);
							((IntegerResource) resource).setValue(intValue);

						}
						else if (resource instanceof StringResource) {
							((StringResource) resource).setValue(value.getStringValue());

						}
						else if (resource instanceof org.ogema.core.model.simple.OpaqueResource) {
							((org.ogema.core.model.simple.OpaqueResource) resource).setValue(value.getByteArrayValue());

						}
						else if (resource instanceof BooleanResource) {

							((BooleanResource) resource).setValue(value.getBooleanValue());

						}
						else if (resource instanceof TimeResource) {
							((TimeResource) resource).setValue(container.getSampledValue().getTimestamp());

						}
						else {
							logger.warn("Channel value of unknown type!");
						}
					}

				} catch (IllegalConversionException e) {
					logger.warn("Unexpected type");
				}
			}
		}
	}

	@Override
	public void resourceChanged(Resource resource) {

		if (resource != null) {
			try {
				// logger.info("Resourcen Event");
				if (resource instanceof FloatResource) {

					ChannelAndMore channelAndMore = channelMapping.get(resource);
					ChannelConfiguration channel = channelAndMore.channelConfiguration;
					float floatValue = (float) ((((FloatResource) resource).getValue() / channelAndMore.scalingFactor) - channelAndMore.valueOffset);
					Value value = new FloatValue(floatValue);
					try {
						channelAccess.setChannelValue(channel, value);
					} catch (ChannelAccessException e) {
						e.printStackTrace();
					}
				}
				else if (resource instanceof IntegerResource) {
					ChannelAndMore channelAndMore = channelMapping.get(resource);
					ChannelConfiguration channel = channelAndMore.channelConfiguration;
					int intValue = (int) ((((IntegerResource) resource).getValue() / channelAndMore.scalingFactor) - channelAndMore.valueOffset);
					Value value = new IntegerValue(intValue);
					try {
						channelAccess.setChannelValue(channel, value);
					} catch (ChannelAccessException e) {
						e.printStackTrace();
					}
				}
				else if (resource instanceof StringResource) {
					Value value = new StringValue(((StringResource) resource).getValue());
					ChannelAndMore channelAndMore = channelMapping.get(resource);
					ChannelConfiguration channel = channelAndMore.channelConfiguration;
					try {
						channelAccess.setChannelValue(channel, value);
					} catch (ChannelAccessException e) {
						e.printStackTrace();
					}

				}
				else if (resource instanceof BooleanResource) {
					Value booleValue = new BooleanValue(((BooleanResource) resource).getValue());
					ChannelAndMore channelAndMore = channelMapping.get(resource);
					ChannelConfiguration channel = channelAndMore.channelConfiguration;
					try {
						channelAccess.setChannelValue(channel, booleValue);
					} catch (ChannelAccessException e) {
						e.printStackTrace();
					}
				}
				else {
					logger.warn("Resource value of unknown type!");
				}
			} catch (IllegalConversionException e) {
				logger.warn("Unexpected type");
			}

		}
	}

}
