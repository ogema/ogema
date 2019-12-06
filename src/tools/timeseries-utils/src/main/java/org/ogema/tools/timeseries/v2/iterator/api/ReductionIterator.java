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
package org.ogema.tools.timeseries.v2.iterator.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;

/**
 * Map a set of timeseries to a single timeseries, by applying a point-wise arithmetic operator.
 * Use {@link ReductionIteratorBuilder} to create an instance.
 */
public class ReductionIterator implements Iterator<SampledValue> {

	private final MultiTimeSeriesIterator multiIt;
	private final Function<Collection<SampledValue>, SampledValue> function;
	private final int size;
	private final List<InterpolationMode> modes;
	private final InterpolationMode mode;
	private final boolean ignoreGaps;
	
	ReductionIterator(MultiTimeSeriesIterator multiIt, Function<Collection<SampledValue>, SampledValue> function, 
			List<InterpolationMode> modes, InterpolationMode mode, boolean ignoreGaps) {
		this.multiIt = Objects.requireNonNull(multiIt);
		this.function = Objects.requireNonNull(function);
		this.size = multiIt.size();
		this.modes = modes;
		this.mode = mode;
		this.ignoreGaps = ignoreGaps;
		if (modes != null && modes.size() != size)
			throw new IllegalArgumentException("Number of interpolation modes does not match the size of the multi iterator");
		advance();
	}

	private SampledValue next;
	
	@Override
	public boolean hasNext() {
		return next != null;
	}
	
	private void advance() {
		final List<SampledValue> list = new ArrayList<>(size * 2);
		while (true) {
			if (!multiIt.hasNext()) {
				next = null;
				return;
			}
			final SampledValueDataPoint next = multiIt.next();
			for (int i=0; i<size; i++) {
				final SampledValue point;
				if (modes != null)
					point = next.getElement(i, modes.get(i));
				else if (mode != null)
					point = next.getElement(i, mode);
				else
					point = next.getElement(i);
				if (point == null) {
					if (!ignoreGaps)
						break;
					else
						continue;
				}
				list.add(point);
			}
			if (list.isEmpty() || (!ignoreGaps && list.size() != size))
				list.clear();
			else
				break;
		}
		if (list.isEmpty())
			next = null;
		else 
			next = function.apply(list);
	}

	@Override
	public SampledValue next() {
		final SampledValue bak = next;
		if (bak == null)
			throw new NoSuchElementException("No further element");
		advance();
		return bak;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove not supported");
	}
	
}
