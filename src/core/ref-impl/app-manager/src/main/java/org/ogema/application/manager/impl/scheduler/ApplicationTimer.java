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
package org.ogema.application.manager.impl.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.timer.TimerRemovedListener;
import org.slf4j.Logger;

/**
 * Holds timer data (listeners, period and state) but does not actually execute
 * listeners, used by {@link DefaultTimerScheduler}.
 *
 * @author Jan Lapp, Fraunhofer IWES
 */
public class ApplicationTimer implements Timer, Comparable<ApplicationTimer>,
        Callable<Boolean>, Runnable {

    public static enum TimerState {

        RUNNING, PAUSED, SHUTDOWN;
    }

    /**
     * Application manager to submit the event to.
     */
    protected final Executor exec;

    protected final Logger logger;

    /**
     * Reference to the scheduler managing this.
     */
    private final DefaultTimerScheduler scheduler;
    
    /**
     * These values must only be changed while holding the lock on scheduler.timers 
     */
    protected volatile long period;
    protected volatile long nextRun;

    /**
     * false iff this timer's listener callbacks are currently being executed
     */
    private volatile boolean idle = true;

    /**
     * Set of all objects receiving callback events from this timer.
     */
    protected final List<TimerListener> listeners = new ArrayList<>();
    /**
     * may be null
     */
    private final TimerRemovedListener timerRemovedListener;

    protected volatile TimerState state = TimerState.RUNNING;

    protected ApplicationTimer(Executor exec, long period,
            DefaultTimerScheduler scheduler, Logger logger) {
    	this(exec, period, scheduler, logger, null);
    }
    
    protected ApplicationTimer(Executor exec, long period,
            DefaultTimerScheduler scheduler, Logger logger, TimerRemovedListener timerRemovedListener) {
        if (period < 1){
            throw new IllegalArgumentException("period must be > 0");
        }
        this.exec = exec;
        this.logger = logger;
        this.period = period;
        this.scheduler = scheduler;
        this.timerRemovedListener = timerRemovedListener;
        long now = scheduler.getExecutionTime();
        if (period > Long.MAX_VALUE - now) {
            this.nextRun = Long.MAX_VALUE;
        } else {
            this.nextRun = scheduler.getExecutionTime() + period;
        }
        scheduler.reschedule(this);
    }

    @Override
    public void resume() {
        if (state != TimerState.SHUTDOWN)
            state = TimerState.RUNNING;
    }

    @Override
    public void stop() {
        if (state != TimerState.SHUTDOWN)
            state = TimerState.PAUSED;
    }

    @Override
    public void addListener(TimerListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    @Override
    public boolean removeListener(TimerListener listener) {
        synchronized (listeners) {
        	return listeners.remove(listener);
        }
    }

    @Override
    public List<TimerListener> getListeners() {
        final List<TimerListener> result = new ArrayList<>(listeners.size());
        synchronized (listeners) {
            result.addAll(listeners);
        }
        return result;
    }

    protected TimerState getState() {
        return state;
    }

    // move nextRun time forward
    // requires synchronization on scheduler.timers
    protected void forward() {
        nextRun += period;
    }

    @Override
    public long getNextRunTime() {
        return nextRun;
    }

    /**
     * Sort criterion for priority queue used in scheduling of timers: Put timer
     * with next elapse time first.
     */
    @Override
    public int compareTo(ApplicationTimer o) {
        return Long.compare(nextRun, o.nextRun);
    }

    @Override
    public Boolean call() {
        synchronized (listeners) {
            try {
                idle = false;
                for (TimerListener l : listeners) {
                    if (state == TimerState.RUNNING) {
                        try {
                            l.timerElapsed(this);
                        } catch (Throwable t) {
                            logger.error("{}.timerElapsed(): ", l, t);
                        }
                    }
                }
            } finally {
                idle = true;
            }
        }
        return true;
    }

    @Override
    public void run() {
        call();
    }

    /**
     * false iff this timer's listener callbacks are currently being executed
     */
    protected boolean isIdle() {
        return idle;
    }

    @Override
    public boolean isRunning() {
        return (state == TimerState.RUNNING);
    }
    
    @Override
    public void setTimingInterval(long millis) {
    	scheduler.setTimingInterval(this, millis);
    }
    
    @Override
    public long getTimingInterval() {
        return period;
    }

    // set shutdown state, scheduler must not execute any more listeners and can
    // discard all timer references
    @Override
    public void destroy() {
        if (state != TimerState.SHUTDOWN && timerRemovedListener != null) {
        	try {
        		timerRemovedListener.timerRemoved(this);
        	} catch (Throwable e) {
        		logger.warn("Error in timerRemoved callback",e);
        	}
        }
        state = TimerState.SHUTDOWN;
    }

    @Override
    public long getExecutionTime() {
        return scheduler.getExecutionTime();
    }

}
