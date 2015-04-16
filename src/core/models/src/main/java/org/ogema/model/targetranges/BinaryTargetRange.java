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
package org.ogema.model.targetranges;

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.model.ranges.BinaryRange;

/**
 * Target range for boolean ranges. The meaning and units of the values
 * must be defined somewhere in the parent resources.
 */
public interface BinaryTargetRange extends TargetRange {

	@Override
	BooleanResource setpoint();

	@Override
	BinaryRange controlLimits();

	@Override
	BinaryRange targetRange();

	@Override
	BinaryRange alarmLimits();
}
