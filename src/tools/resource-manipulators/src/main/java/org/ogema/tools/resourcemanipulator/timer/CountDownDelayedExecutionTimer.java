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
package org.ogema.tools.resourcemanipulator.timer;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;

/**
 * Implementation of a Count-Down timer: An OGEMA timer that after creation calls delayedExecution and
 * afterwards is destroyed. In contrast to {@link CountDownTimer} it does not require an TimerListener.
 */
public abstract class CountDownDelayedExecutionTimer implements TimerListener {

	private final Timer timer;
	
	public abstract void delayedExecution();

	/** Time starts automatically after construction
	 * @param appMan
	 * @param countDownTime time in milliseconds until the method delayedExecution is called
	 * */
	public CountDownDelayedExecutionTimer(ApplicationManager appMan, long countDownTime) {
		this.timer = appMan.createTimer(countDownTime, this);
	}

	@Override
	public void timerElapsed(Timer timer) {
		timer.destroy();
		delayedExecution();
	}

	/**
	 * Start the count down. If the count down already runs, this does nothing.
	 */
	public void start() {
		if (!timer.isRunning())
			timer.resume();
	}
	
	public void restart(long countDownTime) {
		if (!timer.isRunning()) {
			timer.setTimingInterval(countDownTime);
			timer.resume();
		}
	}

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
