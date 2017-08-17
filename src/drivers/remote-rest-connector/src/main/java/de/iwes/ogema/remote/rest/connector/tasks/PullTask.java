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
package de.iwes.ogema.remote.rest.connector.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.SingleValueResource;

import de.iwes.ogema.remote.rest.connector.model.RestConnection;
import de.iwes.ogema.remote.rest.connector.model.RestPullConfig;

public class PullTask extends ConnectionTask {
    
    private final PullListener pullListener;

    public PullTask(RestConnection con, ApplicationManager appman, final TaskScheduler trigger) {
        super(con, appman);
        this.pullListener = new PullListener(this, trigger);
    }

    @Override
    public void close() {
    	pullListener.close();
    }

    @Override
    protected int execute() throws Exception {
    	try {
    		return doPull();
		} catch(Exception e) {
	        increaseIfActive(con.consecutiveErrorPullCounter());
	        // is logged in RemoteRestConnector anyway
//	        logger.debug("Pull Task Failed "+ e); // e.g.: no internet connection; annyoing to print the stack trace all the time
	        throw e;
		}
    }
    
    @Override
    public SingleValueResource getUpdateIntervalResource() {
    	return con.pollingInterval();
    }
    
    public int doPull() throws IOException {
    	return doPull(getClient(), true);
    }
    
    private int doPull(CloseableHttpClient client, boolean pushIfNotFound) throws IOException {
    	int statusCode = 0;
    	ExecutorService exec= null;
    	if (!con.pullConfig().isActive() && con.individualPullConfigs().isActive()) {
    		for (RestPullConfig c: con.individualPullConfigs().getAllElements()) {
    			exec = getExecutor(exec);
    			int localStatusCode = pullWithRetry(c, pushIfNotFound, client, exec);
    			if (localStatusCode > statusCode)
    				statusCode = localStatusCode;
    		}
    	} else if (con.pullConfig().isActive()) {
    		exec = getExecutor(exec);
    		statusCode = pullWithRetry(con.pullConfig(), pushIfNotFound, client, exec);    		
    	}
    	final boolean success = (statusCode != 0 && statusCode <= 200);
    	if(success) {
            increaseIfActive(con.consecutiveSuccessfulPullCounter());
            setZeroIfActive(con.consecutiveErrorPullCounter());   		
    	} else {
            increaseIfActive(con.consecutiveErrorPullCounter());
            setZeroIfActive(con.consecutiveSuccessfulPullCounter());    		
    	}
    	return statusCode;
    }
    
    private final static ExecutorService getExecutor(ExecutorService exec) { 
    	if (exec != null)
    		return exec;
    	return Executors.newSingleThreadExecutor();
    }
    	
    private int pullWithRetry(RestPullConfig config, boolean pushIfNotFound, CloseableHttpClient client, ExecutorService exec) throws IOException {
    	int localStatusCode = pullElement(config, client);
		if (pushIfNotFound && localStatusCode == HttpServletResponse.SC_NOT_FOUND) { 
			pushOnInit(config, exec);
			localStatusCode = pullElement(config, client);
		}
		return localStatusCode;
    }
    
    /**
     * @param pullConfig
     * @param client
     * @param doRetryAfterPush
     * @return HttpStatusCode
     * @throws IOException
     */
	private int pullElement(RestPullConfig pullConfig, CloseableHttpClient client) throws IOException {
        String url = con.remotePath().getValue();
        final boolean individualResource = (pullConfig.remoteRelativePath().isActive());
        final String relativePath;
        if (individualResource) {
        	relativePath  = pullConfig.remoteRelativePath().getValue();
        	url = url + "/" + relativePath;
        }
        else 
        	relativePath = null;
    	if (pullConfig.depth().isActive())
    		url = url + "?depth=" + pullConfig.depth().getValue();
    	url = appendUserInfo(url);
    	boolean schedules = false;
    	boolean references = false;
    	if (pullConfig.schedules().isActive())
    		schedules = pullConfig.schedules().getValue();
    	if (pullConfig.resolveReferences().isActive())
    		references = pullConfig.resolveReferences().getValue();
    	if (schedules)
    		url = url + "&schedules=true";
    	if (references)
    		url = url + "&references=true";
        HttpGet get = new HttpGet(url);
        get.addHeader("Accept", "application/json");
        HttpResponse resp = client.execute(get);
        boolean forceUpdate = false;
        if (pullConfig.forceWrite().isActive())
        	forceUpdate = pullConfig.forceWrite().getValue();
        try (InputStream is = resp.getEntity().getContent()) {
	        StatusLine sl = resp.getStatusLine();
	        int responseCode = sl.getStatusCode();
	        logger.trace("Http response {}: {}", responseCode, sl.getReasonPhrase());
	        Resource targetRes = getTargetResource();       
	    	if (individualResource) 
	    		targetRes = targetRes.getSubResource(relativePath);
	        if (responseCode > 200) {
	        	logger.error("Not found: " + url); 
	        	if (logger.isTraceEnabled()) {
	        		logger.trace(new Scanner(is).useDelimiter("//A").next());
	        	}
	        	return responseCode;
	        }
	        try (Reader in = new InputStreamReader(is)) {
	            appman.getSerializationManager().applyJson(in, targetRes, forceUpdate); 
	        } 
	        if (logger.isTraceEnabled())
	        	logger.trace("updated resource {} from {}", getTargetResource().getPath(), getRemotePath());
	        return responseCode;
        }
    }
    
