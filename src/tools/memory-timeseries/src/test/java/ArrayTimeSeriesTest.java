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
import subtests.FloatTimeSeriesTests;
import subtests.IntegerTimeSeriesTests;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;
import org.ogema.tools.timeseries.implementations.ArrayTimeSeries;

/**
 * Tests for the ArrayTimeSeries implementation of the time series.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ArrayTimeSeriesTest {

	@Test
	public void testFloatTimeSeries() {
		MemoryTimeSeries timeSeries = new ArrayTimeSeries(FloatValue.class);
		FloatTimeSeriesTests tests = new FloatTimeSeriesTests(timeSeries);
		tests.performAllTests();
	}

	@Test
	public void testIntegerTimeSeries() {
		MemoryTimeSeries timeSeries = new ArrayTimeSeries(IntegerValue.class);
		IntegerTimeSeriesTests tests = new IntegerTimeSeriesTests(timeSeries);
		tests.performAllTests();
	}

	@Test
	public void testLongTimeSeries() {
		MemoryTimeSeries timeSeries = new ArrayTimeSeries(LongValue.class);
		FloatTimeSeriesTests tests = new FloatTimeSeriesTests(timeSeries);
		tests.performAllTests();
	}

}
