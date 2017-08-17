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
