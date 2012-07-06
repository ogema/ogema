/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import java.beans.PropertyChangeSupport;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.administration.FrameworkClock;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;

/**
 * 
 * @author jlapp
 */
@Component(immediate = true)
@Service(FrameworkClock.class)
public class SimulationClock implements FrameworkClock {

	protected volatile long startTimeSystem = System.currentTimeMillis();
	protected volatile long startTimeFramework = startTimeSystem;
	protected volatile float simulationFactor = 1.0f;
	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	/**
	 * System or configuration property ({@value} ) that can be used to disable this clock (set to {@code true}).
	 */
	public static String DISABLE = "org.ogema.defaultclock.disable";

	protected void activate(ComponentContext ctx, Map<String, ?> config) {
		boolean disabledInProperties = Boolean.getBoolean(DISABLE);
		boolean disabledInConfig = Boolean.valueOf(String.valueOf(config.get(DISABLE)));
		if (disabledInProperties) {
			throw new ComponentException("disabled by system property.");
		}
		if (disabledInConfig) {
			throw new ComponentException("disabled by component configuration.");
		}
	}

	protected void deactivate(ComponentContext ctx, Map<String, ?> config) {
	}

	@Override
	public long getExecutionTime() {
		long elapsedSystemTime = System.currentTimeMillis() - startTimeSystem;
		return startTimeFramework + (long) Math.floor(elapsedSystemTime * simulationFactor);
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public float getSimulationFactor() {
		return simulationFactor;
	}

	@Override
	public boolean setSimulationFactor(float simulationFactor) {
		if (simulationFactor < 0) {
			throw new IllegalArgumentException("illegal simulation factor: " + simulationFactor);
		}
		this.startTimeFramework = getExecutionTime();
		this.startTimeSystem = System.currentTimeMillis();
		float oldFactor = this.simulationFactor;
		this.simulationFactor = simulationFactor;
		listeners.firePropertyChange(SIMULATION_FACTOR_CHANGED_PROPERTY, oldFactor, simulationFactor);
		return true;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

}
