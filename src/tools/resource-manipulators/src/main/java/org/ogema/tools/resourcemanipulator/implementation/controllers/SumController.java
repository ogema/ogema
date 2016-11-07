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

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.tools.resourcemanipulator.model.SumModel;
import org.ogema.tools.resourcemanipulator.timer.CountDownTimer;

/**
 * Controls a single value summation rule. As in most rules it is assumed
 * that the configuration does not change during runtime of this.
 *
 * @author Marco Postigo Perez, Fraunhofer IWES
 */
public class SumController implements Controller, ResourceStructureListener,
		ResourceValueListener<SingleValueResource>, TimerListener {

	private final SumModel m_config;
	private final CountDownTimer m_timer;
	private final OgemaLogger m_logger;
	private volatile boolean active = false;

	public SumController(ApplicationManager appMan, SumModel configuration) {
		m_config = configuration;
		m_timer = new CountDownTimer(appMan, configuration.delay().getValue(), this);
		m_logger = appMan.getLogger();
	}

	@Override
	public void start() {
		for (SingleValueResource input : m_config.inputs().getAllElements()) {
			input.addStructureListener(this);
			input.addValueListener(this);
		}
		active = true;
		evaluate();
	}

	@Override
	public void stop() {
		active = false;
		m_timer.stop();
		for (SingleValueResource input : m_config.inputs().getAllElements()) {
			input.removeStructureListener(this);
			input.removeValueListener(this);
		}
	}

	/**
	 * Evaluates the mapping and writes the result into the target resource.
	 */
	final void evaluate() {

		// perform summation over all active inputs.
		Class<? extends Resource> clazz = m_config.resultBase().getClass();
		if (FloatResource.class.isAssignableFrom(clazz)) {
			Float sum = 0f;
			boolean isEmpty = true;
			for (SingleValueResource value : m_config.inputs().getAllElements()) {
				if (!value.isActive()) {
					continue;
				}

				if (value instanceof FloatResource) {
					sum += ((FloatResource) value).getValue();
					isEmpty = false;
				}
				else if (value instanceof IntegerResource) {
					sum += ((IntegerResource) value).getValue();
					isEmpty = false;
				}
				else if (value instanceof TimeResource) {
					sum += ((TimeResource) value).getValue();
					isEmpty = false;
				}
				else {
					m_logger.warn("Invalid input - make sum for " + value.getClass().getName()
							+ " while making sum for " + clazz.getName());
				}
			}

			// capture special case of empty sum.
			if (isEmpty) {
				sum = Float.NaN;
				if (m_config.deactivateEmptySum().getValue()) {
					m_config.resultBase().deactivate(false);
				}
			}

			if (FloatResource.class.isAssignableFrom(clazz)) {
				((FloatResource) m_config.resultBase()).setValue(sum);
			}
		}
		else if (IntegerResource.class.isAssignableFrom(clazz) || TimeResource.class.isAssignableFrom(clazz)) {
			Long sum = 0l;
			boolean isEmpty = true;
			for (SingleValueResource value : m_config.inputs().getAllElements()) {
				if (!value.isActive()) {
					continue;
				}

				if (value instanceof FloatResource) {
					m_logger.warn("Float resource found in SumManipulator for result type " + clazz.getSimpleName()
							+ " -> rounding float and adding it to result ...");
					sum += Math.round(((FloatResource) value).getValue());
					isEmpty = false;
				}
				else if (value instanceof IntegerResource) {
					sum += ((IntegerResource) value).getValue();
					isEmpty = false;
				}
				else if (value instanceof TimeResource) {
					sum += ((TimeResource) value).getValue();
					isEmpty = false;
				}
				else {
					m_logger.warn("Invalid input - make sum for " + value.getClass().getName()
							+ " while making sum for " + clazz.getName());
				}
			}

			// capture special case of empty sum.
			if (isEmpty) {
				if (m_config.deactivateEmptySum().getValue()) {
					m_config.resultBase().deactivate(false);
				}
			}

			if (IntegerResource.class.isAssignableFrom(clazz)) {
				if (!isEmpty) {
					if (sum > Integer.MAX_VALUE) {
						m_logger.warn("Integer overflow! Setting sum to Integer.MAX_VALUE ...");
						((IntegerResource) m_config.resultBase()).setValue(Integer.MAX_VALUE);
					}
					else {
						((IntegerResource) m_config.resultBase()).setValue(sum.intValue());
					}
				}
			}
			else {
				if (!isEmpty) {
					((TimeResource) m_config.resultBase()).setValue(sum);
				}
			}
		}
		else {
			// String or Boolean -> don't make sum for that resource types ...
			String msg = "SumManipulator result type is " + clazz.getName() + " which is " + "not supported ...";
			m_logger.error(msg);
			throw new IllegalArgumentException(msg);
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
		case SUBRESOURCE_ADDED:
		case SUBRESOURCE_REMOVED:
		default:
			// no need to do anything in case
		}
	}

	@Override
	public void resourceChanged(SingleValueResource resource) {
		m_timer.start();
	}

	@Override
	public void timerElapsed(Timer timer) {
		if (!active)
			return;
		evaluate();
	}

}
