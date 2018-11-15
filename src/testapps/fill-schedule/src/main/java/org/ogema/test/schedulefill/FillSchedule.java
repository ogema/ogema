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
package org.ogema.test.schedulefill;

import java.io.File;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceManagement;

/**
 * 
 * @author jlapp
 */
@Component(specVersion = "1.1")
@Service(Application.class)
public class FillSchedule implements Application {

	protected OgemaLogger logger;
	protected AbsoluteSchedule schedule;
	protected FloatArrayResource farr;
	protected ApplicationManager appMan;
	int i = 0;

	protected TimerListener timerListener = new TimerListener() {

		@Override
		public void timerElapsed(Timer timer) {

			// print the info.
			long size = 0;
			for (int i = 0; i < 50; ++i) {
				File file = new File("./database/resData" + i);
				size += file.length();
			}
			logger.info("i, size = " + i + " , " + size);

			// do stuff
			// final long t = appMan.getFrameworkTime();
			// schedule.addValue(t, new FloatValue(1.0f));

			farr.setValues(new float[10000 * i]);
			i += 1;
		}
	};

	@Override
	public void start(ApplicationManager appManager) {
		appMan = appManager;

		logger = appManager.getLogger();

		ResourceManagement resMan = appManager.getResourceManagement();

		FloatResource res = resMan.createResource("dummyvalue", FloatResource.class);
		farr = resMan.createResource("dummyarray", FloatArrayResource.class);
		schedule = res.addDecorator("logdata", AbsoluteSchedule.class);
		schedule.activate(false);

		appManager.createTimer(1000, timerListener);
		logger.debug("{} started", getClass().getName());
	}

	@Override
	public void stop(AppStopReason reason) {
		logger.info("Bye OGEMA!");
		logger.debug("{} stopped", getClass().getName());
	}
}
