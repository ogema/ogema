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
package org.ogema.webresourcemanager.impl.internal.appservlet;

import java.io.Serializable;

public class SubnavigationAppEntry implements Serializable, Comparable<SubnavigationAppEntry> {

    private static final long serialVersionUID = 1477901007457951744L;
    private final AppsJsonGet app;
    private final String image;
    private final String name;
    private final String path;
    // <img class="applogo" src="/install/installedapps?action=getIcon&app={{app.id}}"/>

    public SubnavigationAppEntry(AppsJsonGet app) {
        super();
        this.app = app;
        this.image = "/install/installedapps?action=getIcon&app=" + app.getId();
        this.name = app.getMetainfo().get("Bundle_Name");
        this.path = app.getWebResourcePaths().get(0);
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return app.getMetainfo().get("Bundle_Version");
    }

    public String getDescription() {
        return app.getMetainfo().get("Bundle_Description");
    }

    @Override
    public int compareTo(SubnavigationAppEntry o) {
        return this.getName().compareTo(o.getName());
    }

}
