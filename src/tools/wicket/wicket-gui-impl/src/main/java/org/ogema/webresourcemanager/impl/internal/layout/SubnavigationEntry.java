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
package org.ogema.webresourcemanager.impl.internal.layout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.resource.ResourceReference;
import org.ogema.apps.wicket.ApplicationPage;
import org.ogema.apps.wicket.ApplicationPanel;

public class SubnavigationEntry implements Serializable, Comparable<SubnavigationEntry> {

    private static final long serialVersionUID = 1477902007457951744L;
    private final ResourceReference image;
    private final String text;
    private final ApplicationPage page;
    private final List<ApplicationPanel> appContent;

    public SubnavigationEntry(final ResourceReference image, final String text,
            final ApplicationPage page, List<ApplicationPanel> appContents) {
        super();
        this.image = image;
        this.text = text;
        this.page = page;

        if (appContents != null) {
            this.appContent = appContents;
        } else {
            this.appContent = new ArrayList<>();
        }
    }

    public ResourceReference getImage() {
        return image;
    }

    public String getText() {
        return text;
    }

    public ApplicationPage getPage() {
        return page;
    }

    @Override
    public int compareTo(SubnavigationEntry o) {
        return this.getText().compareTo(o.getText());
    }

    public List<ApplicationPanel> getAppContents() {
        return appContent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((appContent.get(0).getTitle() == null) ? 0 : appContent.get(0).getTitle().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SubnavigationEntry other = (SubnavigationEntry) obj;
        try {
            if (appContent.get(0).getTitle() == null) {
                if (other.appContent.get(0).getTitle() != null) {
                    return false;
                }
            } else if (!appContent.get(0).getTitle().equals(other.appContent.get(0).getTitle())) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("text=").append(text);

        if (page != null) {
            sb.append(", page=").append(page.getClass().getCanonicalName());
        }

        if (appContent != null) {
            sb.append(", panel=").append(appContent.getClass().getCanonicalName());
        }
        return sb.toString();
    }

}
