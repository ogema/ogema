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
package org.ogema.frameworkgui;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

/**
 * Redirects all GET requests to a configurable url (usually the system start or menu page).
 * This DS component is activated only if a configuration is present.
 * 
 * @author jlapp
 */
@Component(policy = ConfigurationPolicy.REQUIRE)
@Service(Servlet.class)
@Properties ({
    @Property(name = "osgi.http.whiteboard.servlet.pattern", value = "/*"),
    @Property(name = "osgi.http.whiteboard.context.select", value = "(osgi.http.whiteboard.context.name=*)"),
})
public class RedirectServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    /**
     * Servlet configuration parameter ({@value }) for the redirect url
     * (default={@value #CONFIG_PROPERTY_REDIRECT_DEFAULT}).
     */
    public static final String CONFIG_PROPERTY_REDIRECT = "redirect";
    /**
     * Default value ({@value }) for the {@value #CONFIG_PROPERTY_REDIRECT} parameter.
     */
    public static final String CONFIG_PROPERTY_REDIRECT_DEFAULT = "/ogema/index.html";
    String redirect = CONFIG_PROPERTY_REDIRECT_DEFAULT;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String s = config.getInitParameter(CONFIG_PROPERTY_REDIRECT);
        if (s != null) {
            redirect = s;
        }
    }    

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect(redirect);
    }
    
}
