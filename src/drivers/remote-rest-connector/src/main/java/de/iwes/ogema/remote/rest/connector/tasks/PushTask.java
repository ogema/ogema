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
package de.iwes.ogema.remote.rest.connector.tasks;

import java.io.IOException;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.core.tools.SerializationManager;
import org.ogema.tools.resource.util.SerializationUtils;

import de.iwes.ogema.remote.rest.connector.model.RestConnection;
import de.iwes.ogema.remote.rest.connector.model.RestPullConfig;
import de.iwes.ogema.remote.rest.connector.model.RestPushConfig;

public class PushTask extends ConnectionTask {
    
    private final PushListener pushListener;
	
    public PushTask(final RestConnection con, final ApplicationManager appman, final TaskScheduler trigger) {
        super(con, appman);
        this.pushListener = new PushListener(this, trigger);
    }
    
    @Override
    public void close() {
    	pushListener.close();
    }
    
    @Override
    public SingleValueResource getUpdateIntervalResource() {
    	return con.pushInterval();
    }
    
    /**
     * 
     * @return
     *  	Http status code: 200 = ok
     * @throws Exception
     */
    @Override
    protected int execute() throws Exception {
    	return doPush();
    }
    
//    
    public int doPush() throws IOException {
    	Resource target = getTargetResource();
        CloseableHttpClient client = getClient();
        int i = 0;
        if (!con.pushConfig().isActive() && con.individualPushConfigs().isActive()) {
        	for (RestPushConfig config: con.individualPushConfigs().getAllElements()) {
        		if (!config.remoteRelativePath().isActive()) {
            		logger.error("Individual push configuration lacks relative path. Ignoring this. "+con.pushConfig());
            		i = 1000;
            		continue;
        		}
        		i = Math.max(pushIndividual(target, config, con, client),i);
        	}
        } else if (con.pushConfig().isActive()) {
        	if (con.pushConfig().remoteRelativePath().isActive()) {
        		logger.error("Global push configuration has a relative path. Ignoring this. "+con.pushConfig());
        		return 1000;
        	}
        	i = pushIndividual(target, con.pushConfig(), con, client);
        }
        return i;
    }
    
    @SuppressWarnings("unchecked")
	private int pushIndividual(Resource main, RestPushConfig pushConfig, RestConnection connection, CloseableHttpClient client) 
    		throws ClientProtocolException, IOException {
    	final String relativePath = (pushConfig.remoteRelativePath().isActive() ? pushConfig.remoteRelativePath().getValue() : null);
    	final Resource resource;
    	final boolean remove;
    	if (relativePath != null) {
    		remove = false;
			try {
				resource = main.getSubResource(relativePath);
				if (!resource.exists())
					throw new VirtualResourceException("Subresource " + relativePath + " configured for pushing does not exist");
			} catch (Exception e) {
				logger.error("Could not push subresource " + relativePath + " of " + main,e);
				return 1000;
			}
    	} else {
    		resource = main;
    		remove = true;
    	}
    	
    	final int depth = (pushConfig.depth().isActive() ? Math.max(0, pushConfig.depth().getValue()) : 0);
    	final boolean schedules = (pushConfig.schedules().isActive() ? pushConfig.schedules().getValue() : false);
    	final boolean references = (pushConfig.resolveReferences().isActive() ? pushConfig.resolveReferences().getValue() : false);
    	
    	final SerializationManager sman = appman.getSerializationManager();
    	sman.setMaxDepth(depth);
    	sman.setSerializeSchedules(schedules);
    	sman.setFollowReferences(references);
    	String json = sman.toJson(resource);
        // configuration resource for the RestConnector should not be pushed upstream, neither should be individual pull configs (unless they are explicitly configured to be pushed)
    	if (remove) {
    		String[] subresources = null;
    		if (relativePath == null && con.individualPullConfigs().isActive()) {
    			final List<RestPullConfig> pullConfigs = con.individualPullConfigs().getAllElements();
    			subresources = new String[pullConfigs.size()];
    			int i = 0;
    			for (RestPullConfig pullConfig: pullConfigs) {
    				subresources[i] = pullConfig.remoteRelativePath().getValue();
    				i++;
    			}
    		}
    		json = SerializationUtils.removeSubresources(json, new Class[]{RestConnection.class}, subresources, false);
    	}
    	if (logger.isTraceEnabled()) 
    		logger.trace("Pushing json to remote:\n" + json);
    	String remotePath = getRemotePath();
    	if (relativePath != null && !relativePath.isEmpty())
    		remotePath = remotePath + "/" + relativePath;
    	remotePath = appendUserInfo(remotePath);
    	HttpPut put = new HttpPut(remotePath);
        put.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        HttpResponse resp = client.execute(put);
        int code = resp.getStatusLine().getStatusCode();
        logger.debug("pushed resource {} to {}: {}", getTargetResource().getPath(), getRemotePath(), code);
        if(code <= 200) {
        	increaseIfActive(con.consecutiveSuccessfulPushCounter());
        	setZeroIfActive(con.consecutiveErrorPushCounter());
        } else {
            increaseIfActive(con.consecutiveErrorPushCounter());
            setZeroIfActive(con.consecutiveSuccessfulPushCounter());        	
        }
        return code;
    }
   
    
//    // TODO individual evaluation
    public boolean isRecursivePushTrigger() {
    	if (con.pushConfig().isActive()) 
    		return con.pushConfig().pushOnSubresourceChanged().isActive() && con.pushConfig().pushOnSubresourceChanged().getValue();
    	if (con.individualPushConfigs().isActive()) {
    		for (RestPushConfig config: con.individualPushConfigs().getAllElements()) {
    			if (config.pushOnSubresourceChanged().isActive() && config.pushOnSubresourceChanged().getValue())
    				return true;
    		}
    	}
    	return false;
    }
    
    protected boolean needsPushListener() {
    	if (isRecursivePushTrigger())
    		return true;
    	return !con.pushInterval().isActive();
    }

}
