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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;
import org.ogema.tools.resourcemanipulator.configurations.Sum;
import org.ogema.tools.resourcemanipulator.model.SumModel;

/**
 * Implementation for a sum configuration.
 * 
 * @author Marco Postigo Perez, Fraunhofer IWES
 */
public class SumImpl implements Sum {

	private final ResourceManipulatorImpl m_base;
	private List<SingleValueResource> m_inputs;
	private Resource m_output;
	private long m_delay;
	private boolean m_deactivateEmtpySum;

	// Configuration this is connected to (null if not connected)
	private SumModel m_config;

	/**
	 * Creates an instance of the configuration object from an existing configuration.
	 */
	public SumImpl(ResourceManipulatorImpl base, SumModel configResource) {
		m_base = base;
        m_inputs = new ArrayList<>(configResource.inputs().getAllElements());
        m_output = configResource.resultBase();
        m_delay = configResource.delay().getValue();
        m_deactivateEmtpySum = configResource.deactivateEmptySum().getValue();
		m_config = configResource;
	}

	public SumImpl(ResourceManipulatorImpl base) {
		m_base = base;
		m_inputs = null;
		m_output = null;
		m_delay = 0;
		m_deactivateEmtpySum = false;
		m_config = null;
	}

	@Override
	public boolean commit() {
		if (m_inputs == null || m_output == null) {
			return false;
		}

		// delete the old configuration if it exsited.
		if (m_config != null)
			m_config.delete();

		m_config = m_base.createResource(SumModel.class);

		m_config.inputs().create();
		for (SingleValueResource val : m_inputs) {
			m_config.inputs().add(val);
		}
		m_config.resultBase().setAsReference(m_output);
		m_config.delay().create();
		m_config.delay().setValue(m_delay);
		m_config.deactivateEmptySum().create();
		m_config.deactivateEmptySum().setValue(m_deactivateEmtpySum);

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
    public void setAddends(Collection<? extends SingleValueResource> addends, SingleValueResource sum) {
        m_inputs = new ArrayList<>(addends);
        m_output = sum;
        m_delay = 0;
    }

	@Override
	public void setDelay(long delayTime) {
		m_delay = delayTime;
	}

	@Override
	public long getDelay() {
		return m_delay;
	}

	@Override
	public Resource getTarget() {
		return m_output;
	}

	@Override
	public List<SingleValueResource> getAddends() {
		return Collections.unmodifiableList(m_inputs);
	}

	@Override
	public void setDisableEmptySum(boolean emptySumDisables) {
		m_deactivateEmtpySum = emptySumDisables;
	}

	@Override
	public boolean getDisableEmptySum() {
		return m_deactivateEmtpySum;
	}

	@Override
	public void deactivate() {
		if (m_config != null)
			m_config.deactivate(true);
	}

	@Override
	public void activate() {
		if (m_config != null)
			m_config.activate(true);
	}

}
