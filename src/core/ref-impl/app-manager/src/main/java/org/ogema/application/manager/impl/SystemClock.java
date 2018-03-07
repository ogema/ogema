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
