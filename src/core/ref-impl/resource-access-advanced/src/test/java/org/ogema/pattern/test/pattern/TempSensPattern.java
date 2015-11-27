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
package org.ogema.pattern.test.pattern;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.sensors.TemperatureSensor;

public class TempSensPattern extends ResourcePattern<TemperatureSensor> {

	public TempSensPattern(Resource match) {
		super(match);
	}

	@ValueChangedListener(activate = true)
	TemperatureResource reading = model.reading();

	StringResource res = model.name();

	@Existence(required = CreateMode.OPTIONAL)
	TemperatureResource maxTemp = model.ratedValues().upperLimit();

	@Override
	public boolean accept() {
		if (maxTemp.isActive() && maxTemp.getValue() < reading.getValue())
			return false;
		else if (reading.getCelsius() >= 20)
			return true;
		else
			return false;
	}

}
