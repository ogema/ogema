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
package org.ogema.test.longtermlogging;

import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import static org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType.FIXED_INTERVAL;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.model.sensors.PowerSensor;
import org.ogema.tools.resourcemanipulator.ResourceManipulator;
import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;
import org.ogema.tools.resourcemanipulator.configurations.ProgramEnforcer;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.implementations.FloatTreeTimeSeries;

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
	private int checkCounter = 0;
        private int writeCounter = 0;
	private FloatResource resource;
        private PowerSensor powerSensor;
        
	private static final long LOGGING_PERIOD = 60 * 1000l;
	private static final long RELATIVE_CHECK_PERIOD = 10;
	private static final long CHECK_PERIOD = LOGGING_PERIOD * RELATIVE_CHECK_PERIOD;
        private static final long UPDATE_PROGRAM_PERIOD = 10 * RELATIVE_CHECK_PERIOD;
        
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
		appMan.createTimer(CHECK_PERIOD, checkLogsListener);

		// also change the value, in case this makes a difference
		appMan.createTimer(LOGGING_PERIOD, changeValueListener);

                /*
                Creation of a resource manipulator.
                */
                ResourceManipulator resTool = new ResourceManipulatorImpl(appManager);
                resTool.start();
                ProgramEnforcer enforcer = resTool.createConfiguration(ProgramEnforcer.class);
                powerSensor = resMan.createResource("LOGGING_TEST_POWERSENSOR", PowerSensor.class);
                powerSensor.reading().create().activate(false);
                powerSensor.reading().program().create();
                enforcer.enforceProgram(powerSensor.reading(), 10000l);
                enforcer.commit();
                
                Timer programTimer = appMan.createTimer(UPDATE_PROGRAM_PERIOD, programCreationTimer);
                programCreationTimer.timerElapsed(programTimer);
                appMan.createTimer(CHECK_PERIOD, enforcerCheckListener);

                powerSensor.activate(true);
		powerSensor.reading().getHistoricalData().setConfiguration(logConfig);
                
		logger.debug("{} started", getClass().getName());
	}

        private float getManualLoggingValue(int i) {
            final int imod = i % 200;
            if (imod<50) return 0.02f*imod;
            if (imod<100) return 1.0f;
            if (imod<150) return 1.0f - 0.02f*(imod-100);
            return 0.f;
        }        
        
        private float getEnforcerValue(long t) {
            return (float) Math.sin(2. * Math.PI * 1.e-7 * t);
        }
        
	@Override
	public void stop(AppStopReason reason) {
		resource.delete();
		resource = null;
		logger.debug("{} stopped", getClass().getName());
	}

	protected TimerListener checkLogsListener = new TimerListener() {

		@Override
		public void timerElapsed(Timer timer) {
			checkCounter += 1;
			final long existing = resource.getHistoricalData().getValues(0).size();
			final long max_expected = checkCounter * RELATIVE_CHECK_PERIOD;
			final long min_expected = (checkCounter - 1) * RELATIVE_CHECK_PERIOD;
			if (existing < min_expected || existing > max_expected) {
				logger.error("Wrong number of log entries at step " + checkCounter + ": expected [" + min_expected + "; "
						+ max_expected + "]. Got " + existing);
			}
                        
                        // also check the entries
                        final List<SampledValue> values = resource.getHistoricalData().getValues(0);                        
                        for (int i=0; i<values.size(); ++i) {
                            final float expectedValue = getManualLoggingValue(i);
                            final float value = values.get(i).getValue().getFloatValue();
                            if (Math.abs(expectedValue-value) > 0.01f) {
                                logger.error("Wrong log entry for "+i+"-th entry: Expected "+expectedValue+" but got "+value);
                            }                            
                        }
		}
	};
                
	protected TimerListener changeValueListener = new TimerListener() {

		@Override
		public void timerElapsed(Timer timer) {                       
			final float newValue = getManualLoggingValue(writeCounter++);
			resource.setValue(newValue);                        
		}
	};
        
        private TimerListener programCreationTimer = new TimerListener() {

            @Override
            public void timerElapsed(Timer timer) {
                final long tNow = timer.getExecutionTime();
                FloatTimeSeries program = new FloatTreeTimeSeries();
                program.setInterpolationMode(InterpolationMode.LINEAR);
                for(long t=tNow; t<tNow + 24*3600*1000; t+=5*60*1000) {
                    final SampledValue entry = new SampledValue(new FloatValue(getEnforcerValue(t)), t, Quality.GOOD);
                    program.addValue(entry);
                }
                program.write(powerSensor.reading().program());
                powerSensor.reading().activate(false);
            }
        };

        private TimerListener enforcerCheckListener = new TimerListener() {

            @Override
            public void timerElapsed(Timer timer) {
                if (!powerSensor.isActive()) {
                    logger.warn("Power sensor is not active");
                }
                final long t = timer.getExecutionTime();
                final float value = powerSensor.reading().getValue();
                final float programValue = powerSensor.reading().program().getValue(t).getValue().getFloatValue();
                if (Math.abs(value-programValue) > 0.1f) {
                                logger.error("Wrong enforcer entry for time "+t+": Expected "+programValue+" but got "+value);
                }
            }
        };
}
