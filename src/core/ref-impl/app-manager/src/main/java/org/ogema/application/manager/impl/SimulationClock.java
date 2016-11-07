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
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;


import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.administration.FrameworkClock;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;

/**
 * 
 * @author jlapp
 */
@Component(immediate = true)
@Service(FrameworkClock.class)
public class SimulationClock implements FrameworkClock {

	private final static String SERVICE_PID = "org.ogema.application.manager.impl.SimulationClock";
	protected volatile long startTimeSystem = System.currentTimeMillis();
	protected volatile long startTimeFramework;
	protected volatile float simulationFactor = 1.0f;
	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	/**
	 * System or configuration property ({@value} ) that can be used to disable this clock (set to {@code true}).
	 */
	public static final String DISABLE = "org.ogema.defaultclock.disable";
	
	/**
	 * System property to set initial framework time on clean start
	 */
	public static final String TIMESTAMP = "org.ogema.defaultclock.timestamp";
	
	/**
	 * System property to set initial simulation factor
	 */
	public static final String SIMULATION_FACTOR = "org.ogema.defaultclock.speedfactor";

	protected void activate(ComponentContext ctx, Map<String, ?> config) {
		boolean disabledInProperties = Boolean.getBoolean(DISABLE);
		boolean disabledInConfig = Boolean.valueOf(String.valueOf(config.get(DISABLE)));
		if (disabledInProperties) {
			throw new ComponentException("disabled by system property.");
		}
		if (disabledInConfig) {
			throw new ComponentException("disabled by component configuration.");
		}
		long time;
		Object timestamp = config.get("timestamp");
		if (timestamp != null) {  // either read from ConfigAdmin properties
			time = (long) timestamp;
		}
		else {  // or from system property, or use default: system time
			time = Long.getLong(TIMESTAMP, startTimeSystem);
		}
		startTimeFramework = time;
		String aux = System.getProperty(SIMULATION_FACTOR);
		if (aux != null) {
			try {
				simulationFactor = Float.parseFloat(aux);
			} catch (NumberFormatException e) { /* ignore */ }
		}
	}

	// note: this is only executed on a proper shutdown
	protected void deactivate(ComponentContext ctx, Map<String, ?> config) {
		Long dummy = Long.getLong(TIMESTAMP);
		if (dummy != null) {  // we only set the time explicitly if the framework clock has been set by a system property
			ServiceReference<ConfigurationAdmin> sr = ctx.getBundleContext().getServiceReference(ConfigurationAdmin.class);
			if (sr != null) {
				ConfigurationAdmin configAdmin = ctx.getBundleContext().getService(sr);
				try {
					Configuration cfg = configAdmin.getConfiguration(SERVICE_PID);
					Dictionary<String, Object> props = cfg.getProperties();
					if (props == null) {
						props = new Hashtable<>();
					}
					props.put("timestamp", getExecutionTime());
					cfg.update(props);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
