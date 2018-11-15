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
