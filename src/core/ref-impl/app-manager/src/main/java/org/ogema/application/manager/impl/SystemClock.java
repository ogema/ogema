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
package org.ogema.application.manager.impl;

import java.beans.PropertyChangeListener;

import org.ogema.core.administration.FrameworkClock;

/**
 * Framework clock returning system time (System.currentTimeMillis())
 * 
 * @author jlapp
 */
public class SystemClock implements FrameworkClock {

	@Override
	public long getExecutionTime() {
		return System.currentTimeMillis();
	}

	@Override
	public String getName() {
		return "system time";
	}

	@Override
	public float getSimulationFactor() {
		return 1f;
	}

	@Override
	public boolean setSimulationFactor(float simulationFactor) {
		return false;
	}
	
	@Override
	public boolean setSimulationTime(long timestamp) {
		return false;
	}
	
	@Override
	public boolean setSimulationTimeAndFactor(long timestamp, float factor) {
		return false;
	}

	@Override
    @Deprecated
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		//this clock does not support such property changes.
	}

	@Override
    @Deprecated
	public void removePropertyChangeListener(PropertyChangeListener listener) {
        //this clock does not support such property changes.
	}

    @Override
    public void addClockChangeListener(ClockChangeListener l) {
        //this clock does not create events.
    }

    @Override
    public void removeClockChangeListener(ClockChangeListener l) {
        //this clock does not create events.
    }

}
