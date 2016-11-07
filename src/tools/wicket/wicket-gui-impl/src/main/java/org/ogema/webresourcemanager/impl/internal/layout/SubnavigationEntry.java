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
