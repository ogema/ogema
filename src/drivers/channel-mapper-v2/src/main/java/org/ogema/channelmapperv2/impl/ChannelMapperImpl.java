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
package org.ogema.channelmapperv2.impl;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.channelmapperv2.ChannelMapper;
import org.ogema.channelmapperv2.config.ChannelMapperConfigPattern;
import org.ogema.channelmapperv2.config.ChannelMapperConfiguration;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.CompoundResourceEvent;
import org.ogema.core.resourcemanager.pattern.PatternChangeListener;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.transaction.ResourceTransaction;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * By means of the ChannelMapper, it is possible to map channels to resources without any HighLevel-Driver.
 */
@Component(immediate = true) // need to reload existing configurations
@Service(Application.class)
public class ChannelMapperImpl implements Application, ChannelMapper,PatternListener<ChannelMapperConfigPattern>, PatternChangeListener<ChannelMapperConfigPattern> {

	/*
	 * Map<Resource path, Config pattern>
	 */
	final ConcurrentMap<String, ChannelController> resourceMappings = new ConcurrentHashMap<>();
	private final ConcurrentMap<ChannelLocator, ChannelController> channelMappings = new ConcurrentHashMap<>();
	private volatile ResourceList<ChannelMapperConfiguration> configs;
	protected volatile OgemaLogger logger;
	private volatile ApplicationManager appMan;
	private volatile ChannelAccess ca;
	// this is only needed if we get a mapping for a driver that has not been registered yet
	private volatile WeakReference<ScheduledExecutorService> timer = null;
	private volatile BundleContext ctx;
	private ServiceRegistration<ChannelMapper> sreg;
	
	ScheduledExecutorService getTimer() {
		ScheduledExecutorService t;
		WeakReference<ScheduledExecutorService> timer = this.timer;
		if (timer != null) {
			t = timer.get();
			if (t != null)  
				return t;
		}
		synchronized (this) {
			timer = this.timer;
			if (timer != null) {
				t = timer.get();
				if (t != null)
					return t;
			}
			t = Executors.newSingleThreadScheduledExecutor();
			this.timer = new WeakReference<ScheduledExecutorService>(t);
			return t;
		}
	}

	/*********************************
	 * 
	 * ChannelMapper implementation
	 * 
	 *********************************/

	@Override
	public void mapChannelToResource(ChannelLocator channel, SingleValueResource target, Direction direction,
			long samplingPeriod, float scalingFactor, float valueOffset) {
		Objects.requireNonNull(ca);
		Objects.requireNonNull(channel);
		Objects.requireNonNull(target);
		Objects.requireNonNull(direction);
		ChannelMapperConfiguration config = configs.add();
		ResourceTransaction trans = appMan.getResourceAccess().createResourceTransaction();
		trans.create(config.channelLocator());
		trans.create(config.channelLocator().driverId());
		trans.create(config.channelLocator().interfaceId());
		trans.create(config.channelLocator().deviceAddress());
		trans.create(config.channelLocator().parameters());
		trans.create(config.channelLocator().channelAddress());
		trans.setString(config.channelLocator().driverId(), channel.getDeviceLocator().getDriverName());
		trans.setString(config.channelLocator().deviceAddress(), channel.getDeviceLocator().getDeviceAddress());
		trans.setString(config.channelLocator().interfaceId(), channel.getDeviceLocator().getInterfaceName());
		trans.setString(config.channelLocator().parameters(), channel.getDeviceLocator().getParameters());
		trans.setString(config.channelLocator().channelAddress(), channel.getChannelAddress());
		trans.setAsReference(config.target(), target);
		trans.create(config.direction());
		trans.setString(config.direction(), direction.name());
		trans.create(config.samplingInterval());
		trans.setTime(config.samplingInterval(), samplingPeriod);
		trans.create(config.scalingFactor());
		trans.setFloat(config.scalingFactor(), (float) scalingFactor); 
		trans.create(config.valueOffset());
		trans.setFloat(config.valueOffset(), (float) valueOffset);
		trans.activate(config, false, true);		
		trans.commit();
	}


	@Override
	public void unmapChannel(ChannelLocator channel) {
		unmapChannel(channel, null);
	}

