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
package org.ogema.core.model.simple;

import org.ogema.core.model.SimpleResource;
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.schedule.ForecastSchedule;
import org.ogema.core.recordeddata.RecordedData;

/**
 * Simple resource representing a time difference or a date. Time differences are given in milliseconds. Dates are
 * represented as "milliseconds since begin of 1970". Time differences always refer to physical time, not calendar time.
 * Dates are usually read from some system, so for dates the exact behavior with respect to leap seconds is not defined.
 */
public interface TimeResource extends SimpleResource {

	/**
	 * Gets the time difference (in ms).
	 */
	long getValue();

	/**
	 * Sets the time difference
	 * 
	 * @param value
	 *            new value for the time difference (in ms).
	 * @return returns true if the value could be written, false if not (e.g. if access mode is read-only).
	 */
	boolean setValue(long value);

	/**
	 * Gets recorded past values.
	 * 
	 * @return returns the handler for the recorded data.
	 */
	RecordedData getHistoricalData();

	/**
	 * Future prognosis for this value. The data type, unit and interpretation of
	 * the values in the schedule are the same as the value in this. If multiple
	 * forecasts are available for some reason, this shall reflect the best-guess
	 * (either the best individual forecast or the best combined forecast). The
	 * other forecasts can be added as decorators.
	 */
	ForecastSchedule forecast();

	/**
	 * Future behavior of this value that shall be reached through management
	 * operations. 
	 * The data type, unit and interpretation of
	 * the values in the schedule are the same as the value in this.
	 */
	DefinitionSchedule program();
}
