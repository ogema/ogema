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
package org.ogema.app.securityconsumer;

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceValueListener;

public class HMPowerboxTest implements ResourceValueListener<BooleanResource> {

	private BooleanResource feedback;
	private BooleanResource control;

	public HMPowerboxTest(ResourceAccess ra) {
		feedback = ra.getResource("HM_ES_PMSw1_Pl_PowerMeter_274155/onOffSwitch/stateFeedback");
		control = ra.getResource("HM_ES_PMSw1_Pl_PowerMeter_274155/onOffSwitch/stateControl");
		feedback.addValueListener(this);
	}

	@Override
	public void resourceChanged(BooleanResource resource) {
		long timeStamp = System.currentTimeMillis();
		System.out.print("event: ");
		System.out.println(timeStamp);
		boolean value = resource.getValue();
		control.setValue(!value);

	}

}