	@Override
	public void unmapChannel(final ChannelLocator channel, final SingleValueResource target) {
		ChannelController cc = channelMappings.get(channel);
		if (cc == null) {
			logger.warn("Channel not found, cannot remove it: {}", channel);
			return;
		}
		if (target != null && !cc.pattern.target.equalsLocation(target))
			return;
		cc.pattern.model.delete(); //  will trigger a patternUnavailable callback
	}
	
	@Override
	public void patternAvailable(ChannelMapperConfigPattern pattern) {
		logger.info("New channel mapper configuration found: {}",pattern.model);
		newPattern(pattern);
		appMan.getResourcePatternAccess().addPatternChangeListener(pattern, this, ChannelMapperConfigPattern.class);
		// TODO register channel with channel access
 	}
	
	private void newPattern(ChannelMapperConfigPattern pattern) {
		String path = pattern.model.getPath();
		if (resourceMappings.containsKey(path)) {
			logger.warn("Got a second callback for an existing mapping configuration " + pattern.model);
			return;
		}
		final ChannelController cc;
		try {
			cc = new ChannelController(pattern, ca, this);
		} catch (SecurityException e) {
			logger.warn("Channel configuration could not be created, permission denied: {}",pattern.model,e);
			// We need to delete the configuration, otherwise the channel will be created after the next system restart with this app's permissions.
			// This problem is difficult to overcome, otherwise.
			pattern.model.delete(); 
			return;
		}
		resourceMappings.put(path, cc);
		channelMappings.put(cc.channelLocator, cc);
	}
	
	@Override
	public void patternUnavailable(ChannelMapperConfigPattern pattern) {
		logger.info("Channel mapper configuration removed: {}",pattern.model);
		appMan.getResourcePatternAccess().removePatternChangeListener(pattern, this);
		patternGone(pattern);
	}
	
	private void patternGone(ChannelMapperConfigPattern pattern) {
		ChannelController cc = resourceMappings.remove(pattern.model.getPath());
		if (cc == null)
			return;
		cc.close();
		channelMappings.remove(cc.channelLocator);
	}
	
	@Override
	public void patternChanged(ChannelMapperConfigPattern instance, List<CompoundResourceEvent<?>> changes) {
		logger.info("Channel mapper configuration has changed: {}",instance.model);
		patternGone(instance);
		newPattern(instance);
	}
	
	@Activate
	protected void activate(BundleContext ctx) {
		this.ctx = ctx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void start(ApplicationManager appManager) {
		Objects.requireNonNull(ctx);
		this.logger = appManager.getLogger();
		this.appMan = appManager;
		this.ca = appManager.getChannelAccess();
		this.configs = appManager.getResourceManagement().createResource("channelMapperConfigurations", ResourceList.class);
		configs.setElementType(ChannelMapperConfiguration.class);
		configs.activate(false);
		appManager.getResourcePatternAccess().addPatternDemand(ChannelMapperConfigPattern.class, this, AccessPriority.PRIO_LOWEST);
		this.sreg = ctx.registerService(ChannelMapper.class, this, null);
	}
	
	// delete everything but the persistent configurations
	@Override
	public void stop(AppStopReason reason) {
		final ServiceRegistration<ChannelMapper> sreg = this.sreg;
		if (sreg != null) {
			try {
				sreg.unregister();
			} catch (Exception ignore) {}
			this.sreg = null;
		}
		if (appMan != null) 
			appMan.getResourcePatternAccess().removePatternDemand(ChannelMapperConfigPattern.class, this);
		WeakReference<ScheduledExecutorService> timer = this.timer;
		if (timer != null) {
			ScheduledExecutorService t = timer.get();
			if (t != null) {
				t.shutdownNow();
			}
			this.timer = null;
		}
		for (ChannelController cc: resourceMappings.values()) {
			if (appMan != null)
				appMan.getResourcePatternAccess().removePatternChangeListener(cc.pattern, this);
			cc.close();
		}
		this.logger = null;
		this.appMan = null;
		this.ca = null;
		this.resourceMappings.clear();
		this.channelMappings.clear();
		this.configs = null;
	}
	
}
