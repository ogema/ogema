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

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.slf4j.Logger;

import de.iwes.ogema.remote.rest.connector.RemoteRestConnector;
import de.iwes.ogema.remote.rest.connector.model.RestConnection;

/**
 * Base class for PullTasks and PushTasks
 * @author jlapp, cnoelle
 */
// superclass for pull and push connections
public abstract class ConnectionTask implements Comparable<ConnectionTask>, Callable<ConnectionTask> {
    
    protected final RestConnection con;
    protected final Logger logger;
    protected final ApplicationManager appman;
    private volatile long nextExec = 0;
    private volatile Long lastExec = null;
    
    protected ConnectionTask(RestConnection con, ApplicationManager appman) {
        this.con = con;
        this.appman = appman;
        this.logger = appman.getLogger();
    }
    
    /**
     * @return
     *  	Http status code: 200 = ok
     * @throws Exception
     */
    protected abstract int execute() throws Exception;
    
    public abstract SingleValueResource getUpdateIntervalResource();
    
    // override if required
    public void close() {};
    
    @Override
    public ConnectionTask call() throws Exception {
    	lastExec = appman.getFrameworkTime();
    	try {
    		final int code = execute();
    		if (code >= 300) 
    			throw new RuntimeException("Http request for " + con.remotePath() + " not successful: " + code);
    		else
    			logger.debug("Http request for {}: {}",con.remotePath(), code);
		} finally {
			lastExec = null;
			nextExec = appman.getFrameworkTime() + getValue(getUpdateIntervalResource());
		}
        return this;
    }
    
    
    public final long advanceExecutionTime() {
    	final long pushInterval = getValue(getUpdateIntervalResource());
    	if (pushInterval < 2 * RemoteRestConnector.MIN_EXECUTION_STEP) {
    		logger.warn("Pull/push interval too small: {} ms", pushInterval);
    		nextExec = Long.MAX_VALUE;
    	}
    	else
    		nextExec = appman.getFrameworkTime() + pushInterval;
        return nextExec;
    }
    
    // XXX remove IntegerResource pollingInterval!
    public static long getValue(SingleValueResource sv) {
    	if (sv instanceof TimeResource)
    		return ((TimeResource) sv).getValue();
    	if (sv instanceof IntegerResource)
    		return ((IntegerResource) sv).getValue();
   		return 0;
    }
    
    @Override
    public int compareTo(ConnectionTask o) {
        return Long.compare(nextExec, o.nextExec);
    }
    
    /**
     * @return
     * 		0, if task is not currently active, active duration in ms otherwise 
     */
    public long getExecutionDuration() {
    	final Long lastExec = this.lastExec;
    	if (lastExec == null)
    		return 0;
    	return appman.getFrameworkTime() - lastExec;
    }
    
    public static void increaseIfActive(IntegerResource res) {
    	if (res.isActive()) 
    		res.setValue(res.getValue()+1);
    }
    public static void setZeroIfActive(IntegerResource res) {
    	if (res.isActive()) 
    		res.setValue(0);
    }

    public String getRemotePath() {
        return con.remotePath().getValue();
    }
    
    public long getExecutionTime() {
        return nextExec;
    }
    
    public Resource getTargetResource() {
        return con.getParent();
    }
    
    public RestConnection getConfigurationResource() {
        return con;
    }
    
    protected void triggerImmediately() {
    	nextExec = appman.getFrameworkTime();
    }

    protected static void removeSubresourcesAtDepth(JSONObject resource, int depth) {
    	if (!resource.has("subresources"))
    		return;
    	if (depth == 0) {
    		resource.remove("subresources");
    		return;
    	}
    	JSONArray sub = resource.getJSONArray("subresources");
    	Iterator<Object> it = sub.iterator();
    	while (it.hasNext()) {
    		JSONObject obj  = (JSONObject) it.next();
    		JSONObject subresource = (JSONObject) obj.get(obj.keys().next());
    		removeSubresourcesAtDepth(subresource, depth-1);
    	}
    }
    
