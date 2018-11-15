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
package org.ogema.test.activation;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.sensors.ElectricCurrentSensor;
import org.ogema.model.metering.ElectricityMeter;

@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class ActivationHell implements Application {

	protected OgemaLogger logger;
	protected ApplicationManager appMan;
	protected ResourceManagement resMan;
	protected ResourceAccess resAcc;
	private ElectricityMeter meter;
	private ElectricityConnection conn;
	private ElectricCurrentSensor currSensor;
	private FloatResource value;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();

		createResources();
		appMan.createTimer(50, timerListener);

		logger.debug("{} started", getClass().getName());
	}

	private void createResources() {
		meter = resMan.createResource("activationHellMeter", ElectricityMeter.class);
		conn = (ElectricityConnection) meter.connection().create();
		currSensor = (ElectricCurrentSensor) conn.currentSensor().create();
		value = (FloatResource) currSensor.reading().create();
	}

	@Override
	public void stop(AppStopReason reason) {
		logger.debug("{} stopped", getClass().getName());
	}

	protected TimerListener timerListener = new TimerListener() {

		@Override
		public void timerElapsed(Timer timer) {
			final boolean status = (Math.random() < 0.5) ? false : true;
			final boolean recursive = (Math.random() < 0.5) ? false : true;
			final int idx = (int) (4. * Math.random());
			Resource res = meter;
			switch (idx) {
			case 0:
				res = meter;
				break;
			case 1:
				res = conn;
				break;
			case 2:
				res = currSensor;
				break;
			case 3:
				res = value;
				break;
			}
			if (status == true)
				res.activate(recursive);
			else
				res.deactivate(recursive);
		}
	};
}
