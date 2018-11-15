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
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;

/**
 * Tests specific to time series of integer values.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class IntegerTimeSeriesTests extends NumberTimeSeriesTests {

	public IntegerTimeSeriesTests(MemoryTimeSeries timeSeries) {
		super(timeSeries);
	}

	@Override
	public void performAllTests() {
		super.performAllTests();
	}

	@Override
	final public Value getValue(float x) {
		return new IntegerValue((int) x);
	}

	@Override
	final public float getFloat(Value value) {
		return (float) value.getIntegerValue();
	}
}