	/*
	// FIXME pushes all individual subresources, even if only one individual subresource is missing
    private void pushOnInit() {
    	if (!needsPushOnInit(con))
    		return;
		final int statusCode;
		final CloseableHttpClient client = getClient();
		try {
			statusCode = doPull(client, false);
			if (statusCode < 300) // remote target already exists
				return;
		} catch (Exception e) {}
		final ExecutorService exec = Executors.newSingleThreadExecutor();
		try {
			final InitPushTask initPushTask = new InitPushTask(con, appman, con.pullConfig(), client);
//				int code = pushIndividualOnInit(con.getParent(),null, remoteName, client, depth, schedules, references, true);
			// if we used the standard executor here, there would be a deadlock
			Future<ConnectionTask> future = exec.submit(initPushTask);
			// wait for push init; throws an exception if the call()-method does
			future.get(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			logger.warn("Initial push failed for connection {}",con,e);
			return;
		}
		for (RestPullConfig config: con.individualPullConfigs().getAllElements()) {
    		if (!needsIndividualPushOnInit(config)) 
    			continue;
			final String relativePath = config.remoteRelativePath().getValue();
			if (relativePath == null || relativePath.trim().isEmpty()) 
				throw new IllegalArgumentException("Inconsistent pullConfiguration; remote relative path missing in " + config.remoteRelativePath());
			try {
				final InitPushTask initPushTask = new InitPushTask(con, appman, config, client);
				// if we used the standard executor here, there would be a deadlock
				Future<ConnectionTask> future = exec.submit(initPushTask);
				// wait for push init; throws an exception if the call()-method does
				future.get(10, TimeUnit.SECONDS);
				logger.debug("Initial push for resource {} successful", relativePath);
			} catch (TimeoutException | InterruptedException e) {
				logger.info("Initial push failed for conenction {}, relative path {}: timeout.",con,relativePath);
			} catch (Exception e) {
				logger.warn("Initial push failed for connection {}, local target {}",con,relativePath, e);
			}
    	}
    }
    */
    
    private void pushOnInit(RestPullConfig config, final ExecutorService exec) {
    	if (!needsIndividualPushOnInit(config))
    		return;
		int statusCode = 1000;
		final CloseableHttpClient client = getClient();
		try {
			statusCode = pullElement(con.pullConfig(), client);
		} catch (Exception e) {}
		if (statusCode == HttpServletResponse.SC_NOT_FOUND) {
			try {
				final InitPushTask initPushTask = new InitPushTask(con, appman, con.pullConfig(), client);
	//				int code = pushIndividualOnInit(con.getParent(),null, remoteName, client, depth, schedules, references, true);
				// if we used the standard executor here, there would be a deadlock
				Future<ConnectionTask> future = exec.submit(initPushTask);
				// wait for push init; throws an exception if the call()-method does
				future.get(10, TimeUnit.SECONDS);
			} catch (Exception e) {
				logger.warn("Initial push failed for connection {}",con,e);
				return;
			}
		}
		if (config.equalsLocation(con.pullConfig()))
			return;
		final String relativePath = config.remoteRelativePath().getValue();
		if (relativePath == null || relativePath.trim().isEmpty()) 
			throw new IllegalArgumentException("Inconsistent pullConfiguration; remote relative path missing in " + config.remoteRelativePath());
		try {
			final InitPushTask initPushTask = new InitPushTask(con, appman, config, client);
			Future<ConnectionTask> future = exec.submit(initPushTask);
			// wait for push init; throws an exception if the call()-method does
			future.get(10, TimeUnit.SECONDS);
			logger.debug("Initial push for resource {} successful", relativePath);
		} catch (TimeoutException | InterruptedException e) {
			logger.info("Initial push failed for conenction {}, relative path {}: timeout.",con,relativePath);
		} catch (Exception e) {
			logger.warn("Initial push failed for connection {}, local target {}",con,relativePath, e);
		}
    }
    
    protected static boolean needsPushOnInit(RestConnection con) {
    	if (needsIndividualPushOnInit(con.pullConfig()))
    		return true;
    	if (!con.individualPullConfigs().isActive())
    		return false;
    	for (RestPullConfig config: con.individualPullConfigs().getAllElements()) {
    		if (needsIndividualPushOnInit(config))
    			return true;
    	}
    	return false;
    }
    
    protected static boolean needsIndividualPushOnInit(RestPullConfig pull) {
    	return pull.isActive() && pull.pushOnInit().isActive() && pull.pushOnInit().getValue();
    }
    
}
