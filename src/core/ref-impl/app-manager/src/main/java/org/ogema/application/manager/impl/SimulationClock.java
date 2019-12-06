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

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
@Component(
		immediate = true,
		configurationPid=SimulationClock.SERVICE_PID
)
@Service(FrameworkClock.class)
public class SimulationClock implements FrameworkClock {

	final static String SERVICE_PID = "org.ogema.application.manager.impl.SimulationClock";
	private volatile BundleContext ctx;
	protected volatile long startTimeSystem;
	protected volatile long startTimeFramework;
	protected volatile float simulationFactor = 1.0f;
	// required to check whether config properties changed
	private Object startTimeFrameworkProp;
	private Object startTimeSystemProp;
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

	protected synchronized void activate(ComponentContext ctx, Map<String, Object> config) {
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
		this.simulationFactor = getSimFactor(config);
		this.startTimeFrameworkProp = config.get("startTimeFramework");
		this.startTimeSystemProp = config.get("startTimeSystem");
		final Long startTimeFramework0 = getStartTimeFramework(config, startTimeSystem, simulationFactor);
		if (startTimeFramework0 != null) {
			this.startTimeFramework = startTimeFramework0;
		}
		else {  // or from system property, or use default: system time
			startTimeFramework = Long.getLong(TIMESTAMP, startTimeSystem);
			// if only the sim factor is set via config admin we need to persist the start times for the next framework start
			if (simulationFactor != 1.0F) {
				persistTimeConfig();
			}
		}
		if (startTimeFramework != startTimeSystem)
			LoggerFactory.getLogger(SimulationClock.class).info("Starting framework at simulated time {}", new java.util.Date(startTimeFramework));
		if (simulationFactor != 1.0F) 			
			LoggerFactory.getLogger(SimulationClock.class).info("Starting framework with simulation factor {}", simulationFactor);
	}
	
	@org.apache.felix.scr.annotations.Modified
	protected synchronized void modified(ComponentContext ctx, Map<String, Object> config) {
		// avoid component restarts when we update the configuration properties... need to check if props changed
		final float simFactor = getSimFactor(config);
		final double diff = Math.abs(simFactor - this.simulationFactor);
		final double sum = simFactor + this.simulationFactor;
		boolean changed = diff > sum / 100000;
		if (!changed && (!Objects.equals(config.get("startTimeSystem"), startTimeSystemProp) ||
					!Objects.equals(config.get("startTimeFramework"), startTimeFrameworkProp))) {
			changed = true;
		}
		if (changed) {
			final Long frameworkStart0 = getStartTimeFramework(config, startTimeSystem, simFactor);
			final long frameworkStart = frameworkStart0 != null ? frameworkStart0 : getExecutionTime();
			final Object systemTime0 = frameworkStart0 != null ? config.get("systemTime") : null;
			final long systemTime = systemTime0 instanceof Long ? (Long) systemTime0 : System.currentTimeMillis();
			this.startTimeSystemProp = config.get("startTimeSystem");
			this.startTimeFrameworkProp = config.get("startTimeFramework");
			LoggerFactory.getLogger(SimulationClock.class).info("Framework clock properties changed: start time: {}, simulation factor {}", 
					new java.util.Date(frameworkStart), simFactor);
			setSimulationTimeAndFactorInternal(frameworkStart, simFactor, systemTime, false);
		}
	}

	// note: this is only executed on a proper shutdown
	protected void deactivate(ComponentContext ctx, Map<String, ?> config) {
		this.ctx = null;
	}
	
