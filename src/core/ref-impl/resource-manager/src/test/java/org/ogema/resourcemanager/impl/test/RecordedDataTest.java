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
package org.ogema.resourcemanager.impl.test;

import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.sensors.SolarIrradiationSensor;
import org.ogema.recordeddata.DataRecorder;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;
import org.ogema.tools.resource.util.LoggingUtils;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class RecordedDataTest extends OsgiAppTestBase {

	// takes extremely long...
//	private static final int MAX_FOR_REDUCTION_MODE_TEST = 500000;
	private static final int MAX_FOR_REDUCTION_MODE_TEST = 50000;

	@Inject
	DataRecorder rda;

	public static void removeData() throws IOException {
		Path slotsDbData = Paths.get("data", "slotsdb");
		Files.walkFileTree(slotsDbData, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

		});
		Files.createDirectory(slotsDbData);
	}

	@Override
	@Configuration
	public Option[] config() {
		try {
			removeData();
		} catch (IOException ex) {
			Logger.getLogger(RecordedDataTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		return new Option[] {
				// CoreOptions.systemProperty("org.ogema.persistence").value("active"),
				CoreOptions.mavenBundle("org.apache.commons", "commons-math3", "3.3").start(),
				CoreOptions.composite(super.config()) };
	}

	private boolean dontRunLongTests() {
		return "true".equalsIgnoreCase(System.getenv("NO_LONG_TESTS"));
	}

	@Test
	public void onUpdateWorks() throws Exception {
		ResourceManagement resman = getApplicationManager().getResourceManagement();
		SolarIrradiationSensor sens = resman.createResource(newResourceName(), SolarIrradiationSensor.class);
		sens.reading().create();
		RecordedData rd = sens.reading().getHistoricalData();
		RecordedDataConfiguration conf = rd.getConfiguration();
		Assert.assertNull(conf);
		Assert.assertTrue(rd.getValues(0).isEmpty());
		conf = new RecordedDataConfiguration();
		conf.setFixedInterval(Long.MAX_VALUE);
		conf.setStorageType(RecordedDataConfiguration.StorageType.ON_VALUE_UPDATE);
		rd.setConfiguration(conf);
		sens.activate(true);

		final CountDownLatch cdl = new CountDownLatch(3);
		ResourceValueListener<ValueResource> l = new ResourceValueListener<ValueResource>() {

			@Override
			public void resourceChanged(ValueResource resource) {
				cdl.countDown();
			}
		};
		sens.reading().addValueListener(l, true);

		sens.reading().setValue(3f);
		Thread.sleep(50); // need distinct framework time values
		sens.reading().setValue(6f);
		Thread.sleep(50);
		sens.reading().setValue(9f);
		Thread.sleep(2000);

		assertTrue("not enough callbacks: " + cdl.getCount(), cdl.await(5, TimeUnit.SECONDS));

		Assert.assertEquals(String.format("size(%s) != 3", rd.getValues(0)), 3, rd.getValues(0).size());
		Assert.assertEquals(3, rd.getValues(0).get(0).getValue().getFloatValue(), 0f);
		Assert.assertEquals(6, rd.getValues(0).get(1).getValue().getFloatValue(), 0f);
		Assert.assertEquals(9, rd.getValues(0).get(2).getValue().getFloatValue(), 0f);
	}

	@Test
	public void loggingBooleansWorks() throws Exception {
		final BooleanResource resource = getApplicationManager().getResourceManagement().createResource(
				newResourceName(), BooleanResource.class);
		resource.activate(true);
		final RecordedData data = resource.getHistoricalData();
		RecordedDataConfiguration config = data.getConfiguration();
		Assert.assertNull(config);
		config = new RecordedDataConfiguration();
		config.setStorageType(RecordedDataConfiguration.StorageType.ON_VALUE_UPDATE);
		data.setConfiguration(config);

		Assert.assertEquals(0, data.getValues(0).size());
		resource.setValue(true);
		Thread.sleep(20);
		resource.setValue(true);
		Thread.sleep(20);
		resource.setValue(false);
		final List<SampledValue> values = data.getValues(0);
		assertEquals(3, values.size());
		assertEquals(true, values.get(0).getValue().getBooleanValue());
		assertEquals(true, values.get(1).getValue().getBooleanValue());
		assertEquals(false, values.get(2).getValue().getBooleanValue());
	}

	@Test
	public void loggingIntegersWorks() throws Exception {
		final IntegerResource resource = getApplicationManager().getResourceManagement().createResource(
				newResourceName(), IntegerResource.class);
		resource.activate(true);
		final RecordedData data = resource.getHistoricalData();
		RecordedDataConfiguration config = data.getConfiguration();
		Assert.assertNull(config);
		config = new RecordedDataConfiguration();
		config.setStorageType(RecordedDataConfiguration.StorageType.ON_VALUE_UPDATE);
		data.setConfiguration(config);

		Assert.assertEquals(0, data.getValues(0).size());
		resource.setValue(815);
		Thread.sleep(20);
		resource.setValue(4711);
		Thread.sleep(20);
		resource.setValue(-7324);
		final List<SampledValue> values = data.getValues(0);
		assertEquals(3, values.size());
		assertEquals(815, values.get(0).getValue().getIntegerValue());
		assertEquals(4711, values.get(1).getValue().getIntegerValue());
		assertEquals(-7324, values.get(2).getValue().getIntegerValue());
	}

	@Test
	public void loggingTimeValuesWorks() throws Exception {
		final TimeResource resource = getApplicationManager().getResourceManagement().createResource(newResourceName(),
				TimeResource.class);
		resource.activate(true);
		final RecordedData data = resource.getHistoricalData();
		RecordedDataConfiguration config = data.getConfiguration();
		Assert.assertNull(config);
		config = new RecordedDataConfiguration();
		config.setStorageType(RecordedDataConfiguration.StorageType.ON_VALUE_UPDATE);
		data.setConfiguration(config);

		Assert.assertEquals(0, data.getValues(0).size());
		resource.setValue(815);
		Thread.sleep(20);
		resource.setValue(4711);
		Thread.sleep(20);
		resource.setValue(-7324);
		final List<SampledValue> values = data.getValues(0);
		assertEquals(3, values.size());
		assertEquals(815, values.get(0).getValue().getIntegerValue());
		assertEquals(4711, values.get(1).getValue().getIntegerValue());
		assertEquals(-7324, values.get(2).getValue().getIntegerValue());
	}

	@Test
	public void fixedIntervalUpdatesWork() throws Exception {
		ResourceManagement resman = getApplicationManager().getResourceManagement();
		SolarIrradiationSensor sens = resman.createResource(newResourceName(), SolarIrradiationSensor.class);
		sens.reading().create();
		sens.reading().setValue(47.11f);
		sens.deactivate(true);
		sens.reading().deactivate(true);

		RecordedData rd = sens.reading().getHistoricalData();
		RecordedDataConfiguration conf = rd.getConfiguration();
		Assert.assertNull(conf);
		Assert.assertTrue(rd.getValues(0).isEmpty());
		conf = new RecordedDataConfiguration();
		conf.setFixedInterval(20);
		conf.setStorageType(RecordedDataConfiguration.StorageType.FIXED_INTERVAL);
		rd.setConfiguration(conf);

		Thread.sleep(1500);
		assertTrue("data recorded for inactive resource", rd.getValues(0).isEmpty());

		sens.activate(true);

		Thread.sleep(1500);
		assertFalse("recorded data is empty", rd.getValues(0).isEmpty());

		assertTrue(rd.getValues(0).size() > 50);
		for (SampledValue sv : rd.getValues(0)) {
			Assert.assertEquals(47.11f, sv.getValue().getFloatValue(), 0.1f);
		}

	}

	@Test
	public void testRecordedDataAccess() throws Exception {
		RecordedDataStorage rds = createRecordedDataStorage();
		assertTrue(rds.getValues(0).isEmpty());
		rds.insertValue(new SampledValue(new FloatValue(47.11f), 1, Quality.GOOD));
		assertFalse(rds.getValues(0).isEmpty());
		rds.insertValue(new SampledValue(new FloatValue(47.11f), 2, Quality.GOOD));
		rds.insertValue(new SampledValue(new FloatValue(47.11f), 3, Quality.GOOD));
		rds.insertValue(new SampledValue(new FloatValue(47.11f), 4, Quality.GOOD));
		assertEquals(4, rds.getValues(0).size());
	}

	@Test
	public void testReductionModeOneInterval() throws Exception {
		Assume.assumeFalse(dontRunLongTests());
		int randomInt = new Random().nextInt(MAX_FOR_REDUCTION_MODE_TEST);
		SummaryStatistics ss = new SummaryStatistics();
		// use (probable) prime as seed
		long seed = new BigInteger(String.valueOf(randomInt)).nextProbablePrime().longValue();
		Random rnd = new Random(seed);
		int nmbOfValues = rnd.nextInt(MAX_FOR_REDUCTION_MODE_TEST) + 1;

		RecordedDataStorage rds = initReductionModeTestWithQualityGood(MAX_FOR_REDUCTION_MODE_TEST, Arrays.asList(ss),
				rnd, nmbOfValues, 1);

		System.out.println("Starting reduction mode test single interval with seed=" + seed + " and nmb of values="
				+ nmbOfValues);
		// test for average ...
		List<SampledValue> values = rds.getValues(0, nmbOfValues + 1, nmbOfValues + 1, ReductionMode.AVERAGE);
		Assert.assertTrue(values.size() == 1);
		double result = values.get(0).getValue().getDoubleValue();
		// Double.compare(result, ss.getMean()) == 0  not appropriate here ... due to rounding errors
		Assert.assertTrue("Expected: " + ss.getMean() + " but got: " + result + " - seed: " + seed, nearlyEqual(result,
				ss.getMean(), 1E-6));

		// test for max ...
		values = rds.getValues(0, nmbOfValues + 1, nmbOfValues + 1, ReductionMode.MAXIMUM_VALUE);
		Assert.assertTrue(values.size() == 1);
		result = values.get(0).getValue().getDoubleValue();
		Assert.assertTrue("Expected: " + ss.getMax() + " but got: " + result + " - seed: " + seed, Double.compare(
				result, ss.getMax()) == 0);

		// test for min ...
		values = rds.getValues(0, nmbOfValues + 1, nmbOfValues + 1, ReductionMode.MINIMUM_VALUE);
		Assert.assertTrue(values.size() == 1);
		result = values.get(0).getValue().getDoubleValue();
		Assert.assertTrue("Expected: " + ss.getMin() + " but got: " + result + " - seed: " + seed, Double.compare(
				result, ss.getMin()) == 0);

		// test for min/max ...
		values = rds.getValues(0, nmbOfValues + 1, nmbOfValues + 1, ReductionMode.MIN_MAX_VALUE);
		Assert.assertTrue(values.size() > 1);
		double val1 = values.get(0).getValue().getDoubleValue();
		double val2 = values.get(1).getValue().getDoubleValue();
		double min = val1 > val2 ? val2 : val1;
		double max = val1 > val2 ? val1 : val2;
		Assert.assertTrue("Expected: " + ss.getMin() + " but got: " + min + " - seed: " + seed, Double.compare(min, ss
				.getMin()) == 0);
		Assert.assertTrue("Expected: " + ss.getMax() + " but got: " + max + " - seed: " + seed, Double.compare(max, ss
				.getMax()) == 0);
	}

	@Test
    public void testReductionModeMultipleIntervalsWithQualityGood() throws Exception {
        Assume.assumeFalse(dontRunLongTests());
        int randomInt = new Random().nextInt(MAX_FOR_REDUCTION_MODE_TEST);
        // use (probable) prime as seed
        long seed = new BigInteger(String.valueOf(randomInt)).nextProbablePrime().longValue();
        Random rnd = new Random(seed);
        int nmbOfValues = rnd.nextInt(MAX_FOR_REDUCTION_MODE_TEST) + 2; // at least 2 values ...
        int nmbOfIntervals = rnd.nextInt(nmbOfValues / 2) + 2; // at least two intervals

        // SlotsDBStorage is very slow for multiple intervals ... limit it for now to 10:
        if (nmbOfIntervals > 10) {
            nmbOfIntervals %= 10;
            // could be 0 or 1 but we want at least 2 intervals ...
            if (nmbOfIntervals < 2) {
                nmbOfIntervals = 2;
            }
        }

        System.out.println("Starting reduction mode test multiple intervals (Quality.GOOD) with seed=" + seed + ", nmb of values="
                + nmbOfValues + " and nmb of intervals=" + nmbOfIntervals);

        List<SummaryStatistics> ss = new ArrayList<>();
        for (int i = 0; i < nmbOfIntervals; ++i) {
            ss.add(new SummaryStatistics());
        }

        RecordedDataStorage rds = initReductionModeTestWithQualityGood(MAX_FOR_REDUCTION_MODE_TEST, ss, rnd, nmbOfValues, nmbOfIntervals);

        int intervalSize = nmbOfValues / nmbOfIntervals;
        int endTime = intervalSize * nmbOfIntervals + 1;
        String sizeCmpMsg = "number of values for %s is not the same as the number"
                + " of intervals! values.size()= %d, nmbOfIntervals=%d - seed: %d";

        // test for average ...
        List<SampledValue> values = rds.getValues(1, endTime, intervalSize, ReductionMode.AVERAGE);
        Assert.assertTrue(String.format(sizeCmpMsg, "mean", values.size(), nmbOfIntervals, seed),
                values.size() == nmbOfIntervals);
        for (int i = 0; i < nmbOfIntervals; ++i) {
            double result = values.get(i).getValue().getDoubleValue();
            // Double.compare(result, ss.getMean()) == 0  not appropriate here ... due to rounding errors
            Assert.assertTrue("Mean: expected " + ss.get(i).getMean() + " but got " + result + " - seed: " + seed,
                    nearlyEqual(result, ss.get(i).getMean(), 1E-6));
            assertTrue("Mean: expected Quality.GOOD", values.get(i).getQuality() == Quality.GOOD);
        }

        // test for max ...
        values = rds.getValues(1, endTime, intervalSize, ReductionMode.MAXIMUM_VALUE);
        Assert.assertTrue(String.format(sizeCmpMsg, "max", values.size(), nmbOfIntervals, seed),
                values.size() == nmbOfIntervals);
        for (int i = 0; i < nmbOfIntervals; ++i) {
            double result = values.get(i).getValue().getDoubleValue();
            Assert.assertTrue("Max: expected " + ss.get(i).getMax() + " but got " + result + " - seed: " + seed, Double
                    .compare(result, ss.get(i).getMax()) == 0);
            assertTrue("Mean: expected Quality.GOOD", values.get(i).getQuality() == Quality.GOOD);
        }

        // test for min ...
        values = rds.getValues(1, endTime, intervalSize, ReductionMode.MINIMUM_VALUE);
        Assert.assertTrue(String.format(sizeCmpMsg, "min", values.size(), nmbOfIntervals, seed),
                values.size() == nmbOfIntervals);
        for (int i = 0; i < nmbOfIntervals; ++i) {
            double result = values.get(i).getValue().getDoubleValue();
            Assert.assertTrue("Min: expected " + ss.get(i).getMin() + " but got " + result + " - seed: " + seed, Double
                    .compare(result, ss.get(i).getMin()) == 0);
            assertTrue("Min: expected Quality.GOOD", values.get(i).getQuality() == Quality.GOOD);
        }

        // test for min/max ...
        values = rds.getValues(1, endTime, intervalSize, ReductionMode.MIN_MAX_VALUE);
        Assert.assertTrue(String.format(sizeCmpMsg, "min/max", values.size(), nmbOfIntervals, seed),
                values.size() == nmbOfIntervals * 2);
        for (int i = 0, j = 0; i < nmbOfIntervals; ++i) {
            // min is in first and max in second idx ...
            SampledValue min = values.get(j++);
            SampledValue max = values.get(j++);

            assertTrue("Min/Max: expected " + ss.get(i).getMin() + " for min but got "
                    + min + " - seed: " + seed, Double.compare(min.getValue().getDoubleValue(), ss.get(i).getMin()) == 0);
            assertTrue("Min/Max: expected Quality.GOOD", min.getQuality() == Quality.GOOD);
            assertTrue("Min/Max expected " + ss.get(i).getMax() + " for max but got "
                    + max + " - seed: " + seed, Double.compare(max.getValue().getDoubleValue(), ss.get(i).getMax()) == 0);
            assertTrue("Min/Max: expected Quality.GOOD", max.getQuality() == Quality.GOOD);
        }
    }

	@Test
    public void testReductionModeMultipleIntervalsWithQualityBad() throws Exception {
        Assume.assumeTrue(false);
        int randomInt = new Random().nextInt(MAX_FOR_REDUCTION_MODE_TEST);
        // use (probable) prime as seed
        long seed = new BigInteger(String.valueOf(randomInt)).nextProbablePrime().longValue();
        Random rnd = new Random(seed);
        int nmbOfValues = rnd.nextInt(MAX_FOR_REDUCTION_MODE_TEST) + 2; // at least 2 values ...
        int nmbOfIntervals = rnd.nextInt(nmbOfValues / 2) + 2; // at least two intervals

        // SlotsDBStorage is very slow for multiple intervals ... limit it for now to 10:
        if (nmbOfIntervals > 10) {
            nmbOfIntervals %= 10;
            // could be 0 or 1 but we want at least 2 intervals ...
            if (nmbOfIntervals < 2) {
                nmbOfIntervals = 2;
            }
        }

        System.out.println("Starting reduction mode test multiple intervals (Quality.BAD) with seed=" + seed + ", nmb of values="
                + nmbOfValues + " and nmb of intervals=" + nmbOfIntervals);

        List<SummaryStatistics> ss = new ArrayList<>();
        for (int i = 0; i < nmbOfIntervals; ++i) {
            ss.add(new SummaryStatistics());
        }

        RecordedDataStorage rds = initReductionModeTestWithQualityBad(MAX_FOR_REDUCTION_MODE_TEST, ss, rnd, nmbOfValues, nmbOfIntervals);

        int intervalSize = nmbOfValues / nmbOfIntervals;
        int endTime = intervalSize * nmbOfIntervals + 1;

        String sizeCmpMsg = "number of values for %s is not the same as the number"
                + " of intervals! values.size()= %d, nmbOfIntervals=%d - seed: %d";

        // test for average ...
        List<SampledValue> values = rds.getValues(1, endTime, intervalSize, ReductionMode.AVERAGE);
        Assert.assertTrue(String.format(sizeCmpMsg, "mean", values.size(), nmbOfIntervals, seed),
                values.size() == nmbOfIntervals);
        for (int i = 0; i < nmbOfIntervals; ++i) {
            double result = values.get(i).getValue().getDoubleValue();
            // Double.compare(result, ss.getMean()) == 0  not appropriate here ... due to rounding errors
            assertTrue("Mean: expected " + ss.get(i).getMean() + " but got " + result + " - seed: " + seed,
                    nearlyEqual(result, ss.get(i).getMean(), 1E-6));
            assertTrue("Mean: expected Quality.BAD", values.get(i).getQuality() == Quality.BAD);
        }

        // test for max ...
        values = rds.getValues(1, endTime, intervalSize, ReductionMode.MAXIMUM_VALUE);
        Assert.assertTrue(String.format(sizeCmpMsg, "max", values.size(), nmbOfIntervals, seed),
                values.size() == nmbOfIntervals);
        for (int i = 0; i < nmbOfIntervals; ++i) {
            double result = values.get(i).getValue().getDoubleValue();
            assertTrue("Max: expected " + ss.get(i).getMax() + " but got " + result + " - seed: " + seed, Double
                    .compare(result, ss.get(i).getMax()) == 0);
            assertTrue("Max: expected Quality.BAD", values.get(i).getQuality() == Quality.BAD);
        }

        // test for min ...
        values = rds.getValues(1, endTime, intervalSize, ReductionMode.MINIMUM_VALUE);
        Assert.assertTrue(String.format(sizeCmpMsg, "min", values.size(), nmbOfIntervals, seed),
                values.size() == nmbOfIntervals);
        for (int i = 0; i < nmbOfIntervals; ++i) {
            double result = values.get(i).getValue().getDoubleValue();
            assertTrue("Min: expected " + ss.get(i).getMin() + " but got " + result + " - seed: " + seed, Double
                    .compare(result, ss.get(i).getMin()) == 0);
            assertTrue("Min: expected Quality.BAD", values.get(i).getQuality() == Quality.BAD);
        }

        // test for min/max ...
        values = rds.getValues(1, endTime, intervalSize, ReductionMode.MIN_MAX_VALUE);
        Assert.assertTrue(String.format(sizeCmpMsg, "min/max", values.size(), nmbOfIntervals, seed),
                values.size() == nmbOfIntervals * 2);
        for (int i = 0, j = 0; i < nmbOfIntervals; ++i) {
            // min is in first and max in second idx ...
            double min = values.get(j++).getValue().getDoubleValue();
            double max = values.get(j++).getValue().getDoubleValue();

            assertTrue("Min/Max: expected " + ss.get(i).getMin() + " for min but got "
                    + min + " - seed: " + seed, Double.compare(min, ss.get(i).getMin()) == 0);
            assertTrue("Min/Max expected " + ss.get(i).getMax() + " for max but got "
                    + max + " - seed: " + seed, Double.compare(max, ss.get(i).getMax()) == 0);
            assertTrue("Min/Max: expected Quality.BAD", values.get(i).getQuality() == Quality.BAD);
        }
    }

	@Test
    public void testReductionModeMultipleIntervalsWithRandomQuality() throws Exception {
        int randomInt = new Random().nextInt(MAX_FOR_REDUCTION_MODE_TEST);
        // use (probable) prime as seed
        long seed = new BigInteger(String.valueOf(randomInt)).nextProbablePrime().longValue();
        Random rnd = new Random(seed);
        int nmbOfValues = rnd.nextInt(MAX_FOR_REDUCTION_MODE_TEST) + 2; // at least 2 values ...
        int nmbOfIntervals = rnd.nextInt(nmbOfValues / 2) + 2; // at least two intervals

        // SlotsDBStorage is very slow for multiple intervals ... limit it for now to 10:
        if (nmbOfIntervals > 10) {
            nmbOfIntervals %= 10;
            // could be 0 or 1 but we want at least 2 intervals ...
            if (nmbOfIntervals < 2) {
                nmbOfIntervals = 2;
            }
        }

        System.out.println("Starting reduction mode test multiple intervals (random quality) with seed=" + seed + ", nmb of values="
                + nmbOfValues + " and nmb of intervals=" + nmbOfIntervals);

        List<SummaryStatistics> ss = new ArrayList<>();
        for (int i = 0; i < nmbOfIntervals; ++i) {
            ss.add(new SummaryStatistics());
        }

        // this list will contain the expected quality of the result from RecordedDataStorage
        // for the interval i at index i in this list
        List<Quality> expectedQuality = new ArrayList<>();
        RecordedDataStorage rds = initReductionModeTest(MAX_FOR_REDUCTION_MODE_TEST, ss, rnd, nmbOfValues, nmbOfIntervals, true, Quality.GOOD, expectedQuality);

        int intervalSize = nmbOfValues / nmbOfIntervals;
        int endTime = intervalSize * nmbOfIntervals + 1;

        String sizeCmpMsg = "number of values for %s is not the same as the number"
                + " of intervals! values.size()= %d, nmbOfIntervals=%d - seed: %d";

        // test for average ...
        List<SampledValue> values = rds.getValues(1, endTime, intervalSize, ReductionMode.AVERAGE);
        Assert.assertTrue(String.format(sizeCmpMsg, "mean", values.size(), nmbOfIntervals, seed),
                values.size() == nmbOfIntervals);
        for (int i = 0; i < nmbOfIntervals; ++i) {
            double result = values.get(i).getValue().getDoubleValue();
            // Double.compare(result, ss.getMean()) == 0  not appropriate here ... due to rounding errors
            assertTrue("Mean: expected " + ss.get(i).getMean() + " but got " + result + " - seed: " + seed,
                    nearlyEqual(result, ss.get(i).getMean(), 1E-6));
            assertTrue("Mean: expected " + expectedQuality.get(i).toString(), values.get(i).getQuality() == expectedQuality.get(i));
        }

        // test for max ...
        values = rds.getValues(1, endTime, intervalSize, ReductionMode.MAXIMUM_VALUE);
        Assert.assertTrue(String.format(sizeCmpMsg, "max", values.size(), nmbOfIntervals, seed),
                values.size() == nmbOfIntervals);
        for (int i = 0; i < nmbOfIntervals; ++i) {
            double result = values.get(i).getValue().getDoubleValue();
            assertTrue("Max: expected " + ss.get(i).getMax() + " but got " + result + " - seed: " + seed, Double
                    .compare(result, ss.get(i).getMax()) == 0);
            assertTrue("Max: expected " + expectedQuality.get(i).toString(), values.get(i).getQuality() == expectedQuality.get(i));
        }

        // test for min ...
        values = rds.getValues(1, endTime, intervalSize, ReductionMode.MINIMUM_VALUE);
        Assert.assertTrue(String.format(sizeCmpMsg, "min", values.size(), nmbOfIntervals, seed),
                values.size() == nmbOfIntervals);
        for (int i = 0; i < nmbOfIntervals; ++i) {
            double result = values.get(i).getValue().getDoubleValue();
            assertTrue("Min: expected " + ss.get(i).getMin() + " but got " + result + " - seed: " + seed, Double
                    .compare(result, ss.get(i).getMin()) == 0);
            assertTrue("Min: expected " + expectedQuality.get(i).toString(), values.get(i).getQuality() == expectedQuality.get(i));
        }

        // test for min/max ...
        values = rds.getValues(1, endTime, intervalSize, ReductionMode.MIN_MAX_VALUE);
        Assert.assertTrue(String.format(sizeCmpMsg, "min/max", values.size(), nmbOfIntervals, seed),
                values.size() == nmbOfIntervals * 2);
        for (int i = 0, j = 0; i < nmbOfIntervals; ++i) {
            // min is in first and max in second idx ...
            SampledValue min = values.get(j++);
            SampledValue max = values.get(j++);

            assertTrue("Min/Max: expected " + ss.get(i).getMin() + " for min but got "
                    + min + " - seed: " + seed, Double.compare(min.getValue().getDoubleValue(), ss.get(i).getMin()) == 0);
            assertTrue("Min/Max: expected " + expectedQuality.get(i).toString(), min.getQuality() == expectedQuality.get(i));
            assertTrue("Min/Max expected " + ss.get(i).getMax() + " for max but got "
                    + max + " - seed: " + seed, Double.compare(max.getValue().getDoubleValue(), ss.get(i).getMax()) == 0);
            assertTrue("Min/Max: expected " + expectedQuality.get(i).toString(), max.getQuality() == expectedQuality.get(i));
        }
    }

	@Test
    public void testReductionModeWithEmptyInterval() throws Exception {
        int randomInt = new Random().nextInt(MAX_FOR_REDUCTION_MODE_TEST);
        long seed = new BigInteger(String.valueOf(randomInt)).nextProbablePrime().longValue();
        Random rnd = new Random(seed);
        int nmbOfValues = rnd.nextInt(MAX_FOR_REDUCTION_MODE_TEST) + 2; // at least two values
        List<DoubleValue> randomValues = generateRandomValues(-MAX_FOR_REDUCTION_MODE_TEST,
                MAX_FOR_REDUCTION_MODE_TEST, nmbOfValues, rnd);
        RecordedDataStorage rds = createRecordedDataStorage();
        List<SummaryStatistics> ss = new ArrayList<>();

        System.out.println("Starting reduction mode test with empty interval. Seed=" + seed + ", nmb of values="
                + nmbOfValues);

        // split random values over 2 intervals
        ss.add(new SummaryStatistics());
        for (int i = 0; i < nmbOfValues / 2; ++i) {
            rds.insertValue(new SampledValue(randomValues.get(i), i + 1, Quality.GOOD));
            ss.get(0).addValue(randomValues.get(i).getDoubleValue());
        }

        ss.add(new SummaryStatistics());
        // if nmbOfValues odd - subtract one because the last value won't be in our interval
        // and should be excluded from SummaryStatistics
        int end = nmbOfValues - nmbOfValues % 2;
        for (int i = nmbOfValues / 2; i < end; ++i) {
            rds.insertValue(new SampledValue(randomValues.get(i), i + 1 + nmbOfValues / 2, Quality.GOOD));
            ss.get(1).addValue(randomValues.get(i).getDoubleValue());
        }

        long endTime;
        if (nmbOfValues % 2 == 0) {
            endTime = nmbOfValues + nmbOfValues / 2 + 1;
        } else {
            endTime = nmbOfValues + nmbOfValues / 2;
            // insert last value as well:
            rds.insertValue(new SampledValue(randomValues.get(nmbOfValues - 1), nmbOfValues - 1 + nmbOfValues / 2,
                    Quality.GOOD));
        }

        // mean
        List<SampledValue> values = rds.getValues(1, endTime, nmbOfValues / 2, ReductionMode.AVERAGE);
        assertTrue("Mean: expected size 3 but got " + values.size(), values.size() == 3);

        Value val = values.get(0).getValue();
        assertTrue("Mean: expected " + ss.get(0).getMean() + " but got " + val.getDoubleValue(), nearlyEqual(val
                .getDoubleValue(), ss.get(0).getMean(), 1E-6));
        val = values.get(2).getValue();
        assertTrue("Mean: expected " + ss.get(1).getMean() + " but got " + val.getDoubleValue(), nearlyEqual(val
                .getDoubleValue(), ss.get(1).getMean(), 1E-6));

        assertTrue("Reduction of empty interval should return Quality.BAD!", values.get(1).getQuality() == Quality.BAD);

        // max
        values = rds.getValues(1, endTime, nmbOfValues / 2, ReductionMode.MAXIMUM_VALUE);
        assertTrue("Max: expected size 3 but got " + values.size(), values.size() == 3);

        val = values.get(0).getValue();
        assertTrue("Max: expected " + ss.get(0).getMax() + " but got " + val.getDoubleValue(), Double.compare(val
                .getDoubleValue(), ss.get(0).getMax()) == 0);
        val = values.get(2).getValue();
        assertTrue("Max: expected " + ss.get(1).getMax() + " but got " + val.getDoubleValue(), Double.compare(val
                .getDoubleValue(), ss.get(1).getMax()) == 0);

        assertTrue("Reduction of empty interval should return Quality.BAD!", values.get(1).getQuality() == Quality.BAD);

        // min
        values = rds.getValues(1, endTime, nmbOfValues / 2, ReductionMode.MINIMUM_VALUE);
        assertTrue("Min: expected size 3 but got " + values.size(), values.size() == 3);

        val = values.get(0).getValue();
        assertTrue("Min: expected " + ss.get(0).getMin() + " but got " + val.getDoubleValue(), Double.compare(val
                .getDoubleValue(), ss.get(0).getMin()) == 0);
        val = values.get(2).getValue();
        assertTrue("Min: expected " + ss.get(1).getMin() + " but got " + val.getDoubleValue(), Double.compare(val
                .getDoubleValue(), ss.get(1).getMin()) == 0);

        assertTrue("Reduction of empty interval should return Quality.BAD!", values.get(1).getQuality() == Quality.BAD);

        // min/max
        values = rds.getValues(1, endTime, nmbOfValues / 2, ReductionMode.MIN_MAX_VALUE);
        assertTrue("Min/Max: expected size 6 but got " + values.size(), values.size() == 6);

        val = values.get(0).getValue();
        assertTrue("Min/Max: expected " + ss.get(0).getMin() + " but got " + val.getDoubleValue(), Double.compare(val
                .getDoubleValue(), ss.get(0).getMin()) == 0);
        val = values.get(1).getValue();
        assertTrue("Min/Max: expected " + ss.get(0).getMax() + " but got " + val.getDoubleValue(), Double.compare(val
                .getDoubleValue(), ss.get(0).getMax()) == 0);
        val = values.get(4).getValue();
        assertTrue("Min/Max: expected " + ss.get(1).getMin() + " but got " + val.getDoubleValue(), Double.compare(val
                .getDoubleValue(), ss.get(1).getMin()) == 0);
        val = values.get(5).getValue();
        assertTrue("Min/Max: expected " + ss.get(1).getMax() + " but got " + val.getDoubleValue(), Double.compare(val
                .getDoubleValue(), ss.get(1).getMax()) == 0);

        assertTrue("Reduction of empty interval should return Quality.BAD!", values.get(2).getQuality() == Quality.BAD);
        assertTrue("Reduction of empty interval should return Quality.BAD!", values.get(3).getQuality() == Quality.BAD);
    }

	@Test
	public void testReductionModeMinMaxAtBoundary() throws Exception {
		Assume.assumeFalse(dontRunLongTests());
		int randomInt = new Random().nextInt(MAX_FOR_REDUCTION_MODE_TEST);
		// use (probable) prime as seed
		long seed = new BigInteger(String.valueOf(randomInt)).nextProbablePrime().longValue();
		Random rnd = new Random(seed);
		int nmbOfValues = rnd.nextInt(MAX_FOR_REDUCTION_MODE_TEST) + 2; // at least two values
		int nmbOfIntervals = rnd.nextInt(nmbOfValues / 2) + 2; // at least two intervals

		// SlotsDBStorage is very slow for multiple intervals ... limit it for now to 10:
		if (nmbOfIntervals > 10) {
			nmbOfIntervals %= 10;
			// could be 0 or 1 but we want at least 2 intervals ...
			if (nmbOfIntervals < 2) {
				nmbOfIntervals = 2;
			}
		}

		int intervalSize = nmbOfValues / nmbOfIntervals; // rest is ignored / not in requested interval with reduction mode
		int endTime = intervalSize * nmbOfIntervals + 1;

		System.out.println("Starting reduction mode (min/max at boundary) test with seed=" + seed + ", nmb of values="
				+ nmbOfValues + " and nmb of intervals=" + nmbOfIntervals);

		List<DoubleValue> rndValues = generateRandomValues(-MAX_FOR_REDUCTION_MODE_TEST, MAX_FOR_REDUCTION_MODE_TEST,
				nmbOfValues, rnd);
		// sort in ascending order
		Collections.sort(rndValues, new Comparator<DoubleValue>() {
			@Override
			public int compare(DoubleValue o1, DoubleValue o2) {
				Double v1 = o1.getDoubleValue();
				Double v2 = o2.getDoubleValue();
				return v1.compareTo(v2);
			}
		});

		RecordedDataStorage rds = createRecordedDataStorage();
		for (int i = 0; i < nmbOfIntervals; ++i) {
			for (int j = 0; j < intervalSize; ++j) {
				rds.insertValue(new SampledValue(rndValues.get(j + (i * intervalSize)), (j + 1) + (i * intervalSize),
						Quality.GOOD));
			}
		}

		List<SampledValue> maxValues = rds.getValues(1, endTime, intervalSize, ReductionMode.MAXIMUM_VALUE);
		List<SampledValue> minValues = rds.getValues(1, endTime, intervalSize, ReductionMode.MINIMUM_VALUE);
		List<SampledValue> minMaxValues = rds.getValues(1, endTime, intervalSize, ReductionMode.MIN_MAX_VALUE);
		for (int i = 0; i < nmbOfIntervals; ++i) {
			// max is at the right boundary of each interval - test ReductionMode.MAXIMUM_VALUE
			double result = maxValues.get(i).getValue().getDoubleValue();
			double max = rndValues.get(intervalSize + i * intervalSize - 1).getDoubleValue();
			assertTrue("Expected maximum value " + max + " but got " + result, Double.compare(max, result) == 0);

			// min is at the left boundary of each interval - test ReductionMode.MINIMUM_VALUE
			result = minValues.get(i).getValue().getDoubleValue();
			double min = rndValues.get(i * intervalSize).getDoubleValue();
			assertTrue("Expected minimum value " + min + " but got " + result, Double.compare(min, result) == 0);

			// first is min, second is max - test ReductionMode.MIN_MAX_VALUE
			result = minMaxValues.get(i * 2).getValue().getDoubleValue(); // min ...
			assertTrue("Expected minimum value " + min + " but got " + result, Double.compare(min, result) == 0);

			result = minMaxValues.get(i * 2 + 1).getValue().getDoubleValue(); // max ...
			assertTrue("Expected maximum value " + max + " but got " + result, Double.compare(max, result) == 0);
		}

		// now sort in descending order (put max at left and min at right boundary) 
		Collections.sort(rndValues, new Comparator<DoubleValue>() {
			@Override
			public int compare(DoubleValue o1, DoubleValue o2) {
				Double v1 = o1.getDoubleValue();
				Double v2 = o2.getDoubleValue();
				return v2.compareTo(v1);
			}
		});

		rds = createRecordedDataStorage();
		for (int i = 0; i < nmbOfIntervals; ++i) {
			for (int j = 0; j < intervalSize; ++j) {
				rds.insertValue(new SampledValue(rndValues.get(j + (i * intervalSize)), (j + 1) + (i * intervalSize),
						Quality.GOOD));
			}
		}

		maxValues = rds.getValues(1, endTime, intervalSize, ReductionMode.MAXIMUM_VALUE);
		minValues = rds.getValues(1, endTime, intervalSize, ReductionMode.MINIMUM_VALUE);
		minMaxValues = rds.getValues(1, endTime, intervalSize, ReductionMode.MIN_MAX_VALUE);
		for (int i = 0; i < nmbOfIntervals; ++i) {
			// max is at the left boundary of each interval - test ReductionMode.MAXIMUM_VALUE
			double result = maxValues.get(i).getValue().getDoubleValue();
			double max = rndValues.get(i * intervalSize).getDoubleValue();
			assertTrue("Expected maximum value " + max + " but got " + result, Double.compare(max, result) == 0);

			// min is at the right boundary of each interval - test ReductionMode.MINIMUM_VALUE
			result = minValues.get(i).getValue().getDoubleValue();
			double min = rndValues.get(intervalSize + i * intervalSize - 1).getDoubleValue();
			assertTrue("Expected minimum value " + min + " but got " + result, Double.compare(min, result) == 0);

			// first is min, second is max - test ReductionMode.MIN_MAX_VALUE
			result = minMaxValues.get(i * 2).getValue().getDoubleValue(); // min ...
			assertTrue("Expected minimum value " + min + " but got " + result, Double.compare(min, result) == 0);

			result = minMaxValues.get(i * 2 + 1).getValue().getDoubleValue(); // max ...
			assertTrue("Expected maximum value " + max + " but got " + result, Double.compare(max, result) == 0);
		}
	}
	
	@Test
	public void loggingStopWorks() {
		final FloatResource f = getApplicationManager().getResourceManagement().createResource(newResourceName()+ (int) (Math.random()*1000), FloatResource.class);
		final RecordedData rd = f.getHistoricalData();
		Assert.assertEquals(0,rd.size());
		rd.setConfiguration(null);
		Assert.assertEquals(0,rd.size());
		final RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(Long.MAX_VALUE);
		conf.setStorageType(RecordedDataConfiguration.StorageType.ON_VALUE_UPDATE);
		rd.setConfiguration(conf);
		f.activate(false);
		f.setValue(23);
		f.setValue(24);
		f.setValue(25);
		Assert.assertTrue(rd.size()>0);
		rd.setConfiguration(null);
		rd.size();
		deleteRecordedDataStorage(f.getPath());
		f.delete();
	}

	/**
	 * @see #initReductionModeTest(int, List, Random, int, int, boolean,
	 * Quality, List)
	 */
	private RecordedDataStorage initReductionModeTestWithQualityGood(final int max, List<SummaryStatistics> ss,
			Random rnd, int nmbOfValues, int nmbOfIntervals) throws DataRecorderException {
		return initReductionModeTest(max, ss, rnd, nmbOfValues, nmbOfIntervals, false, Quality.GOOD);
	}

	/**
	 * @see #initReductionModeTest(int, List, Random, int, int, boolean,
	 * Quality, List)
	 */
	private RecordedDataStorage initReductionModeTestWithQualityBad(final int max, List<SummaryStatistics> ss,
			Random rnd, int nmbOfValues, int nmbOfIntervals) throws DataRecorderException {
		return initReductionModeTest(max, ss, rnd, nmbOfValues, nmbOfIntervals, false, Quality.BAD);
	}

	/**
	 * @see #initReductionModeTest(int, List, Random, int, int, boolean,
	 * Quality, List)
	 */
	private RecordedDataStorage initReductionModeTest(final int max, List<SummaryStatistics> ss, Random rnd,
			int nmbOfValues, int nmbOfIntervals, boolean randomQuality, Quality defQuality)
			throws DataRecorderException {
		return initReductionModeTest(max, ss, rnd, nmbOfValues, nmbOfIntervals, randomQuality, defQuality,
				new ArrayList<Quality>());
	}

	/**
	 * Initializing a reduction mode test with values between -max and max. If
	 * randomQuality is <code>true</code> the given defQuality will be ignored.
	 * Otherwise only the defQuality will be used. The time stamp of each value
	 * that were added is defined by when it was added. The first entry has time
	 * stamp 1, the second 2 and so on. So the used time stamp is in the
	 * interval [1, nmbOfValues].
	 *
	 * @param max - max value that will be added [-max, max]
	 * @param ss - list of summary statistics that are used to test if the max,
	 * min or average is correct. For each interval we will need one
	 * {@link SummaryStatistics} object.
	 * @param rnd - {@link Random} object with predefined seed that will be used
	 * to generate the values ( with the given seed it is possible to reproduce
	 * failed tests).
	 * @param nmbOfValues - the number of values to generate / add to
	 * {@link RecordedDataStorage}.
	 * @param nmbOfIntervals - number of intervals
	 * @param randomQuality - if <code>true</code> the quality chosen will be
	 * random, else the defQuality will be used.
	 * @param defQuality - only used if randomQuality is <code>false</code>
	 * @param expectedQuality - this list will be filled up with the quality of
	 * the interval i (at position i in this list) that should be returned by
	 * the {@link RecordedDataStorage}. May not be <code>null</code>.
	 * @return The {@link RecordedDataStorage} that was created and where the
	 * values were added.
	 * @throws DataRecorderException
	 */
	private RecordedDataStorage initReductionModeTest(final int max, List<SummaryStatistics> ss, Random rnd,
            int nmbOfValues, int nmbOfIntervals, boolean randomQuality,
            Quality defQuality, List<Quality> expectedQuality) throws DataRecorderException {
        assertTrue("SummaryStatistics list size must be equals to nmbOfIntervals!", ss.size() == nmbOfIntervals);

        RecordedDataStorage rds = createRecordedDataStorage();
        assertTrue(rds.getValues(0).isEmpty());

        List<DoubleValue> randomValues = generateRandomValues(-max, max, nmbOfValues, rnd);
        int intervalSize = randomValues.size() / nmbOfIntervals;

        Quality q = defQuality;
        for (int i = 0; i < nmbOfIntervals; ++i) {
            // while reducing data bad quality entries will be ignored ... they will be considered
            // if we have only bad quality entries ... so add bad quality values only to summary
            // statistic objects if all added values have Quality.BAD
            boolean addedEntryWithQualityBad = false;
            List<SampledValue> list = new ArrayList<>();
            for (int j = 0; j < intervalSize; ++j) {
                if (randomQuality) {
                    q = rnd.nextFloat() > 0.49 ? Quality.GOOD : Quality.BAD;
                }

                SampledValue value = new SampledValue(randomValues.get(j + (i * intervalSize)),
                        (j + 1) + (i * intervalSize), q);
                list.add(value);

                rds.insertValue(value);

                if (q != Quality.BAD) {
                    ss.get(i).addValue(randomValues.get(j + (i * intervalSize)).getDoubleValue());
                } else {
                    addedEntryWithQualityBad = true;
                }
            }

            if (addedEntryWithQualityBad) {
                boolean allEntriesHaveQualityBad = true;
                for (SampledValue val : list) {
                    if (val.getQuality() != Quality.BAD) {
                        allEntriesHaveQualityBad = false;
                        break;
                    }
                }

                if (allEntriesHaveQualityBad) {
                    ss.get(i).clear();
                    for (SampledValue val : list) {
                        ss.get(i).addValue(val.getValue().getDoubleValue());
                    }

                    expectedQuality.add(Quality.BAD);
                } else {
                    expectedQuality.add(Quality.GOOD);
                }
            } else {
                expectedQuality.add(Quality.GOOD);
            }
        }

        int rest = randomValues.size() % nmbOfIntervals;
        if (rest != 0) {
            // add the rest to the list
            for (int i = nmbOfIntervals * intervalSize; i < randomValues.size(); ++i) {
                rds.insertValue(new SampledValue(randomValues.get(i), i + 1, q));
            }
        }

        assertEquals(nmbOfValues, rds.getValues(1).size());

        return rds;
    }

	private RecordedDataStorage createRecordedDataStorage() throws DataRecorderException {
		String id = newResourceName();
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setStorageType(RecordedDataConfiguration.StorageType.ON_VALUE_UPDATE);
		conf.setFixedInterval(-1);
		rda.createRecordedDataStorage(id, conf);

		RecordedDataStorage rds = rda.getRecordedDataStorage(id);
		return rds;
	}
	
	private void deleteRecordedDataStorage(String id) {
		rda.deleteRecordedDataStorage(id);
	}

	private List<DoubleValue> generateRandomValues(int min, int max, int n, Random rnd) {
        List<DoubleValue> result = new ArrayList<>();
        for (int i = 0; i < n; ++i) {
            result.add(new DoubleValue(rnd.nextDouble() * (max - min) + min));
        }
        return result;
    }

	/**
	 * We use our own comparison for double values here because
	 * Double.compare(...) won't use an epsilon due to rounding errors.
	 *
	 * @return <code>true</code> if a and b are nearly equal, <code>false</code>
	 * otherwise.
	 */
	private boolean nearlyEqual(double a, double b, double epsilon) {
		if (Double.compare(a, b) == 0) {
			return true;
		}
		double diff = Math.abs(a - b);
		if (a == 0 || b == 0 || diff < Double.MIN_NORMAL) {
			// a or b is zero or both are extremely close to it
			// relative error is less meaningful here
			return diff < (epsilon * Double.MIN_NORMAL);
		}
		else { // use relative error
			return diff / (Math.abs(a) + Math.abs(b)) < epsilon;
		}
	}
}
