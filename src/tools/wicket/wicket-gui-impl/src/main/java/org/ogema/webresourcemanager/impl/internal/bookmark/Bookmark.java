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
