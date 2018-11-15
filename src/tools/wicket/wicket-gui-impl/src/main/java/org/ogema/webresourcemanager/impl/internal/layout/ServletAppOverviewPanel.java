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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.ogema.apps.wicket.ApplicationPanel;
import org.ogema.webresourcemanager.impl.internal.appservlet.SubnavigationAppEntry;
import org.ogema.webresourcemanager.impl.internal.appservlet.Util;

/**
 *
 * @author skarge
 */
public class ServletAppOverviewPanel extends ApplicationPanel {

	private static final long serialVersionUID = 1L;
	private static ServletAppOverviewPanel instance;
    private final ListView<SubnavigationAppEntry> servletList;
    private final ListView<SubnavigationEntry> wicketList;
    private final FeedbackPanel feedback = new CustomFeedbackPanel("feedback");

    private ServletAppOverviewPanel() {

        setOutputMarkupId(true);
        feedback.setOutputMarkupId(true);

        final List<SubnavigationAppEntry> servletApps = Util.getInstance().getServletApps();
        this.servletList = new ListView<SubnavigationAppEntry>("servletList", servletApps) {

			private static final long serialVersionUID = 1L;

			@Override
            protected void populateItem(final ListItem<SubnavigationAppEntry> item) {
                final SubnavigationAppEntry app = item.getModelObject();
                final ExternalLink link = new ExternalLink("link", app.getPath(), app.getName());
                item.add(link);
                WebMarkupContainer image = new WebMarkupContainer("image");
                image.add(new AttributeModifier("src", app.getImage()));
                item.add(image);
                item.add(new Label("buildBy", app.getVersion()));
                item.add(new Label("description", app.getDescription()));
            }
        };

        final List<SubnavigationEntry> wicketApps = SubNavigation.getInstance().getNavigationEntries();
        this.wicketList = new ListView<SubnavigationEntry>("wicketList", wicketApps) {

			private static final long serialVersionUID = 1L;

			@Override
            protected void populateItem(ListItem<SubnavigationEntry> item) {
                final SubnavigationEntry app = item.getModelObject();
                item.add(new Image("image", app.getImage()));
                item.add(new Label("buildBy", "buildBy"));
                final StringBuilder sb = new StringBuilder();
                for (ApplicationPanel entry : app.getAppContents()) {
                    if (entry != null) {
                        sb.append(entry.getTitle()).append(" ");
                    }
                }
                item.add(new Label("description", sb.toString()));
                AjaxLink<Void> link = new AjaxLink<Void>("link") {

					private static final long serialVersionUID = 1L;

					@Override
                    public void onClick(AjaxRequestTarget target) {

                        if (app.getAppContents() == null || app.getAppContents().isEmpty()) {
                            target.add(feedback);
                            info("This App has no working GUI");
                            return;
                        }
                        
                        final ApplicationPanel panel = app.getAppContents().get(0);
                         if (panel == null) {
                            target.add(feedback);
                            info("This App has no working GUI");
                            return;
                        }
                        final Basepage page = SubNavigation.getInstance().getBasePage();
                        final List<AppContentLinkDO> list = new ArrayList<>();
                        panel.removeAll();
                        panel.initContent();
                        page.getMaincontentPanel().replaceContent(panel);
                        page.updateTitle(panel.getTitle()); 

                        for (final ApplicationPanel content : app.getAppContents()) {
                            final String title = content.getTitle();
                            final MainContentPanel mainContent = page.getMaincontentPanel();
                            final AppContentLinkDO link = new AppContentLinkDO(content, title, mainContent, app.getText());
                            list.add(link);
                        }
                        page.getMaincontentPanel().getBreadcrumpPanel().update(list, app.getText());
                        target.add(page);
                    }
                };

                link.add(new Label("text", app.getText()));
                item.add(link);
            }

        };

    }

    @Override
    public void initContent() {
        addOrReplace(servletList);
        addOrReplace(wicketList);
        addOrReplace(feedback);
    }

    public static ServletAppOverviewPanel getInstance() {
        if (ServletAppOverviewPanel.instance == null) {
            ServletAppOverviewPanel.instance = new ServletAppOverviewPanel();
        }
        return ServletAppOverviewPanel.instance;
    }

    @Override
    public String getTitle() {
        return "OGEMA: Applications";
    }

}
