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
import org.ogema.core.model.schedule.DefinitionSchedule;
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
	protected DefinitionSchedule schedule;
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
		schedule = res.addDecorator("logdata", DefinitionSchedule.class);
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
