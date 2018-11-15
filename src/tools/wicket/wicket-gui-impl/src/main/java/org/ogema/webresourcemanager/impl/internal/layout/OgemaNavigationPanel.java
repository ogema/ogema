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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.time.Duration;
import org.ogema.webresourcemanager.impl.internal.websession.OgemaAuthentificatedWebsession;

public class OgemaNavigationPanel extends Panel {

    private static final long serialVersionUID = 7551886738121151164L;

    public OgemaNavigationPanel(String id) {
        super(id);
        AjaxLink<Void> loginLogout;

        if (OgemaAuthentificatedWebsession.get().isSignedIn()) {
            loginLogout = getLogoutLink("login");
        } else {
            loginLogout = getLink("login", Basepage.class);
        }

        add(loginLogout);
        
        final Image image = new Image("image", new PackageResourceReference(OgemaNavigationPanel.class, "logo.png"));
        final AjaxLink<Void> homeLink = new AjaxLink<Void>("homeLink") {
            private static final long serialVersionUID = 572426689890698087L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                System.out.println("Home-Link");
                setResponsePage(Basepage.class);
            }
        };
        
        homeLink.add(image);
        add(homeLink);
        

    }

    private AjaxLink<Void> getLogoutLink(final String id) {

        final AjaxLink<Void> logoutL = new AjaxLink<Void>(id) {
            private static final long serialVersionUID = 572426689890698087L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                OgemaAuthentificatedWebsession.get().invalidate();
                OgemaAuthentificatedWebsession.get().clear();
                OgemaAuthentificatedWebsession.get().logout();
                setResponsePage(Basepage.class);
            }
        };

        final Label label = new Label("label", new AbstractReadOnlyModel<String>() {

            private static final long serialVersionUID = -1863117426871619288L;

            @Override
            public String getObject() {
                final long time = (System.currentTimeMillis() - OgemaAuthentificatedWebsession.get().getLoginTime()) / 60000;
                
                StringBuilder sb = new StringBuilder("Logout (");
                sb.append(OgemaAuthentificatedWebsession.get().getUsername()).append(") ")
                        .append(time).append(" ").append("Minutes logged in");
                return sb.toString();
            }
        });

        label.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(15)));
        logoutL.add(label);
        logoutL.setVisible(OgemaAuthentificatedWebsession.get().isSignedIn());

        return logoutL;
    }

    private AjaxLink<Void> getLink(final String id, final Class<? extends Basepage> responsePage) {

        final AjaxLink<Void> link = new AjaxLink<Void>(id) {
            private static final long serialVersionUID = 572426689890698087L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                setResponsePage(responsePage);
            }
        };
        final Label l = new Label("label", new AbstractReadOnlyModel<String>() {

            private static final long serialVersionUID = -1617734296545275800L;

            @Override
            public String getObject() {
                return "Login";
            }

        });
        link.add(l);
        return link;
    }

 /*   private AjaxLink<Void> getLanguageLink(final String id, final Locale locale) {
        final AjaxLink<Void> link = new AjaxLink<Void>(id) {

            private static final long serialVersionUID = -1949775457260878337L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                OgemaAuthentificatedWebsession.get().setLocale(locale);
                setResponsePage(getPage().getClass());

            }
        };

        final String imgName = locale.getCountry() + ".png";
        final Image image = new Image("image", new PackageResourceReference(OgemaNavigationPanel.class, imgName));
        link.add(image);

        return link;

    }
*/

}
