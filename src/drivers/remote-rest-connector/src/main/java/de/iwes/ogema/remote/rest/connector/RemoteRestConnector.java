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
package de.iwes.ogema.remote.rest.connector;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.CompoundResourceEvent;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.PatternChangeListener;
import org.ogema.core.resourcemanager.pattern.PatternListener;

import de.iwes.ogema.remote.rest.connector.model.ConnectionConfiguration;
import de.iwes.ogema.remote.rest.connector.model.RestConnection;
import de.iwes.ogema.remote.rest.connector.model.RestConnectionPattern;
import de.iwes.ogema.remote.rest.connector.model.RestPullConfig;
import de.iwes.ogema.remote.rest.connector.model.RestPushConfig;
import de.iwes.ogema.remote.rest.connector.tasks.ConnectionTask;
import de.iwes.ogema.remote.rest.connector.tasks.TaskScheduler;

/**
 *  Connects to the REST interface of another OGEMA gateway 
 *  and synchronizes parts of the resource trees of the
 *  two instances, either by pulling data from remote, or by pushing
 *  to remote. <br>
 *  In order to configure a resource for use by the Remote REST connector, attach a 
 *  decorator of type {@link RestConnection} to it. 
 */
@Component(specVersion = "1.2")
@Service(Application.class)
public class RemoteRestConnector implements Application, PatternListener<RestConnectionPattern>, PatternChangeListener<RestConnectionPattern>, TaskScheduler {

    /**
     * minimum interval at which the REST connection tasks scheduler will run
     * (milliseconds).<br>
     * All tasks whose update time is smaller than twice this value are ignored.
     */
	public static final long MIN_EXECUTION_STEP = 500;
	public static final long MAX_POLL_DURATION = 10000;

    private OgemaLogger logger;
    private ApplicationManager appMan;

    private final Map<RestConnection, ConnectionConfiguration> connections = new HashMap<>();
    private final PriorityQueue<ConnectionTask> tasks = new PriorityQueue<>();

    private ExecutorService executor;
    private final Map<Future<ConnectionTask>, ConnectionTask> pendingTasks = new HashMap<>();
    private final List<CorrectionListener> correctionListeners = new ArrayList<>();
    private Timer t; 
    // state variable
    private int failCounter = 0;
    
    @Override
    public void patternAvailable(RestConnectionPattern pattern) {
    	connectionAdded(pattern);
    	appMan.getResourcePatternAccess().addPatternChangeListener(pattern, this, RestConnectionPattern.class);
    }
    
    @Override
    public void patternUnavailable(RestConnectionPattern pattern) {
    	connectionRemoved(pattern);
    	appMan.getResourcePatternAccess().removePatternChangeListener(pattern, this);
    }
    
    @Override
    public void patternChanged(RestConnectionPattern instance, List<CompoundResourceEvent<?>> changes) {
    	connectionRemoved(instance);
    	connectionAdded(instance);
    }
    
    @Override
    public void start(ApplicationManager appManager) {
        this.appMan = appManager;
        this.logger = appManager.getLogger();
        this.executor  = Executors.newSingleThreadExecutor();
//        appMan.getResourceAccess().addResourceDemand(RestConnection.class, connectionListener);
        appManager.getResourcePatternAccess().addPatternDemand(RestConnectionPattern.class, this, AccessPriority.PRIO_LOWEST);
        // timing interval will be reset when a connection becomes available
        t = appMan.createTimer(1000000, new TimerListener() {

            @Override
            public void timerElapsed(Timer timer) {
                taskSchedulerStep();
            }

        });
        t.stop();
        //testSetup();

        logger.info("{} started", getClass().getName());
    }
    
