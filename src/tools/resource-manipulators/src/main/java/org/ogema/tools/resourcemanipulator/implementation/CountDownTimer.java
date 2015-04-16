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
package org.ogema.tools.resourcemanipulator.implementation;

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

	/**
	 * Stops the count-down.
	 */
	public void stop() {
		timer.stop();
	}
}
