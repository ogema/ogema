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
