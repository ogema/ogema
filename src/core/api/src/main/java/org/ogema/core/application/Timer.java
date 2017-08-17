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
package org.ogema.core.application;

import java.util.List;
import org.ogema.core.administration.FrameworkClock;

/**
 * A periodic timer. Every so-many milli-seconds, the timer is executed and calls the {@link TimerListener} objects
 * registered on it.
 */
public interface Timer {

	/** Stop generating callbacks. */
	void stop();

	/**
	 * Start generating callbacks again after a previous stop call.<br>
	 * When a resume is called very soon after a stop command, the time between a new callback and the last callback
	 * before the stop call may not be shorter than the timer period.
	 */
	void resume();

	/**
	 * Returns whether the driver is running (true) or stopped (false).
	 */
	boolean isRunning();

	/**
	 * (Re-)sets the length of the time interval between two timer callbacks.
	 * 
	 * @param millis
	 *            the size of the interval in milliseconds {@code > 0}.
	 * @throws IllegalArgumentException if {@code millis < 1}.
	 */
	void setTimingInterval(long millis);

	/**
	 * Returns the time interval between two timer callbacks.
	 * 
	 * @return the timing interval of this timer.
	 */
	long getTimingInterval();

	/**
	 * Destroy the timer and remove it from the framework.
	 */
	void destroy();

	/** 
	 * Add listener. It is possible to add more than one callback to a timer. 
	 * If the listener is already added, this does nothing (the listener is not
	 * added a 2nd time).
	 */
	void addListener(TimerListener listener);

	/**
	 * Removes a listener from the list of registered listeners.
	 * @param listener reference to the listener to remove.
	 * @return true if the listener was removed, false if not (e.g. because it
	 * was not registered in the first place).
	 */
	boolean removeListener(TimerListener listener);

	/**
	 * Gets all listeners registered on the timer.
	 * @return returns the list of all listeners registered.
	 */
	List<TimerListener> getListeners();

	/**
	 * Returns the current framework time, 
	 * not the time for which this timer has been scheduled.
	 * 
	 * @return current framework time.
	 * 
	 * @see FrameworkClock
	 */
	long getExecutionTime();

	/**
	 * Returns the next execution time of the timer.
	 * @return
	 */
	long getNextRunTime();

}
