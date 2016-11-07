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
