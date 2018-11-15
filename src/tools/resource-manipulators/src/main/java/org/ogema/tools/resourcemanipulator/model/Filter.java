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

import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.prototypes.Data;

public interface Filter extends Data {

	/**
	 * Specifies what to do if filter is not satisfied
	 * <ul>
	 * 	  <li>0: deactivate target resource</li>
	 *    <li>1: keep current value</li>
	 * </ul>
	 */
	IntegerResource mode();

}
