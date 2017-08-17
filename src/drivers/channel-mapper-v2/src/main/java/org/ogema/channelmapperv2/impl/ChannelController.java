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
package org.ogema.channelmapperv2.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.ogema.channelmapperv2.config.ChannelMapperConfigPattern;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.tools.resource.util.ValueResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelController implements ResourceValueListener<SingleValueResource>, ChannelEventListener, Runnable {

	private final static long MAX_DELAY = 900000; // 15min
	final ChannelMapperConfigPattern pattern;
	private volatile ChannelConfiguration channelConfiguration;
	private final Logger logger;
	private final ChannelAccess ca;
	final ChannelLocator channelLocator;
	private final ChannelMapperImpl mapper;
	private volatile ScheduledExecutorService t;
	private volatile ScheduledFuture<?> openTask;
	private volatile long delay = 5000;
	
	public ChannelController(ChannelMapperConfigPattern pattern, ChannelAccess ca, ChannelMapperImpl mapper) {
		this.pattern = pattern;
		this.ca = ca;
		this.logger = mapper.logger;
		this.mapper = mapper;
		this.channelLocator = ChannelMapperUtil.getChannelLocator(pattern.channelLocator);
		this.run();
	}
	
	@Override
	public void run() {
		if (!pattern.model.isActive())
			return;
		try {
			Direction direction = Direction.valueOf(pattern.direction.getValue()); // potentially throws an exception
			long samplingPeriod;
			if (pattern.samplingInterval.isActive())
				samplingPeriod = pattern.samplingInterval.getValue();
			else
				samplingPeriod = 60000;
			channelConfiguration = ca.addChannel(channelLocator, direction, samplingPeriod);
			if (direction == Direction.DIRECTION_OUTPUT || direction == Direction.DIRECTION_INOUT)  {
				pattern.target.addValueListener(this);
			}
			if (direction == Direction.DIRECTION_INPUT || direction == Direction.DIRECTION_INOUT)
				ca.registerUpdateListener(Collections.singletonList(channelConfiguration), this);
			pattern.registrationSuccessful.<BooleanResource> create().setValue(true);
			pattern.registrationSuccessful.activate(false);
			t = null;
			openTask = null;
		} catch (ChannelAccessException cae) {
			if (!pattern.model.isActive())
				return;
			if (delay < MAX_DELAY) {
				delay = delay * 2;
				if (delay > MAX_DELAY)
					delay = MAX_DELAY;
			}
			logger.warn("Channel registration failed: {}. Trying again in {}s.", channelLocator, (delay / 1000), cae);
			pattern.registrationSuccessful.<BooleanResource> create().setValue(false);
			pattern.registrationSuccessful.activate(false);
			if (channelConfiguration != null) {
				ca.deleteChannel(channelConfiguration);
			}
			if (t == null) 
				t = mapper.getTimer();
			try {
				openTask = t.schedule(this, delay, TimeUnit.MILLISECONDS);
			} catch (RejectedExecutionException e) { // if task has been canceled
				t = null;
				openTask = null;
			}
		}
	}
	
	
	@Override
	public void resourceChanged(SingleValueResource resource) {
		float factor = 1;
		float offset = 0;
		if (pattern.scalingFactor.isActive())
			factor = pattern.scalingFactor.getValue();
		if (pattern.valueOffset.isActive())
			offset = pattern.valueOffset.getValue();
		try {
			ca.setChannelValue(channelConfiguration, transformBack(resource, factor, offset));
		} catch (ChannelAccessException e) {
			logger.error("Could not set new value for channel " + channelConfiguration + ", resource " + resource,e);
		}
	}
	
	@Override
	public void channelEvent(EventType type, List<SampledValueContainer> channels) {
		logger.trace("Channel event for {}", pattern.target);
		for (SampledValueContainer container: channels) {
			if (container.getChannelLocator().equals(channelConfiguration.getChannelLocator())) {
				float factor = 1;
				float offset = 0;
				if (pattern.scalingFactor.isActive())
					factor = pattern.scalingFactor.getValue();
				if (pattern.valueOffset.isActive())
					offset = pattern.valueOffset.getValue();
				Value value = transform(container.getSampledValue().getValue(), factor, offset);
				ValueResourceUtils.setValue(pattern.target, value);
				pattern.target.activate(false);
			}
		}
	}
	
	void close() {
		try{ 
			pattern.target.removeValueListener(this);
		} catch (Exception e) {
			LoggerFactory.getLogger(ChannelMapperImpl.class).warn("Error closing channel controller",e);
		}
		if (channelConfiguration != null) {
			try{ 
				ca.unregisterUpdateListener(Collections.singletonList(channelConfiguration), this);
			} catch (Exception e) {
				LoggerFactory.getLogger(ChannelMapperImpl.class).warn("Error closing channel controller",e);
			}
			try {
				ca.deleteChannel(channelConfiguration);
			} catch (Exception e) {
				LoggerFactory.getLogger(ChannelMapperImpl.class).warn("Error closing channel controller",e);
			}
		}
		ScheduledFuture<?> openTask = this.openTask;
		if (openTask != null)
			openTask.cancel(true);
		this.openTask = null;
		t = null;
	}
	
	// from channel value to resource
	private static Value transform(Value in, float factor, float offset) {
		if (in instanceof FloatValue)
			return new FloatValue(in.getFloatValue() * factor + offset);
		else if (in instanceof IntegerValue)
			return new IntegerValue(in.getIntegerValue() * ((int) factor) + (int) offset); // problematic...
		else if (in instanceof LongValue)
			return new LongValue(in.getLongValue() * ((long) factor) + (long) offset);// problematic...
		else if (in instanceof BooleanValue)
			return new BooleanValue(in.getBooleanValue() && (factor >= 0));
		else
			return in;
	}
	
	// from resource value to channel value
	private static Value transformBack(SingleValueResource in, float factor, float offset) {
		final Object value = ValueResourceUtils.getValue((ValueResource) in);
		if (in instanceof FloatResource)
			return new FloatValue(((float) value - offset) / factor);
		else if (in instanceof IntegerResource)
			return new IntegerValue(((int) value - (int) offset) / ((int) factor));
		else if (in instanceof TimeResource)
			return new LongValue(((long) value - (long) offset) / ((long) factor));
		else if (in instanceof BooleanResource)
			return new BooleanValue((boolean) value && (factor >= 0));
		else
			throw new IllegalArgumentException("Unsupported resource type " + in.getResourceType().getName());
	}

}
