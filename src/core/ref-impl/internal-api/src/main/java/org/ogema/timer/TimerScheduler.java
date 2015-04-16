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
package org.ogema.timer;

import java.util.concurrent.Executor;

import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.application.Timer;
import org.slf4j.Logger;

/**
 * Instances of this type are responsible for creating {@link Timer}s and executing
 * the timer listeners at the specified times using the given {@link Executor}.
 * While the functionality is similar to the standard {@link java.util.Timer java.util.Timer},
 * an OGEMA TimerScheduler will use the OGEMA framework clock.
 * 
 * @see FrameworkClock
 * 
 * @author jlapp
 */
public interface TimerScheduler {

	/**
	 * @param logger Logger for uncaught exceptions.
	 * @param executor Executor that will run the timer's listeners on an extra thread.
	 * @return new Timer that will use the given Executor.
	 */
	public Timer createTimer(Executor executor, Logger logger);

}
