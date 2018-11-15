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
package org.ogema.resourcemanager.impl.transaction.actions;

import java.util.Collection;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.resourcemanager.transaction.WriteConfiguration;

public class ScheduleReplaceAction extends ResourceWriteAction<Collection<SampledValue>, Schedule>{

	private final long[] interval;
	
	public ScheduleReplaceAction(Schedule resource, Collection<SampledValue> value, WriteConfiguration config, long t0, long t1) {
		super(resource, value, config);
		this.interval = new long[]{t0,t1};
	}

	@Override
	protected void write(Schedule resource, Collection<SampledValue> values) {
		resource.replaceValues(interval[0], interval[1], values);
	}
	
	@Override
	protected void undo(Schedule resource, Collection<SampledValue> value) {
		super.write(resource, value);
	}
	
	
}