    /**
     * Takes a string of the form "https://localhost:8447/rest/resources/myResource?schedules=true"
     * and returns the resource path: "myResource"
     * @param fullPath
     * @return
     */
    protected static String getRemoteResourcePath(String fullPath) {
    	final String id = "/rest/resources/";
    	int idx = fullPath.indexOf(id);
    	if (idx > 0)
    		fullPath = fullPath.substring(idx + id.length());
    	idx = fullPath.indexOf("?");
    	if (idx > 0)
    		fullPath = fullPath.substring(0, idx);
    	return fullPath;
    }
    
    // XXX
    protected static String getIdentifier(Resource resource) {
    	if (resource instanceof SingleValueResource) {
	    	if (resource instanceof FloatResource)
	    		return FloatResource.class.getSimpleName();
	    	if (resource instanceof IntegerResource)
	    		return IntegerResource.class.getSimpleName();
	    	if (resource instanceof BooleanResource)
	    		return BooleanResource.class.getSimpleName();
	    	if (resource instanceof StringResource)
	    		return StringResource.class.getSimpleName();
	    	if (resource instanceof TimeResource)
	    		return TimeResource.class.getSimpleName();
    	}
    	if (resource instanceof ArrayResource) {
    		if (resource instanceof FloatArrayResource)
	    		return FloatArrayResource.class.getSimpleName();
	    	if (resource instanceof IntegerArrayResource)
	    		return IntegerArrayResource.class.getSimpleName();
	    	if (resource instanceof BooleanArrayResource)
	    		return BooleanArrayResource.class.getSimpleName();
	    	if (resource instanceof StringArrayResource)
	    		return StringArrayResource.class.getSimpleName();
	    	if (resource instanceof TimeArrayResource)
	    		return TimeArrayResource.class.getSimpleName();
	    	if (resource instanceof ByteArrayResource)
	    		return ByteArrayResource.class.getSimpleName();
    	}
    	if (resource instanceof Schedule) {
    		Resource parent = resource.getParent();
    		if (parent instanceof FloatResource)
	    		return "FloatSchedule";
	    	if (parent instanceof IntegerResource)
	    		return "IntegerSchedule";
	    	if (parent instanceof BooleanResource)
	    		return "BooleanSchedule";
	    	if (parent instanceof StringResource)
	    		return "StringSchedule";
	    	if (parent instanceof TimeResource)
	    		return "TimeSchedule";
    	}
    	return "resource";
    }
    
    protected static String getResourceName(String path) {
    	if (path.endsWith("/"))
    		path = path.substring(0, path.length()-1);
    	int idx = path.lastIndexOf('/');
    	if (idx == 0) 
    		return "";
    	return path.substring(idx+1);
    }
    
    protected static String getRemoteParent(String path) {
    	if (path.endsWith("/"))
    		path = path.substring(0, path.length()-1);
    	int idx = path.lastIndexOf('/');
    	if (idx < 0) 
    		return path; // should not happen
    	return path.substring(0, idx);
    }
    
    final static TrustStrategy TRUST_ALL = new TrustStrategy() {
		
		@Override
		public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			return true;
		}
	};

    //XXX reuse client; add option for disabling SSL hostname verification and certificate validation
    protected static CloseableHttpClient getClient() {
        SSLContext ctx;
        try {
            ctx = SSLContexts.custom().loadTrustMaterial(TRUST_ALL).build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
            throw new RuntimeException(ex);
        }
        
        CloseableHttpClient httpClient
                = HttpClients.custom()
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setSslcontext(ctx)
                .build();
        return httpClient;
    }

    protected String appendUserInfo(String path) {
    	StringBuilder sb = new StringBuilder(path);
    	int idx = path.indexOf('?');
    	if (idx < 0)
    		sb.append('?');
    	else
    		sb.append('&');
    	sb.append("user=").append(con.remoteUser().getValue()).append("&pw=").append(con.remotePw().getValue());
    	return sb.toString();
    }
    
}
