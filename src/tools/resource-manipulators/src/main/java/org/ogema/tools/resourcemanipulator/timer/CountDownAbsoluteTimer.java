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
 * Implementation of a Count-Down timer: An OGEMA timer that evaluates exactly once (in contrast to
 * the default periodic timer in OGEMA, that will continue with the next count down). In contrast to
 * {@link CountDownTimer} this timer aims to generate the single callback exactly at an absolute
 * framework time and does not take a time duration as input. This means also that it starts running
 * immediately after creation and does not require an additional start call.
 * 
 * @author David Nestle, Fraunhofer IWES
 */
public class CountDownAbsoluteTimer implements TimerListener {

	private final Timer timer;
	private final TimerListener listener;
	long interval;
	
	/** Create and start CountDownAbsoluteTimer
	 * 
	 * @param appMan
	 * @param absoluteTime time in milliseconds since epoch when listener.timerElapsed shall
	 * 		be called
	 * @param listener listener that shall be called once at the absolute time specified
	 */
	public CountDownAbsoluteTimer(ApplicationManager appMan, long absoluteTime,
			TimerListener listener) {
		this(appMan, absoluteTime, false, listener);
	}
	/**
	 * 
	 * @param appMan
	 * @param countDownTime
	 * @param callOnPast if true the callback will be initiated immediately if the countDownTime is
	 * 		in the past relative to the framework time. If false an exception is thrown if the
	 * 		countDoenTime is in the past.
	 * @param listener
	 */
	public CountDownAbsoluteTimer(ApplicationManager appMan, long absoluteTime,
			boolean callOnPast, TimerListener listener) {
		this.listener = listener;
		//this.callOnPast = callOnPast;
		//this.appMan = appMan;
		
		//if(timer.isRunning()) return;
		interval = absoluteTime - appMan.getFrameworkTime();
		if(interval <= 10) {
			if(callOnPast) {
				this.timer = appMan.createTimer(65536l, this);
				listener.timerElapsed(timer);
				this.timer.destroy();
				return;
			} else {
				throw new IllegalArgumentException("Absolute time must be in the future (and not only a few milliseconds!)");
			}
		}
		this.timer = appMan.createTimer(interval, this);		
	}

	@Override
	public void timerElapsed(Timer timer) {
		timer.stop();
		listener.timerElapsed(timer);
		timer.destroy();
		timer = null;
	}

	/**
	 * Stops ans destroys the count-down prematurely. Does not destroy if destination time is reached and
	 * the listener is currently evaluated
	 */
	public void destroy() {
		if(timer.isRunning()) {
			timer.destroy();
		}
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
