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
