/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package ${package};

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceAccess;

// The annotations encapsule the OSGi required. They expose the service Application
// to OSGi, which the OGEMA framework uses to detect this piece of code as an
// OGEMA application.
@Component(specVersion = "1.1", immediate=true)
@Service(Application.class)
public class ${app-name} implements Application {

	protected OgemaLogger logger;
	protected ApplicationManager appMan;
	protected ResourceManagement resMan;
	protected ResourceAccess resAcc;


	/**
	 * Start method is called by the framework once this application has
	 * been discovered. From the application's perspective, this is where
	 * the program starts. Applications memorize the reference to their 
	 * ApplicationManager and usually register timers or resource
	 * demands here.
	 * 
	 * The example application registers a timer task to be periodically
	 * invoked by the framework.
	 */
	@Override
	public void start(ApplicationManager appManager) {
		// Store references to the application manager and common services for future use.
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();

		// Create a task to be invoked periodically.
		appManager.createTimer(3000l, timerTask);

		logger.debug("{} started", getClass().getName());
	}

	/**
	 * This is called when the application is stopped by the framework.
	 */
	@Override
	public void stop(AppStopReason reason) {
		logger.debug("{} stopped", getClass().getName());
	}

	/**
	 * TimerListener class that can be passed to the OGEMA framework as
	 * a piece of code to be executed periodically. Here, a counter is
	 * increased and a logger message is spammed.
	 */
	private final TimerListener timerTask = new TimerListener() {

		int countUp = 0;
		@Override
		public void timerElapsed(Timer timer) {
			countUp += 1;
			logger.info("Timer elapsed. Count="+countUp);
		}
	};
}