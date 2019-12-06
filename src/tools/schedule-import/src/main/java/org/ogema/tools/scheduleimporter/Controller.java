/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package org.ogema.tools.scheduleimporter;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseriesimport.api.ImportConfiguration;
import org.ogema.tools.timeseriesimport.api.ImportConfigurationBuilder;
import org.ogema.tools.timeseriesimport.api.TimeseriesImport;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentServiceObjects;

class Controller implements TimerListener, Closeable {

	private static final long DEFAULT_DELAY = 60000; // 1 minute
	private static final long DELAY;
	
	static {
		long delay = DEFAULT_DELAY;
		try { // configurable mainly for tests
			delay = Long.getLong("org.ogema.tools.scheduleimporter.ErrorDelay", delay);
			if (delay <= 0)
				delay = DEFAULT_DELAY;
		} catch (SecurityException e) {}
		DELAY = delay;
	}
	private final ApplicationManager appMan;
	private final ConfigPattern config;
	private Timer timer;
	private final BundleContext ctx;
	private final ComponentServiceObjects<TimeseriesImport> csvImporter;
	private int errorCnt = 0;

	Controller(ConfigPattern config, ApplicationManager appMan, BundleContext ctx, ComponentServiceObjects<TimeseriesImport> csvImporter) {
		this.config = config;
		this.csvImporter = csvImporter;
		this.ctx = ctx;
		this.appMan = appMan;
		tryImport();
	}

	private Timer getTimer(long step, boolean error) {
		if (!error)
			errorCnt = 0;
		else {
			errorCnt++;
			step = Math.min(errorCnt, 100) * step;
		}
		Timer timer = this.timer;
		if (timer == null) {
			timer = appMan.createTimer(step, this);
			this.timer = timer;
		}
		else {
			timer.setTimingInterval(step);
			timer.resume();
		}
		return timer;
	}
	
	@Override
	public void timerElapsed(Timer timer) {
		tryImport();
	}
	
	@Override
	public void close() {
		if (timer != null)
			timer.destroy();
	}
	
	private final void tryImport() {
		if (timer != null)
			timer.stop();
		final List<Resource> resources = config.getSchedules();
		final List<Schedule> schedules = new ArrayList<>(resources.size());
		for (Resource r : resources) {
			if (r == null) {
				if (errorCnt == 0)
					appMan.getLogger().info("Target schedule for parent {} not yet available, waiting for it.",  r);
				getTimer(DELAY, true);
				return;
			}
			else if (!(r instanceof Schedule)) {
				appMan.getLogger().error("Invalid target resource {}", r);
			} else {
				schedules.add((Schedule) r);
			}
		}
		final URL url = getFile(config);
		if (url == null) {
			if (errorCnt == 0)
				appMan.getLogger().error("Could not find specified resource {}", config.csvFile.getValue());
			getTimer(DELAY, true);
			return;
		}
		final ImportConfigurationBuilder importConfigBuilder = ImportConfigurationBuilder.newInstance()
				.setParseEagerly(true);
		config.configure(importConfigBuilder, appMan.getFrameworkTime());
		final ImportConfiguration importConfig = importConfigBuilder.build();
		final List<ReadOnlyTimeSeries> result;
		long nextSubmitTime = getNextSubmitTime(schedules);
		final long now = appMan.getFrameworkTime();
		if (nextSubmitTime > now) {
			getTimer(nextSubmitTime - now, false);
			return;
		}
		final ComponentServiceObjects<TimeseriesImport> service = this.csvImporter;
		final TimeseriesImport imp = service.getService();
		try {
			result = imp.parseMultiple(url, importConfig, schedules.size());
		} catch (IOException e) {
			if (errorCnt == 0)
				appMan.getLogger().error("Failed to import CSV file for config {}", config, e);
			getTimer(DELAY, true);
			return;
		} finally {
			service.ungetService(imp);
		}
		final Iterator<ReadOnlyTimeSeries> it = result.iterator();
		for (Schedule s : schedules) {
			if (!it.hasNext()) {
				appMan.getLogger().error("Number of timeseries imported from file ({}) "
						+ "does not match number of schedules ({}). Config: {}", result.size(), schedules.size(), config.model);
				break;
			}
			final ReadOnlyTimeSeries next = it.next();
			try {
			final long nextT = tryImport(s, next, now);
			if (nextT < nextSubmitTime)
				nextSubmitTime = nextT;
			} catch (Exception e) {
				appMan.getLogger().error("Failed to import schedule from CSV for schedule {}", s, e);
			}
		}
		if (!config.isPeriodic()) {
			close();
			return;
		}
		long itv = nextSubmitTime - now;
		boolean error = itv < DELAY;
		if (error)
			itv = DELAY;
		appMan.getLogger().debug("Next execution in {}s", itv/1000);
		getTimer(itv, error);
	}
	
