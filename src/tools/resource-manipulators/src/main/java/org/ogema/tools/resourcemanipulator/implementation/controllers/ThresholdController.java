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
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
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

	public ThresholdController(ApplicationManager appMan, ThresholdModel configuration) {
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
	}

	@Override
	public void resourceChanged(FloatResource resource) {
		enforceRules();
	}

	@Override
	public void resourceStructureChanged(ResourceStructureEvent event) {
		updateAndEnforceRules();
	}

}
