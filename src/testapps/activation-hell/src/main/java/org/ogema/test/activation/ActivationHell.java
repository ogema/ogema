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
