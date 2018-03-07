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
package org.ogema.util.test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.tools.SerializationManager;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.ResourceAssertions;
import org.ogema.model.actors.OnOffSwitch;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * OSGi/OGEMA integrated tests.
 * 
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class JsonSerializationOsgiTest extends OsgiAppTestBase {

	SerializationManager sman;
	ResourceManagement resman;

	@Before
	public void setup() {
		sman = getApplicationManager().getSerializationManager();
		resman = getApplicationManager().getResourceManagement();
	}

	@Test
	public void arraysOfStringWork() throws IOException {
		Resource arrays = resman.createResource(newResourceName(), Resource.class);

		StringArrayResource strings = arrays.addDecorator("arr", StringArrayResource.class);
        String[] serializedValues = new String[] { "a", "b", "c" };
		strings.setValues(serializedValues);

		StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
		sman.writeJson(output, arrays);
		System.out.println(output);

        strings.setValues(new String[] { "x", "y", "z" });
        Assert.assertFalse(Arrays.asList(serializedValues).equals(Arrays.asList(strings.getValues())));
        sman.applyJson(output.toString(), arrays, true);
        Assert.assertTrue(Arrays.asList(serializedValues).equals(Arrays.asList(strings.getValues())));
	}
    
    @Test
	public void arraysOfByteWork() throws IOException {
		Resource arrays = resman.createResource(newResourceName(), Resource.class);

		ByteArrayResource bytes = arrays.addDecorator("arr", ByteArrayResource.class);
        byte[] serializedValues = "test".getBytes(StandardCharsets.UTF_8);
		bytes.setValues(serializedValues);

		StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
		sman.writeJson(output, arrays);
		System.out.println(output);

        bytes.setValues("fnord".getBytes(StandardCharsets.UTF_8));
        Assert.assertFalse(Arrays.equals(serializedValues, "fnord".getBytes(StandardCharsets.UTF_8)));
        sman.applyJson(output.toString(), arrays, true);
        System.out.println(new String(bytes.getValues(), StandardCharsets.UTF_8));
        Assert.assertTrue(Arrays.equals(serializedValues, bytes.getValues()));
	}
    
    @Test
    public void floatScheduleWorks() throws IOException {
        PowerResource pow = resman.createResource(newResourceName(), PowerResource.class);
        Schedule s = pow.program().create();
        Collection<SampledValue> values = Arrays.asList(
                new SampledValue(new FloatValue(0), 0, Quality.GOOD),
                new SampledValue(new FloatValue(1), 1, Quality.GOOD),
                new SampledValue(new FloatValue(4), 2, Quality.GOOD),
                new SampledValue(new FloatValue(9), 3, Quality.GOOD));
        s.addValues(values);
        
        StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
        sman.setSerializeSchedules(true);
		sman.writeJson(output, pow);
		//System.out.println(output);
        
        Assert.assertEquals(values, s.getValues(0));
        s.addValue(3, new FloatValue(7));
        Assert.assertNotEquals(values, s.getValues(0));
        
        sman.applyJson(output.toString(), pow, true);
        Assert.assertEquals(values, s.getValues(0));
    }
    
    /* test de-/serialization of float NaN and Infinity values */
    @Test
    public void floatScheduleWithSpecialValuesWork() throws IOException {
        PowerResource pow = resman.createResource(newResourceName(), PowerResource.class);
        Schedule s = pow.program().create();
        Collection<SampledValue> values = Arrays.asList(
                new SampledValue(new FloatValue(Float.NaN), 0, Quality.GOOD),
                new SampledValue(new FloatValue(Float.POSITIVE_INFINITY), 1, Quality.GOOD),
                new SampledValue(new FloatValue(Float.NEGATIVE_INFINITY), 2, Quality.GOOD));
        s.addValues(values);
        
        StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
        sman.setSerializeSchedules(true);
		sman.writeJson(output, pow);
		//System.out.println(output);
        
        Assert.assertEquals(values, s.getValues(0));
        s.addValue(3, new FloatValue(7));
        Assert.assertNotEquals(values, s.getValues(0));
        s.deleteValues();
        Assert.assertTrue(s.getValues(0).isEmpty());
        
        sman.applyJson(output.toString(), pow, true);
        Assert.assertEquals(3, s.getValues(0).size());
        Assert.assertTrue(Float.isNaN(s.getValue(0).getValue().getFloatValue()));
        Assert.assertTrue(Float.isInfinite(s.getValue(1).getValue().getFloatValue()));
        Assert.assertTrue(0 < s.getValue(1).getValue().getFloatValue());
        Assert.assertTrue(Float.isInfinite(s.getValue(2).getValue().getFloatValue()));
        Assert.assertTrue(0 > s.getValue(2).getValue().getFloatValue());
    }
    
    @Test
    public void floatNaNWorks() throws IOException {
        FloatResource f = resman.createResource(newResourceName(), FloatResource.class);
        f.setValue(Float.NaN);
        
        StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
        sman.setSerializeSchedules(true);
		sman.writeJson(output, f);
		System.out.println(output);
        
        Assert.assertTrue(Float.isNaN(f.getValue()));
        f.setValue(0);
        Assert.assertFalse(Float.isNaN(f.getValue()));
        
        sman.applyJson(output.toString(), f, true);
        Assert.assertTrue(Float.isNaN(f.getValue()));
    }
    
    @Test
    public void floatInfinityWorks() throws IOException {
        FloatResource f = resman.createResource(newResourceName(), FloatResource.class);
        f.setValue(Float.POSITIVE_INFINITY);
        
        StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
        sman.setSerializeSchedules(true);
		sman.writeJson(output, f);
		System.out.println(output);
        
        Assert.assertTrue(Float.isInfinite(f.getValue()));
        f.setValue(0);
        Assert.assertFalse(Float.isInfinite(f.getValue()));
        
        sman.applyJson(output.toString(), f, true);
        Assert.assertTrue(Float.isInfinite(f.getValue()));
        Assert.assertTrue(0 < f.getValue());
    }
    
    @Test
    public void floatNegativeInfinityWorks() throws IOException {
        FloatResource f = resman.createResource(newResourceName(), FloatResource.class);
        f.setValue(Float.NEGATIVE_INFINITY);
        
        StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
        sman.setSerializeSchedules(true);
		sman.writeJson(output, f);
		System.out.println(output);
        
        Assert.assertTrue(Float.isInfinite(f.getValue()));
        f.setValue(0);
        Assert.assertFalse(Float.isInfinite(f.getValue()));
        
        sman.applyJson(output.toString(), f, true);
        Assert.assertTrue(Float.isInfinite(f.getValue()));
        Assert.assertTrue(0 > f.getValue());
    }
    
    @Test
    public void collectionsWork() throws IOException {
        PowerResource pow = resman.createResource(newResourceName(), PowerResource.class);
        OnOffSwitch sw = resman.createResource(newResourceName(), OnOffSwitch.class);
        pow.setValue(42);
        sw.stateControl().create();
        sw.stateControl().setValue(true);
        
        StringWriter output = new StringWriter();
        sman.writeJson(output, Arrays.asList(pow, sw));
        System.out.println(output);
        
        pow.setValue(0);
        pow.delete();
        sw.delete();
        ResourceAssertions.assertDeleted(pow);
        ResourceAssertions.assertDeleted(sw);
        
        sman.createResourcesFromJson(output.toString());
        
        ResourceAssertions.assertExists(pow);
        ResourceAssertions.assertExists(sw);
        assertEquals(42, pow.getValue(), 0);
    }

}
