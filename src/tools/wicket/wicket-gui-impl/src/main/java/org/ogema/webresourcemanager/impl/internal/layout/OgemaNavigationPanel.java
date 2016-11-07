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
