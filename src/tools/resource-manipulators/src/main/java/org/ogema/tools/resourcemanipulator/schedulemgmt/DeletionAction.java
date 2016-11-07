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
package org.ogema.tools.resourcemanipulator.schedulemgmt;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.timeseries.TimeSeries;

/**
 * Delete values older than some threshold.
 */
public class DeletionAction extends TimeSeriesReduction {

	public DeletionAction(ApplicationManager am) {
		super(am);
	}
	
	@Override
	public void apply(TimeSeries schedule, long ageThreshold) {
		if (logger.isDebugEnabled())
			logger.debug("Schedule management removing " + schedule.getValues(Long.MIN_VALUE, am.getFrameworkTime() - ageThreshold).size()
				+ " values from schedule {}", schedule);
		schedule.deleteValues(Long.MIN_VALUE, am.getFrameworkTime() - ageThreshold);
	}
	
}
