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
package subtests;

/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;

/**
 * Tests specific to time series of float values.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class FloatTimeSeriesTests extends NumberTimeSeriesTests {

	public FloatTimeSeriesTests(MemoryTimeSeries timeSeries) {
		super(timeSeries);
	}

	@Override
	public void performAllTests() {
		super.performAllTests();
	}

	@Override
	final public Value getValue(float x) {
		return new FloatValue(x);
	}

	@Override
	final public float getFloat(Value value) {
		return value.getFloatValue();
	}
}
