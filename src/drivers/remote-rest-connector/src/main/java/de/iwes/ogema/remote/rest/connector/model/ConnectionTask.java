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
package de.iwes.ogema.remote.rest.connector.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.tools.resource.util.SerializationUtils;
import org.slf4j.Logger;

/**
 *
 * @author jlapp
 */
// FIXME must be in exported package, for the tests to work
public class ConnectionTask implements Comparable<ConnectionTask>, Callable<ConnectionTask> {
    
    private final RestConnection con;
    private final Logger logger;
    private final ApplicationManager appman;
    private volatile long nextPoll = 0;
    
    public ConnectionTask(RestConnection con, ApplicationManager appman, Logger logger) {
        this.con = con;
        this.appman = appman;
        this.logger = logger;
    }
    
    @Override
    public int compareTo(ConnectionTask o) {
        return Long.compare(nextPoll, o.nextPoll);
    }
    
    @Override
    public ConnectionTask call() throws Exception {
        doPull();
        nextPoll = appman.getFrameworkTime() + con.pollingInterval().getValue();
        return this;
    }
    
    public void doPull() throws IOException {
        //XXX user & password?
        String url = con.remotePath().getValue();
        if (con.remoteUser().isActive() && con.remotePw().isActive()) {
        	url = url + "?user=" + con.remoteUser().getValue() + "&pw=" + con.remotePw().getValue();
        }
        CloseableHttpClient client = getClient();
        HttpGet get = new HttpGet(url);
        get.addHeader("Accept", "application/json");
        HttpResponse resp = client.execute(get);
        boolean forceUpdate = false;
        if (con.forceWrite().isActive())
        	forceUpdate = con.forceWrite().getValue();
        InputStream is = resp.getEntity().getContent();
        StatusLine sl = resp.getStatusLine();
        int responseCode = sl.getStatusCode();
        logger.debug("Http response  {}: {}", responseCode, sl.getReasonPhrase());
        if (responseCode == HttpServletResponse.SC_NOT_FOUND) {
        	logger.error("Not found: " + url); 
        	if (logger.isDebugEnabled()) {
        		logger.debug(new Scanner(is).useDelimiter("//A").next());
        	}
        	return;
        }
        try (Reader in = new InputStreamReader(is)) {
            appman.getSerializationManager().applyJson(in, getTargetResource(), forceUpdate); 
        } 
        logger.debug("updated resource {} from {}", getTargetResource().getPath(), getRemotePath());
    }
    
    public int doPush() throws IOException {
        String json = appman.getSerializationManager().toJson(getTargetResource());
        // configuration resource for the RestConnector should not be pushed upstream
        json = SerializationUtils.removeSubresources(json, RestConnection.class, false);
        
        CloseableHttpClient client = getClient();
        HttpPut put = new HttpPut(getRemotePath());
        put.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        HttpResponse resp = client.execute(put);
        int code = resp.getStatusLine().getStatusCode();
        logger.debug("pushed resource {} to {}: {}", getTargetResource().getPath(), getRemotePath(), code);
        return code;
    }
   
    public Callable<Integer> createPushTask() {
        return new Callable<Integer>() {
            
            @Override
            public Integer call() throws Exception {
                return doPush();
            }
        };
    }
    
    public String getRemotePath() {
        return con.remotePath().getValue();
    }
    
    public long getPollingTime() {
        return nextPoll;
    }
    
    public Resource getTargetResource() {
        return con.getParent();
    }
    
    public RestConnection getConfigurationResource() {
        return con;
    }
    
    public boolean isPush() {
    	if (!con.push().isActive())
    		return false;
        return con.push().getValue();
    }
    
    public boolean isRecursivePushTrigger() {
    	if (!con.pushOnSubresourceChanged().isActive())
    		return false;
    	return con.pushOnSubresourceChanged().getValue();
    }

    /**
     * advance polling time according to pollingIntervall
     *
     * @return next polling time.
     */
    public long advancePollingTime() {
        nextPoll = appman.getFrameworkTime() + con.pollingInterval().getValue();
        return nextPoll;
    }

    //XXX reuse client; add option for disabling SSL hostname verification and certificate validation
    private CloseableHttpClient getClient() {
        SSLContext ctx;
        try {
            ctx = SSLContexts.custom().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
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
    
}
