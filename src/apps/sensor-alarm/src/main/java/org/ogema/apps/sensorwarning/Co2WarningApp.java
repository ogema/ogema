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
