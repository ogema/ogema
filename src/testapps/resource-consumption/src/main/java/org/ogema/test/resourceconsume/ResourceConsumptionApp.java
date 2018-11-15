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
package org.ogema.test.resourceconsume;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.schedule.AbsoluteSchedule;
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
		final AbsoluteSchedule schedule = floatRes.program();
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
