package de.iwes.ogema.remote.rest.connector.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.tools.SerializationManager;
import org.osgi.framework.BundleContext;

import de.iwes.ogema.remote.rest.connector.model.RestConnection;

// this task is never added to the execution chain, but only executed on app start
public class PushParentInitTask extends ConnectionTask {

	private final int level;
	
	public PushParentInitTask(RestConnection con, ApplicationManager appman, BundleContext ctx, int level) {
		super(con, appman, ctx);
		this.level = level; 
	}

	@Override
	protected int execute() throws Exception {
		int length;
		String remotePath = getRemotePath();
		final SerializationManager sman = appman.getSerializationManager(0, false, false);
		Resource r = con.getParent();
		final List<String> json = new ArrayList<>(level + 1);
		for (int i=0; i < level + 1; i++) {
			length = remotePath.length();
			remotePath = getRemoteParent(remotePath);
			final int newLength = remotePath.length();
			if (newLength == length)
				throw new IllegalArgumentException("Level too high... remote parent does not exist.");
			length = newLength;
			json.add(sman.toJson(r));
			r = r.getParent();
		}
		String jsonStr = null;
		for (String j : json) {
			final int subresIdx = j.indexOf("\"subresources\"");
			if (subresIdx < 0) {
				jsonStr = j;
				continue;
			}
			final int nextOpen = j.indexOf('[', subresIdx);
			final int nextClosed = j.indexOf(']', nextOpen);
			final StringBuilder sb = new StringBuilder();			
			sb.append(j.substring(0, nextOpen + 1));
			if (jsonStr != null) {
				sb.append("{ \"resource\" : ");
				sb.append(jsonStr);
				sb.append('}');
			}
			sb.append(j.substring(nextClosed));
			jsonStr = sb.toString();
		}
		
		
        try (final CloseableHttpClient client = getClient();
        		final CloseableHttpResponse resp = send(remotePath, client, "POST", new StringEntity(jsonStr, ContentType.APPLICATION_JSON), null)) {
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

	@Override
	public SingleValueResource getUpdateIntervalResource() {
		return null;
	}
	
}
