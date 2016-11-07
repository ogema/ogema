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
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.timeseries.TimeSeries;

public abstract class TimeSeriesReduction {
	
	protected final ApplicationManager am;
	protected final OgemaLogger logger;
	
	protected TimeSeriesReduction(ApplicationManager am) {
		this.am = am;
		this.logger = am.getLogger();
	}

	public abstract void apply(TimeSeries schedule, long ageThreshold);
	
	public final static long subtract(long l1, long l2) throws ArithmeticException {
		long r = l1 - l2;
		long aux = -l2;
		if (((l1 & aux & ~r) | (~l1 & ~aux & r)) <0) 
			throw new ArithmeticException("long overflow");
		return r;
	}
	
}
