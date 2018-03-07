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
package org.ogema.persistence.impl.faketree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceNotFoundException;
import org.ogema.core.timeseries.TimeSeries;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;
import org.ogema.resourcetree.TreeElement;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;
import org.ogema.tools.timeseries.implementations.TreeTimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tree element representing a schedule. The schedule data are stored in
 * attached tree elements (primitive types and arrays) that are assigned names
 * not legal for resources (making them invisible to OGEMA applications). This
 * also performs the synchronization between threads using a
 * ReentrantReadWriteLock over the otherwise non-synchronized schedules.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ScheduleTreeElement implements TimeSeries {

	// names of the sub-tree-elements containing the data. May not be legal resource names.
	public static final String OWN_NAME = "+schedule", TIME_NAME = "+t", QUALITY_NAME = "+q", UPDATE_TIME_NAME = "+u",
			CALCULATION_TIME_NAME = "+c", INTERPOLATION_NAME = "+i", VALUE_NAME = "+v";

	// actual tree element and element attached to realElement that contains actual schedule data.
	final VirtualTreeElement baseElement;
    private final VirtualTreeElement scheduleDataElement;

	// actual data in memory, also provides the schedule functionalities.
	private final MemoryTimeSeries m_schedule;
	private final Class<? extends Value> m_valueType;

	// Sub-elements to pseudo-element: Hold the actual data.
	private final TreeElement m_times, m_qualities, m_values;
	private final TreeElement m_updateTime, m_calculationTime, m_interpolationMode;

	// Synchronization
	private final ReadWriteLock m_lock = new ReentrantReadWriteLock();
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleTreeElement.class);

	protected ScheduleTreeElement(VirtualTreeElement element) {
        LOGGER.debug("creating new ScheduleTreeElement for {}", element.getPath());
		baseElement = element;
		final VirtualTreeElement existingPseudoElement = element.getChild(OWN_NAME);
		if (existingPseudoElement != null) {
			scheduleDataElement = existingPseudoElement;
		}
		else {
			// if we used a custom type we'd also need to export the package
//			scheduleDataElement = (VirtualTreeElement) element.addChild(OWN_NAME, PseudoSchedule.class, true);
			scheduleDataElement = (VirtualTreeElement) element.addChild(OWN_NAME, Resource.class, true);
		}

		// Read in or create the sub-treenodes
		m_updateTime = getOrAddChild(UPDATE_TIME_NAME, TimeResource.class, true);
		m_calculationTime = getOrAddChild(CALCULATION_TIME_NAME, TimeResource.class, true);
		m_times = getOrAddChild(TIME_NAME, TimeArrayResource.class, true);
		m_qualities = getOrAddChild(QUALITY_NAME, IntegerArrayResource.class, true);

		Class<?> parentClass = baseElement.getParent().getType();
		if (!SingleValueResource.class.isAssignableFrom(parentClass)) {
			// error: parent is not a simple resource
			throw new ResourceNotFoundException(String.format("Cannot create a schedule on parent of type %s (%s)",
					parentClass.getCanonicalName(), baseElement.getParent().getPath()));
		}

		if (FloatResource.class.isAssignableFrom(parentClass)) {
			m_valueType = FloatValue.class;
			m_values = getOrAddChild(VALUE_NAME, FloatArrayResource.class, true);
		}
		else if (BooleanResource.class.isAssignableFrom(parentClass)) {
			m_valueType = BooleanValue.class;
			m_values = getOrAddChild(VALUE_NAME, BooleanArrayResource.class, true);
		}
		else if (TimeResource.class.isAssignableFrom(parentClass)) {
			m_valueType = LongValue.class;
			m_values = getOrAddChild(VALUE_NAME, TimeArrayResource.class, true);
		}
		else if (IntegerResource.class.isAssignableFrom(parentClass)) {
			m_valueType = IntegerValue.class;
			m_values = getOrAddChild(VALUE_NAME, IntegerArrayResource.class, true);
		}
		else if (StringResource.class.isAssignableFrom(parentClass)) {
			m_valueType = StringValue.class;
			m_values = getOrAddChild(VALUE_NAME, StringArrayResource.class, true);
		}
		else {
			throw new ResourceException("Cannot create a schedule for parent resource type "
					+ parentClass.getCanonicalName() + ": Schedules of this type are not supported.");
		}
		m_schedule = new TreeTimeSeries(m_valueType);

		final TreeElement existingInterpolationMode = scheduleDataElement.getChild(INTERPOLATION_NAME);
		if (existingInterpolationMode == null) {
			m_interpolationMode = scheduleDataElement.addChild(INTERPOLATION_NAME, IntegerResource.class, true);
			m_interpolationMode.getData().setInt(InterpolationMode.NONE.getInterpolationMode());
		}
		else {
			m_interpolationMode = existingInterpolationMode;
		}
        if (!baseElement.isVirtual()) {
            create();
        }
		// load existing data into memory schedule, if applicable.
		load();
	}

	public final void create() {
		((VirtualTreeElement) m_calculationTime).create();
		((VirtualTreeElement) m_interpolationMode).create();
		((VirtualTreeElement) m_qualities).create();
		((VirtualTreeElement) m_times).create();
		((VirtualTreeElement) m_updateTime).create();
		((VirtualTreeElement) m_values).create();
	}

	public Class<? extends Value> getValueType() {
		return m_valueType;
	}

	/**
	 * Adds the respective child tree element if it does not exist. If the
	 * element already exists (because OGEMA is started from the persistent
	 * storage) the existing element is returned, instead. Newly-created arrays
	 * are initialized to empty arrays.
	 */
	private TreeElement getOrAddChild(String name, Class<? extends Resource> type, boolean isDecorating)
			throws ResourceAlreadyExistsException, InvalidResourceTypeException {
		final TreeElement existingElement = scheduleDataElement.getChild(name);
		if (existingElement != null) {
			return existingElement;
		}

		final TreeElement result = scheduleDataElement.addChild(name, type, isDecorating);

		// in case of array pseudoresources to be created, initialize with empty array.
		if (FloatArrayResource.class.isAssignableFrom(type)) {
			result.getData().setFloatArr(new float[0]);
		}
		if (BooleanArrayResource.class.isAssignableFrom(type)) {
			result.getData().setBooleanArr(new boolean[0]);
		}
		if (TimeArrayResource.class.isAssignableFrom(type)) {
			result.getData().setLongArr(new long[0]);
		}
		if (IntegerArrayResource.class.isAssignableFrom(type)) {
			result.getData().setIntArr(new int[0]);
		}
		if (StringArrayResource.class.isAssignableFrom(type)) {
			result.getData().setStringArr(new String[0]);
		}
		return result;
	}

	/**
	 * Loads the data from the underlying tree elements into the memory
	 * schedule. Called at most once, when the data is load from persistence
	 * after re-start of the system.
	 */
	private void load() {
		// note: update time is not stored in the memory schedules, only calculation time.
		final long calculationTime = m_calculationTime.getData().getLong();

		final List<SampledValue> values;
		if (m_valueType == FloatValue.class) {
			values = getFloatValues();
		}
		else if (m_valueType == BooleanValue.class) {
			values = getBooleanValues();
		}
		else if (m_valueType == IntegerValue.class) {
			values = getIntegerValues();
		}
		else if (m_valueType == LongValue.class) {
			values = getLongValues();
		}
		else if (m_valueType == StringValue.class) {
			values = getStringValues();
		}
		else {
			throw new IllegalArgumentException("Cannot create SampledValues for Value type"
					+ m_valueType.getCanonicalName());
		}

		if (!values.isEmpty()) {
			// delete+insert instead of replace to capture exotic case of value at t=MAX_LONG.
			m_schedule.deleteValues();
			m_schedule.addValues(values, calculationTime);
		}

		final InterpolationMode mode = InterpolationMode.getInterpolationMode(m_interpolationMode.getData().getInt());
		m_schedule.setInterpolationMode(mode);
	}

	/**
	 * Writes the content of the memory schedule to persistence.
	 */
	private void write() {
		final List<SampledValue> entries = m_schedule.getValues(0);

		if (m_valueType == FloatValue.class) {
			writeFloatValues(entries);
		}
		else if (m_valueType == BooleanValue.class) {
			writeBooleanValues(entries);
		}
		else if (m_valueType == IntegerValue.class) {
			writeIntegerValues(entries);
		}
		else if (m_valueType == LongValue.class) {
			writeLongValues(entries);
		}
		else if (m_valueType == StringValue.class) {
			writeStringValues(entries);
		}
		else {
			throw new IllegalArgumentException("Cannot create SampledValues for Value type"
					+ m_valueType.getCanonicalName());
		}

		// note: update time is not stored in the memory schedules, only calculation time.
		if (m_schedule.getLastCalculationTime() != null) {
			m_calculationTime.getData().setLong(m_schedule.getLastCalculationTime());
		}

		final int mode = m_schedule.getInterpolationMode().getInterpolationMode();
		m_interpolationMode.getData().setInt(mode);
	}

	/*---------------------------------------------------------------------
	 * Lost of copy&paste load/save routines for the different data types.
	 ---------------------------------------------------------------------*/
	/**
	 * Gets the entries as a list of SampledValues with type FloatValue
	 *
	 * @return
	 */
	private List<SampledValue> getFloatValues() {
		assert baseElement.isVirtual()  || !((VirtualTreeElement) m_values).isVirtual() : "Schedule state inconsistent";
        final long[] times = m_times.getData().getLongArr();
        final float[] values = m_values.getData().getFloatArr();
        final int[] qualities = m_qualities.getData().getIntArr();
        List<SampledValue> result = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; ++i) {
            final float v = values[i];
            final long t = times[i];
            final Quality q = Quality.getQuality(qualities[i]);
            result.add(new SampledValue(new FloatValue(v), t, q));
        }
        return result;
    }

	private List<SampledValue> getBooleanValues() {
		assert baseElement.isVirtual()  || !((VirtualTreeElement) m_values).isVirtual() : "Schedule state inconsistent";
        final long[] times = m_times.getData().getLongArr();
        final boolean[] values = m_values.getData().getBooleanArr();
        final int[] qualities = m_qualities.getData().getIntArr();
        List<SampledValue> result = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; ++i) {
            final boolean v = values[i];
            final long t = times[i];
            final Quality q = Quality.getQuality(qualities[i]);
            result.add(new SampledValue(new BooleanValue(v), t, q));
        }
        return result;
    }

	private List<SampledValue> getIntegerValues() {
		assert baseElement.isVirtual()  || !((VirtualTreeElement) m_values).isVirtual() : "Schedule state inconsistent";
        final long[] times = m_times.getData().getLongArr();
        final int[] values = m_values.getData().getIntArr();
        final int[] qualities = m_qualities.getData().getIntArr();
        List<SampledValue> result = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; ++i) {
            final int v = values[i];
            final long t = times[i];
            final Quality q = Quality.getQuality(qualities[i]);
            result.add(new SampledValue(new IntegerValue(v), t, q));
        }
        return result;
    }

	private List<SampledValue> getLongValues() {
		assert baseElement.isVirtual()  || !((VirtualTreeElement) m_values).isVirtual() : "Schedule state inconsistent";
        final long[] times = m_times.getData().getLongArr();
        final long[] values = m_values.getData().getLongArr();
        final int[] qualities = m_qualities.getData().getIntArr();
        List<SampledValue> result = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; ++i) {
            final long v = values[i];
            final long t = times[i];
            final Quality q = Quality.getQuality(qualities[i]);
            result.add(new SampledValue(new LongValue(v), t, q));
        }
        return result;
    }

	private List<SampledValue> getStringValues() {
		assert baseElement.isVirtual()  || !((VirtualTreeElement) m_values).isVirtual() : "Schedule state inconsistent";
        final long[] times = m_times.getData().getLongArr();
        final String[] values = m_values.getData().getStringArr();
        final int[] qualities = m_qualities.getData().getIntArr();
        List<SampledValue> result = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; ++i) {
            final String v = values[i];
            final long t = times[i];
            final Quality q = Quality.getQuality(qualities[i]);
            result.add(new SampledValue(new StringValue(v), t, q));
        }
        return result;
    }

	private void writeFloatValues(List<SampledValue> entries) {
		assert entries.isEmpty() || baseElement.isVirtual()  || !((VirtualTreeElement) m_values).isVirtual() : "Schedule state inconsistent"; // 1st case on delete
		final int size = entries.size();
		final long[] times = new long[size];
		final int[] qualities = new int[size];
		final float[] values = new float[size];
		for (int i = 0; i < size; ++i) {
			final SampledValue entry = entries.get(i);
			times[i] = entry.getTimestamp();
			qualities[i] = entry.getQuality().getQuality();
			values[i] = entry.getValue().getFloatValue();
		}
		m_times.getData().setLongArr(times);
		m_qualities.getData().setIntArr(qualities);
		m_values.getData().setFloatArr(values);
	}

	private void writeBooleanValues(List<SampledValue> entries) {
		assert entries.isEmpty() || baseElement.isVirtual()  || !((VirtualTreeElement) m_values).isVirtual() : "Schedule state inconsistent"; // 1st case on delete
		final int size = entries.size();
		final long[] times = new long[size];
		final int[] qualities = new int[size];
		final boolean[] values = new boolean[size];
		for (int i = 0; i < size; ++i) {
			final SampledValue entry = entries.get(i);
			times[i] = entry.getTimestamp();
			qualities[i] = entry.getQuality().getQuality();
			values[i] = entry.getValue().getBooleanValue();
		}
		m_times.getData().setLongArr(times);
		m_qualities.getData().setIntArr(qualities);
		m_values.getData().setBooleanArr(values);
	}

	private void writeIntegerValues(List<SampledValue> entries) {
		assert entries.isEmpty() || baseElement.isVirtual()  || !((VirtualTreeElement) m_values).isVirtual() : "Schedule state inconsistent"; // 1st case on delete
		final int size = entries.size();
		final long[] times = new long[size];
		final int[] qualities = new int[size];
		final int[] values = new int[size];
		for (int i = 0; i < size; ++i) {
			final SampledValue entry = entries.get(i);
			times[i] = entry.getTimestamp();
			qualities[i] = entry.getQuality().getQuality();
			values[i] = entry.getValue().getIntegerValue();
		}
		m_times.getData().setLongArr(times);
		m_qualities.getData().setIntArr(qualities);
		m_values.getData().setIntArr(values);
	}

	private void writeLongValues(List<SampledValue> entries) {
		assert entries.isEmpty() || baseElement.isVirtual()  || !((VirtualTreeElement) m_values).isVirtual() : "Schedule state inconsistent"; // 1st case on delete
		final int size = entries.size();
		final long[] times = new long[size];
		final int[] qualities = new int[size];
		final long[] values = new long[size];
		for (int i = 0; i < size; ++i) {
			final SampledValue entry = entries.get(i);
			times[i] = entry.getTimestamp();
			qualities[i] = entry.getQuality().getQuality();
			values[i] = entry.getValue().getLongValue();
		}
		m_times.getData().setLongArr(times);
		m_qualities.getData().setIntArr(qualities);
		m_values.getData().setLongArr(values);
	}

	private void writeStringValues(List<SampledValue> entries) {
		assert entries.isEmpty() || baseElement.isVirtual()  || !((VirtualTreeElement) m_values).isVirtual() : "Schedule state inconsistent"; // 1st case on delete
		final int size = entries.size();
		final long[] times = new long[size];
		final int[] qualities = new int[size];
		final String[] values = new String[size];
		for (int i = 0; i < size; ++i) {
			final SampledValue entry = entries.get(i);
			times[i] = entry.getTimestamp();
			qualities[i] = entry.getQuality().getQuality();
			values[i] = entry.getValue().getStringValue();
		}
		m_times.getData().setLongArr(times);
		m_qualities.getData().setIntArr(qualities);
		m_values.getData().setStringArr(values);
	}

	/*-----------------------------------------------------------
	 * Schedule functionality not covered by memory-schedules.
	 ----------------------------------------------------------*/
	@Override
	@Deprecated
	public Long getTimeOfLatestEntry() {
		return m_updateTime.getData().getLong();
	}

	public void setLastUpdateTime(long time) {
		m_lock.writeLock().lock();
		try {
			m_updateTime.getData().setLong(time);
		} finally {
			m_lock.writeLock().unlock();
		}
	}

	public void setLastModified(long time) {
		scheduleDataElement.setLastModified(time);
	}

	public long getLastModified() {
		return scheduleDataElement.getLastModified();
	}
    
    public TreeElement getParent() {
        return scheduleDataElement.getParent();
    }

	/**
	 *
	 * Methods from being a MemoryTimeSeries. Some are not supported here.
	 *
	 */
	@Override
	public final boolean setInterpolationMode(InterpolationMode mode) {
		m_lock.writeLock().lock();
		try {
			m_schedule.setInterpolationMode(mode);
			final int modeval = mode.getInterpolationMode();
			m_interpolationMode.getData().setInt(modeval);
		} finally {
			m_lock.writeLock().unlock();
		}
		return true;
	}

	@Override
	public final InterpolationMode getInterpolationMode() {
		m_lock.readLock().lock();
		try {
			return m_schedule.getInterpolationMode();
		} finally {
			m_lock.readLock().unlock();
		}
	}

	@Override
	public boolean addValue(long timestamp, Value value) {
		m_lock.writeLock().lock();
		try {
			m_schedule.addValue(timestamp, value);
			write();
		} finally {
			m_lock.writeLock().unlock();
		}
		return true;
	}

	@Override
	public boolean addValues(Collection<SampledValue> values) {
		m_lock.writeLock().lock();
		try {
			m_schedule.addValues(values);
			write();
		} finally {
			m_lock.writeLock().unlock();
		}
		return true;
	}

	@Override
	@Deprecated
	public final boolean addValueSchedule(long startTime, long stepSize, List<Value> values) {
		return replaceValuesFixedStep(startTime, values, stepSize);
	}

	@Override
	public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize) {
		m_lock.writeLock().lock();
		try {
			m_schedule.replaceValuesFixedStep(startTime, values, stepSize);
			write();
		} finally {
			m_lock.writeLock().unlock();
		}
		return true;
	}

	@Override
	public boolean addValue(long timestamp, Value value, long timeOfCalculation) {
		m_lock.writeLock().lock();
		try {
			m_schedule.addValue(timestamp, value, timeOfCalculation);
			write();
		} finally {
			m_lock.writeLock().unlock();
		}
		return true;
	}

	@Override
	public boolean addValues(Collection<SampledValue> values, long timeOfCalculation) {
		m_lock.writeLock().lock();
		try {
			m_schedule.addValues(values, timeOfCalculation);
			write();
		} finally {
			m_lock.writeLock().unlock();
		}
		return true;
	}

	@Override
	@Deprecated
	public final boolean addValueSchedule(long startTime, long stepSize, List<Value> values, long timeOfCalculation) {
		return replaceValuesFixedStep(startTime, values, stepSize, timeOfCalculation);
	}

	@Override
	public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize, long timeOfCalculation) {
		m_lock.writeLock().lock();
		try {
			m_schedule.replaceValuesFixedStep(startTime, values, stepSize, timeOfCalculation);
			write();
		} finally {
			m_lock.writeLock().unlock();
		}
		return true;
	}

	@Override
	public boolean deleteValues() {
		m_lock.writeLock().lock();
		try {
			m_schedule.deleteValues();
			write();
		} finally {
			m_lock.writeLock().unlock();
		}
		return true;
	}

	@Override
	public boolean deleteValues(long endTime) {
		m_lock.writeLock().lock();
		try {
			m_schedule.deleteValues(endTime);
			write();
		} finally {
			m_lock.writeLock().unlock();
		}
		return true;
	}

	@Override
	public boolean deleteValues(long startTime, long endTime) {
		m_lock.writeLock().lock();
		try {
			m_schedule.deleteValues(startTime, endTime);
			write();
		} finally {
			m_lock.writeLock().unlock();
		}
		return true;
	}

	@Override
	public boolean replaceValues(long startTime, long endTime, Collection<SampledValue> values) {
		m_lock.writeLock().lock();
		try {
			m_schedule.replaceValues(startTime, endTime, values);
			write();
		} finally {
			m_lock.writeLock().unlock();
		}
		return true;
	}

	@Override
	public SampledValue getValue(long time) {
		m_lock.readLock().lock();
		try {
			return m_schedule.getValue(time);
		} finally {
			m_lock.readLock().unlock();
		}
	}

	@Override
	public SampledValue getNextValue(long time) {
		m_lock.readLock().lock();
		try {
			return m_schedule.getNextValue(time);
		} finally {
			m_lock.readLock().unlock();
		}
	}

	@Override
	public List<SampledValue> getValues(long startTime) {
		m_lock.readLock().lock();
		try {
			return m_schedule.getValues(startTime);
		} finally {
			m_lock.readLock().unlock();
		}
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		m_lock.readLock().lock();
		try {
			return m_schedule.getValues(startTime, endTime);
		} finally {
			m_lock.readLock().unlock();
		}
	}

	@Override
	public Long getLastCalculationTime() {
		m_lock.readLock().lock();
		try {
			return m_schedule.getLastCalculationTime();
		} finally {
			m_lock.readLock().unlock();
		}
	}

    public VirtualTreeElement getScheduleElement() {
        return scheduleDataElement;
    }

    @Override
    public SampledValue getPreviousValue(long time) {
    	m_lock.readLock().lock();
		try {
			return m_schedule.getPreviousValue(time);
		} finally {
			m_lock.readLock().unlock();
		}
    }

	@Override
	public boolean isEmpty() {
    	m_lock.readLock().lock();
		try {
			return m_schedule.isEmpty();
		} finally {
			m_lock.readLock().unlock();
		}
	}

	@Override
	public boolean isEmpty(long startTime, long endTime) {
    	m_lock.readLock().lock();
		try {
			return m_schedule.isEmpty(startTime, endTime);
		} finally {
			m_lock.readLock().unlock();
		}
	}

	@Override
	public int size() {
    	m_lock.readLock().lock();
		try {
			return m_schedule.size();
		} finally {
			m_lock.readLock().unlock();
		}
	}

	@Override
	public int size(long startTime, long endTime) {
    	m_lock.readLock().lock();
		try {
			return m_schedule.size(startTime, endTime);
		} finally {
			m_lock.readLock().unlock();
		}
	}

	/**
	 * This returns a non-fail-fast iterator. Intermediates changes to 
	 * the schedule may or may not be reflected in the iterator. 
	 */
	@Override
	public Iterator<SampledValue> iterator() {
		return new ScheduleIterator(m_schedule, m_lock);
	}

	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		return new ScheduleIterator(m_schedule, m_lock, startTime, endTime);
	}
    
}
