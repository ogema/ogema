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
package org.ogema.tools.resourcemanipulator.model;

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;

/**
 * Data model for the configuration of a threshold rule.
 */
public interface ThresholdModel extends ResourceManipulatorModel {

	/**
	 * Reference to the resource whose value is supervised.
	 */
	FloatResource source();

	/**
	 * Value of the threshold to compare to.
	 */
	FloatResource threshold();

	/**
	 * Reference to the target resource that is set according to the relative
	 * values of source and threshold.
	 */
	BooleanResource target();

	/**
	 * If true, equality of source and threshold is considered as "exceeds the value".
	 */
	BooleanResource equalityExceeds();

	/**
	 * If true, the result of the comparison between {@link #source()} and 
	 * {@link #threshold() } is inverted
	 * before being written to the {@link #target()}
	 */
	BooleanResource invert();
}
