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
package org.ogema.tools.resourcemanipulator.trashcan;

import java.util.HashMap;
import java.util.Map;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.tools.resourcemanipulator.ResourceManipulator;
import org.ogema.tools.resourcemanipulator.implementation.controllers.Controller;
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;

/**
 * Base class for the {@link ResourceManipulator} classes. Takes care of 
 * managing the available controllers and the filtering for the correct application.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */

abstract class ManipulatorBase<PATTERN extends ResourcePattern<? extends ResourceManipulatorModel>> implements ResourceManipulator, PatternListener<PATTERN> {

    protected ApplicationManager appMan;    
    protected ResourceManagement resMan;
    protected ResourceAccess resAcc;
    protected ResourcePatternAccess patAcc;    
    protected OgemaLogger logger;   
    protected String ownName;
    protected final Map<String, Controller> controllers = new HashMap<>();

    private final Class<PATTERN> patternClass;
    
    protected ManipulatorBase(Class<PATTERN> patternClass) {
        this.patternClass = patternClass;
    }

    @Override
    public void start(ApplicationManager applicationManager) {
        appMan = applicationManager;
        resMan = applicationManager.getResourceManagement();
        resAcc = applicationManager.getResourceAccess();
        patAcc = applicationManager.getResourcePatternAccess();
        logger = applicationManager.getLogger();
        ownName = applicationManager.getAppID().getIDString();
        // add a pattern demand with "device specific": No one else should modify this, except perhaps an administrator who knows what he is doing.
        patAcc.addPatternDemand(patternClass, this, AccessPriority.PRIO_DEVICESPECIFIC);
    }

    @Override
    public void stop() {
        for (Controller controller : controllers.values()) {
            controller.stop();
        }
        controllers.clear();
        patAcc.removePatternDemand(patternClass, this);
    }

    @Override
    public void patternAvailable(PATTERN pattern) {
        logger.debug("Received available callback on pattern with primary demand at "+pattern.model.getLocation());
        
        // only worry about your own configuration patterns.
        final String appName = pattern.model.application().getValue();
        if (!appName.equals(ownName)) {
            logger.debug("Name of creating application is "+appName+" which is not my own name="+ownName+". Ignoring the pattern.");
            return;
        }
        
        final String location = pattern.model.getLocation();
        if (controllers.containsKey(location)) {
            logger.warn("Warning from the resource-manipulators tool: Got a pattern available callback for a resource that already has a controller attached. Will ignore the callback.");
            return;
        }

        
        final Controller controller = createNewControllerInstance(pattern);
        controllers.put(location, controller);
        controller.start();
        logger.debug("Started new controller of type "+controller.getClass().getSimpleName()+" for pattern at "+pattern.model.getLocation());
    }

    @Override
    public void patternUnavailable(PATTERN pattern) {
        logger.debug("Received unavailable callback on pattern with primary demand at "+pattern.model.getLocation());
        
        // only worry about your own configuration patterns.
        final String appName = pattern.model.application().getValue();
        if (!appName.equals(ownName)) {
            logger.debug("Name of creating application is "+appName+" which is not my own name="+ownName+". Ignoring the pattern.");            
            return;
        }

        final String location = pattern.model.getLocation();
        final Controller controller = controllers.remove(location);
        if (controller == null) {
         logger.warn("Warning from the resource-manipulators tool: Got a pattern-unavailable callback for a pattern that has no associated controller. Ignoring the call.");
         return;
        }
        controller.stop();
        logger.debug("Stopped controller of type "+controller.getClass().getSimpleName()+" for pattern at "+pattern.model.getLocation());        
    }

    /**
     * Creates an instance of a new controller.
     */
    abstract Controller createNewControllerInstance(PATTERN pattern);
    
}
