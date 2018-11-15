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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

public class BreadcrumbPanel extends Panel {

    private static final long serialVersionUID = 41681948787568L;
    private final List<AppContentLinkDO> links = new ArrayList<>();
    private final Label nameLabel = new Label("name", "");
    private static String subSite = "";

    public BreadcrumbPanel(String id) {
        super(id);
        nameLabel.setOutputMarkupId(true);
        add(nameLabel);

        final ListView<AppContentLinkDO> list = new ListView<AppContentLinkDO>("list", links) {

            private static final long serialVersionUID = 846435156456134l;

            @Override
            protected void populateItem(final ListItem<AppContentLinkDO> item) {
                final AppContentLinkDO linkDO = item.getModelObject();
                item.setOutputMarkupId(true);
                if (BreadcrumbPanel.subSite.equals(linkDO.getText())) {
                    item.add(new AttributeModifier("class", "active"));
                } else {
                    item.add(new AttributeModifier("class", ""));
                }

                final AjaxLink<Void> link = new AjaxLink<Void>("link") {
                    private static final long serialVersionUID = -2901981673537690564L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        linkDO.getContent().removeAll();
                        linkDO.getContent().initContent();
                        linkDO.getContent().setOutputMarkupId(true);
                        linkDO.getMainContent().replaceContent(linkDO.getContent());
                        nameLabel.setDefaultModelObject(linkDO.getBundleName());
                        target.add(nameLabel);
                        target.add(item);
                        item.add(new AttributeModifier("class", "active"));
                        target.add(linkDO.getMainContent());
                        BreadcrumbPanel.subSite = linkDO.getText();
                    }
                };
                Label label = new Label("breadtext", linkDO.getText());
                link.add(label);
                item.add(link);
            }
        };

        add(list);

        if (links.isEmpty()) {
            setVisible(true);
        }
    }

    public void update(List<AppContentLinkDO> panels, String bundleName) {
        links.clear();
        links.addAll(panels);
        nameLabel.setDefaultModelObject(bundleName);

    }

    public boolean isEmpty() {
        return links.isEmpty();
    }

}
