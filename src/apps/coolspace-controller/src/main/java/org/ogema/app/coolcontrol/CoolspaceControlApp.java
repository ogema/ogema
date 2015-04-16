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
package org.ogema.app.coolcontrol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;

/**
 * A very simple controller which attempts to keep the temperature of all
 * fridges found in the system into their specified temperature range.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class CoolspaceControlApp implements Application, PatternListener<CoolspacePattern> {

    private OgemaLogger m_logger;
    private ApplicationManager m_appMan;
    private ResourcePatternAccess m_advAcc;
    private final List<CoolspaceController> m_controllers = new ArrayList<>();

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

        // Remember framework references for later.
        m_appMan = appManager;
        m_logger = appManager.getLogger();

		// Register a resource demand on cooling devices. Currently using the experimental implementation of advanced
        // access.
        m_advAcc = appManager.getResourcePatternAccess();
        m_advAcc.addPatternDemand(CoolspacePattern.class, this, AccessPriority.PRIO_LOWEST);

        m_logger.info("{} started", getClass().getName());
    }

    /**
     * Callback called when the application is going to be stopped.
     * de-initializes all connected fridges.
     */
    @Override
    public void stop(AppStopReason reason) {
        m_advAcc.removePatternDemand(CoolspacePattern.class, this);
        for (CoolspaceController controller : m_controllers)
            controller.stopListener();
        m_logger.info("{} stopped", getClass().getName());
    }

    /**
     * Called when a new object fitting the CoolspacePattern demand is found.
     * Starts a new controller for the RAD.
     */
    @Override
    public void patternAvailable(CoolspacePattern fridge) {
        final CoolspaceController controller = new CoolspaceController(fridge, m_appMan);
        m_controllers.add(controller);
    }

    /**
     * Called when a RAD became incomplete. Stops and destroys the controller
     * for the RAD.
     */
    @Override
    public void patternUnavailable(CoolspacePattern fridge) {
        Iterator<CoolspaceController> iter = m_controllers.iterator();
        while (iter.hasNext()) {
            final CoolspaceController controller = iter.next();
            if (controller.getDevice().equalsLocation(fridge.model)) {
                controller.stopListener();
                iter.remove();
            }
        }
    }
}
