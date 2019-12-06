package org.ogema.tools.scheduleimporter.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.exam.latest.LatestVersionsTestBase;
import org.ogema.tools.scheduleimporter.config.ScheduleImportConfig;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class ImporterTest extends LatestVersionsTestBase {

	private Path tempFolder;
	private FloatResource parent;
	private static final AtomicInteger FILE_CNT = new AtomicInteger(0);
	
	public ImporterTest() {
		super(true);
	}
	
	@Before
	public void cleanTempDir() throws IOException {
		this.tempFolder = ctx.getDataFile("temp").toPath();
		Files.createDirectories(tempFolder);
		Files.walkFileTree(tempFolder, new SimpleFileVisitor<Path>() {
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
			
		});
		parent = getApplicationManager().getResourceManagement().createResource(newResourceName(), FloatResource.class);
		parent.activate(false);
	}
	
	@After
	public void removeResource() {
		parent.delete();
		parent = null;
	}
	
	@Override
	public Option[] frameworkBundles() {
		return new Option[] {
			CoreOptions.composite(super.frameworkBundles()),
			CoreOptions.mavenBundle("org.ogema.tools", "timeseries-import", ogemaVersion),
			CoreOptions.mavenBundle("org.apache.commons", "commons-csv", "1.5"),
			CoreOptions.systemProperty("org.ogema.tools.scheduleimporter.ErrorDelay").value("1000"),
			CoreOptions.mavenBundle("org.ops4j.pax.tinybundles", "tinybundles", "3.0.0"),
			CoreOptions.mavenBundle("biz.aQute.bnd", "biz.aQute.bndlib", "3.5.0")
		};
	}
	
	private Path newTempFile() {
		return tempFolder.resolve("tmp_" + FILE_CNT.getAndIncrement());
	}
	
	private static void assertValuesContained(final Schedule schedule, final List<SampledValue> values) {
		for (SampledValue sv : values) {
			final SampledValue actual = schedule.getNextValue(sv.getTimestamp());
			Assert.assertEquals("Unexpected timestamp; expected " + sv.getTimestamp() + ", got " 
					+ actual.getTimestamp(), sv.getTimestamp(), actual.getTimestamp());
			Assert.assertEquals("Unexpected value; expected " + sv + ", got " + actual,
					sv.getValue().getFloatValue(), actual.getValue().getFloatValue(), 0.01);
		}
	}
	
	private static void assertValuesContained(final List<Schedule> schedules, final List<List<SampledValue>> values) {
		for (List<SampledValue> row  : values) {
			final Iterator<Schedule> sIt = schedules.iterator();
			final Iterator<SampledValue> vIt = row.iterator();
			while (vIt.hasNext()) {
				Assert.assertTrue("Nr of schedules does not match nr of points", sIt.hasNext());
				final SampledValue sv = vIt.next();
				final Schedule schedule = sIt.next();
				if (sv == null)
					continue;
				final SampledValue actual = schedule.getNextValue(sv.getTimestamp());
				Assert.assertEquals("Unexpected timestamp; expected " + sv.getTimestamp() + ", got " 
						+ actual.getTimestamp(), sv.getTimestamp(), actual.getTimestamp());
				Assert.assertEquals("Unexpected value; expected " + sv + ", got " + actual,
						sv.getValue().getFloatValue(), actual.getValue().getFloatValue(), 0.01);
			}
		}
	}
	
	private Path writeValuesSingle(final List<SampledValue> values) throws IOException {
		final List<List<SampledValue>> rows = new ArrayList<List<SampledValue>>(values.size());
		for (SampledValue sv : values) {
			rows.add(Collections.singletonList(sv));
		}
		return writeValues(rows);
	}
	
	private Path writeValues(final List<List<SampledValue>> rows) throws IOException {
		final Path tmpFile = newTempFile();
		try (final BufferedWriter writer = Files.newBufferedWriter(tmpFile, StandardCharsets.UTF_8)) {
			for (List<SampledValue> values: rows) {
				boolean empty = true;
				for (SampledValue value : values) {
					if (value != null) {
						writer.write(value.getTimestamp() + "");
						empty = false;
						break;
					}
				}
				if (empty)
					continue;
				for (SampledValue value : values) {
					writer.write(';');
					if (value != null)
						writer.write(value.getValue().getFloatValue() + "");
				}
				writer.write('\n');
			}
		}
		return tmpFile;
	}
	
	private ScheduleImportConfig addConfig(final List<SampledValue> values, final String url) throws IOException, InterruptedException {
		final AbsoluteSchedule target = parent.addDecorator(newResourceName(), AbsoluteSchedule.class);
		target.activate(false);
		final ScheduleImportConfig cfg = waitForImportConfig(target, url);
		assertValuesContained(target, values);
		return cfg;
	}
	
	private ScheduleImportConfig addConfig(final List<SampledValue> values) throws IOException, InterruptedException {
		final Path tmpFile = writeValuesSingle(values);
		final AbsoluteSchedule target = parent.addDecorator(newResourceName(), AbsoluteSchedule.class);
		target.activate(false);
		final ScheduleImportConfig cfg = waitForImportConfig(target, "file:" + tmpFile.toString());
		assertValuesContained(target, values);
		return cfg;
	}
	
	private ScheduleImportConfig addConfigs(final List<List<SampledValue>> values, final List<SingleValueResource> parents) throws IOException, InterruptedException {
		final Path tmpFile = writeValues(values);
		final List<Schedule> schedules = new ArrayList<Schedule>(parents.size());
		for (SingleValueResource p: parents) {
			final Schedule s =p.addDecorator(newResourceName(), AbsoluteSchedule.class);
			s.activate(false);
			schedules.add(s);
		}
		final ScheduleImportConfig cfg = waitForImportConfig(schedules, "file:" + tmpFile.toString());
		assertValuesContained(schedules, values);
		return cfg;
	}

	private ScheduleImportConfig addConfigWithoutSchedule(final List<SampledValue> values) throws IOException, InterruptedException {
		final Path tmpFile = writeValuesSingle(values);
		final String relativePath = newResourceName();
		final ScheduleImportConfig cfg = createImportConfig(parent, relativePath, "file:" + tmpFile.toString());
		return cfg;
	}
	
	private ScheduleImportConfig waitForImportConfig(final Schedule target, final String url) throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		final ResourceValueListener<Schedule> listener = new ResourceValueListener<Schedule>() {

			@Override
			public void resourceChanged(Schedule resource) {
				latch.countDown();
			}
		};
		target.addValueListener(listener);
		final ScheduleImportConfig config = createImportConfig(target, url);
		Assert.assertTrue("Missing value changed callback for " + target, latch.await(5, TimeUnit.SECONDS));
		target.removeValueListener(listener);
		return config;
	}
	
	private ScheduleImportConfig waitForImportConfig(final List<Schedule> targets, final String url) throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(targets.size());
		final ResourceValueListener<Schedule> listener = new ResourceValueListener<Schedule>() {

			@Override
			public void resourceChanged(Schedule resource) {
				latch.countDown();
			}
		};
		for (Schedule target: targets) {
			target.addValueListener(listener);
		}
		final ScheduleImportConfig config = createImportConfig(targets, url);
		Assert.assertTrue("Missing value changed callback for " + targets, latch.await(5, TimeUnit.SECONDS));
		for (Schedule target: targets) {
			target.removeValueListener(listener);
		}
		return config;
	}
	
	private ScheduleImportConfig createImportConfig(final Schedule target, final String url) {
		return createImportConfig((SingleValueResource) target.getParent(), target.getName(), url);
	}
	
	private ScheduleImportConfig createImportConfig(final SingleValueResource parent, final String relativePath, final String url) {
		final ScheduleImportConfig config = getApplicationManager().getResourceManagement().createResource(newResourceName(), ScheduleImportConfig.class);
		config.targetParent().setAsReference(parent);
		config.scheduleRelativePath().<StringResource> create().setValue(relativePath);
		config.csvFile().<StringResource> create().setValue(url);
		config.activate(true);
		return config;
	}
	
	private ScheduleImportConfig createImportConfig(final List<Schedule> schedules, final String url) {
		final ScheduleImportConfig config = getApplicationManager().getResourceManagement().createResource(newResourceName(), ScheduleImportConfig.class);
		config.targetParents().create();
		final String[] relativePaths = new String[schedules.size()];
		int idx = 0;
		for (Schedule schedule : schedules) {
			final SingleValueResource p = schedule.getParent();
			final String name = schedule.getName();
			config.targetParents().addDecorator(newResourceName(), p);
			relativePaths[idx++] = name;
		}
		config.scheduleRelativePaths().<StringArrayResource> create().setValues(relativePaths);
		config.csvFile().<StringResource> create().setValue(url);
		config.activate(true);
		return config;
	}

	@Test
	public void singleValueWorks() throws IOException, InterruptedException {
		final List<SampledValue> values = Arrays.asList(new SampledValue(new FloatValue(3.4F), 2, Quality.GOOD));
		final ScheduleImportConfig cfg = addConfig(values);
		cfg.delete();
	}

	@Test
	public void multipleValuesWork() throws IOException, InterruptedException {
		final List<SampledValue> values = Arrays.asList(
				new SampledValue(new FloatValue(3.4F), 2, Quality.GOOD),
				new SampledValue(new FloatValue(-123F), 2342, Quality.GOOD),
				new SampledValue(new FloatValue(19), 2344524, Quality.GOOD)
		);
		final ScheduleImportConfig cfg = addConfig(values);
		cfg.delete();
	}
	
	@Test
	public void retryWorks() throws IOException, InterruptedException {
		final List<SampledValue> values = Arrays.asList(new SampledValue(new FloatValue(3.4F), 2, Quality.GOOD));
		final ScheduleImportConfig cfg = addConfigWithoutSchedule(values);
		Thread.sleep(1000);
		final AbsoluteSchedule schedule = parent.addDecorator(cfg.scheduleRelativePath().getValue(), AbsoluteSchedule.class);
		schedule.activate(false);
		for (int i=0; i < 100; i++) { // wait for up to 5s
			if (schedule.size() > 0)
				break;
			Thread.sleep(50);
		}
		Assert.assertEquals("Unexpected schedule size", values.size(), schedule.size()); 
		cfg.delete();
	}
	
	@Test
	public void multipleTimeseriesWork() throws IOException, InterruptedException {
		final long[] ts = {7, 15, 23};
		final List<List<SampledValue>> rows = new ArrayList<List<SampledValue>>(ts.length);
		for (long t : ts) {
			rows.add(Arrays.asList(
					new SampledValue(new FloatValue(t + (float) (10 * Math.random())), t, Quality.GOOD),
					new SampledValue(new FloatValue(t + (float) (10 * Math.random())), t, Quality.GOOD),
					new SampledValue(new FloatValue(t + (float) (10 * Math.random())), t, Quality.GOOD),
					new SampledValue(new FloatValue(t + (float) (10 * Math.random())), t, Quality.GOOD)
			));
		}
		final List<SingleValueResource> parents = new ArrayList<SingleValueResource>();
		for (int i=0; i<rows.get(0).size(); i++) {
			final FloatResource p = getApplicationManager().getResourceManagement().createResource(newResourceName(), FloatResource.class);
			p.activate(false);
			parents.add(p);
		}
		final ScheduleImportConfig cfg = addConfigs(rows, parents);
		cfg.delete();
		for (SingleValueResource p : parents) {
			p.delete();
		}
	}
	
	@Test
	public void multipleTimeseriesWorkWithGaps() throws IOException, InterruptedException {
		final long[] ts = {7, 15, 23};
		final List<List<SampledValue>> rows = new ArrayList<List<SampledValue>>(ts.length);
		final long t0 = ts[0];
		rows.add(Arrays.asList(
				new SampledValue(new FloatValue(t0 + (float) (10 * Math.random())), t0, Quality.GOOD),
				new SampledValue(new FloatValue(t0 + (float) (10 * Math.random())), t0, Quality.GOOD),
				new SampledValue(new FloatValue(t0 + (float) (10 * Math.random())), t0, Quality.GOOD),
				new SampledValue(new FloatValue(t0 + (float) (10 * Math.random())), t0, Quality.GOOD)
		));
		final long t1 = ts[1];
		rows.add(Arrays.asList(
				new SampledValue(new FloatValue(t1 + (float) (10 * Math.random())), t1, Quality.GOOD),
				null,
				new SampledValue(new FloatValue(t1 + (float) (10 * Math.random())), t1, Quality.GOOD),
				null
		));
		final long t2 = ts[2];
		rows.add(Arrays.asList(
				null,
				null,
				new SampledValue(new FloatValue(t2 + (float) (10 * Math.random())), t2, Quality.GOOD),
				new SampledValue(new FloatValue(t2 + (float) (10 * Math.random())), t2, Quality.GOOD)
		));
		final List<SingleValueResource> parents = new ArrayList<SingleValueResource>();
		for (int i=0; i<rows.get(0).size(); i++) {
			final FloatResource p = getApplicationManager().getResourceManagement().createResource(newResourceName(), FloatResource.class);
			p.activate(false);
			parents.add(p);
		}
		final ScheduleImportConfig cfg = addConfigs(rows, parents);
		cfg.delete();
		for (SingleValueResource p : parents) {
			p.delete();
		}
	}
	
}
