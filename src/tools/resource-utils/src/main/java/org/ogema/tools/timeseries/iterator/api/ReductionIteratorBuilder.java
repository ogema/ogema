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
package org.ogema.tools.timeseries.iterator.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;

import com.google.common.base.Function;

public class ReductionIteratorBuilder {

	private final MultiTimeSeriesIterator iterator;
	private final Function<Collection<SampledValue>, SampledValue> function;
	private boolean ignoreGaps;
	private InterpolationMode globalMode;
	private List<InterpolationMode> individualModes;
	
	private ReductionIteratorBuilder(MultiTimeSeriesIterator iterator, Function<Collection<SampledValue>, SampledValue> function) {
		this.iterator = Objects.requireNonNull(iterator);
		this.function = Objects.requireNonNull(function);
	}

	public static ReductionIteratorBuilder newBuilder(MultiTimeSeriesIterator iterator, Function<Collection<SampledValue>, SampledValue> function) {
		return new ReductionIteratorBuilder(iterator, function);
	}
	
	public ReductionIterator build() {
		return new ReductionIterator(iterator, function, individualModes, globalMode, ignoreGaps);
	}
	
	public ReductionIteratorBuilder setIgnoreGaps(boolean ignoreGaps) {
		this.ignoreGaps = ignoreGaps;
		return this;
	}
	
	public ReductionIteratorBuilder setGlobalMode(InterpolationMode globalMode) {
		this.globalMode = globalMode;
		if (globalMode != null)
			this.individualModes = null;
		return this;
	}
	
	public ReductionIteratorBuilder setIndividualModes(List<InterpolationMode> individualModes) {
		this.individualModes = individualModes == null || individualModes.isEmpty() ? null : new ArrayList<>(individualModes);
		if (this.individualModes !=null)
			this.globalMode = null;
		return this;
	}
	
}
