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
