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
