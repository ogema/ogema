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

import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Executor;

import org.apache.felix.scr.annotations.Activate;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.application.Timer;
import org.ogema.timer.TimerRemovedListener;
import org.ogema.timer.TimerScheduler;
import org.slf4j.Logger;

@Component
@Service(TimerScheduler.class)
public class DefaultTimerScheduler implements TimerScheduler, FrameworkClock.ClockChangeListener {

    protected final PriorityQueue<ApplicationTimer> timers = new PriorityQueue<>();
    protected Thread dispatchThread;

    @Reference
    protected FrameworkClock clock;

    private final Runnable dispatchImpl = new Runnable() {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                synchronized (timers) {
                    if (timers.isEmpty()) {
                        try {
                            timers.wait();
                        } catch (InterruptedException ex) {
                            break;
                        }
                        continue;
                    }
                    long now = clock.getExecutionTime();
                    long next = timers.peek().getNextRunTime();
                    if (next <= now) {
                        processTimer(timers.poll(), now);
                    }
                    try {
                        float factor = clock.getSimulationFactor();
                        long waitTime = factor != 0
                                ? (long) ((next - now) / factor)
                                : next - now;
                        if (waitTime > 0) {
                            timers.wait(waitTime);
                        }
                    } catch (InterruptedException ex) {
                        // shut down
                        break;
                    }
                }
            }
        }

    };

    private void processTimer(ApplicationTimer timer, long executionTime) {
        //open synchronization issues: possible to enqueue callback for paused/stopped timer.
        switch (timer.getState()) {
            case PAUSED:
                timer.forward();
                timers.offer(timer);
                break;
            case RUNNING:
                if (timer.isIdle()) {
                    executeTimer(timer, executionTime);
                }
                timer.forward();
                timers.offer(timer);
                break;
            case SHUTDOWN:
                break;
        }
    }

    @Activate
    protected void activate(Map<String, ?> config) {
        dispatchThread = new Thread(dispatchImpl);
        dispatchThread.setName("OGEMA timer dispatcher");
        dispatchThread.setDaemon(true);
        clock.addClockChangeListener(this);
        start();
    }

    @Deactivate
    protected void deactivate(Map<String, ?> config) {
        clock.removeClockChangeListener(this);
        shutdown();
    }

    /*
     * public Timer createTimer(ApplicationManager app, long period) { ApplicationTimer t = new ApplicationTimer(app,
     * period, this); synchronized (timers) { timers.offer(t); timers.notifyAll(); } return t; }
     */
    /**
     * (Re-) schedule a timer for exection. This is used for new timers
     * registering with the scheduler, and when a timer needs to be re-scheduled
     * due to change in its properties.
     *
     * @param timer ApplicationTimer to be inserted into the scheduling queue.
     */
    protected void reschedule(ApplicationTimer timer) {
        synchronized (timers) {
            timers.remove(timer);
            timers.offer(timer);
            timers.notifyAll();
        }
    }

     protected void setTimingInterval(ApplicationTimer timer, long period) {
        if (period < 1){
            throw new IllegalArgumentException("period must be > 0");
        }
        synchronized(timers) {
	        timer.period = period;
	        timer.nextRun = getExecutionTime() + period;
	        reschedule(timer);
        }
    }
    
    /**
     * Shut down this.
     */
    private void shutdown() {
        synchronized (timers) {
            for (ApplicationTimer t : timers) {
                t.destroy();
            }
            timers.clear();
            dispatchThread.interrupt();
        }
    }

    private void executeTimer(ApplicationTimer t, long time) {
        t.exec.execute(t);
    }

    // starts the scheduler thread.
    private void start() {
        dispatchThread.start();
    }

    protected long getExecutionTime() {
        return clock.getExecutionTime();
    }

    @Override
    public Timer createTimer(Executor executor, Logger logger) {
        return new ApplicationTimer(executor, Long.MAX_VALUE, this, logger);
    }
    
    @Override
    public Timer createTimer(Executor executor, Logger logger, TimerRemovedListener listener) {
    	return new ApplicationTimer(executor, Long.MAX_VALUE, this, logger, listener);
    }

    @Override
    public void clockChanged(FrameworkClock.ClockChangedEvent e) {
        synchronized (timers) {
            timers.notifyAll();
        }
    }

}
