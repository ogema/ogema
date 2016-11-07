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
package org.ogema.examples;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Application.AppStopReason;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;


/**
 * A basic application that accesses OGEMA resources via a pattern demand.
 */
@Component(specVersion = "1.2", immediate=true)
@Service(Application.class)
public class PatternApp implements Application, PatternListener<CoolspacePattern> {

    private OgemaLogger logger;
    private ApplicationManager appMan;
    private ResourcePatternAccess advAcc;
    private final List<CoolspaceController> controllers = new ArrayList<>();

    /**
     * This method is called by the framework when the app is recognized as a new
     * application on the system. Typically, resource demands and timers are
     * registered here.
     *
     * @param appManager Reference to the application's ApplicationManager,
     * which is the entry point to the OGEMA system. Applications should usually
     * remember this. 
     */
    @Override
    public void start(ApplicationManager appManager) {

        // Remember framework references for later.
        this.appMan = appManager;
        this.logger = appManager.getLogger();
        this.advAcc = appManager.getResourcePatternAccess();
        /** 
         * this app wants to be informed by the framework about all fridges/freezers that posses a certain minimum configuration
         * of subfields, defined in {@link CoolspacePattern}. OGEMA is usually shipped with another application, SimulatedFreezer,
         * which creates a fitting freezer pattern, so that we should get at least one {@link #patternAvailable(CoolspacePattern) patternAvailable}
         * callback if this app is running as well.
         */
        advAcc.addPatternDemand(CoolspacePattern.class, this, AccessPriority.PRIO_LOWEST);

        logger.info("{} started", getClass().getName());
    }

    /**
     * Callback called when the application is going to be stopped.
     * de-initializes all connected fridges.
     */
    @Override
    public void stop(AppStopReason reason) {
        advAcc.removePatternDemand(CoolspacePattern.class, this);
        for (CoolspaceController controller : controllers)
            controller.stopListener();
        logger.info("{} stopped", getClass().getName());
    }

    /**
     * Called when a new object fitting the CoolspacePattern demand is found.
     * Starts a new controller for the RAD.
     */
    @Override
    public void patternAvailable(CoolspacePattern fridge) {
    	// FIXME it is bad practice to use System.out.println() in OGEMA. Instead, use logger.debug(), logger.info(), logger.warn()
    	// or logger.error().The log levels to be displayed on the console and stored in a file can be adjusted either statically
    	// (through a configuration file) or dynamically (through the OGEMA admin GUI).
    	System.out.println("A new pattern has been found in the system: " + fridge.model.getLocation());
        final CoolspaceController controller = new CoolspaceController(fridge, appMan);
        controllers.add(controller);
    }

    /**
     * Called when a pattern becomes incomplete. Stops and destroys the controller
     * for the pattern.
     */
    @Override
    public void patternUnavailable(CoolspacePattern fridge) {
    	// FIXME see above
    	System.out.println("A pattern has become unavailable: " + fridge.model.getLocation());
        Iterator<CoolspaceController> iter = controllers.iterator();
        while (iter.hasNext()) {
            final CoolspaceController controller = iter.next();
            if (controller.getDevice().equalsLocation(fridge.model)) {
                controller.stopListener();
                iter.remove();
            }
        }
    }
}
