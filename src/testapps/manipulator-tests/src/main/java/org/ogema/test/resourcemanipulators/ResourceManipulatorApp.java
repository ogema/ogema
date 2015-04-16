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
package org.ogema.test.resourcemanipulators;

import java.util.ArrayList;
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
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.test.resourcemanipulators.pattern.ProgrammedFloatPattern;
import org.ogema.tools.resourcemanipulator.ResourceManipulator;
import org.ogema.tools.resourcemanipulator.configurations.ProgramEnforcer;
import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;

/**
 * Tests for the resource-manipulator tools.
 *
 * TODO This does not need to be a longterm-test. Tests for the
 * resource-manipulator should instead be added to the Pax Exam tests of its
 * bundle.
 *
 *  TODO add a check that tests both, linear interpolation and step-interpolations (which work differently)
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
final public class ResourceManipulatorApp implements Application {

	private ApplicationManager appMan;
	private ResourceManagement resMan;
	private ResourceAccess resAcc;
	private ResourcePatternAccess patAcc;

	private OgemaLogger logger;

	private ResourceManipulator manipulatorTool;
	private ProgrammedFloatPattern floatPattern;

	private Timer timer;
	public static final long TIMESTEP = 30000l;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();
		this.patAcc = appManager.getResourcePatternAccess();

		// Create and start a new programEnforcer
		manipulatorTool = new ResourceManipulatorImpl(appManager);
		manipulatorTool.start();

		// create a float resource with a program and set it to auto-program
		floatPattern = createFloatPattern();
		ProgramEnforcer config = manipulatorTool.createConfiguration(ProgramEnforcer.class);
		config.enforceProgram(floatPattern.value, -1);
		config.commit();
		patAcc.activatePattern(floatPattern);

		// create a timer checking the validity of the entries.
		timer = appManager.createTimer(TIMESTEP, floatListener);

		logger.debug("{} started", getClass().getName());
	}

	@Override
	public void stop(AppStopReason reason) {
		timer.destroy();
		manipulatorTool.deleteAllConfigurations();
		manipulatorTool.stop();
		logger.debug("{} stopped", getClass().getName());
	}

	private TimerListener floatListener = new TimerListener() {

		@Override
		public void timerElapsed(Timer timer) {
			final long t = timer.getExecutionTime();
			final SampledValue currentValue = floatPattern.program.getValue(t);
			final float required = currentValue.getValue().getFloatValue();
			final float actual = floatPattern.value.getValue();
			if (actual == required) {
				logger.debug("Correct float value programmed: actual=required=" + actual);
			}
			else {
				logger.error("Error checking the programmed value: actual=" + actual + " does not equal required="
						+ required);
			}
		}
	};

	private ProgrammedFloatPattern createFloatPattern() {
        final ProgrammedFloatPattern result = patAcc.createResource("RESMANTEST_FLOATPATTERN", ProgrammedFloatPattern.class);
        final DefinitionSchedule program = result.program;
        
        // create the program pattern as a series of random steps.
        final int N_ENTRIES = 10000;
        final List<SampledValue> entries = new ArrayList<>(N_ENTRIES);
        long t = appMan.getFrameworkTime();
        for (int i=0; i<N_ENTRIES; ++i, t+=TIMESTEP) {
            final SampledValue entry = new SampledValue(new FloatValue(200.f* (float) Math.random()-100.f), t, Quality.GOOD);
            entries.add(entry);
        }
        program.addValues(entries, appMan.getFrameworkTime());
        program.setInterpolationMode(InterpolationMode.STEPS);        
        
        return result;
    }
}
