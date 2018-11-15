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
package org.ogema.channelmapperv2.test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogema.channelmapperv2.ChannelMapper;
import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.exam.OsgiAppTestBase;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@ExamReactorStrategy(PerClass.class)
public class ChannelMapperComponentTest extends OsgiAppTestBase {
	
	public ChannelMapperComponentTest() {
		super(true);
	}
	
	@Inject
	private BundleContext ctx;
	
	@Inject
	private ChannelMapper channelMapper;
	
	@Inject
	private FrameworkClock clock;
	
	private ServiceRegistration<ChannelDriver> sreg;
	private TestDriver driver;
	
	@Before
	public void registerDriver() {
		this.driver = new TestDriver(clock);
		this.sreg = ctx.registerService(ChannelDriver.class, driver, null);
	}
	
	@After
	public void unregisterDriver() {
		this.driver = null;
		this.sreg.unregister();
	}
	
	@Test
	public void channelMapperWorksInOut() throws InterruptedException {
		final FloatResource f = getApplicationManager().getResourceManagement().createResource(newResourceName(), FloatResource.class);
		f.activate(false);
		final float value = 20F;
		driver.channel0Value = value;
		final CountDownLatch listenerLatch = new CountDownLatch(1);
		final ResourceValueListener<FloatResource> listener = new ResourceValueListener<FloatResource>() {

			@Override
			public void resourceChanged(FloatResource resource) {
				listenerLatch.countDown();
			}
		};
		f.addValueListener(listener);
		channelMapper.mapChannelToResource(TestDriver.CHANNEL_0, f, Direction.DIRECTION_INOUT, 100, 1, 0);
		Assert.assertTrue("Channel mapper config ignored, channels not read", driver.readLatch.await(5, TimeUnit.SECONDS));
		final List<SampledValueContainer> channels = driver.lastReadChannels;
		Assert.assertNotNull(channels);
		Assert.assertEquals("Unexpected number of channels being read",1, channels.size());
		Assert.assertEquals("Unexpected channel being read",TestDriver.CHANNEL_0, channels.get(0).getChannelLocator());
		Assert.assertTrue("Channel value not written to target resource", listenerLatch.await(5, TimeUnit.SECONDS));
		Assert.assertEquals("Resource value does not match channel value", value, f.getValue(), 0.1F);
		channelMapper.unmapChannel(TestDriver.CHANNEL_0);
		Thread.sleep(200); // give channel manager some time to issue its callbacks... otherwise annoying exceptions occur
		f.removeValueListener(listener);
		f.delete();
	}
	
	@Test
	public void channelMapperWorksOutput() throws InterruptedException {
		final FloatResource f = getApplicationManager().getResourceManagement().createResource(newResourceName(), FloatResource.class);
		f.activate(false);
		channelMapper.mapChannelToResource(TestDriver.CHANNEL_0, f, Direction.DIRECTION_OUTPUT, 100, 1, 0);
		Assert.assertTrue("Channel mapper config ignored, channel not added", driver.channelAddedLatch.await(5, TimeUnit.SECONDS));
		Thread.sleep(200); // ensure value listener has been registered before changing the value
		final float value = 20F;
		f.setValue(value);
		Assert.assertTrue("Resource value not written to channel", driver.writeLatch.await(5, TimeUnit.SECONDS));
		Assert.assertEquals("Resource value does not match channel value", f.getValue(), driver.channel0Value, 0.1F);
		Assert.assertFalse(driver.readLatch.await(100, TimeUnit.MILLISECONDS));
		channelMapper.unmapChannel(TestDriver.CHANNEL_0);
		Thread.sleep(200); // give channel manager some time to issue its callbacks... otherwise annoying exceptions occur
		f.delete();
	}

	
	
}