    @Override
    public void stop(AppStopReason reason) {
    	if (t != null)
    		t.destroy();
    	if (appMan != null)
//    		appMan.getResourceAccess().removeResourceDemand(RestConnection.class, connectionListener);
    		appMan.getResourcePatternAccess().removePatternDemand(RestConnectionPattern.class, this);
    	if (logger != null)
    		logger.info("{} stopped", getClass().getName());
    	if (executor != null) {
    		try {
    			executor.shutdownNow();
    		} catch (AccessControlException e) {
    			// missing permission... ("java.lang.RuntimePermission" "modifyThread")
    			// nothing we can do about it
    			if (logger != null)
    				logger.warn("Could not shut down executor",e);
    		}
    	}
    	connections.clear();
    	pendingTasks.clear();
    	tasks.clear();
    	for (CorrectionListener cl : correctionListeners) {
    		cl.close();
    	}
    	correctionListeners.clear();
    	t = null;
    	appMan = null;
    	logger = null;
    	executor = null;
    }

    private void connectionAdded(final RestConnectionPattern con) {
        logger.info("connection added: {}", con);
        final ConnectionConfiguration connection = new ConnectionConfiguration(con.model, appMan, this);
        connections.put(con.model, connection);
        tasks.addAll(connection.getTasks());
        // fallback for deprecated settings in RestConnection
        repairConfig(con.model);
        resetTimer();
    }

    private void connectionRemoved(RestConnectionPattern con) {
        ConnectionConfiguration connection = connections.remove(con.model);
        if (connection == null) {
        	logger.warn("Connection removed callback for non-existent configuration {}",con);
        	return;
        }
        tasks.removeAll(connection.getTasks());
        connection.close();
        resetTimer();
    }
    
    // called when the execution time has changed, typically by triggering an immediate execution (triggerPush() subresource)
    @Override
    public void reschedule(ConnectionTask task) {
    	tasks.remove(task);
   		tasks.add(task);
   		taskSchedulerStep();
    }
    
    public void taskSchedulerStep() {
    	t.stop();
        for (Iterator<Map.Entry<Future<ConnectionTask>, ConnectionTask>> it = pendingTasks.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Future<ConnectionTask>, ConnectionTask> e = it.next();
            if (e.getKey().isDone()) {
                it.remove();
                ConnectionTask t = e.getValue();
                try {
                    e.getKey().get();
                } catch (InterruptedException | CancellationException ie) {
                    logger.warn("update task interrupted for " + t.getRemotePath() + ": " + ie);
                } catch (ExecutionException ee){
                    Throwable cause = ee.getCause();
                    logger.error("update failed for remote resource {}: {}: {}", t.getRemotePath(), cause.getClass(), cause.getLocalizedMessage());
                }
                if (connections.containsKey(t.getConfigurationResource())) {
                    t.advanceExecutionTime();
                    //logger.debug("next update for {} from {} scheduled for {}", t.getTargetResource().getPath(), t.getRemotePath(), t.getPollingTime());
                    if (t.getExecutionTime() != Long.MAX_VALUE)
                    	tasks.add(t);
                    else 
                    	correctionListeners.add(new CorrectionListener(t));
                }
            } 
            else {
            	long duration = e.getValue().getExecutionDuration();
            	if (duration > MAX_POLL_DURATION){
            		e.getKey().cancel(true); 
            	}
            }
        }
        while (!tasks.isEmpty() && tasks.peek().getExecutionTime() <= appMan.getFrameworkTime()) {
            ConnectionTask t = tasks.poll();
            pendingTasks.put(executor.submit(t), t);
        }
    	if (logger.isTraceEnabled())
    		logger.trace("Scheduler step completed at " + new Date(appMan.getFrameworkTime()) + ". Pending tasks: " + pendingTasks.size() + ", overall tasks " + tasks.size());
    	if (pendingTasks.size() > 0) {
    		if (failCounter < 10)
    			failCounter++;
    	}
    	else 
    		failCounter = 0;
        resetTimer();
    }
    
