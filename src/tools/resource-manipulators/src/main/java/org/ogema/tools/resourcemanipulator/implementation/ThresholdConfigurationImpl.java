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
package org.ogema.tools.resourcemanipulator.implementation;

import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.tools.resourcemanipulator.configurations.Threshold;
import org.ogema.tools.resourcemanipulator.model.ThresholdModel;

/**
 * Implementation for a threshold configuration.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ThresholdConfigurationImpl implements Threshold {

	private final ResourceManipulatorImpl m_base;
	private final ApplicationManager m_appMan;
	private FloatResource m_source;
	private float m_threshold = 0.f;
	private BooleanResource m_target;
	private boolean m_equalityExceeds = false;
	private boolean m_invert = false;

	// Configuration this is connected to (null if not connected)
	private ThresholdModel m_config;

	/**
	 * Creates an instance of the configuration object from an existing configuration.
	 */
	public ThresholdConfigurationImpl(ResourceManipulatorImpl base, ThresholdModel configResource) {
		m_base = base;
		m_appMan = base.getApplicationManager();
		m_source = configResource.source();
		m_threshold = configResource.threshold().getValue();
		m_target = configResource.target();
		m_equalityExceeds = configResource.equalityExceeds().getValue();
		m_invert = configResource.invert().getValue();
		m_config = configResource;
	}

	public ThresholdConfigurationImpl(ResourceManipulatorImpl base) {
		m_base = base;
		m_appMan = base.getApplicationManager();
		m_source = null;
		m_threshold = 0.f;
		m_target = null;
		m_equalityExceeds = false;
		m_invert = false;
		m_config = null;
	}

	@Override
	public void setThreshold(FloatResource source, float threshold, BooleanResource target) {
		m_source = source;
		m_threshold = threshold;
		m_target = target;
	}

	@Override
	public void setInversion(boolean inversion) {
		m_invert = inversion;
	}

	@Override
	public boolean getInversion() {
		return m_invert;
	}

	@Override
	public void setEqualityExceeds(boolean equalityCountsAsExceeding) {
		m_equalityExceeds = equalityCountsAsExceeding;
	}

	@Override
	public boolean getEqualityExceeds() {
		return m_equalityExceeds;
	}

	@Override
	public boolean commit() {
		if (m_source == null || m_target == null) {
			return false;
		}

		// delete the old configuration if it exsited.
		if (m_config != null)
			m_config.delete();

		m_config = m_base.createResource(ThresholdModel.class);

		m_config.source().setAsReference(m_source);
		m_config.target().setAsReference(m_target);
		m_config.threshold().create();
		m_config.threshold().setValue(m_threshold);
		m_config.equalityExceeds().create();
		m_config.equalityExceeds().setValue(m_equalityExceeds);
		m_config.invert().create();
		m_config.invert().setValue(m_invert);
		m_config.activate(true);
		return true;
	}

	@Override
	public void remove() {
		if (m_config != null) {
			m_config.delete();
		}
	}

	@Override
	public void setThreshold(float threshold) {
		m_threshold = threshold;
	}

	@Override
	public float getThreshold() {
		return m_threshold;
	}

}
