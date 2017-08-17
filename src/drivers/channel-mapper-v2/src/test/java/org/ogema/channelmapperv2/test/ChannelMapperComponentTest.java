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
