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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.tools.SerializationManager;
import org.ogema.tools.resource.util.SerializationUtils;

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

	protected InitPushTask(RestConnection con, ApplicationManager appman, RestPullConfig pullConfig, CloseableHttpClient client) {
		super(con, appman);
		this.pullConfig = pullConfig;
		this.client = client;
	}
	
	@Override
	public SingleValueResource getUpdateIntervalResource() {
		return null;
	}

	@Override
	protected int execute() throws Exception {
		int depth = 0;
		boolean schedules = false;
		boolean references = false;
		if (pullConfig.depth().isActive())
			depth = pullConfig.depth().getValue();
		if (pullConfig.schedules().isActive())
			schedules = pullConfig.schedules().getValue();
		if (pullConfig.resolveReferences().isActive())
			references = pullConfig.resolveReferences().getValue();
		String relativePath = pullConfig.remoteRelativePath().getValue();
		if (relativePath != null && relativePath.trim().isEmpty())
			relativePath = null;
		final boolean remove = relativePath == null;
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
		final SerializationManager sman = appman.getSerializationManager();
    	sman.setMaxDepth(depth);
    	sman.setSerializeSchedules(schedules);
    	sman.setFollowReferences(references);
    	String json = sman.toJson(resource);
        // configuration resource for the RestConnector should not be pushed upstream
    	if (remove)
    		json = SerializationUtils.removeSubresources(json, RestConnection.class, false);
    	
    	String remotePath = getRemotePath();
    	if (relativePath != null)
    		remotePath = remotePath + "/" + relativePath;
    	remotePath = getRemoteParent(remotePath);
    	
    	JSONObject aux = new JSONObject();
    	JSONArray arr = new JSONArray();
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
    	removeSubresourcesAtDepth(targetJson, depth);
    	final String identifier = getIdentifier(resource);
    	aux2.put(identifier, targetJson);
    	
    	arr.put(aux2);
    	aux.put("subresources", arr);
    	aux.put("@type", "Resource");
    	aux.put("type", Resource.class.getName());
    	String resourcePathRemote = getRemoteResourcePath(remotePath);
    	aux.put("path", resourcePathRemote);
    	aux.put("name", getResourceName(remotePath));
    	json = aux.toString();
    	
    	if (logger.isTraceEnabled())
    		logger.trace("Initial push for " + resource + " to target "  + remotePath + ":\n" + aux.toString(4));
    	
    	HttpPut put = new HttpPut(appendUserInfo(remotePath));
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
	
	
}
