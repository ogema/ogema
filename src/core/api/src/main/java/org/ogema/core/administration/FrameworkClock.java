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
