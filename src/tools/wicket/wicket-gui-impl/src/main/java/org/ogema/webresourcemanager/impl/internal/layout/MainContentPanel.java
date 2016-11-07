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

import org.apache.wicket.markup.html.panel.Panel;
import org.ogema.apps.wicket.ApplicationPanel;
import org.ogema.webresourcemanager.impl.internal.LoginPanel;
import org.ogema.webresourcemanager.impl.internal.websession.OgemaAuthentificatedWebsession;

public class MainContentPanel extends Panel {

    private static final long serialVersionUID = -793191221612429313L;
    public final static String CONTENT_ID = "content";
    private final BreadcrumbPanel breadcrumbPanel = new BreadcrumbPanel("BreadcrumbPanel");

    public MainContentPanel(String id) {
        super(id);
        add(breadcrumbPanel);
        LoginPanel login = new LoginPanel();

        boolean signedIn = OgemaAuthentificatedWebsession.get().isSignedIn();

        if (signedIn) {
            login.setVisible(false);
        } else {
            login.initContent();
        }
        add(login);
        setOutputMarkupId(true);
    }

    public void replaceContent(ApplicationPanel content) {
        if (content.getId().equals(CONTENT_ID)) {
            addOrReplace(content);
        }
    }

    public BreadcrumbPanel getBreadcrumpPanel() {
        return breadcrumbPanel;
    }

}
