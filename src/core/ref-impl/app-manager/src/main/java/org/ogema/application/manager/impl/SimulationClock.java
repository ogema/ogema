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

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.administration.FrameworkClock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jlapp
 */
@Component(immediate = true)
@Service(FrameworkClock.class)
public class SimulationClock implements FrameworkClock {

	private final static String SERVICE_PID = "org.ogema.application.manager.impl.SimulationClock";
	private volatile BundleContext ctx;
	protected volatile long startTimeSystem;
	protected volatile long startTimeFramework;
	protected volatile float simulationFactor = 1.0f;
	protected java.beans.PropertyChangeSupport propertyListeners;
    protected ConcurrentLinkedQueue<ClockChangeListener> listeners = new ConcurrentLinkedQueue<>();
    
    final class ClockEvent implements ClockChangedEvent {
        
        final float factor;

        public ClockEvent(float factor) {
            this.factor = factor;
        }
        
        @Override
        public float getSimulationFactor() {
            return factor;
        }

        @Override
        public FrameworkClock getClock() {
            return SimulationClock.this;
        }
    }

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
		this.ctx = ctx.getBundleContext();
		boolean disabledInProperties = Boolean.getBoolean(DISABLE);
		boolean disabledInConfig = Boolean.valueOf(String.valueOf(config.get(DISABLE)));
		if (disabledInProperties) {
			throw new ComponentException("disabled by system property.");
		}
		if (disabledInConfig) {
			throw new ComponentException("disabled by component configuration.");
		}
		startTimeSystem = System.currentTimeMillis();
		final long time;
		if (config.containsKey("startTimeSystem") && config.containsKey("startTimeFramework") && config.containsKey("simulationFactor")) {
			final long lastStartTimeSystem = (Long) config.get("startTimeSystem");
			final long lastStartTimeFramework = (Long) config.get("startTimeFramework");
			simulationFactor = (Float) config.get("simulationFactor");
			time = (long) ((startTimeSystem - lastStartTimeSystem) * simulationFactor + lastStartTimeFramework);
		}
		else {  // or from system property, or use default: system time
			time = Long.getLong(TIMESTAMP, startTimeSystem);
			String aux = System.getProperty(SIMULATION_FACTOR);
			if (aux != null) {
				try {
					simulationFactor = Float.parseFloat(aux);
				} catch (NumberFormatException e) { /* ignore */ }
			}
		}
		startTimeFramework = time;
	}
	
	@org.apache.felix.scr.annotations.Modified
	protected void modified(ComponentContext ctx, Map<String, ?> config) {
		// only here to avoid component restarts when we update the configuration properties
	}

	// note: this is only executed on a proper shutdown
	protected void deactivate(ComponentContext ctx, Map<String, ?> config) {
		this.ctx = null;
	}
	
	// persistence is only used if simulation clock deviates from system clock, i.e. if simulation factor 
	// or timestamp have been set via the admin interface
	private void persistTimeConfig() {
		ServiceReference<ConfigurationAdmin> sr = null;
		try {
			sr = ctx.getServiceReference(ConfigurationAdmin.class);
		} catch (IllegalStateException | NullPointerException e) {}
		if (sr == null) {
			LoggerFactory.getLogger(SimulationClock.class).warn("Configuration admin not found... cannot persist time information");
			return;
		}
		final ConfigurationAdmin configAdmin = ctx.getService(sr);
		try {
			Configuration cfg = configAdmin.getConfiguration(SERVICE_PID);
			Dictionary<String, Object> props = cfg.getProperties();
			if (props == null) {
				props = new Hashtable<>();
			}
			props.put("startTimeSystem", startTimeSystem);
			props.put("startTimeFramework", startTimeFramework);
			props.put("simulationFactor", simulationFactor);
			cfg.update(props);
		} catch (IOException e) {
			e.printStackTrace();
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
	public synchronized boolean setSimulationTimeAndFactor(long timestamp, float factor) {
		final float oldFactor = this.simulationFactor;
		final long oldT = getExecutionTime();
		final boolean result = setSimulationTimeInternal(timestamp) | setSimulationFactorInternal(factor);
		if (propertyListeners != null) {
            propertyListeners.firePropertyChange(EXECUTION_TIME_CHANGED_PROPERTY, oldT, timestamp);
            propertyListeners.firePropertyChange(SIMULATION_FACTOR_CHANGED_PROPERTY, oldFactor, simulationFactor);
        }
        if (!listeners.isEmpty()) {
            ClockEvent e = new ClockEvent(simulationFactor);
            for (ClockChangeListener l: listeners) {
                l.clockChanged(e);
            }
        }
	    persistTimeConfig();
		return result;
	}
	
	private boolean setSimulationTimeInternal(final long timestamp) {
		this.startTimeFramework = timestamp;
		this.startTimeSystem = System.currentTimeMillis();
		return true;
	}
	
	private boolean setSimulationFactorInternal(final float simulationFactor) {
		if (simulationFactor < 0) {
			throw new IllegalArgumentException("illegal simulation factor: " + simulationFactor);
		}
		this.startTimeFramework = getExecutionTime();
		this.startTimeSystem = System.currentTimeMillis();
		this.simulationFactor = simulationFactor;
		return true;
	}
	
	@Override
	public synchronized boolean setSimulationTime(long timestamp) {
		final long oldTimestamp = getExecutionTime();
		final boolean result  = setSimulationTimeInternal(timestamp);
        if (propertyListeners != null) {
            propertyListeners.firePropertyChange(EXECUTION_TIME_CHANGED_PROPERTY, oldTimestamp, timestamp);
        }
        if (!listeners.isEmpty()) {
            ClockEvent e = new ClockEvent(simulationFactor);
            for (ClockChangeListener l: listeners) {
                l.clockChanged(e);
            }
        }
        persistTimeConfig();
		return result;
	}

	@Override
    @SuppressWarnings("deprecation")
	public synchronized boolean setSimulationFactor(float simulationFactor) {
		float oldFactor = this.simulationFactor;
		final boolean result = setSimulationFactorInternal(simulationFactor);
        if (propertyListeners != null) {
            propertyListeners.firePropertyChange(SIMULATION_FACTOR_CHANGED_PROPERTY, oldFactor, simulationFactor);
        }
        if (!listeners.isEmpty()) {
            ClockEvent e = new ClockEvent(simulationFactor);
            for (ClockChangeListener l: listeners) {
                l.clockChanged(e);
            }
        }
        persistTimeConfig();
		return result;
	}

	@Override
    @Deprecated
	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
        if (propertyListeners == null) {
            propertyListeners = new java.beans.PropertyChangeSupport(this);
        }
		propertyListeners.addPropertyChangeListener(listener);
	}

	@Override
    @Deprecated
	public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
        if (propertyListeners != null) {
            propertyListeners.removePropertyChangeListener(listener);
        }
	}

    @Override
    public void addClockChangeListener(ClockChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeClockChangeListener(ClockChangeListener l) {
        listeners.remove(l);
    }

}
