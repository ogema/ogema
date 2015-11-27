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
package org.ogema.tools.resourcemanipulator.implementation.controllers;

import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.tools.resourcemanipulator.implementation.CountDownTimer;
import org.ogema.tools.resourcemanipulator.model.ScheduleSumModel;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.implementations.FloatTreeTimeSeries;

/**
 * Controls a single schedule summation rule. As in most rules it is assumed
 * that the configuration does not change during runtime of this.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ScheduleSumController implements Controller, ResourceStructureListener, ResourceValueListener<Schedule>,
		TimerListener {

	private final ScheduleSumModel m_config;
	private final CountDownTimer m_timer;
	private final OgemaLogger m_logger;

	public ScheduleSumController(ApplicationManager appMan, ScheduleSumModel configuration) {
		m_config = configuration;
		long delay = configuration.delay().getValue();
		if (delay <= 0) {
			delay = 1;
		}
		m_timer = new CountDownTimer(appMan, delay, this);
		m_logger = appMan.getLogger();
	}

	@Override
	public void start() {
		for (Schedule input : m_config.inputs().getAllElements()) {
			input.addStructureListener(this);
			input.addValueListener(this);
		}
		evaluate();
	}

	@Override
	public void stop() {
		for (Schedule input : m_config.inputs().getAllElements()) {
			input.removeStructureListener(this);
			input.removeValueListener(this);
		}
	}

	/**
	 * Evaluates the mapping and writes the result into the target resource.
	 */
	final void evaluate() {

		List<Schedule> inputs = m_config.inputs().getAllElements();
		AbsoluteSchedule output = m_config.resultBase().program();
		for (Schedule schedule : inputs) {
			if (schedule.getLocation().equals(output.getLocation())) {
				String msg = this.getClass().getSimpleName() + ": input schedule "
						+ "is referencing output schedule! Aborting computation ..."
						+ "location of referenced schedule -> " + schedule.getLocation();
				m_logger.error(msg);
				throw new IllegalArgumentException(msg);
			}
		}

		// perform summation over all active inputs.
		FloatTimeSeries sum = null;
		for (Schedule schedule : inputs) {
			if (!schedule.isActive()) {
				continue;
			}
			final FloatTimeSeries addend = new FloatTreeTimeSeries();
			addend.read(schedule);
			if (sum == null) {
				sum = addend;
			}
			else {
				sum.add(addend);
			}
		}

		final boolean emptySum = (sum == null);

		// special treatment for deprecated deactivateEmptySum handle.
		if (m_config.deactivateEmptySum().getValue()) {
			if (emptySum) {
				output.deactivate(false);
			}
		}

		if (emptySum) {
			sum = new FloatTreeTimeSeries();
		}
		sum.write(output);

		if (m_config.activationControl().getValue()) {
			if (emptySum) {
				output.deactivate(false);
			}
			else {
				output.activate(false);
			}
		}
	}

	@Override
	@SuppressWarnings("fallthrough")
	public void resourceStructureChanged(ResourceStructureEvent event) {
		switch (event.getType()) {
		case RESOURCE_ACTIVATED:
		case RESOURCE_DEACTIVATED:
		case RESOURCE_CREATED:
		case RESOURCE_DELETED:
			m_timer.start();
		case REFERENCE_ADDED:
		case REFERENCE_REMOVED:
			// no need to do anything in case of references being added or removed.
		}
	}

	@Override
	public void resourceChanged(Schedule resource) {
		m_timer.start();
	}

	@Override
	public void timerElapsed(Timer timer) {
		evaluate();
	}

}
