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
package org.ogema.tools.timeseries.v2.memory;

import java.time.Clock;
import java.util.concurrent.ConcurrentSkipListSet;

import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.v2.base.TimeSeriesBase;

/**
 * Writable memory time series based on a {@link ConcurrentSkipListSet}.
 * This implementation is thread-safe, i.e. concurrent write/write or write/read access 
 * need not be synchronized.
 */
public class ConcurrentTreeTimeSeries extends TreeTimeSeriesBase implements TimeSeriesBase {

	public ConcurrentTreeTimeSeries(InterpolationMode mode) {
		this(mode, Clock.systemUTC());
	}
	
	public ConcurrentTreeTimeSeries(InterpolationMode mode, Clock clock) {
		super(new ConcurrentSkipListSet<>(), mode, clock);
	}

}
