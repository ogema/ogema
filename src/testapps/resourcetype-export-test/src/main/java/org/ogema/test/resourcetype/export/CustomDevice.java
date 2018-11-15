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
package org.ogema.test.resourcetype.export;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.units.ColourResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.Sensor;

/**
 * We export a custom resource type and create an instance of this
 * in the app's start method
 */
public interface CustomDevice extends PhysicalElement {

	TemperatureResource temperature();
	
	ColourResource color();
	
	SingleValueResource value();
	
	ResourceList<Sensor> list();
	
}
