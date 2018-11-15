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

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.generators.ElectricHeater;

public class NonDefaultConstructorPattern extends ResourcePattern<ElectricHeater> {

	public final BooleanResource sc = model.onOffSwitch().stateControl();

	public final FloatResource readng = model.location().room().temperatureSensor().reading();

	public final int i;

	public NonDefaultConstructorPattern(ElectricHeater match, int somefunnynonsense, BooleanResource stateFeedback) {
		super(match);
		if (stateFeedback.exists() && !model.onOffSwitch().stateFeedback().exists())
			model.onOffSwitch().stateFeedback().setAsReference(stateFeedback);
		this.i = somefunnynonsense;
	}

	@Override
	public boolean accept() {
		return i < 9;
	}

}
