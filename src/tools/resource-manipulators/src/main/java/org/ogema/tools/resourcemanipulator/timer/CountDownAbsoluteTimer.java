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
	 * @param absoluteTime
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
