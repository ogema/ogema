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
import org.slf4j.LoggerFactory;

/**
 * Implementation for a sum configuration.
 * 
 * @author Marco Postigo Perez, Fraunhofer IWES
 */
public class SumImpl implements Sum {

	private final ResourceManipulatorImpl m_base;
	private List<SingleValueResource> m_inputs;
	private float[] factors;
	private float[] offsets;
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
			if (!val.exists()) {
				val.create();
				LoggerFactory.getLogger(ResourceManipulatorImpl.class).warn("Virtual resource passed to Sum configuration; creating it. {}",val);
			}
			m_config.inputs().add(val);
		}
		m_config.resultBase().setAsReference(m_output);
		m_config.delay().create();
		m_config.delay().setValue(m_delay);
		m_config.deactivateEmptySum().create();
		m_config.deactivateEmptySum().setValue(m_deactivateEmtpySum);
		if (factors != null) {
			m_config.factors().create();
			m_config.factors().setValues(factors);
		}
		if (offsets != null) {
			m_config.offsets().create();
			m_config.offsets().setValues(offsets);
		}
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
        setAddends(new ArrayList<>(addends), null, null, sum);
    }
	
	@Override
	public void setAddends(List<? extends SingleValueResource> addends, List<Float> factors, List<Float> offsets, SingleValueResource sum) {
		m_inputs = new ArrayList<>(addends);
        m_output = sum;
        m_delay = 0;
        if (factors != null || offsets != null) {
        	if ((factors != null && factors.size() != addends.size()) ||
        			(offsets != null && offsets.size() != addends.size()))
        		throw new IllegalArgumentException("Factors and/or offsets size does not match addends size");
        	if (factors != null) {
        		final float[] arr = new float[addends.size()];
        		boolean required= false;
        		int cnt = 0;
        		for (Float f : factors) {
        			if (f != null && f != 1) {
        				arr[cnt] = f;
        				required = true;
        			} else {
        				arr[cnt] = 1;
        			}
        			cnt++;
        		}
        		if (required)
        			this.factors = arr;
        	}
        	if (offsets != null) {
        		final float[] arr = new float[addends.size()];
        		boolean required= false;
        		int cnt = 0;
        		for (Float f : offsets) {
        			if (f != null && f != 0) {
        				arr[cnt] = f;
        				required = true;
        			} else {
        				arr[cnt] = 0;
        			}
        			cnt++;
        		}
        		if (required)
        			this.offsets = arr;
        	}
        }
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
    @SuppressWarnings("unchecked")
	public List<SingleValueResource> getAddends() {
		return m_inputs != null ? Collections.unmodifiableList(m_inputs) : Collections.EMPTY_LIST;
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
