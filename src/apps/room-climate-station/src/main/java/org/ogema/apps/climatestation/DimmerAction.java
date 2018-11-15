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
package org.ogema.apps.climatestation;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.model.devices.buildingtechnology.ElectricDimmer;

@SuppressWarnings("deprecation")
public class DimmerAction implements org.ogema.core.resourcemanager.ResourceListener {

	FloatResource control, feedback;

	boolean stop;
	Thread dimmerThread;

	public DimmerAction() {
		System.out.println(dimm);
		stop = true;
		dimmerThread = new Thread(dimm);
		dimmerThread.setName("DimmerAction");
		dimmerThread.start();
	}

    @SuppressWarnings("deprecation")
	public void setSource(BooleanResource swtch) {
		swtch.addResourceListener(this, true);
	}

	public void setTarget(ElectricDimmer dimmer) {
		control = dimmer.setting().stateControl().create();
		control.activate(false);
		feedback = dimmer.setting().stateFeedback().create();
		feedback.activate(false);
	}

	@Override
	public void resourceChanged(Resource resource) {
		// reaction only when change from false tostrue
		if (((BooleanResource) resource).getValue()) {
			System.out.println("Start Dimmer");
			stop = false;
			synchronized (dimm) {
				dimm.notify();
			}
		}
		else {
			stop = true;
			System.out.println("Stop Dimmer");
		}
	}

	Runnable dimm = new Runnable() {

		@Override
		public void run() {
			System.out.println(this);
			while (true) {
				if (stop) {
					System.out.println("Dimmer stopped");
					synchronized (this) {
						try {
							wait();
						} catch (InterruptedException e) {
						}
					}
					System.out.println("Dimmer started");
				}
				float value = control.getValue();
				if (value >= 1.0f)
					value = -0.1f;
				value += 0.1f;
				control.setValue(value);
				feedback.setValue(value);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	};
}
