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
package org.ogema.test.memorylog;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;

/**
 * Simple application that periodically writes information about the system's
 * current memory usage to the logger. Intended to be used in long-term tests of
 * a system, not for "final" solutions.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
final public class MemoryLogger implements Application {

	private ApplicationManager appMan;
	private OgemaLogger logger;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		appMan.createTimer(5 * 60 * 1000l, statusWriter);
		logger.debug("{} started", getClass().getName());

	}

	@Override
	public void stop(AppStopReason reason) {
		logger.debug("{} stopped", getClass().getName());

	}

	protected TimerListener statusWriter = new TimerListener() {

		@Override
		public void timerElapsed(Timer timer) {
			final Runtime runtime = Runtime.getRuntime();
			List<Resource> allResources = appMan.getResourceAccess().getResources(null);
			runtime.gc();
			final long free = runtime.freeMemory();
			final long time = appMan.getFrameworkTime();
			appMan.getLogger()
					.debug(
							"time = " + time + " , free mem = " + free + " bytes - count of resources = "
									+ allResources.size());
		}
	};
}
