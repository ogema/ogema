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
