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
