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
package org.ogema.tools.resourcemanipulator.trashcan;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;

/**
 * Basic structure for symmetric N-&gt;1 math operation supported by the automath tools.
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
