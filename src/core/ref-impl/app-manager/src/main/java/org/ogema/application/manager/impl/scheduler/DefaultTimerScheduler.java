/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
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
import org.ogema.timer.TimerScheduler;
import org.slf4j.Logger;

@Component
@Service(TimerScheduler.class)
public class DefaultTimerScheduler implements TimerScheduler, PropertyChangeListener {

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
                    List<ApplicationTimer> batch = new ArrayList<>(10);
                    do {
                        next = timers.peek().getNextRunTime();
                        batch.add(timers.poll());
                    } while (!timers.isEmpty() && (next = timers.peek().getNextRunTime()) < now);
                    for (ApplicationTimer timer: batch){
                        processTimer(timer, now);
                    }
                    try {
                        float factor = clock.getSimulationFactor();
                        long waitTime = factor != 0
                                ? (long) ((next - now) / factor)
                                : 1000;
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
        clock.addPropertyChangeListener(this);
        start();
    }

    @Deactivate
    protected void deactivate(Map<String, ?> config) {
        clock.removePropertyChangeListener(this);
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
    public Timer createTimer(Executor excutor, Logger logger) {
        return new ApplicationTimer(excutor, Long.MAX_VALUE, this, logger);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        synchronized (timers) {
            timers.notifyAll();
        }
    }

}
