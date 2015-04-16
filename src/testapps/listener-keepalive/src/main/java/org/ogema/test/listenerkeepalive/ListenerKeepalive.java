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
package org.ogema.test.listenerkeepalive;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceValueListener;

/**
 * Simple application that periodically writes information about the system's
 * current memory usage to the logger. Intended to be used in long-term tests of
 * a system, not for "final" solutions.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
final public class ListenerKeepalive implements Application, TimerListener, ResourceValueListener {

	private ApplicationManager appMan;
	private OgemaLogger m_logger;
	private int m_setCounter = 0;
	private int m_reveiveCounter = 0;
	private IntegerResource m_resource;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.m_logger = appManager.getLogger();

		final ResourceManagement resMan = appMan.getResourceManagement();
		final String name = resMan.getUniqueResourceName("listenerCounter");
		m_resource = resMan.createResource(name, IntegerResource.class);
		m_resource.setValue(0);
		m_resource.activate(false);
		appMan.createTimer(5 * 60 * 1000l, this);
		m_resource.addValueListener(this);
		m_logger.debug("{} started", getClass().getName());
	}

	@Override
	public void stop(AppStopReason reason) {
		m_logger.debug("{} stopped", getClass().getName());
		m_resource.removeValueListener(this);
		m_resource.delete();
	}

	@Override
	public void timerElapsed(Timer timer) {
		if (m_reveiveCounter != m_setCounter) {
			m_logger.error("Receive counter is not equal to set counter ( " + m_reveiveCounter + " , " + m_setCounter
					+ " ). Callback for change was either not received or encountered an error.");
		}
		m_setCounter += 1;
		m_resource.setValue(m_setCounter);
	}

	@Override
	public void resourceChanged(Resource resource) {
		if (!resource.equalsPath(m_resource)) {
			throw new RuntimeException("Received callback for wrong resource at " + resource.getPath());
		}
		m_reveiveCounter = m_resource.getValue();
		m_logger.info("Counter is " + m_reveiveCounter);
	}

}
