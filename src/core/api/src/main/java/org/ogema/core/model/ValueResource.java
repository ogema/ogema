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
package org.ogema.core.model;

import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.timeseries.TimeSeries;

/**
 * This is a marker interface for resource types whose nodes not only define structure
 * but also contain actual values. Three different specializations of this exist:
 * {@link SingleValueResource} for nodes containing a single value, {@link ArrayResource}
 * for nodes containing an array of nodes, and {@link Schedule} for nodes containing
 * a full {@link TimeSeries}.
 */
public interface ValueResource extends Resource {

	/**
	 * Gets the time of the most recent write access to the resource. Write
	 * accesses are counted as an update even when the new value equals the
	 * old one.
	 * @return timestamp of the last write access to the resource in ms since 1970.
	 * If the resource has never been written to so far, this returns -1. For resources that are
	 * not stored persistently, also the last update time is not stored persistently, otherwise
	 * the last update time is stored persistently.
	 */
	long getLastUpdateTime();
}
