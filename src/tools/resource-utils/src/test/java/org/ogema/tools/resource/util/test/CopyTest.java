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
package org.ogema.tools.resource.util.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.ResourceAssertions;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.tools.resource.util.ResourceCopyConfiguration;
import org.ogema.tools.resource.util.ResourceCopyConfigurationBuilder;
import org.ogema.tools.resource.util.ResourceUtils;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;


/**
 * Tests for resource utils copy tool.
 */
@ExamReactorStrategy(PerClass.class)
public class CopyTest extends OsgiAppTestBase {

	public CopyTest() {
		super(true);
	}

	@Test
	public void copySimpleWorks() {
		final FloatResource f0 = getApplicationManager().getResourceManagement().createResource(newResourceName(), FloatResource.class);
		final FloatResource f1 = getApplicationManager().getResourceManagement().createResource(newResourceName(), FloatResource.class);
		final float val = 23.5F;
		f0.setValue(val);
		f0.activate(false);
		ResourceUtils.copy(f0, f1, getApplicationManager().getResourceAccess());
		Assert.assertEquals(val, f1.getValue(), 0.01F);
		Assert.assertTrue(f1.isActive());
	}
	
	@Test
	public void copyScheduleWorks() {
		final FloatResource f0 = getApplicationManager().getResourceManagement().createResource(newResourceName(), FloatResource.class);
		final FloatResource f1 = getApplicationManager().getResourceManagement().createResource(newResourceName(), FloatResource.class);
		final Schedule s0 = f0.program().create();
		final List<SampledValue> values = Arrays.asList(
				new SampledValue(new FloatValue(-234.4F), -192, Quality.GOOD),
				new SampledValue(new FloatValue(-213143), 1, Quality.BAD),
				new SampledValue(new FloatValue(32), 1000, Quality.GOOD)
		);
		s0.addValues(values);
		s0.activate(false);
		ResourceUtils.copy(f0, f1, getApplicationManager().getResourceAccess());
		final List<SampledValue> values2 = f1.program().getValues(Long.MIN_VALUE);
		Assert.assertEquals(f0.isActive(), f1.isActive());
		Assert.assertEquals(s0.isActive(), f1.program().isActive());
		Assert.assertEquals(values.size(), values2.size());
		for (int i=0;i<values.size(); i++) {
			Assert.assertEquals(values.get(i), values2.get(i));
		}
	}

	@Test
	public void complexCopyWorks0() {
		final TemperatureSensor source = getApplicationManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		final Room targetParent = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		source.reading().forecast().create();
		final String sub = "test";
		source.addDecorator(sub, Room.class);
		source.reading().activate(false);
		ResourceUtils.copy(source, targetParent.temperatureSensor(), getApplicationManager().getResourceAccess());
		ResourceAssertions.assertExists(targetParent.temperatureSensor());
		ResourceAssertions.assertExists(targetParent.temperatureSensor().reading().forecast());
		ResourceAssertions.assertActive(targetParent.temperatureSensor().reading());
		ResourceAssertions.assertExists(targetParent.temperatureSensor().getSubResource(sub, Room.class));
		Assert.assertFalse(targetParent.temperatureSensor().isActive());
		Assert.assertFalse(targetParent.temperatureSensor().reading().forecast().isActive());		
	}
	
	@Test
	public void referenceCopyWorks1() {
		final TemperatureSensor source = getApplicationManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		final Room targetParent = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		final Room room = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		source.location().room().setAsReference(room);
		ResourceUtils.copy(source, targetParent.temperatureSensor(), getApplicationManager().getResourceAccess());
		ResourceAssertions.assertLocationsEqual(room, targetParent.temperatureSensor().location().room());
	}
	
	@Test
	public void referenceCopyWorks2() {
		final TemperatureSensor source = getApplicationManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		final Room targetParent = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		final Room room = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		source.location().room().setAsReference(room);
		final ResourceCopyConfiguration cfg = ResourceCopyConfigurationBuilder.newInstance()
				.setCopyReferences(false)
				.build();
		ResourceUtils.copy(source, targetParent.temperatureSensor(), getApplicationManager().getResourceAccess(), cfg);
		ResourceAssertions.assertIsVirtual(targetParent.temperatureSensor().location().room());
		ResourceAssertions.assertExists(targetParent.temperatureSensor().location());
	}
	
	@Test
	public void typeExclusionWorks() {
		final TemperatureSensor source = getApplicationManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		final Room targetParent = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		source.location().room().create();
		source.location().device().create();
		final ResourceCopyConfiguration cfg = ResourceCopyConfigurationBuilder.newInstance()
				.setExcludedResourceTypes(Arrays.<Class<? extends Resource>> asList(Room.class))
				.build();
		ResourceUtils.copy(source, targetParent.temperatureSensor(), getApplicationManager().getResourceAccess(), cfg);
		ResourceAssertions.assertIsVirtual(targetParent.temperatureSensor().location().room());
		ResourceAssertions.assertExists(targetParent.temperatureSensor().location().device());
	}
	
	@Test
	public void pathExclusionWorks() {
		final TemperatureSensor source = getApplicationManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		final Room targetParent = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		source.location().room().create();
		source.location().device().create();
		final ResourceCopyConfiguration cfg = ResourceCopyConfigurationBuilder.newInstance()
				.setExcludedRelativePaths(Arrays.asList("location/room"))
				.build();
		ResourceUtils.copy(source, targetParent.temperatureSensor(), getApplicationManager().getResourceAccess(), cfg);
		ResourceAssertions.assertIsVirtual(targetParent.temperatureSensor().location().room());
		ResourceAssertions.assertExists(targetParent.temperatureSensor().location().device());
	}
	
}
