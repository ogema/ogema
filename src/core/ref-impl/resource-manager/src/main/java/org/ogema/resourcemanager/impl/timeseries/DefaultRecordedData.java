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
package org.ogema.resourcemanager.impl.timeseries;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.recordeddata.DataRecorder;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;
import org.ogema.resourcemanager.impl.ResourceDBManager;
import org.ogema.resourcetree.TreeElement;
import org.ogema.timer.TimerScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jan Lapp, Fraunhofer IWES
 */
public class DefaultRecordedData implements RecordedData {

	final static boolean SECURITY_ENABLED = System.getSecurityManager() != null;

	protected final String id;
	protected final DataRecorder dataAccess;
	protected final TimerScheduler scheduler;
	protected final Executor exec;
	protected final TreeElement el;
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected RecordedDataStorage data = new EmptyRecordedData();
	protected RecordedDataConfiguration config = null;
	protected Updater<?> updater;
	private Timer timer;

	public DefaultRecordedData(DataRecorder dataAccess, TimerScheduler scheduler, Executor exec, TreeElement el) {
		Objects.requireNonNull(dataAccess);
		Objects.requireNonNull(el);
		Objects.requireNonNull(scheduler);
		Objects.requireNonNull(exec);

		this.dataAccess = dataAccess;
		this.scheduler = scheduler;
		this.exec = exec;
		this.el = el;

		this.id = createId(el);

		RecordedDataStorage tsData = dataAccess.getRecordedDataStorage(id);
		if (tsData != null) {
			data = tsData;
			config = tsData.getConfiguration();
			createUpdater();
			setConfiguration(config);
		}
	}

	public static String createId(TreeElement el) {
		List<String> elements = new ArrayList<>();
		for (TreeElement p = el; p != null; p = p.getParent()) {
			elements.add(p.getName());
		}
		StringBuilder sb = new StringBuilder(elements.get(elements.size() - 1));
		for (int i = elements.size() - 2; i > -1; i--) {
			sb.append("/").append(elements.get(i));
		}
		return sb.toString();
	}

	public static TreeElement findTreeElement(String id, ResourceDBManager dbman) {
		String[] path = id.split("/");
		if (path.length == 0) {
			return null;
		}
		TreeElement el = dbman.getToplevelResource(path[0]);
		for (int i = 1; i < path.length && el != null; i++) {
			el = el.getChild(path[i]);
		}
		return el;
	}

	public void update(long time) {
		if (updater != null) {
			if (System.getSecurityManager() == null) {
				updater.elementUpdated(time);
			}
			else {
				updatePrivileged(time);
			}
		}
	}

	private void updatePrivileged(final long time) {
		AccessController.doPrivileged(new PrivilegedAction<Void>() {

			@Override
			public Void run() {
				updater.elementUpdated(time);
				return null;
			}

		});
	}

	public void logIntervalElapsed(long time) {
		if (updater != null && el.isActive()) {
			updater.writeFixedIntervalData(time);
		}
	}

	public void close() {
		if (timer != null) {
			timer.destroy();
		}
	}

	final void createUpdater() {
		if (config == null) {
			return;
		}
		if (FloatResource.class.isAssignableFrom(el.getType())) {
			updater = new FloatUpdater(config.getStorageType());
		}
		else if (IntegerResource.class.isAssignableFrom(el.getType())) {
			updater = new IntegerUpdater(config.getStorageType());
		}
		else if (BooleanResource.class.isAssignableFrom(el.getType())) {
			updater = new BooleanUpdater(config.getStorageType());
		}
		else if (TimeResource.class.isAssignableFrom(el.getType())) {
			updater = new LongUpdater(config.getStorageType());
		}
		else {
			throw new UnsupportedOperationException("unsupported recorded data type: " + el.getType());
		}
	}

	@Override
	public synchronized final void setConfiguration(final RecordedDataConfiguration configuration) {
		AccessController.doPrivileged(new PrivilegedAction<Void>() {

			@Override
			public Void run() {
				setConfigurationPrivileged(configuration);
				return null;
			}
		});
	}

	private synchronized final void setConfigurationPrivileged(RecordedDataConfiguration configuration) {
		if (configuration != null) {
			setNewConfig(configuration);
		}
		else {
			//TODO: test!
			disable();
		}
	}

	private void setNewConfig(RecordedDataConfiguration configuration) {
		try {
			if (config == null) {
				data = dataAccess.getRecordedDataStorage(id);
				if (data == null) {
					data = dataAccess.createRecordedDataStorage(id, configuration);
				}
			}
			data.setConfiguration(configuration);
			config = data.getConfiguration();
			createUpdater();
			setupTimer();
			if (configuration.getStorageType() == StorageType.ON_VALUE_CHANGED) {
				logger.debug("new RecordedData configuration with ON_VALUE_CHANGED set, writing current value");
				Timer t = scheduler.createTimer(exec, logger);
				data.insertValue(updater.createValue(t.getExecutionTime()));
				t.destroy();
			}
		} catch (DataRecorderException rdae) {
			logger.warn("could not set new configuration", rdae);
		}
	}

