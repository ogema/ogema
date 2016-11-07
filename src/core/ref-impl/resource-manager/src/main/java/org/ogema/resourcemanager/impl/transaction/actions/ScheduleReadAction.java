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

import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.resourcemanager.transaction.ReadConfiguration;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.resource.util.ValueResourceUtils;
import org.ogema.tools.timeseries.implementations.ArrayTimeSeries;

public class ScheduleReadAction extends ResourceReadAction<ReadOnlyTimeSeries, Schedule> {

	private final long[] interval;
	
	public ScheduleReadAction(Schedule resource, ReadConfiguration config, long t0, long t1) {
		super(resource, config);
		this.interval = new long[]{t0,t1};
	}
	
	@Override
	protected ReadOnlyTimeSeries readResource(Schedule resource) {
		return new ArrayTimeSeries(ValueResourceUtils.getValueType(resource)).read(resource,interval[0],interval[1]);
	}

}
