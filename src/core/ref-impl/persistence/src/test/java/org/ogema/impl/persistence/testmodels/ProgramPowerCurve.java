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
package org.ogema.impl.persistence.testmodels;

import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.prototypes.Data;

/**
 * Test model for the persistence tests. Used to be part of the OGEMA data model, but was thrown out for the release.
 * Copy-pasting it to the actual tests seemed the easiest solution to keep the tests working.
 */
public interface ProgramPowerCurve extends Data {
	/** Power curve */
	RelativeTimeRow estimation();

	/**
	 * Maximum duration of power curve (to avoid infinitive length due to measurement problems etc.), not relevant if
	 * the curve is not the result of an estimation process
	 */
	TimeResource maxDuration();
}
