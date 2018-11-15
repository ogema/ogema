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

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;
import org.ogema.tools.resourcemanipulator.configurations.Threshold;
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;
import org.ogema.tools.resourcemanipulator.model.ThresholdModel;

/**
 * Controls a single threshold rule configured by a configuration resource
 * of type {@link ThresholdModel}.
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ThresholdController implements Controller, ResourceValueListener<FloatResource>, ResourceStructureListener {

	private final ThresholdModel m_config;

	private boolean m_active;
	private float m_threshold;
	private boolean m_invert;
	private boolean m_equalityExceeds;
	private final ApplicationManager appMan;
	private Long lastExecTime;

	public ThresholdController(ApplicationManager appMan, ThresholdModel configuration) {
		this.appMan = appMan;
		m_config = configuration;
	}

	@Override
	public void start() {
		m_config.source().addValueListener(this);
		m_config.source().addStructureListener(this);
		updateAndEnforceRules();
	}

	@Override
	public void stop() {
		m_config.source().removeStructureListener(this);
		m_config.source().removeValueListener(this);
	}

	@Override
	public Class<? extends ManipulatorConfiguration> getType() {
		return Threshold.class;
	}
	
	/**
	 * Update the rules how the threshold shall be calculated, including the
	 * question if the target resource should be set active or inactive.
	 */
	private void updateAndEnforceRules() {
		m_active = m_config.source().isActive();
		m_threshold = m_config.threshold().getValue();
		m_invert = m_config.invert().getValue();
		m_equalityExceeds = m_config.equalityExceeds().getValue();
		enforceRules();
	}

	/**
	 * Apply the threshold rule.
	 */
	private void enforceRules() {
		BooleanResource target = m_config.target();
		if (!m_active) {
			target.deactivate(false);
			return;
		}
		final float value = m_config.source().getValue();
		final boolean thresholdExceeded = (m_equalityExceeds) ? (value >= m_threshold) : (value > m_threshold);
		final boolean newState = (m_invert) ? (!thresholdExceeded) : thresholdExceeded;
		if (target.isActive()) {
			final boolean oldState = target.getValue();
			if (oldState != newState)
				target.setValue(newState);
		}
		else {
			target.setValue(newState);
			target.activate(false);
		}
		lastExecTime = appMan.getFrameworkTime();
	}

	@Override
	public void resourceChanged(FloatResource resource) {
		enforceRules();
	}

	@Override
	public void resourceStructureChanged(ResourceStructureEvent event) {
		updateAndEnforceRules();
	}

	@Override
	public ResourceManipulatorModel getConfigurationResource() {
		return m_config;
	}
	
	@Override
	public Long getLastExecutionTime() {
		return lastExecTime;
	}
	
	public String toString() {
		return "ThresholdController for " + m_config.target().getLocation() + ", configuration: " + getConfigurationResource().getName();
	}
	
}
