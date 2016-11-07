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
package org.ogema.webresourcemanager.impl.internal.bookmark;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ogema.apps.wicket.ComponentProvider;
import org.ogema.webresourcemanager.impl.internal.websession.OgemaAuthentificatedWebsession;

//https://localhost/ogema/deeplink?u=u&p=p&t=<name der App>
public class Bookmark extends WebPage {

    private static final long serialVersionUID = -7638292205325919320L;

    @Named("componentProvider")
    @Inject
    private volatile List<ComponentProvider> componentProvider;

    public Bookmark(PageParameters param) {

        if (controllParams(param)) {
            return;
        }

        final String title = param.get("t").toString();
        for (final ComponentProvider cp : componentProvider) {
            try {
                if (title.equals(cp.getTitle())) {
                    cp.getPage().initContent();
                    setResponsePage(cp.getPage());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private boolean controllParams(PageParameters param) {
        final String username = param.get("u").toString();
        final String password = param.get("p").toString();
        final String title = param.get("t").toString();

        if (title == null || title.isEmpty()) {
            return false;
        }

        try {
            return OgemaAuthentificatedWebsession.get().signIn(username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

}
