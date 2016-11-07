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
package de.iwes.ogema.remote.rest.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
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
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.tools.listener.util.TransitiveValueListener;
import org.ogema.tools.resource.util.ListenerUtils;

import de.iwes.ogema.remote.rest.connector.model.ConnectionTask;
import de.iwes.ogema.remote.rest.connector.model.RestConnection;

@Component(specVersion = "1.2")
@Service(Application.class)
public class RemoteRestConnector implements Application {

    /**
     * interval at which the REST connection tasks scheduler will run
     * (milliseconds).
     */
    static final long SCHEDULING_INTERVAL = 500;

    private OgemaLogger logger;
    private ApplicationManager appMan;

    private final Collection<RestConnection> connections = new ArrayList<>();
    private final PriorityQueue<ConnectionTask> tasks = new PriorityQueue<>();
    private final Map<RestConnection, PushListener> pushListeners = new HashMap<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<Future<ConnectionTask>, ConnectionTask> pendingTasks = new HashMap<>();

    class PushListener implements ResourceValueListener<Resource> {

        final ConnectionTask conn;
        TransitiveValueListener<Resource> transitiveListener;
 
        public PushListener(ConnectionTask conn) {
        	 this.conn = conn;
		}

        @Override
        public void resourceChanged(Resource resource) {
        	logger.debug("Value callback for "+resource.getLocation());
            //XXX no retry and proper logging for push
        	//TODO: Remove this as soon as serialization manager transaction works correctly
            new CountDownDelayedExecutionTimer(appMan, 5000l) {
 				@Override
				public void delayedExecution() {
 		           	executor.submit(conn.createPushTask());
 				}
            };
        }

    }

    private final ResourceDemandListener<RestConnection> connectionListener = new ResourceDemandListener<RestConnection>() {

        @Override
        public void resourceAvailable(RestConnection resource) {
            connectionAdded(resource);
        }

        @Override
        public void resourceUnavailable(RestConnection resource) {
            connectionRemoved(resource);
        }

    };

    @Override
    public void start(ApplicationManager appManager) {
        this.appMan = appManager;
        this.logger = appManager.getLogger();

        appMan.getResourceAccess().addResourceDemand(RestConnection.class, connectionListener);
        appMan.createTimer(SCHEDULING_INTERVAL, new TimerListener() {

            @Override
            public void timerElapsed(Timer timer) {
                taskSchedulerStep();
            }

        });

        //XXX test setup
        //testSetup();

        logger.info("{} started", getClass().getName());
    }

    @Override
    public void stop(AppStopReason reason) {
        appMan.getResourceAccess().removeResourceDemand(RestConnection.class, connectionListener);
        logger.info("{} stopped", getClass().getName());
    }

    private void connectionAdded(RestConnection con) {
        logger.info("connection added: {}", con);
        connections.add(con);
        ConnectionTask ct = new ConnectionTask(con, appMan, logger);
        tasks.add(ct);
       	logger.debug("Push is "+ct.isPush()+ " pushResource:"+con.push().isActive()+"/"+con.push().getValue());
        if (ct.isPush()) {
        	boolean recursive = ct.isRecursivePushTrigger();
            PushListener l = new PushListener(ct);
           	logger.debug("Push recursive is "+recursive+ " recursiveResource:"+con.pushOnSubresourceChanged().isActive()+"/"+con.pushOnSubresourceChanged().getValue());
            if (recursive) {
            	TransitiveValueListener<Resource> transListener = 
            		ListenerUtils.registerTransitiveValueListener(ct.getTargetResource(), l, Resource.class, true);
            	l.transitiveListener = transListener;
            	logger.debug("Registered transitive listener for "+ct.getTargetResource().getLocation());

            }
            else
            	ct.getTargetResource().addValueListener(l, true); 
            logger.debug("resource {} configured to push updates to {}",
                    ct.getTargetResource().getPath(), ct.getRemotePath());
            if (!ct.getTargetResource().isActive()) {
                logger.warn("resource {} is configured for REST push but is inactive", ct.getTargetResource().getPath());
            }
            pushListeners.put(con, l);
        }
        taskSchedulerStep();
    }

    private void connectionRemoved(RestConnection con) {
        connections.remove(con);
        for (ConnectionTask t : tasks) {
            if (t.getConfigurationResource().equals(con)) {
                tasks.remove(t);
                if (t.isPush()) {
                    PushListener pl = pushListeners.remove(con);
                    if (pl != null && pl.transitiveListener !=  null)
                    	pl.transitiveListener.destroy();
                    else if (pl != null) 
                    	con.getParent().removeValueListener(pl);
                }
            }
        }
    }

    private void taskSchedulerStep() {
        for (Iterator<Map.Entry<Future<ConnectionTask>, ConnectionTask>> it = pendingTasks.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Future<ConnectionTask>, ConnectionTask> e = it.next();
            if (e.getKey().isDone()) {
                it.remove();
                ConnectionTask t = e.getValue();
                try {
                    e.getKey().get();
                } catch (InterruptedException ie) {
                    logger.warn("update task interrupted for " + t.getRemotePath(), ie);
                } catch (ExecutionException ee){
                    Throwable cause = ee.getCause();
                    logger.error("update failed for remote resource {}: {}: {}", t.getRemotePath(), cause.getClass(), cause.getLocalizedMessage());
                }
                if (connections.contains(t.getConfigurationResource())) {
                    t.advancePollingTime();
                    logger.debug("next update for {} from {} scheduled for {}", t.getTargetResource().getPath(), t.getRemotePath(), t.getPollingTime());
                    tasks.add(t);
                }
            }
        }

        while (!tasks.isEmpty() && tasks.peek().getPollingTime() < appMan.getFrameworkTime()) {
            ConnectionTask t = tasks.poll();
            pendingTasks.put(executor.submit(t), t);
        }
    }

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

}
