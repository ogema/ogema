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
package org.ogema.drivers.homematic.xmlrpc.ll.internal;

import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;
import org.apache.xmlrpc.server.XmlRpcNoSuchHandlerException;
import org.apache.xmlrpc.webserver.XmlRpcServlet;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlapp
 */
public class HomeMaticXmlRpcServlet extends XmlRpcServlet {
    
    private static final long serialVersionUID = 1L;

    protected final BundleContext ctx;
    protected final RequestProcessorFactoryFactory procfac;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public HomeMaticXmlRpcServlet(BundleContext ctx, RequestProcessorFactoryFactory procfac) {
        this.ctx = ctx;
        this.procfac = procfac;
    }

    //overwritten to support simple method identifiers used by HomeMatic (e.g. "listDevices")
    @Override
    protected PropertyHandlerMapping newPropertyHandlerMapping(URL url) throws IOException, XmlRpcException {
        PropertyHandlerMapping phm = new PropertyHandlerMapping() {

            @Override
            public XmlRpcHandler getHandler(String pHandlerName) throws XmlRpcNoSuchHandlerException, XmlRpcException {
                if (!pHandlerName.contains(".")) {
                    return super.getHandler("default." + pHandlerName);
                } else {
                    return super.getHandler(pHandlerName);
                }
            }

        };
        phm.setRequestProcessorFactoryFactory(procfac);
        phm.addHandler("default", HomeMaticCalls.class);
        phm.addHandler("system", SystemCalls.class);
        phm.setVoidMethodEnabled(true);
        return phm;
    }

    @Override
    public void init(ServletConfig pConfig) throws ServletException {
        super.init(pConfig);
        logger.debug("HomeMatic XML-RPC servlet initialized");
    }

};
