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
package org.ogema.core.administration;

/**
 * Interface for classes providing a different framework time from the system time. As it is possible to run the
 * framework with increased or decreased speed compared to real time (or with an offset) in a test environment, the
 * framework time may differ from real time
 */
public interface FrameworkClock {
    
    /**
     * Clock change listeners are notified when clock settings are updated.
     */
    public static interface ClockChangeListener {
        
        void clockChanged(ClockChangedEvent e);
        
    }
    
    public static interface ClockChangedEvent {
        
        float getSimulationFactor();
        
        FrameworkClock getClock();
        
    }

	/**
	 * Identifier that the execution time has been changed manually, i.e. a change that is not
	 * due to progression of the physical or (in case of a speedup factor) simulated time has
	 * occured. This identifier is reported to a PropertyChangedListener registered on the
	 * framework clock.
	 */
    @Deprecated
	public static final String EXECUTION_TIME_CHANGED_PROPERTY = "executionTime";

	/**
	 * Identifier that the simulation speedup factor has been changed.. This identifier is reported to a PropertyChangedListener registered on the
	 * framework clock.
	 */
    @Deprecated
	public static final String SIMULATION_FACTOR_CHANGED_PROPERTY = "simulationFactor";

	/** @return framework time defined by clock in ms since epoch */
	long getExecutionTime();

	/**
	 * Get describing name in human readable form
	 * 
	 * @return name
	 */
	String getName();

	/**
	 * @return speed of framework time in relation to real time. Should be provided for informational purposes, not
	 *         relevant for the framework operation.
	 */
	float getSimulationFactor();

	/**
	 * @param simulationFactor
	 *            speed of framework time in relation to real time, must be greater than 0.
	 * @return true if the clock speed has been modified, false if this clock does not support setting of the simulation
	 *         factor.
	 * @throws IllegalArgumentException
	 *             if the requested simulation factor is {@code <= 0}
	 */
	boolean setSimulationFactor(float simulationFactor);
	
	/**
	 * Set the simulation time, in milliseconds since epoch
	 * @param timestamp
	 * 		new simulation time
	 * @return
	 */
	boolean setSimulationTime(long timestamp);
	
	/**
	 * See {@link #setSimulationTime(long)} and {@link #setSimulationFactor(float)}.
	 * @param timestamp
	 * @param factor
	 * @return
	 */
	boolean setSimulationTimeAndFactor(long timestamp, float factor);

	/**
	 * Registered property listeners must be notified when the simulation factor
	 * (property name {@value #SIMULATION_FACTOR_CHANGED_PROPERTY}) is changed,
	 * or the execution time (property name {@value #EXECUTION_TIME_CHANGED_PROPERTY}) has been
	 * modified outside of the normal progression of time.
	 * 
	 * @param listener the property listener.
     * 
     * @deprecated PropertyChangeListener requires module java.desktop on Java 9
	 */
    @Deprecated
	void addPropertyChangeListener(java.beans.PropertyChangeListener listener);

	/**
	 * Removes a previously-registered PropertyChangeListener.
	 * @param listener Listener to remove.
     * 
     * @deprecated PropertyChangeListener requires module java.desktop on Java 9
	 */
    @Deprecated
	void removePropertyChangeListener(java.beans.PropertyChangeListener listener);
    
    void addClockChangeListener(ClockChangeListener l);
    
    void removeClockChangeListener(ClockChangeListener l);
}
