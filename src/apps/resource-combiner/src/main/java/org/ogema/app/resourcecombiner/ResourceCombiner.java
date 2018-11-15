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
package org.ogema.app.resourcecombiner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.app.resourcecombiner.config.CombineConfigurator;
import org.ogema.app.resourcecombiner.config.CombinerDO;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;

/**
 * Combines resources via referencing.
 * 
 * @author Timo Fischer, Fraunhofer IWES TODO should read in the requests from file TODO should have a gui showing
 *         satisfied and unsatisfied requests. TODO should allow passing requests via GUI.
 */
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class ResourceCombiner implements Application, TimerListener {

	OgemaLogger m_logger;
	ApplicationManager m_appMan;
	ResourceManagement m_resMan;
	ResourceAccess m_resAcc;
	final List<CombineRequest> m_requests = new ArrayList<>();

	@Override
	public void start(ApplicationManager appManager) {
		m_appMan = appManager;
		m_logger = appManager.getLogger();
		m_resAcc = appManager.getResourceAccess();
		m_resMan = appManager.getResourceManagement();

		List<CombinerDO> list = new CombineConfigurator().readCombines();
		for (CombinerDO c : list) {
			m_requests.add(new CombineRequest(appManager, c.getSource(), c.getTarget(), c.isCreate()));
		}

		m_appMan.createTimer(3000, this);
		m_logger.debug("{} started", getClass().getName());
	}

	@Override
	public void stop(AppStopReason reason) {
		m_logger.debug("{} stopped", getClass().getName());
	}

	/**
	 * Tries to connect the yet unresolved combination requests. If all requests are fullfilled, the periodic timer
	 * calling this is terminated.
	 */
	@Override
	public void timerElapsed(Timer timer) {
		Iterator<CombineRequest> iterator = m_requests.iterator();
		while (iterator.hasNext()) {
			final CombineRequest request = iterator.next();
			if (request.connect()) {
				iterator.remove();
			}
		}
		if (m_requests.isEmpty()) {
			timer.destroy();
		}
	}
}
