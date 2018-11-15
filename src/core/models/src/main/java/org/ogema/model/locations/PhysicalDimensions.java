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
package org.ogema.model.locations;

import org.ogema.core.model.units.LengthResource;
import org.ogema.model.prototypes.Data;

/**
 * Size of a physical object. The three room dimensions depend on the context.
 * For devices, length width and height should usually refer to a front-view on
 * the device.
 */
public interface PhysicalDimensions extends Data {
	/**
	 * Length or depth of the physical object       
	 */
	LengthResource length();

	/**
	 * Width of the physical object.
	 */
	LengthResource width();

	/**
	 * Height of the physical object.
	 */
	LengthResource height();
}
