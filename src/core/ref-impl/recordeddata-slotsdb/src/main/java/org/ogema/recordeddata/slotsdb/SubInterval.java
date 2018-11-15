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
package org.ogema.recordeddata.slotsdb;

//import java.util.ArrayList;
//import java.util.List;
//
//import org.ogema.core.channelmanager.measurements.SampledValue;

/**
 * @deprecated this class does not seem to be used any more 
 */
// TODO remove
@Deprecated
public class SubInterval {

//	private long period;
//	private int offset;
//	private int maxOffset;
//	private long currentTimestamp;
//	private SampledValue loggedValue;
//	private List<SampledValue> loggedValues;
//	private long currentEnd;
//	private long currentStart;
//
//	//	private long nextEnd;
//	//	private long nextStart;
//
//	public SubInterval(long start, long period, List<SampledValue> loggedValues) {
//		this.currentStart = start;
//		this.period = period;
//		this.currentEnd = start + this.period;
//		this.offset = 0;
//		this.maxOffset = loggedValues.size() - 1;
//		this.loggedValues = loggedValues;
//		this.loggedValue = loggedValues.get(offset);
//		this.currentTimestamp = loggedValue.getTimestamp();
//	}
//
//	public List<SampledValue> getValuesOfNextInterval() {
//
//		List<SampledValue> values = new ArrayList<SampledValue>();
//
//		//logger.info("#*# currentEnd on start = " + currentEnd);
//
//		//		currentStart = nextStart;
//		//		currentEnd = nextEnd;
//
//		while (currentTimestamp >= currentStart && currentTimestamp <= currentEnd) {
//			values.add(loggedValue);
//			// TODO possible performance improvement when using exception instead of if condition?
//			if (offset < maxOffset) {
//				offset++;
//				loggedValue = loggedValues.get(offset);
//				currentTimestamp = loggedValue.getTimestamp();
//			}
//			else {
//				break;
//			}
//		}
//
//		currentStart = currentEnd;
//		currentEnd = currentStart + period;
//
//		//logger.info("#*# currentEnd on exit = " + currentEnd);
//
//		return values;
//	}
//
//	/**
//	 * Returns all values of the current interval from start of the interval till the specified end time.
//	 */
//	public List<SampledValue> getValuesTill(long endTime) {
//
//		List<SampledValue> values = new ArrayList<SampledValue>();
//
//		while (currentTimestamp >= currentStart && currentTimestamp <= endTime) {
//			values.add(loggedValue);
//
//			//logger.info("#*#getValuesTill add: " + loggedValue.getTimestamp() + " " + loggedValue.getValue().getDoubleValue());
//
//			if (offset < maxOffset) {
//				offset++;
//				loggedValue = loggedValues.get(offset);
//				currentTimestamp = loggedValue.getTimestamp();
//			}
//			else {
//				break;
//			}
//		}
//
//		return values;
//	}
//
//	public long getCurrentEnd() {
//		return currentEnd;
//	}
//
//	public long getPeriod() {
//		return period;
//	}
//
//	public long getCurrentStart() {
//		return currentStart;
//	}

}
