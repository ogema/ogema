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
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.pattern.ContextSensitivePattern;
import org.ogema.model.devices.generators.ElectricHeater;

public class ContainerPattern extends ContextSensitivePattern<ElectricHeater, Container> {

	private int id = -1;

	public final BooleanResource sc = model.onOffSwitch().stateControl();

	public final FloatResource readng = model.location().room().temperatureSensor().reading();

	public ContainerPattern(Resource match) {
		super(match);
	}

	@Override
	public boolean accept() {
		if (id < 0)
			id = context.getNextId();
		return id < 5;
	}

	@Override
	public void init() {
		model.onOffSwitch().stateFeedback().create();
		model.onOffSwitch().stateFeedback().setValue(context.getFeedbackState());
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "ContainerPattern " + id;
	}

}