	private void disable() {
		data = dataAccess.getRecordedDataStorage(id);
		if (data != null) {
			data.setConfiguration(null);
		}
		updater = null;
		config = null;
		if (timer != null) {
			timer.destroy();
			timer = null;
		}
	}

	private synchronized void setupTimer() {
		if (config.getStorageType() == StorageType.FIXED_INTERVAL) {
			if (timer == null) {
				timer = scheduler.createTimer(exec, logger);
				timer.addListener(new TimerListener() {

					@Override
					public void timerElapsed(Timer timer) {
						logIntervalElapsed(timer.getExecutionTime());
					}
				});
			}
			timer.setTimingInterval(config.getFixedInterval());
			logger.debug("RecordedData {} configured for update rate of {}ms", id, config.getFixedInterval());
		}
		else {
			if (timer != null) {
				timer.destroy();
				timer = null;
			}
		}
	}

	@Override
	public RecordedDataConfiguration getConfiguration() {
		return config;
	}

	@Override
	public List<SampledValue> getValues(long startTime) {
		return data.getValues(startTime, Long.MAX_VALUE);
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		return SECURITY_ENABLED ? getValuesPrivileged(startTime, endTime) : data.getValues(startTime, endTime);
	}

	private List<SampledValue> getValuesPrivileged(final long startTime, final long endTime) {
		return AccessController.doPrivileged(new PrivilegedAction<List<SampledValue>>() {

			@Override
			public List<SampledValue> run() {
				return data.getValues(startTime, endTime);
			}
		});
	}

	@Override
	public SampledValue getValue(long timestamp) {
		return data.getValue(timestamp);
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime, long interval, ReductionMode mode) {
		return data.getValues(startTime, endTime, interval, mode);
	}

	@Override
	public SampledValue getNextValue(long time) {
		return data.getNextValue(time);
	}

	@Override
	public InterpolationMode getInterpolationMode() {
		return data.getInterpolationMode();
	}

	@Override
	@Deprecated
	public Long getTimeOfLatestEntry() {
		return data.getTimeOfLatestEntry();
	}

	protected abstract class Updater<T> {

		final RecordedDataConfiguration.StorageType mode;
		T lastValue;

		public Updater(RecordedDataConfiguration.StorageType mode) {
			this.mode = mode;
		}

		void elementUpdated(long time) {
			try {
				switch (mode) {
				case FIXED_INTERVAL:
					break;
				case ON_VALUE_CHANGED:
					if (valueChanged()) {
						data.insertValue(createValue(time));
					}
					break;
				case ON_VALUE_UPDATE:
					data.insertValue(createValue(time));
					break;
				default:
					throw new UnsupportedOperationException();
				}
			} catch (DataRecorderException rdae) {
				logger.error("failed to write recorded data", rdae);
			}
		}

		void writeFixedIntervalData(long time) {
			try {
				data.insertValue(createValue(time));
			} catch (DataRecorderException rdae) {
				logger.error("failed to write recorded data", rdae);
			}
		}

		abstract boolean valueChanged();

		abstract SampledValue createValue(long time);
	}

	protected class FloatUpdater extends Updater<Float> {

		public FloatUpdater(StorageType mode) {
			super(mode);
		}

		@Override
		SampledValue createValue(long time) {
			return new SampledValue(new FloatValue(el.getData().getFloat()), time, Quality.GOOD);
		}

		@Override
		boolean valueChanged() {
			Float newVal = el.getData().getFloat();
			boolean changed = lastValue == null || !lastValue.equals(newVal);
			lastValue = newVal;
			return changed;
		}
	}

	protected class IntegerUpdater extends Updater<Integer> {

		public IntegerUpdater(StorageType mode) {
			super(mode);
		}

		@Override
		SampledValue createValue(long time) {
			return new SampledValue(new IntegerValue(el.getData().getInt()), time, Quality.GOOD);
		}

		@Override
		boolean valueChanged() {
			Integer newVal = el.getData().getInt();
			boolean changed = lastValue == null || !lastValue.equals(newVal);
			lastValue = newVal;
			return changed;
		}
	}

	protected class BooleanUpdater extends Updater<Boolean> {

		public BooleanUpdater(StorageType mode) {
			super(mode);
		}

		@Override
		SampledValue createValue(long time) {
			return new SampledValue(new BooleanValue(el.getData().getBoolean()), time, Quality.GOOD);
		}

		@Override
		boolean valueChanged() {
			Boolean newVal = el.getData().getBoolean();
			boolean changed = lastValue == null || !lastValue.equals(newVal);
			lastValue = newVal;
			return changed;
		}
	}

	protected class LongUpdater extends Updater<Long> {

		public LongUpdater(StorageType mode) {
			super(mode);
		}

		@Override
		SampledValue createValue(long time) {
			return new SampledValue(new LongValue(el.getData().getLong()), time, Quality.GOOD);
		}

		@Override
		boolean valueChanged() {
			Long newVal = el.getData().getLong();
			boolean changed = lastValue == null || !lastValue.equals(newVal);
			lastValue = newVal;
			return changed;
		}
	}

}
