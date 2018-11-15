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
