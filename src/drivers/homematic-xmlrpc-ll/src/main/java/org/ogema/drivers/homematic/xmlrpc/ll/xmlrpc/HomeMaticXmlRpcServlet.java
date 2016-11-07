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
package org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc;

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
