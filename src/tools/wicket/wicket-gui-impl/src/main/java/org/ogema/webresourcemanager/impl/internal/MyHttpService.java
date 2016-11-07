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
package org.ogema.webresourcemanager.impl.internal;

import java.io.Serializable;
import javax.servlet.Servlet;
import org.ogema.webresourcemanager.impl.internal.layout.css.OgemaCustomCSS;
import org.osgi.service.http.HttpService;

public class MyHttpService implements Serializable {

    private static final long serialVersionUID = 2908870049432894456L;
    private final HttpService httpService;

    public MyHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void registerCustomCSS() {
        OgemaCustomCSS css = new OgemaCustomCSS();
        if (OgemaCustomCSS.getCSSUrl().isEmpty() || "".equals(OgemaCustomCSS.getCSSUrl())) {
            return;
        }
        try {
            httpService.registerServlet(OgemaCustomCSS.getCSSUrl(), css, null, null);
            System.out.println("CSS: " + OgemaCustomCSS.getCSSUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterWebResource(String alias) {
        if (httpService != null) {
            httpService.unregister(alias);
        } else {
            throw new RuntimeException("Cannot unregister Servlet " + alias + " because httpService == null");
        }
    }

    public String registerWebResource(String alias, String name) {
        alias = formatAlias(alias);
        try {
            if (httpService != null) {
                httpService.registerResources(alias, name, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(alias + " allready registred", e);
        }
        return alias;
    }

    public String registerWebResource(String alias, Servlet servlet) {
        alias = formatAlias(alias);

        try {
            if (httpService != null) {
                httpService.registerServlet(alias, servlet, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(alias + " allready registred", e);
        }
        return alias;
    }

    private String formatAlias(String alias) {
        if (alias.startsWith("/")) {
            alias = alias.substring(1);
        }
        alias = "/apps/" + alias;
        return alias;
    }

}
