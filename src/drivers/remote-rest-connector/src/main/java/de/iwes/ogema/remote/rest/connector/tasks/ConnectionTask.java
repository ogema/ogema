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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;

import com.google.common.io.BaseEncoding;

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
    protected final BundleContext ctx;
    private volatile long nextExec = 0;
    private volatile Long lastExec = null;
    
    protected ConnectionTask(RestConnection con, ApplicationManager appman, BundleContext ctx) {
        this.con = con;
        this.appman = appman;
        this.logger = appman.getLogger();
        this.ctx = ctx;
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
    			throw new RuntimeException("Http request for " + con.remotePath().getValue() + " not successful: " + code);
    		else if (logger.isDebugEnabled())
    			logger.debug(this.getClass().getSimpleName()+" request for {}: {}",con.remotePath().getValue(), code);
		} finally {
			lastExec = null;
			nextExec = appman.getFrameworkTime() + getValue(getUpdateIntervalResource());
		}
        return this;
    }
    
    
    public final long advanceExecutionTime() {
    	final long pushInterval = getValue(getUpdateIntervalResource());
    	if (pushInterval < 2 * RemoteRestConnector.MIN_EXECUTION_STEP) {
    		if (pushInterval != 0)
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
    
    protected CloseableHttpResponse send(String path, final CloseableHttpClient client, final String method,
    		HttpEntity entity, final Map<String,String> headers) throws ClientProtocolException, IOException {
    	final HttpUriRequest request;
    	switch (method) {
    	case "GET":
    		request = new HttpGet(path);
    		break;
    	case "POST":
    		request = new HttpPost(path);
    		break;
    	case "PUT":
    		request = new HttpPut(path);
    		break;
    	case "DELETE":
    		request = new HttpDelete(path);
    		break;
    	case "HEAD":
    		request = new HttpHead(path);
    		break;
    	default:
    		throw new IllegalArgumentException("Unsupported method " + method);
    	}
    	if (entity != null) {
   			((HttpEntityEnclosingRequestBase) request).setEntity(entity);
    	}
    	if (headers != null) {
    		for (Map.Entry<String, String> entry : headers.entrySet()) {
    			request.setHeader(entry.getKey(), entry.getValue());
    		}
    	}
    	final CloseableHttpResponse resp0 = sendViaAuthService(client, request);
    	if (resp0 != null)
    		return resp0;
		logger.debug("No auth service configured for connection {}" ,con);
		final String user = con.remoteUser().getValue();
		final String pw = con.remotePw().getValue();
		if (!user.isEmpty() && !pw.isEmpty()) {
    		final String str = con.remoteUser().getValue() + ":" + con.remotePw().getValue();
    		request.addHeader("Authorization", "Basic " + BaseEncoding.base64().encode(str.getBytes(StandardCharsets.UTF_8)));
		} else {
			logger.warn("No authentication information available for connection {}, sending anyway...", con);
		}
		return client.execute(request);
    }
    
    protected static String appendParam(final String in, final String param) {
    	final StringBuilder sb = new StringBuilder(in);
    	if (in.indexOf('?') > 0)
    		sb.append('&');
    	else
    		sb.append('?');
    	sb.append(param);
    	return sb.toString();
    }

    private CloseableHttpResponse sendViaAuthService(final CloseableHttpClient client, final HttpUriRequest request) throws ClientProtocolException, IOException {
    	try {
    		return sendViaAuthServiceInternal(client, request);
    	} catch (NoClassDefFoundError e) { // optional dependency RemoteOgemaAuth
    		return null;
    	}
    }
    
    private CloseableHttpResponse sendViaAuthServiceInternal(final CloseableHttpClient client, final HttpUriRequest request) throws ClientProtocolException, IOException {
    	final URI uri = request.getURI();
    	final ServiceReference<org.ogema.tools.remote.ogema.auth.RemoteOgemaAuth> ref = getAuthService(uri.getHost(), uri.getPort());
    	final org.ogema.tools.remote.ogema.auth.RemoteOgemaAuth auth = ref != null ? ctx.getService(ref) : null;
    	if (auth == null)
    		return null;
		try {
			return auth.execute(client, request);
		} finally {
			try {
				ctx.ungetService(ref);
			} catch (Exception ignore) {}
    	}
    }
    
    private final ServiceReference<org.ogema.tools.remote.ogema.auth.RemoteOgemaAuth> getAuthService(final String host, final int port) {
    	final Collection<ServiceReference<org.ogema.tools.remote.ogema.auth.RemoteOgemaAuth>> services;
		try {
			services = ctx.getServiceReferences(org.ogema.tools.remote.ogema.auth.RemoteOgemaAuth.class, 
					"(&(" + org.ogema.tools.remote.ogema.auth.RemoteOgemaAuth.REMOTE_HOST_PROPERTY + "=" + host 
							+ ")(" + org.ogema.tools.remote.ogema.auth.RemoteOgemaAuth.REMOTE_PORT_PROPERTY + "=" + port + "))");
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}
    	return services != null && !services.isEmpty() ? services.iterator().next() : null;
    }

    
}
