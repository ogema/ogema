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
