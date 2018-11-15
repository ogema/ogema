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
