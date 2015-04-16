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
/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OGEMA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.test.longtermlogging;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import static org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType.FIXED_INTERVAL;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;

/**
 * Tests the logging over a long period of time. Impatient users may want to use
 * this with sped-up simulated time.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
final public class LongtermLoggingApp implements Application {

	private ApplicationManager appMan;
	private ResourceManagement resMan;
	private ResourceAccess resAcc;
	private OgemaLogger logger;
	private int counter = 0;
	private FloatResource resource;

	private static final long LOGGING_PERIOD = 60 * 1000l;
	private static final long RELATIVE_CHECK_PERIOD = 10;
	private static final long CHECK_PERIOD = LOGGING_PERIOD * RELATIVE_CHECK_PERIOD;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();

		// create a resource 
		resource = resMan.createResource("LONGTERMTESTFLOAT", FloatResource.class);
		resource.setValue(0.f);
		resource.activate(false);

		// configure the resource for periodic logging.
		final RecordedDataConfiguration logConfig = new RecordedDataConfiguration();
		logConfig.setStorageType(FIXED_INTERVAL);
		logConfig.setFixedInterval(LOGGING_PERIOD);
		resource.getHistoricalData().setConfiguration(logConfig);

		// create the check timer.
		appMan.createTimer(CHECK_PERIOD, checkCorrectCount);

		// also change the value, in case this makes a difference
		appMan.createTimer(LOGGING_PERIOD, changeValue);

		logger.debug("{} started", getClass().getName());
	}

	@Override
	public void stop(AppStopReason reason) {
		resource.delete();
		resource = null;
		logger.debug("{} stopped", getClass().getName());
	}

	protected TimerListener checkCorrectCount = new TimerListener() {

		@Override
		public void timerElapsed(Timer timer) {
			counter += 1;
			final long existing = resource.getHistoricalData().getValues(0).size();
			final long max_expected = counter * RELATIVE_CHECK_PERIOD;
			final long min_expected = (counter - 1) * RELATIVE_CHECK_PERIOD;
			if (existing < min_expected || existing > max_expected) {
				logger.error("Wrong number of log entries at step " + counter + ": expected [" + min_expected + "; "
						+ max_expected + "]. Got " + existing);
			}
		}
	};
	protected TimerListener changeValue = new TimerListener() {

		@Override
		public void timerElapsed(Timer timer) {
			final float newValue = 1.f - resource.getValue();
			resource.setValue(newValue);
		}
	};

}
