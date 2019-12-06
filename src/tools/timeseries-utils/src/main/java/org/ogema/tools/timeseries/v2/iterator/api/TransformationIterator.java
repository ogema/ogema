package org.ogema.tools.timeseries.v2.iterator.api;

import java.util.Iterator;
import java.util.function.Function;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

public class TransformationIterator implements Iterator<SampledValue> {

	private final Iterator<SampledValue> in;
	private final Function<Value, Value> transformation;
	
	public TransformationIterator(Iterator<SampledValue> in, Function<Value, Value> transformation) {
		this.in = in;
		this.transformation = transformation;
	}

	@Override
	public boolean hasNext() {
		return in.hasNext();
	}

	@Override
	public SampledValue next() {
		final SampledValue next = this.in.next();
		return new SampledValue(transformation.apply(next.getValue()), next.getTimestamp(), next.getQuality());
	}
	
	@Override
	public void remove() {
		in.remove();
	}
	
	
}