	/**
	 * @param timeSeries
	 * @return
	 *   	next submit time;
	 *   	{@value Long#MAX_VALUE} means no further action required
	 */
	private long tryImport(final Schedule schedule, final ReadOnlyTimeSeries result, final long now) {
		final SampledValue last = schedule.getPreviousValue(Long.MAX_VALUE);
		if (last != null && !config.isPeriodic()) {
			return Long.MAX_VALUE;
		}
		final SampledValue lastNew = result.getPreviousValue(Long.MAX_VALUE);
		if (lastNew == null)
			return getNextSubmitTime(schedule);
		final long lastT = last == null ? Long.MIN_VALUE : last.getTimestamp();
		long offset = 0;
		long lastNewT = lastNew.getTimestamp();
		final SampledValue previous = result.getPreviousValue(lastNewT - 1);
		final SampledValue first = result.getNextValue(Long.MIN_VALUE);
		// in case the csv file does not have newer values than the schedule 
		// but the import is periodic we import the next batch
		final long diff = previous != null ? lastNewT - previous.getTimestamp() : 0;
		while (diff > 0 && lastNewT <= lastT) {
			if (!config.isPeriodic())
				break;
			offset = lastNewT - first.getTimestamp() + diff; 
			if (config.alignmentType.isActive()) {
				final long newStart = ConfigPattern.getAlignedIntervalStart(first.getTimestamp() + offset, config.alignmentType.getValue());
				offset = newStart - first.getTimestamp();
				if (offset == 0)
					break;
			}
			lastNewT = lastNewT + offset;
		}
		schedule.create();
		if (offset == 0) {
			schedule.addValues(result.getValues(lastT + 1), now);
		}
		else {
			final List<SampledValue> list = result.getValues(lastT + 1 - offset);
			final List<SampledValue> offsetValues = new ArrayList<>(list.size());
			for (SampledValue sv : list)
				offsetValues.add(new SampledValue(sv.getValue(), sv.getTimestamp() + offset, sv.getQuality()));
			schedule.addValues(offsetValues, now);
		}
		schedule.activate(false);
		final int sz = schedule.size(lastT+1, Long.MAX_VALUE);
		if (sz > 0)
			appMan.getLogger().info("Imported {} points into schedule {}", sz, schedule);
		else
			appMan.getLogger().debug("No further points imported into schedule {}", schedule);
		return getNextSubmitTime(schedule);
	}
	
	private long getNextSubmitTime(final List<Schedule> schedules) {
		long next = Long.MAX_VALUE;
		for (ReadOnlyTimeSeries t : schedules) {
			final long n = getNextSubmitTime(t);
			if (n < next)
				next = n;
		}
		return next;
	}
	
	private long getNextSubmitTime(final ReadOnlyTimeSeries schedule) {
		if (schedule.isEmpty())
			return Long.MIN_VALUE;
		if (!config.isPeriodic())
			return Long.MAX_VALUE;
		final SampledValue last = schedule.getPreviousValue(Long.MAX_VALUE);
		return last.getTimestamp() - config.model.importHorizon().getValue();
	}
	
	public boolean isActive() {
		return timer != null && timer.isRunning();
	}
	
	private URL getFile(final ConfigPattern pattern) {
		try {
			return new URL(pattern.csvFile.getValue());
		} catch (MalformedURLException e) {}
		return ctx.getBundle().getEntry(pattern.csvFile.getValue());
	}
	
}
