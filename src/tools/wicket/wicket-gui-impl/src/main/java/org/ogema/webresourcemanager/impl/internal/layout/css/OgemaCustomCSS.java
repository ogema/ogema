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
package org.ogema.webresourcemanager.impl.internal.layout.css;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OgemaCustomCSS extends HttpServlet implements Serializable {

    private static final long serialVersionUID = 421435415415487542L;
    private final String css;
    private static String url;

    public OgemaCustomCSS() {
        
        final String alias = "/ogema.css";
        final String name = "config" + File.separator + "ogema.css";
        final File file = new File(name);
        if (file.exists()) {
            this.css = readCSS(file);
            OgemaCustomCSS.url = alias;
        } else {
            this.css = "";
            OgemaCustomCSS.url = "";
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        resp.setStatus(200);
        resp.getWriter().append(css);
    }

    private String readCSS(final File f) {
        final StringBuilder sb = new StringBuilder();
        try {
            final FileReader fr = new FileReader(f);
            final BufferedReader br = new BufferedReader(fr);
            String zeile;
            while ((zeile = br.readLine()) != null) {
                sb.append(zeile).append("\n");
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return sb.toString();
    }

    public static String getCSSUrl() {
        return url;
    }
}
