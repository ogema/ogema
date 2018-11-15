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
package org.ogema.app.simulation.freezer;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.model.devices.whitegoods.CoolingDevice;

/**
 * OGEMA application creating a simulated fridge as an OGEMA resource and
 * simulating it as if it was a (semi-realistic) real fridge. The main class
 * only creates the simulation objects, which then do all the action and
 * interaction with OGEMA.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class FreezerSimulationApp implements Application {

	private OgemaLogger m_log;
	private ApplicationManager m_appMan;
	private ResourcePatternAccess m_advAcc;

	private FreezerSimulation m_freezer;

	/**
	 * This is the entry point of any OGEMA application. Start() is called by
	 * the OGEMA framework.
	 *
	 * @param appManager reference to the application's application manager. The
	 * appManager as the application's entry point to OGEMA.
	 */
	@Override
	public void start(ApplicationManager appManager) {

		m_appMan = appManager;
		m_log = appManager.getLogger();
		m_advAcc = appManager.getResourcePatternAccess();

		m_log.debug("Creating a simulation object.");
		final String name = m_appMan.getResourceManagement().getUniqueResourceName("SimulatedFreezer");
		final CoolingDevice existingFreezer = m_appMan.getResourceAccess().getResource(name);
		if (existingFreezer != null) { // resource already exists (appears in case of non-clean start)
			final FreezerPattern freezer = new FreezerPattern(existingFreezer);
			m_freezer = new FreezerSimulation(m_appMan, freezer);
			m_freezer.init();
			m_log.debug("{} started with previously-existing device", getClass().getName());
		}
		else { // resource does not exist yet: Create it.
			final FreezerPattern freezer = m_advAcc.createResource(name, FreezerPattern.class);
			if (freezer != null) {
				freezer.init();
				m_advAcc.activatePattern(freezer);

				m_freezer = new FreezerSimulation(m_appMan, freezer);
				m_freezer.init();
				m_log.debug("{} started with newly-created device", getClass().getName());
			}
			else {
				m_log.warn("{} - unable to create simulated freezer!", getClass().getName());
			}
		}
	}

	@Override
	public void stop(AppStopReason reason) {
		if (m_freezer != null) {
			m_freezer.destroy();
			m_advAcc.deactivatePattern(m_freezer.getDevice());
			m_freezer.getDevice().model.delete();
		}
		m_log.debug(this + " is stopped for reason " + reason);
	}

	public long getFrameworkTime() {
		return m_appMan.getFrameworkTime();
	}

}
