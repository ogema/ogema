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
package org.ogema.tools.timeseries.api;

import org.ogema.core.channelmanager.measurements.SampledValue;

/**
 * An linear operation SampledValue -&gt; SampledValue.
 */
public interface LinearSampledValueOperator {

	/**
	 * Performs the operation and returns the result. Argument is constant. The
	 * quality of the result is good if the quality of the input was good and is
	 * bad if the quality of the input was bad. Timestamp equals that of the input.
	 */
	SampledValue apply(final SampledValue value);
}
