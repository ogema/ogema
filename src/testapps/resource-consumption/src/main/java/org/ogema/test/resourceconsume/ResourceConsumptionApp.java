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
package org.ogema.test.resourceconsume;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.devices.whitegoods.CoolingDevice;

/**
 * Simple application that periodically writes information about the system's
 * current memory usage to the logger. Intended to be used in long-term tests of
 * a system, not for "final" solutions.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
final public class ResourceConsumptionApp implements Application {

	private ApplicationManager appMan;
	private ResourceManagement resMan;
	private ResourceAccess resAcc;
	private OgemaLogger logger;

	private static int testToDo = 0;
	private static int counter = 0;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();

		testToDo = 0;
		appMan.createTimer(1l, addAnotherResource);
		logger.debug("{} started", getClass().getName());

	}

	@Override
	public void stop(AppStopReason reason) {
		logger.debug("{} stopped", getClass().getName());
	}

	private final void addFloatResource() {
		final String name = "float" + counter;
		resMan.createResource(name, FloatResource.class);
	}

	private final void addCoolSpace() {
		final String name = "coolspace" + counter;
		resMan.createResource(name, CoolingDevice.class);
	}

	private final void addScheduleEntry() {
		final String name = "basicFloat";
		FloatResource floatRes = resAcc.getResource(name);
		if (floatRes == null) {
			floatRes = resMan.createResource(name, FloatResource.class);
		}
		final DefinitionSchedule schedule = floatRes.program();
		if (!schedule.exists())
			schedule.create();
		schedule.addValue(counter, new FloatValue((float) Math.random() * 50.f));
	}

	protected TimerListener addAnotherResource = new TimerListener() {

		@Override
		public void timerElapsed(Timer timer) {
			counter += 1;
			switch (testToDo) {
			case 0:
				addFloatResource();
				break;
			case 1:
				addCoolSpace();
				break;
			case 2:
				addScheduleEntry();
				break;
			default:
				logger.error("Invalid test with id = " + testToDo
						+ " specified: Please go to the source code and select a proper test.");
			}
			if (counter % 100 != 0)
				return;
			final Runtime runtime = Runtime.getRuntime();
			runtime.gc();
			final long free = runtime.freeMemory();
			final long time = appMan.getFrameworkTime();
			appMan.getLogger().debug("Testcase " + testToDo + " free memory: counter,mem = " + counter + " , " + free);
		}
	};
}
