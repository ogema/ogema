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
package org.ogema.drivers.homematic.xmlrpc.ll;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSun15HttpTransport;
import org.apache.xmlrpc.client.XmlRpcSun15HttpTransportFactory;
import org.apache.xmlrpc.client.XmlRpcTransport;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HomeMatic;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ServiceMessage;
import org.ogema.drivers.homematic.xmlrpc.ll.api.XmlRpcStruct;
import org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc.DeviceDescriptionXmlRpc;
import org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc.MapXmlRpcStruct;
import org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc.ParameterDescriptionXmlRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class HomeMaticClient implements HomeMatic {

    final XmlRpcClient client;
    final Logger logger;
    private final URL url;
    private final HomematicType type;
    
    public static class DefaultServiceMessage implements ServiceMessage {
        
        private final String address;
        private final String ID;
        private final Object value;

        DefaultServiceMessage(String address, String ID, Object value) {
            this.address = address;
            this.ID = ID;
            this.value = value;
        }
        
        DefaultServiceMessage(Object[] a) {
            this.address = (String) a[0];
            this.ID = (String) a[1];
            this.value = a[2];
        }

        @Override
        public String getAddress() {
            return address;
        }

        @Override
        public String getID() {
            return ID;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("(%s, %s, %s)", address, ID, value);
        }
        
    }

    /**
     * @param urlString
     * @param user
     * 		may be null
     * @param pw
     * 		may be null
     * @throws IOException
     */
    public HomeMaticClient(String urlString, String user, String pw) throws IOException {
        this.url = new URL(urlString);
        this.type = HomematicType.forPort(url.getPort());
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(url);
        config.setEnabledForExtensions(true);
        config.setEncoding("ISO-8859-1"); //only used in modified XmlRpcStreamTransport
        config.setEnabledForExceptions(true);
        client = new XmlRpcClient();
        client.setConfig(config);
        // we cannot expect valid certificates in a home network
        if ("https".equalsIgnoreCase(url.getProtocol())) {
        	disableSslChecks(client);
        }
        if (user != null && pw != null) {
        	config.setBasicUserName(user);
        	config.setBasicPassword(pw);
        }
        this.logger = LoggerFactory.getLogger(getClass().getCanonicalName() + "-" + url.getHost());
    }

    @Override
    public void init(String url, String interfaceId) throws XmlRpcException {
        client.execute("init", new Object[]{url, interfaceId});
    }

    @Override
    public void ping(String callerId) throws XmlRpcException {
        client.execute("ping", new Object[]{callerId});
    }

    @Override
    @SuppressWarnings("unchecked")
    public DeviceDescription getDeviceDescription(String address) throws XmlRpcException {
        Object o = client.execute("getDeviceDescription", new Object[]{address});
        return new DeviceDescriptionXmlRpc((Map<String, Object>) o);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<DeviceDescription> listDevices() throws XmlRpcException {
        Object[] a = (Object[]) client.execute("listDevices", new Object[]{});
        List<DeviceDescription> rval = new ArrayList<>(a.length);
        for (Object o: a) {
            rval.add(new DeviceDescriptionXmlRpc((Map<String, Object>) o));
        }
        return rval;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, ParameterDescription<?>> getParamsetDescription(String address, String type) throws XmlRpcException {
        Map<String, Object> rawMap = (Map<String, Object>) client.execute("getParamsetDescription", new Object[]{address, type});
        Map<String, ParameterDescription<?>> rval = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e: rawMap.entrySet()) {
            rval.put(e.getKey(), new ParameterDescriptionXmlRpc<>((Map<String, Object>)e.getValue()));
        }
        return rval;
    }

    @Override
    @SuppressWarnings("unchecked")
    public XmlRpcStruct getParamset(String address, String paramset_key) throws XmlRpcException {
        return new MapXmlRpcStruct((Map<String, Object>)client.execute("getParamset", new Object[]{address, paramset_key}));
    }

    @Override
    public String getParamsetId(String address, String type) throws XmlRpcException {
        return (String) client.execute("getParamsetId", new Object[]{address, type});
    }

    @Override
    public void putParamset(String address, String paramset_key, XmlRpcStruct set) throws XmlRpcException {
        client.execute("putParamset", new Object[]{address, paramset_key, set.toMap()});
    }
    
    @Override
    public int getInstallMode() throws XmlRpcException {
        return ((Number) client.execute("getInstallMode", new Object[]{})).intValue();
    }

    @Override
    public void setInstallMode(boolean on, int time, int mode) throws XmlRpcException {
    	if (type == HomematicType.Ip)
    		client.execute("setInstallMode", new Object[]{on, time});
    	else
    		client.execute("setInstallMode", new Object[]{on, time, mode});
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Object> T getValue(String address, String value_key) throws XmlRpcException {
        return (T) client.execute("getValue", new Object[]{address, value_key});
    }

    @Override
    public void setValue(String address, String value_key, Object value) throws XmlRpcException {
        Object obj = client.execute("setValue", new Object[]{address, value_key, value});
        if(obj != null)
            logger.debug("setValue returned object of type: {}, value: {}", obj.getClass().getName(), obj.toString());
        else
            logger.debug("setValue returned object null");

    }
    
    @Override
    public boolean reportValueUsage(String address, String valueId, int refCounter) throws XmlRpcException {
        return (boolean) client.execute("reportValueUsage", new Object[]{address, valueId, refCounter});
    }
    
    @Override
    public void addLink(String sender, String receiver, String name, String description) throws XmlRpcException {
        client.execute("addLink", new Object[]{sender, receiver, name, description});
    }
    
    @Override
    public void removeLink(String sender, String receiver) throws XmlRpcException {
        client.execute("removeLink", new Object[]{sender, receiver});
    }

    @Override
    public List<String> getLinkPeers(String address) throws XmlRpcException {
        Object[] rval = (Object[]) client.execute("getLinkPeers", new Object[]{address});
        List<String> peers = new ArrayList<>(rval.length);
        for (Object o: rval) {
            peers.add(String.valueOf(o));
        }
        return peers;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getLinkInfo(String sender, String receiver) throws XmlRpcException {
        return (Map<String, Object>) client.execute("getLinkInfo", new Object[]{sender, receiver});
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getLinks(String address, int flags) throws XmlRpcException {
        Object[] rval = (Object[]) client.execute("getLinks", new Object[]{address, flags});
        List<Map<String, Object>> l = new ArrayList<>();
        for (Object o: rval) {
            l.add((Map<String, Object>)o);
        }
        return l;
    }

    @Override
    public List<ServiceMessage> getServiceMessages() throws XmlRpcException {
        Object o = client.execute("getServiceMessages", new Object[]{});
        if (o != null && !(o instanceof Object[])) {
            System.err.printf("getServiceMessages strange return value: '%s'%n", o);
            return Collections.emptyList();
        }
        Object[] rval = (Object[]) o;
        if (rval == null || rval.length == 0) {
            return Collections.emptyList();
        }
        List<ServiceMessage> l = new ArrayList<>(rval.length);
        for (Object e: rval) {
            l.add(new DefaultServiceMessage((Object[])e));
        }
        return l;
    }
    
    @Override
    public void deleteDevice(String address, int flags) throws XmlRpcException {
        client.execute("deleteDevice", new Object[]{address, flags});
    }

    @Override
    public void abortDeleteDevice(String address) throws XmlRpcException {
        client.execute("abortDeleteDevice", new Object[]{address});
    }

    @Override
    @SuppressWarnings("unchecked")
    public XmlRpcStruct rssiInfo() throws XmlRpcException {
        return new MapXmlRpcStruct((Map<String, Object>)client.execute("rssiInfo", new Object[]{}));
    }
 
    @Override
    public URL getServerUrl() {
    	return url;
    }
    
    private static void disableSslChecks(final XmlRpcClient client) {
    	client.setTransportFactory(new XmlRpcSun15HttpTransportFactory(client) {
    		
    		private final SSLSocketFactory factory;
    		private final Proxy proxyCopy;
    		private final HostnameVerifier noop = new HostnameVerifier() {
				
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			};
    		
    		{
    			try {
	    			final SSLContext ctx = SSLContext.getInstance("TLS"); // or SSL?
	    			ctx.init(null, new TrustManager[] {
	    					new X509TrustManager() {
								
								@Override
								public X509Certificate[] getAcceptedIssuers() {
									return null;
								}
								
								@Override
								public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
									// trust all
								}
								
								@Override
								public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
									// trust all
								}
							}
	    			}, new java.security.SecureRandom());
	    			factory = ctx.getSocketFactory();
    			} catch (KeyManagementException | NoSuchAlgorithmException e) {
    				throw new RuntimeException(e);
    			}
    			try {
    				final Field field = XmlRpcSun15HttpTransportFactory.class.getDeclaredField("proxy");
    				field.setAccessible(true);
    				proxyCopy = (Proxy) field.get(this);
    			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
					throw new RuntimeException(e);
				}
    		}
    		
    		@Override
    		public SSLSocketFactory getSSLSocketFactory() {
    			return factory;
    		}
    		
    		@Override
    	    public XmlRpcTransport getTransport() {
    	        XmlRpcSun15HttpTransport transport = new XmlRpcSun15HttpTransport(getClient()) {
    	        	
    	        	@Override
    	        	protected URLConnection getURLConnection() {
    	        		final URLConnection conn = super.getURLConnection();
    	        		if (conn instanceof HttpsURLConnection)
    	        			((HttpsURLConnection) conn).setHostnameVerifier(noop);
    	        		return conn;
    	        	}
    	        	
    	        };
    	        transport.setSSLSocketFactory(getSSLSocketFactory());
    	        transport.setProxy(proxyCopy);
    	        return transport;
    	    }
    		
    	});
    }
    
}
