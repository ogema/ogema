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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.tools.SerializationManager;
import org.osgi.framework.BundleContext;

import de.iwes.ogema.remote.rest.connector.model.RestConnection;
import de.iwes.ogema.remote.rest.connector.model.RestPullConfig;

/**
 * These tasks are never added to the task list of the RemoteRestConnector,
 * but are only used internally by pull tasks which need to push on init (if their target
 * resource is not available yet on the server).
 */
public class InitPushTask extends ConnectionTask {
	
	private final RestPullConfig pullConfig;
	private final CloseableHttpClient client;

	protected InitPushTask(RestConnection con, ApplicationManager appman, BundleContext ctx, RestPullConfig pullConfig, CloseableHttpClient client) {
		super(con, appman, ctx);
		this.pullConfig = pullConfig;
		this.client = client;
	}
	
	@Override
	public SingleValueResource getUpdateIntervalResource() {
		return null;
	}

	@Override
	protected int execute() throws Exception {
		// not for the initial push task!
		/*
		if (pullConfig.depth().isActive())
			depth = pullConfig.depth().getValue();
		if (pullConfig.schedules().isActive())
			schedules = pullConfig.schedules().getValue();
		if (pullConfig.resolveReferences().isActive())
			references = pullConfig.resolveReferences().getValue();
		*/
		String relativePath = pullConfig.remoteRelativePath().getValue();
		if (relativePath != null && relativePath.trim().isEmpty())
			relativePath = null;
		final String customName = (relativePath == null ? getResourceName(con.remotePath().getValue()) : null);
		final Resource resource;
		if (relativePath == null) {
			resource = con.getParent();
		} else {
			Resource res = con.getParent();
			if (res == null)
				throw new IllegalArgumentException("Invalid top-level Remote REST connector pullConfiguration: " + pullConfig);
			res = res.getSubResource(relativePath);
			if (res == null) {
				throw new IllegalArgumentException("Invalid Remote REST connector pullConfiguration: referenced subresource "
							+ relativePath + " of " + pullConfig.getParent() + " does not exist");
			}
			resource = res;
		}
		final SerializationManager sman = appman.getSerializationManager(0, false, false);
    	String json = sman.toJson(resource);
    	
    	String remotePath = getRemotePath();
    	if (relativePath != null)
    		remotePath = remotePath + "/" + relativePath;
    	remotePath = getRemoteParent(remotePath);
    	
//    	JSONObject aux = new JSONObject();
//    	JSONArray arr = new JSONArray();
    	JSONObject aux2 = new JSONObject();
    	JSONObject targetJson = new JSONObject(json);
    	targetJson.remove("@type");
    	if (customName != null) { // overwrite values
    		targetJson.put("name", customName);
    		String path = targetJson.getString("path");
    		int idx = path.lastIndexOf('/');
    		path = path.substring(0, idx+1) + customName;
    		targetJson.put("path", path);
    	}
    	removeSubresourcesAtDepth(targetJson, 0);
    	final String identifier = getIdentifier(resource);
    	aux2.put(identifier, targetJson);
    	aux2.put("@type", "Resource");

    	// put version -> problematic permission-wise
    	/*
    	arr.put(aux2);
    	aux.put("subresources", arr);
    	aux.put("@type", "Resource");
    	aux.put("type", Resource.class.getName());
    	String resourcePathRemote = getRemoteResourcePath(remotePath);
    	aux.put("path", resourcePathRemote);
    	aux.put("name", getResourceName(remotePath));
    	*/
    	json = aux2.toString();
    	if (logger.isTraceEnabled())
    		logger.trace("Initial push for " + resource + " to target "  + remotePath + ":\n" + aux2.toString(4));
    	
        try (CloseableHttpResponse resp = send(remotePath, client, "POST", new StringEntity(json, ContentType.APPLICATION_JSON), null)) {
	        int code = resp.getStatusLine().getStatusCode();
	        logger.debug("pushed resource {} to {}: {}", getTargetResource().getPath(), getRemotePath(), code);
	        if(code <= 200) {
	        	increaseIfActive(con.consecutiveSuccessfulPushCounter());
	        	setZeroIfActive(con.consecutiveErrorPushCounter());
	        } else {
	            increaseIfActive(con.consecutiveErrorPushCounter());
	            setZeroIfActive(con.consecutiveSuccessfulPushCounter());
	            if (logger.isTraceEnabled())
	            	logger.trace("Response " + EntityUtils.toString(resp.getEntity(), "UTF-8"));
	        }
	        return code;
		}
	}
	
	
}
