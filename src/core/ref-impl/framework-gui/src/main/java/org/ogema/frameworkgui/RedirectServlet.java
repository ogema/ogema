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