    // old default mode was: pull always, push if #push() resource is active
    @SuppressWarnings("deprecation")
	private static void repairConfig(final RestConnection con) {
    	final boolean globalPull = con.pullConfig().isActive();
    	final boolean globalPush = con.pushConfig().isActive();
    	if (con.pullConfigs().isActive() && !globalPull) {
    		if (!con.individualPullConfigs().exists())
    			con.individualPullConfigs().setAsReference(con.pullConfigs());
    		else if (!con.individualPullConfigs().isReference(false)) {
    			for (RestPullConfig el: con.pullConfigs().getAllElements()) {
    				boolean found = false;
    				for (RestPullConfig existing : con.individualPullConfigs().getAllElements()) {
    					if (existing.equalsLocation(el)) {
    						found = true;
    						break;
    					}
    				}
    				if (!found)
    					con.individualPullConfigs().add(el);
    			}
    			con.individualPullConfigs().activate(false);
    		}
    	}
    	if (!globalPush && con.push().isActive())
	    		con.pushConfig().create().activate(false);
    	if (con.pushOnSubresourceChanged().isActive() && con.pushOnSubresourceChanged().getValue()) {
    		if (con.pushConfig().isActive()) {
    			con.pushConfig().pushOnSubresourceChanged().<BooleanResource> create().setValue(true);
    			con.pushConfig().pushOnSubresourceChanged().activate(false);
    		}
    		else if (con.individualPushConfigs().isActive()) {
    			for (RestPushConfig config: con.individualPushConfigs().getAllElements()) {
    				if (config.pushOnSubresourceChanged().isActive())
    					continue;
    				config.pushOnSubresourceChanged().<BooleanResource> create().setValue(true);
    				config.pushOnSubresourceChanged().activate(false);
    			}
    		}
    	}
    	if (!globalPull && !globalPush && !con.individualPullConfigs().isActive() && !con.individualPushConfigs().isActive())
    		con.pullConfig().create().activate(false);
    }
    
    private void resetTimer() {
    	t.stop();
    	final long nextTime;
    	final long now = appMan.getFrameworkTime();
    	if (failCounter > 0) 
    		nextTime = now + MIN_EXECUTION_STEP * failCounter;
    	else {
    		final ConnectionTask next = tasks.peek();
    		if (next == null) {
    			logger.info("No further poll tasks");
    			return;
    		}
    		nextTime = next.getExecutionTime();
    	}
    	long diff = nextTime - now;
    	// may lead to high recursion
//    	if (diff <= 10) {
//			taskSchedulerStep(failCounter);
//			return;
//    	}
    	if (diff < MIN_EXECUTION_STEP)
    		diff = MIN_EXECUTION_STEP;
    	if (logger.isTraceEnabled())
    		logger.trace("Next execution time in {}s", (diff/1000));
    	t.setTimingInterval(diff);
    	t.resume();
    }
    
    /**
     * Keep track of configurations whose update time is too small, until it fits again.
     */
    private class CorrectionListener implements ResourceValueListener<SingleValueResource> {
    	
    	private final ConnectionTask task;
    	private final SingleValueResource target;
    	
    	public CorrectionListener(final ConnectionTask task) {
			this.task = task;
			this.target = task.getUpdateIntervalResource();
			target.addValueListener(this);
		}

		@Override
		public void resourceChanged(SingleValueResource resource) {
			final long newStep = ConnectionTask.getValue(resource);
			if (newStep >= 2 * MIN_EXECUTION_STEP) {
				target.removeValueListener(this);
				task.advanceExecutionTime();
				reschedule(task);
				logger.debug("Configuration update time admissible again {}: {} ms", target, newStep);
			}
		}
    	
    	public void close() {
    		target.removeValueListener(this);
    	}
    	
    }

    /*
    private void testSetup() {
        final IntegerResource src = appMan.getResourceManagement().createResource("source", IntegerResource.class);
        final IntegerResource target = appMan.getResourceManagement().createResource("target", IntegerResource.class);
        target.activate(false);
        src.activate(false);
        RestConnection conn = target.addDecorator("remote", RestConnection.class);

        conn.pollingInterval().create();
        conn.remotePath().create();
        conn.pollingInterval().setValue(1000);
        conn.remotePath().setValue("https://localhost:8443/rest/resources/source");
        conn.push().create();
        conn.push().setValue(true);
        conn.activate(true);

        appMan.createTimer(2000, new TimerListener() {
            @Override
            public void timerElapsed(Timer timer) {
                src.setValue(src.getValue() + 1);
                System.out.println("target value is '" + target.getValue() + "'");
                if (target.getValue() > 5) {
                    target.setValue(0);
                }
            }
        });
    }
    */

}
