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
package org.ogema.application.manager.impl.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
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
    protected long period;
    protected long nextRun;

    /**
     * false iff this timer's listener callbacks are currently being executed
     */
    private volatile boolean idle = true;

    /**
     * Set of all objects receiving callback events from this timer.
     */
    protected final List<TimerListener> listeners = new ArrayList<>();

    protected volatile TimerState state = TimerState.RUNNING;

    protected ApplicationTimer(Executor exec, long period,
            DefaultTimerScheduler scheduler, Logger logger) {
        if (period < 1){
            throw new IllegalArgumentException("period must be > 0");
        }
        this.exec = exec;
        this.logger = logger;
        this.period = period;
        this.scheduler = scheduler;
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
            if (listeners.contains(listener)) {
                listeners.remove(listener);
                return true;
            } else {
                return false;
            }
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
    protected void forward() {
        nextRun += period;
    }

    protected long getNextRunTime() {
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
    public void setTimingInterval(long period) {
        if (period < 1){
            throw new IllegalArgumentException("period must be > 0");
        }
        this.period = period;
        nextRun = scheduler.getExecutionTime() + period;
        scheduler.reschedule(this);
    }
    
    @Override
    public long getTimingInterval() {
        return period;
    }

    // set shutdown state, scheduler must not execute any more listeners and can
    // discard all timer references
    @Override
    public void destroy() {
        state = TimerState.SHUTDOWN;
    }

    @Override
    public long getExecutionTime() {
        return scheduler.getExecutionTime();
    }

}
