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
		if (feedback != null)
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
