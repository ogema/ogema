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
package org.ogema.tools.resourcemanipulator.implementation.controllers;

import java.util.List;

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
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;
import org.ogema.tools.resourcemanipulator.configurations.Sum;
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;
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

	private final ApplicationManager appMan;
	private final SumModel m_config;
	private final CountDownTimer m_timer;
	private volatile Long lastExecTime;
	private final OgemaLogger m_logger;
	private volatile boolean active = false;

	public SumController(ApplicationManager appMan, SumModel configuration) {
		this.appMan = appMan; 
		m_config = configuration;
		final long delay = configuration.delay().isActive() ? configuration.delay().getValue() : 0;
		m_timer = delay > 0 ? new CountDownTimer(appMan, configuration.delay().getValue(), this) : null;
		m_logger = appMan.getLogger();
	}

	@Override
	public void start() {
        boolean callOnEveryUpdate = true;
        if (m_config.callOnEveryUpdate().isActive()) {
            callOnEveryUpdate = m_config.callOnEveryUpdate().getValue();
        }
		for (SingleValueResource input : m_config.inputs().getAllElements()) {
			input.addStructureListener(this);
			input.addValueListener(this, callOnEveryUpdate);
		}
		active = true;
		evaluate();
	}

	@Override
	public void stop() {
		active = false;
		if (m_timer != null)
			m_timer.stop();
		for (SingleValueResource input : m_config.inputs().getAllElements()) {
			input.removeStructureListener(this);
			input.removeValueListener(this);
		}
	}

	@Override
	public Class<? extends ManipulatorConfiguration> getType() {
		return Sum.class;
	}

	/**
	 * Evaluates the mapping and writes the result into the target resource.
	 */
	final void evaluate() {

		// perform summation over all active inputs.
		Class<? extends Resource> clazz = m_config.resultBase().getResourceType();
		final float[] factors = m_config.factors().isActive() ? m_config.factors().getValues() : null;
		final float[] offsets = m_config.offsets().isActive() ? m_config.offsets().getValues() : null;
		final List<SingleValueResource> inputs = m_config.inputs().getAllElements();
		if ((factors != null && factors.length != inputs.size()) ||
				(offsets != null && offsets.length != inputs.size())) {
			m_logger.error("Factors or offsets length does not match input for {}", m_config);
			return;
		}
		if (FloatResource.class.isAssignableFrom(clazz)) {
			Float sum = 0f;
			boolean isEmpty = true;
			int cnt = -1;
			for (SingleValueResource value : inputs) {
				cnt++;
				if (!value.isActive()) {
					continue;
				}
				if (value instanceof FloatResource) {
					float temp = ((FloatResource) value).getValue();
					if (factors != null)
						temp = temp * factors[cnt];
					if (offsets != null)
						temp = temp + offsets[cnt];
					sum += temp;
					isEmpty = false;
				}
				else if (value instanceof IntegerResource) {
					int temp = ((IntegerResource) value).getValue();
					if (factors != null)
						temp = temp * (int) factors[cnt];
					if (offsets != null)
						temp = temp + (int) offsets[cnt];
					sum += temp;
					isEmpty = false;
				}
				else if (value instanceof TimeResource) {
					long temp = ((TimeResource) value).getValue();
					if (factors != null)
						temp = temp * (long) factors[cnt];
					if (offsets != null)
						temp = temp + (long) offsets[cnt];
					sum += temp;
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
			int cnt = -1;
			for (SingleValueResource value : inputs) {
				cnt++;
				if (!value.isActive()) {
					continue;
				}

				if (value instanceof FloatResource) {
					m_logger.warn("Float resource found in SumManipulator for result type " + clazz.getSimpleName()
							+ " -> rounding float and adding it to result ...");
					float temp = ((FloatResource) value).getValue();
					if (factors != null)
						temp = temp * factors[cnt];
					if (offsets != null)
						temp = temp + offsets[cnt];
					sum += Math.round(temp);
					isEmpty = false;
				}
				else if (value instanceof IntegerResource) {
					int temp = ((IntegerResource) value).getValue();
					if (factors != null)
						temp = temp * (int) factors[cnt];
					if (offsets != null)
						temp = temp + (int) offsets[cnt];
					sum += temp;
					isEmpty = false;
				}
				else if (value instanceof TimeResource) {
					long temp = ((TimeResource) value).getValue();
					if (factors != null)
						temp = temp * (long) factors[cnt];
					if (offsets != null)
						temp = temp + (long) offsets[cnt];
					sum += temp;
					isEmpty = false;
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
					if (sum > Integer.MAX_VALUE) { // XXX
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
			String msg = "SumManipulator result type of "+m_config.getLocation()+" is " + clazz.getName() + " which is " + "not supported ...";
			m_logger.error(msg);
			throw new IllegalArgumentException(msg);
		}
		lastExecTime = appMan.getFrameworkTime();
	}

	@Override
	@SuppressWarnings("fallthrough")
	public void resourceStructureChanged(ResourceStructureEvent event) {
		switch (event.getType()) {
		case RESOURCE_ACTIVATED:
		case RESOURCE_DEACTIVATED:
		case RESOURCE_CREATED:
		case RESOURCE_DELETED:
			resourceChanged((SingleValueResource) event.getChangedResource());
			break;
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
		if (m_timer != null)
			m_timer.start();
		else if (active)
			evaluate();
	}

	@Override
	public void timerElapsed(Timer timer) {
		if (!active)
			return;
		evaluate();
	}

	@Override
	public Long getLastExecutionTime() {
		return lastExecTime;
	}
	
	@Override
	public ResourceManipulatorModel getConfigurationResource() {
		return m_config;
	}
	
	@Override
	public String toString() {
		return "SumController for " + m_config.resultBase().getLocation() + ", configuration: " + getConfigurationResource().getName();
	}
	
}
