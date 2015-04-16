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
package org.ogema.tools.resourcemanipulator.trashcan;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;

/**
 * Basic structure for symmetric N->1 math operation supported by the automath tools.
 * Concrete math operations inherit from this. Note that in this context "symmetric"
 * means that f(..., x, y, ...) = f(..., x, y, ...) and that hence no order of the
 * arguments needs to be stored (and a {@link ResourceList} can be used as the type
 * for the input fields).
 */
public interface AutomathCommutativeFunction<T extends Resource> extends ResourceManipulatorModel {
	/**
	 * Input values. If an input value is not active (or non-existing), the input
	 * is usually considered as the neutral element.
	 */
	ResourceList<T> inputs();

	/**
	 * Output value
	 */
	T result();

	/**
	 * Time delay between the change of an input and the evaluation of the
	 * new result. The delay counter starts to count-down when an input has
	 * changed. If another input changes during the count-down, the behavior
	 * (re-set count-down or simply continue) is determined by the entry in
	 * {@link #resetCountdownOnChange()}. If this is zero, no count-down occurs
	 * in the first place.
	 */
	TimeResource delay();

	/**
	 * Determines what happens when subsequent changes in the inputs occur
	 * during a count-down. If true, the count-down is re-set to {@link #delay() }.
	 * Otherwise, the count-down is simply continued.
	 */
	BooleanResource resetCountdownOnChange();
}
