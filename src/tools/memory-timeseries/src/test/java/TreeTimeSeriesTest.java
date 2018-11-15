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
import subtests.FloatTimeSeriesTests;
import subtests.IntegerTimeSeriesTests;

import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;
import org.ogema.tools.timeseries.implementations.TreeTimeSeries;

/**
 * Tests for the TreeTimeSeries implementation of the time series.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class TreeTimeSeriesTest {

	@Test
	public void testFloatTimeSeries() {
		MemoryTimeSeries timeSeries = new TreeTimeSeries(FloatValue.class);
		FloatTimeSeriesTests tests = new FloatTimeSeriesTests(timeSeries);
		tests.performAllTests();
	}

	@Test
	public void testIntegerTimeSeries() {
		MemoryTimeSeries timeSeries = new TreeTimeSeries(IntegerValue.class);
		IntegerTimeSeriesTests tests = new IntegerTimeSeriesTests(timeSeries);
		tests.performAllTests();
	}

	@Test
	public void testLongTimeSeries() {
		MemoryTimeSeries timeSeries = new TreeTimeSeries(LongValue.class);
		FloatTimeSeriesTests tests = new FloatTimeSeriesTests(timeSeries);
		tests.performAllTests();
	}
	
}
