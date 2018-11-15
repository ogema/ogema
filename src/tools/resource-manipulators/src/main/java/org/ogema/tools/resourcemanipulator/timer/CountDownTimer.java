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
package org.ogema.tools.resourcemanipulator.timer;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;

/**
 * Implementation of a Count-Down timer: An OGEMA timer that after start counts down
 * the timer time and then evaluates exactly once (in contrast to the default 
 * periodic timer in OGEMA, that will continue with the next count down). 
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class CountDownTimer implements TimerListener {

	private final Timer timer;
	private final TimerListener listener;

	/** Note: The timer is not started automatically after construction, only after call of start*/
	public CountDownTimer(ApplicationManager appMan, long countDownTime, TimerListener listener) {
		this.listener = listener;
		this.timer = appMan.createTimer(countDownTime, this);
		timer.stop();
	}

	@Override
	public void timerElapsed(Timer timer) {
		timer.stop();
		listener.timerElapsed(timer);
	}

	/**
	 * Start the count down. If the count down already runs, this does nothing.
	 */
	public void start() {
		if (!timer.isRunning())
			timer.resume();
	}
	
	/**If the timer is not running, starts the timer. Otherwise restarts the timer to generate the callback only
	 * after the full interval specified for the timer
	 */
	public void restart() {
		restart(timer.getTimingInterval());
	}
	public void restart(long countDownTime) {
		if (!timer.isRunning()) {
			timer.setTimingInterval(countDownTime);
			timer.resume();
		} else {
			timer.stop();
			timer.setTimingInterval(countDownTime);
			timer.resume();
		}
	}
	/*public void restart(long countDownTime) {
		if (!timer.isRunning()) {
			timer.setTimingInterval(countDownTime);
			timer.resume();
		}
	}*/

	/**
	 * Stops the count-down.
	 */
	public void stop() {
		timer.stop();
	}
	
	public void destroy() {
		timer.destroy();
	}

	public long getExecutionTime() {
		return timer.getExecutionTime();
	}

	public long getTimingInterval() {
		return timer.getTimingInterval();
	}

	public boolean isRunning() {
		return timer.isRunning();
	}
}
