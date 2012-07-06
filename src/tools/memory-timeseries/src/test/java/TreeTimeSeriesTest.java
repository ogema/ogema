/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
