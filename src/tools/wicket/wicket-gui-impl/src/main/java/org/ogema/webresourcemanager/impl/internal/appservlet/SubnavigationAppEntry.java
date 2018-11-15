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
