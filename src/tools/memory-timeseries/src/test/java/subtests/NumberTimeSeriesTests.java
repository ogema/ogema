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
import java.util.Arrays;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;

/**
 * Tests for TimeSeries that can be sensibly interpreted as a TimeSeries of
 * numbers over time, i.e. floats, integers and longs.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public abstract class NumberTimeSeriesTests extends TimeSeriesTests {

	public NumberTimeSeriesTests(MemoryTimeSeries timeSeries) {
		super(timeSeries);
	}

	public void performAllTests() {
		super.performAllTests();
		replacingValuesInARangeWorks();
	}

	// FIXME will not work for Boolean and String 
	public void replacingValuesInARangeWorks() {
		m_timeSeries.deleteValues();
		m_timeSeries.addValue(1, getValue((41)));
		m_timeSeries.addValue(2, getValue((42)));
		m_timeSeries.addValue(3, getValue((43)));

		assert 3 == m_timeSeries.getValues(0).size();
		assert 42 == m_timeSeries.getValue(2).getValue().getIntegerValue();
		m_timeSeries.replaceValues(2, 3, Arrays.asList(new SampledValue(getValue(2.f), 2, Quality.GOOD)));
		assert 3 == m_timeSeries.getValues(0).size();
		assert 2 == m_timeSeries.getValue(2).getValue().getIntegerValue();
	}
}
