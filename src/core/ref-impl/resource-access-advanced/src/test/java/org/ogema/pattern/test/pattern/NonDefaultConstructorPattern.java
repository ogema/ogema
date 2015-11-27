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
