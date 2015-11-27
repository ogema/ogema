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
package org.ogema.core.model.simple;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.timeseries.TimeSeries;

/**
 * Simple resource holding a float value.
 */
public interface FloatResource extends SingleValueResource {

	/**
	 * Gets the value stored in the resource.
	 */
	float getValue();

	/**
	 * Sets the resource value to value.
	 * @return returns true if the value could be written, false if not (e.g. if access mode is read-only).
	 */
	boolean setValue(float value);

	/**
	 * Gets an access to the value's logged data.
	 */
	RecordedData getHistoricalData();

	/**
	 * Future prognosis for this value. The data type, unit and interpretation of
	 * the values in the schedule are the same as the value in this. If multiple
	 * forecasts are available for some reason, this shall reflect the best-guess
	 * (either the best individual forecast or the best combined forecast). The
	 * other forecasts can be added as decorators.
	 */
	AbsoluteSchedule forecast();

	/**
	 * Future behavior of this value that shall be reached through management
	 * operations. 
	 * The data type, unit and interpretation of
	 * the values in the schedule are the same as the value in this.
	 */
	AbsoluteSchedule program();

	/**
	 * Historical data, including data obtained through the OGEMA logging service and
	 * explicitly added historical data. <br>
	 * In order to access only the data logged by the framework use the method
	 * {@link #getHistoricalData()} instead. <br>
	 * Note that the {@link TimeSeries#deleteValues} methods only remove the explicitly added data here, 
	 * not the framework log data. You can override a logged data point by adding a {@link SampledValue} at 
	 * the same timestamp. The logged value will still be accessible via {@link #getHistoricalData()}.
	 */
	AbsoluteSchedule historicalData();

}
