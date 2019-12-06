package org.ogema.tools.timeseries.iterator.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIterator;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIteratorBuilder;
import org.ogema.tools.timeseries.iterator.api.SampledValueDataPoint;

/**
 * To instantiate this, use a {@link MultiTimeSeriesIteratorBuilder}. Since this class is not exported, it cannot be
 * instantiated directly. 
 */
public class TimeSeriesMultiIteratorImplDiff implements MultiTimeSeriesIterator {
	
	private final MultiTimeSeriesIterator base;
	private final double timeInterpolation; // important to use double vs float here 
	private final int size;
	
	// state
	private SampledValueDataPoint lastBase;
	private SampledValueDataPoint coming;
	private SampledValueDataPoint current;
	
	public TimeSeriesMultiIteratorImplDiff(MultiTimeSeriesIterator base, float timeInterpolation) {
		this.base = base;
		this.timeInterpolation = timeInterpolation;
		this.size = base.size();
		init();
	}

	@Override
	public boolean hasNext() {
		return coming != null;
	}

	@Override
	public int maxNrHistoricalValues() {
		return base.maxNrHistoricalValues();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove not supported");
	}

	@Override
	public SampledValueDataPoint next() {
		if (coming == null)
			throw new NoSuchElementException("no further element");
		this.current = coming;
		if (base.hasNext()) {
			final SampledValueDataPoint point = base.next();
			this.coming = new DiffSampledPoint(lastBase, point, timeInterpolation);
			this.lastBase = point;
		} else {
			this.coming = null;
			this.lastBase = null;
		}
		return current;
	}
	
	private void init() {
		if (base.hasNext()) {
			this.lastBase = base.next();
			if (base.hasNext()) {
				final SampledValueDataPoint point = base.next();
				this.coming = new DiffSampledPoint(lastBase, point, timeInterpolation);
				this.lastBase = point;
			}
		}
	}
	
	private static class DiffSampledPoint implements SampledValueDataPoint {
		
		private final SampledValueDataPoint previous;
		private final SampledValueDataPoint current;
		private final Map<Integer,SampledValue> values;
		private final double timeInterpolation;
		
		private final long t;
		private Object context;
		
		public DiffSampledPoint(SampledValueDataPoint previous, SampledValueDataPoint current, final double timeInterpolation) {
			assert previous != null;
			assert current != null;
			this.previous = previous;
			this.current = current;
			this.timeInterpolation = timeInterpolation;
			this.t = intermediate(previous.getTimestamp(), current.getTimestamp(), timeInterpolation);
			final Map<Integer, SampledValue> last = previous.getElements();
			final Map<Integer, SampledValue> values = new HashMap<>(Math.max(last.size(), 2));
			for (Map.Entry<Integer, SampledValue> entry : current.getElements().entrySet()) {
				final SampledValue old = last.get(entry.getKey());
				if (old != null)
					values.put(entry.getKey(), diff(entry.getValue(), old, t));
				else
					values.put(entry.getKey(), new SampledValue(new FloatValue(Float.NaN), current.getTimestamp(), Quality.BAD));
			}
			this.values = Collections.unmodifiableMap(values);
		}
		
		private static final SampledValue diff(final SampledValue current, final SampledValue last, final double factor) {
			return diff(current, last, intermediate(last.getTimestamp(), current.getTimestamp(), factor));
		}
		
		private static final SampledValue diff(final SampledValue current, final SampledValue last, final long t) {
			final Value v = current.getValue();
			final Value vNew;
			if (v instanceof FloatValue)
				vNew = new FloatValue(v.getFloatValue() - last.getValue().getFloatValue());
			else if (v instanceof IntegerValue)
				vNew = new IntegerValue(v.getIntegerValue() - last.getValue().getIntegerValue());
			else if (v instanceof LongValue)
				vNew = new LongValue(v.getLongValue() - last.getValue().getLongValue());
			else if (v instanceof DoubleValue)
				vNew = new DoubleValue(v.getDoubleValue() - last.getValue().getDoubleValue());
			else
				throw new IllegalArgumentException("Unsupported value type " + v.getClass().getName());
			return new SampledValue(vNew, t, current.getQuality() == Quality.GOOD && last.getQuality() == Quality.GOOD ? Quality.GOOD : Quality.BAD);
		}
		
		private static final long intermediate(final long t1, final long t2, final double factor) {
			return (long) (t1 + (t2-t1) * factor);
		}

		@Override
		public Map<Integer, SampledValue> getElements() {
			return values;
		}

		@Override
		public boolean hasNext(int idx) {
			return current.hasNext(idx);
		}

		@Override
		public SampledValue next(int idx) {
			return getNextElement(idx); // correct?
		}

		@Override
		public SampledValue previous(int idx) {
			return getPreviousElement(idx); // correct?
		}

		@Override
		public <S> void setContext(S object) {
			this.context = object;
		}

		@Override
		public <S> S getContext() {
			return (S) context;
		}

		@Override
		public int inputSize() {
			return current.inputSize();
		}

		@Override
		public long getTimestamp() {
			return t;
		}

		@Override
		public long getPreviousTimestamp() {
			return current.getPreviousTimestamp(); // FIXME?
		}

		@Override
		public long getNextTimestamp() {
			return current.getNextTimestamp(); // FIXME?
		}

		@Override
		public SampledValue getElement(int idx) {
			if (values.containsKey(idx)) 
				return values.get(idx);
			final SampledValue next = getNextElement(idx);
			final SampledValue previous = getPreviousElement(idx);
			if (next == null || previous == null)
				return null;
			final long diff = next.getTimestamp() - previous.getTimestamp();
			final long share = (getTimestamp() - previous.getTimestamp()) / diff;
			return new SampledValue(new FloatValue(next.getValue().getFloatValue() * share), getTimestamp(), next.getQuality());
		}

		// interpolation mode does not make sense here
		@Override
		public SampledValue getElement(int idx, InterpolationMode interpolationMode) {
			return getElement(idx);
		}

		@Override
		public SampledValue getNextElement(int idx) {
			final SampledValue next = current.getNextElement(idx);
			final SampledValue last = values.containsKey(idx) ? values.get(idx) : current.getPreviousElement(idx);
			if (next == null || last == null)
				return null;
			return diff(next, last, timeInterpolation);
		}

		@Override
		public SampledValue getPreviousElement(int idx) {
			final SampledValue last = previous.getPreviousElement(idx);
			final Map<Integer, SampledValue> lastOnes = previous.getElements();
			final SampledValue next = lastOnes.containsKey(idx) ? lastOnes.get(idx) : previous.getNextElement(idx);
			if (next == null || last == null)
				return null;
			return diff(next, last, timeInterpolation);
		}

		@Override
		public SampledValueDataPoint getPrevious(int stepsBack) throws IllegalArgumentException, IllegalStateException {
			throw new IllegalArgumentException("not supported");
		}

		@Override
		public float getSum(boolean ignoreMissingPoints, InterpolationMode mode) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
}
