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