	// persistence is only used if simulation clock deviates from system clock, i.e. if simulation factor 
	// or timestamp have been set via the admin interface
	// requires sync on this
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
			this.startTimeFrameworkProp = Long.valueOf(startTimeFramework);
			this.startTimeSystemProp = Long.valueOf(startTimeSystem);
		} catch (IOException e) { // TODO
			e.printStackTrace();
		} finally {
			ctx.ungetService(sr);
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
	public boolean setSimulationTimeAndFactor(long timestamp, float factor) {
		return setSimulationTimeAndFactorInternal(timestamp, factor, null, true);
	}
	
	private synchronized boolean setSimulationTimeAndFactorInternal(long timestamp, float factor, 
			Long systemTime, boolean doPersist) {
		final float oldFactor = this.simulationFactor;
		final long oldT = getExecutionTime();
		final boolean result = setSimulationTimeInternal(timestamp, systemTime) | setSimulationFactorInternal(factor);
		if (propertyListeners != null) {
            propertyListeners.firePropertyChange(EXECUTION_TIME_CHANGED_PROPERTY, oldT, timestamp);
            propertyListeners.firePropertyChange(SIMULATION_FACTOR_CHANGED_PROPERTY, oldFactor, simulationFactor);
        }
		dispatchClockChangedEvent(new ClockEvent(simulationFactor));
        if (doPersist)
        	persistTimeConfig();
		return result;
	}
	
	private boolean setSimulationTimeInternal(final long timestamp, Long systemTime) {
		this.startTimeFramework = timestamp;
		this.startTimeSystem = systemTime != null ? systemTime : System.currentTimeMillis();
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
		final boolean result  = setSimulationTimeInternal(timestamp, null);
        if (propertyListeners != null) {
            propertyListeners.firePropertyChange(EXECUTION_TIME_CHANGED_PROPERTY, oldTimestamp, timestamp);
        }
        dispatchClockChangedEvent(new ClockEvent(simulationFactor));
        persistTimeConfig();
		return result;
	}

	@Override
    @SuppressWarnings("deprecation")
	public synchronized boolean setSimulationFactor(final float simulationFactor) {
		float oldFactor = this.simulationFactor;
		final boolean result = setSimulationFactorInternal(simulationFactor);
        if (propertyListeners != null) {
            propertyListeners.firePropertyChange(SIMULATION_FACTOR_CHANGED_PROPERTY, oldFactor, simulationFactor);
        }
        dispatchClockChangedEvent(new ClockEvent(simulationFactor));
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
    
    private void dispatchClockChangedEvent(final ClockChangedEvent e) {
    	if (listeners.isEmpty())
    		return;
		final ExecutorService exec = Executors.newSingleThreadExecutor(new ThreadFactory() {
			
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "clock-listener-dispatch");
			}
		});
		try {
			for (ClockChangeListener l: listeners) {
				exec.submit(new ClockListenerCallback(l, e));
			}
		} finally {
			try {
				AccessController.doPrivileged(new PrivilegedAction<Void>() {
	
					@Override
					public Void run() {
						exec.shutdown();
						return null;
					}
					
				});
			} catch (SecurityException ee) {
				ee.printStackTrace();
			}
		}
    }
    
    private static class ClockListenerCallback implements Runnable {
    	
    	private final ClockChangeListener listener;
    	private final ClockChangedEvent event;
    	
    	public ClockListenerCallback(ClockChangeListener listener, ClockChangedEvent event) {
    		this.listener = listener;
    		this.event = event;
		}
    	
    	@Override
    	public void run() {
    		listener.clockChanged(event);
    	}
    	
    }
    
    
    private static float getSimFactor(final Map<String, Object> config) {
    	float simulationFactor = 1.0F;
    	if (config.containsKey("simulationFactor")) {
			final Object factor = config.get("simulationFactor");
			if (factor instanceof Number)
				simulationFactor = ((Number) factor).floatValue();
			else {
				try {
					simulationFactor = Float.parseFloat((String) config.get("simulationFactor"));
				} catch (ClassCastException | NumberFormatException e) {
					LoggerFactory.getLogger(SimulationClock.class).error("Simulation factor must be of type Float, got {}", factor,e);
				}
			}
		}
		else {
			String aux = System.getProperty(SIMULATION_FACTOR);
			if (aux != null) {
				try {
					simulationFactor = Float.parseFloat(aux);
				} catch (NumberFormatException e) { 
					LoggerFactory.getLogger(SimulationClock.class).error("Simulation factor must be of type Float, got {}", aux);
				}
			}
		}
    	return simulationFactor;
    }
    
    private static Long getStartTimeFramework(final Map<String, Object> config, final long startTimeSystem, final float simulationFactor) {
    	if (!config.containsKey("startTimeFramework") || !(config.get("startTimeFramework") instanceof Long))
    		return null;
    	if (!config.containsKey("startTimeSystem"))
    		return (Long) config.get("startTimeFramework");
		try {
			final Object startSystem0 = config.get("startTimeSystem");
			final Object startFramework0 = config.get("startTimeFramework");
			final long lastStartTimeSystem = startSystem0 instanceof Long ? (Long) startSystem0 : Long.parseLong((String) config.get("startTimeSystem"));
			final long lastStartTimeFramework = startFramework0 instanceof Long ? (Long) startFramework0 : Long.parseLong((String) config.get("startTimeFramework"));
			return (long) ((startTimeSystem - lastStartTimeSystem) * simulationFactor + lastStartTimeFramework);
		} catch (ClassCastException | NumberFormatException e) {
			LoggerFactory.getLogger(SimulationClock.class).error("Simulation start times must be of type Long, got {}, {}", 
					config.get("startTimeSystem"), config.get("startTimeFramework"));
			return null;
		}
    }

}
