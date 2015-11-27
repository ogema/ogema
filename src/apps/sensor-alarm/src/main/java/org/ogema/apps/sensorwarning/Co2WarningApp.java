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
package org.ogema.apps.sensorwarning;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.apps.sensorwarning.pattern.ConfiguredSensorPattern;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;

/**
 * Application that automatically triggers an on/off alarm switch when the 
 reading alarm limits of a configured CO2 sensor are violated.
 * Configuration is done via the configuration model provided with this
 * application.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class Co2WarningApp implements Application, PatternListener<ConfiguredSensorPattern> {

    private OgemaLogger logger;
    private ApplicationManager appMan;
    private ResourceAccess resAcc;
    private ResourcePatternAccess patAcc;
    private final Map<ConfiguredSensorPattern, AlarmController> controllers = new HashMap<>();

    /**
     * This is called by the framework when this is recognized as a new
     * application on the system. Typically, resource demands and timers are
     * registered here.
     *
     * @param appManager Reference to the application's ApplicationManager,
     * which is the entry point to the OGEMA system. Applications should usually
     * remember this.
     */
    @Override
    public void start(ApplicationManager appManager) {

        appMan = appManager;
        logger = appManager.getLogger();
        resAcc = appManager.getResourceAccess();
        patAcc = appManager.getResourcePatternAccess();

        patAcc.addPatternDemand(ConfiguredSensorPattern.class, this, AccessPriority.PRIO_DEVICESPECIFIC);

        logger.info("{} started", getClass().getName());
    }

    /**
     * Callback called when the application is going to be stopped.
     * de-initializes all connected fridges.
     */
    @Override
    public void stop(AppStopReason reason) {
        patAcc.removePatternDemand(ConfiguredSensorPattern.class, this);
        for (AlarmController controller : controllers.values())
            controller.stop();
        controllers.clear();
        logger.info("{} stopped", getClass().getName());
    }

    @Override
    public void patternAvailable(ConfiguredSensorPattern pattern) {
        if (controllers.containsKey(pattern)) {
            logger.warn("Caught pattern available for a pattern that is already controlled at "+pattern.model.getLocation()+". Ignoring the callback.");
            return;
        }
        final AlarmController controller = new AlarmController(pattern, appMan);
        controller.start();
        controllers.put(pattern, controller);
    }

    @Override
    public void patternUnavailable(ConfiguredSensorPattern pattern) {
        final AlarmController controller = controllers.remove(pattern);        
        if (controller == null) {
            logger.warn("Caught pattern unavailable for a pattern that is not already controlled at "+pattern.model.getLocation()+". Ignoring the callback.");
            return;
        }
        controller.stop();
    }
}
