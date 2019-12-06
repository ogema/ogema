package org.ogema.tools.timeseries.v2.iterator.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

public class TransformationMultiIterator implements Iterator<SampledValue> {

	/**
	 * Resulting quality is good if and only if all quality values are good
	 */
	public static Function<List<Quality>, Quality> DEFAULT_QUALITY_FUNCTION = 
			qualities -> qualities.stream().filter(q -> q != Quality.GOOD).map(q -> Quality.BAD).findAny().orElse(Quality.GOOD);
	/**
	 * Resulting quality is good if any good quality value is present
	 */
	public static Function<List<Quality>, Quality> INGORANT_QUALITY_FUNCTION = 
			qualities -> qualities.stream().filter(q -> q == Quality.GOOD).findAny().orElse(Quality.BAD);
	private final MultiTimeSeriesIterator in;
	private final Function<List<Value>, Value> transformation;
	private final Function<List<Quality>, Quality> qualityFunction;

	public TransformationMultiIterator(MultiTimeSeriesIterator in, Function<List<Value>, Value> transformation) {
		this(in, transformation, DEFAULT_QUALITY_FUNCTION);
	}

	public TransformationMultiIterator(MultiTimeSeriesIterator in, Function<List<Value>, Value> transformation, 
			Function<List<Quality>, Quality> qualityFunction) {
		this.in = Objects.requireNonNull(in);
		this.transformation = Objects.requireNonNull(transformation);
		this.qualityFunction = Objects.requireNonNull(qualityFunction);
	}

	@Override
	public boolean hasNext() {
		return in.hasNext();
	}

	@Override
	public SampledValue next() {
		final SampledValueDataPoint next = this.in.next();
		final List<Value> values = new ArrayList<>(in.size());
		final List<Quality> q = new ArrayList<>(in.size());
		for (int i=0; i<in.size(); i++) {
			final SampledValue sv = next.getElement(i);
			values.add(sv == null ? null : sv.getValue());
			q.add(sv == null ? null : sv.getQuality());
		}
		return new SampledValue(transformation.apply(values), next.getTimestamp(), qualityFunction.apply(q));
	}
	
	@Override
	public void remove() {
		in.remove();
	}
	
	
}