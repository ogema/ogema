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
package org.ogema.model.functions;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.model.prototypes.Data;

/**
 * Data basic model for a function X to X. A function is modeled similarly to a
 * schedule: it consists of a number of support points (x,f,q) defined at range
 * elements x with a value of f and a quality q. The values of the function
 * between the support points, and with that the domain of the function, are
 * determined by the {@link #interpolationMode() }. The arrays {@link #points()}
 * and {@link #values()} always must have the same length. The interpretation
 * of the function must be defined in the data model using it.
 */
public interface Function extends Data {
	/**
	 * Locations x of the support points (x,f,q). Models inheriting from the
	 * base model must override this data type with a suitable array resource.
	 */
	ArrayResource points();

	/**
	 * Values f of the support points (x,f,q). Models inheriting from the
	 * base model must override this data type with a suitable array resource.
	 */
	ArrayResource values();

	/**
	 * {@link Quality Qualities} q of the support points (x,f,q). True is the equivalent of 
	 * Quality.GOOD, false is the equivalent of Quality.BAD. If no qualities
	 * are given, all points are assumed to have GOOD quality.
	 */
	BooleanArrayResource qualities();

	/**
	 * {@link InterpolationMode} to be used for determining function values between
	 * the defined support point. The value should equal the ID of the interpolation
	 * mode.
	 */
	IntegerResource interpolationMode();

}
