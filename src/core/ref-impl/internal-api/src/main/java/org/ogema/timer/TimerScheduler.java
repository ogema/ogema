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

	/**
	 * @param logger Logger for uncaught exceptions.
	 * @param executor Executor that will run the timer's listeners on an extra thread.
	 * @param listener Listener that shall be informed when the timer is destroyed.
	 * @return new Timer that will use the given Executor.
	 */
	public Timer createTimer(Executor executor, Logger logger, TimerRemovedListener listener);
	
}
