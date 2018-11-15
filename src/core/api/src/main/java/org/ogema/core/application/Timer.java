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
