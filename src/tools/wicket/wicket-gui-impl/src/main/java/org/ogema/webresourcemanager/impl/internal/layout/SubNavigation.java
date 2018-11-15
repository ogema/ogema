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

import org.ogema.webresourcemanager.impl.internal.appservlet.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.ogema.apps.wicket.ApplicationPage;
import org.ogema.apps.wicket.ApplicationPanel;
import org.ogema.webresourcemanager.impl.internal.MyWebResourceManager;
import org.ogema.webresourcemanager.impl.internal.appservlet.SubnavigationAppEntry;
import org.ogema.webresourcemanager.impl.internal.contact.ContactPanel;
import org.ogema.webresourcemanager.impl.internal.websession.OgemaAuthentificatedWebsession;

public class SubNavigation extends Panel {

    public static final long serialVersionUID = -2829188402098775332L;
    private final List<SubnavigationEntry> navigationEntries = new ArrayList<>();
    private final List<SubnavigationAppEntry> servletPageList;
    private static SubNavigation instance;
    private Basepage page;

    public SubNavigation(final String id, final Basepage page) {
        super(id);

        if (OgemaAuthentificatedWebsession.get().isSignedIn()) {
            servletPageList = Util.getInstance().getServletApps();
            page.getMaincontentPanel().getBreadcrumpPanel().update(Collections.<AppContentLinkDO>emptyList(), "OGEMA Apps & Drivers");
        } else {
            servletPageList = new ArrayList<>();
        }
        final ListView<SubnavigationEntry> list = new ListView<SubnavigationEntry>("list", navigationEntries) {

            private static final long serialVersionUID = 330163425634308216L;

            @Override
            protected void populateItem(final ListItem<SubnavigationEntry> item) {
                final SubnavigationEntry entry = item.getModelObject();
                final ListView<ApplicationPanel> secondLevelList = getSecondLevelLinks(entry, page);

                if (entry.getAppContents().size() >= 1) {
                    item.setVisible(true);
                } else {
                    secondLevelList.setVisible(false);
                }
                final String id = Util.getJsID(entry.getText());
                final WebMarkupContainer pageLink = new WebMarkupContainer("pageLink");
                final WebMarkupContainer collapse = new WebMarkupContainer("collapse");
                collapse.add(pageLink);
                collapse.add(new AttributeModifier("id", id));
                collapse.add(secondLevelList);

                final AjaxLink<Void> link = getAppLink(page, entry);
                link.add(new Label("subtext", entry.getText()));
                link.add(new Image("img", entry.getImage()));
                pageLink.add(link);
                if (entry.getPage() == null) {
                    link.setVisible(false);
                    pageLink.setVisible(false);
                }

                final Link<Void> panelTitle = new Link<Void>("panelTitle") {
                    private static final long serialVersionUID = 85740243570557041L;

                    @Override
                    public void onClick() {
                    }
                };
                final String id2 = "#" + Util.getJsID(entry.getText());
                panelTitle.add(new AttributeModifier("href", id2));
                panelTitle.add(new Label("title", entry.getText()));
                final Image img = new Image("img", entry.getImage());
                panelTitle.add(img);
                item.add(collapse);
                item.add(panelTitle);
            }
        };

        add(list);
        add(getContactLink(page));
        //final OgemaSVGPanelMini ogema20 = new OgemaSVGPanelMini("ogemaSVG", 0, 0, 150f, 50f, "0 212.81 430.979 113.319", "new 0 212.81 430.979 113.319");
        Image ogema20 = new Image("ogemaFav", new PackageResourceReference(this.getClass(), "ogemaFav.png"));
        add(ogema20);
        addServletApplicationPage();
        this.page = page;
        SubNavigation.instance = this;
//        add(getAppLink(page));
    }

    private AjaxLink<Void> getAppLink(final Basepage page, final SubnavigationEntry entry) {
        final AjaxLink<Void> link = new AjaxLink<Void>("link") {

            private static final long serialVersionUID = 2666818913283350711L;

            @Override
            public void onClick(AjaxRequestTarget target) {

                final ApplicationPage fullScreenPage = entry.getPage();
                if (fullScreenPage != null) {
//                    if (Util.isUserpermitted(entry.getBundle())) {
                    fullScreenPage.removeAll();
                    fullScreenPage.initContent();
                    setResponsePage(fullScreenPage);
//                    } else {
//                        setResponsePage(AccessDeniedPage.class);
//                    }
                }

                page.initBundleNavigation();
                target.add(page);
            }
        };
        return link;
    }

    public void replaceAppContent(final Basepage page, ApplicationPanel panel, final SubnavigationEntry entry) {
        if (panel == null) {
            return;
        }

        final long initStart = System.currentTimeMillis();
        panel.removeAll();
        panel.initContent();
        final long initEnd = System.currentTimeMillis();
        final long breadStart = initEnd;
        if (entry != null) {
            updateBreadCrumbPanel(page, entry);
        }
        final long breadEnd = System.currentTimeMillis();

        final long replaceStart = System.currentTimeMillis();
        page.getMaincontentPanel().replaceContent(panel);
        page.updateTitle(panel.getTitle()); 
        final long replaceEnd = System.currentTimeMillis();

        try {
            MyWebResourceManager.getAppManager().getLogger().info(
                    "initApplicationPanel(" + panel.getTitle() + ") =" + (initEnd - initStart)
                    + " millis,\t replaceApplicationPanel =" + (replaceEnd - replaceStart)
                    + " millis,\t BreadCrump = " + (breadEnd - breadStart) + " millis");
        } catch (Exception e) {

        }
    }

    public void addSubMenuEntry(SubnavigationEntry entry) {

        if (navigationEntries.contains(entry)) {
            return;
        }
        navigationEntries.add(entry);
    }

    public void removeAllEntries() {
        navigationEntries.clear();
    }

    private void updateBreadCrumbPanel(final Basepage page, final SubnavigationEntry entry) {

        final List<AppContentLinkDO> list = new ArrayList<>();

        for (final ApplicationPanel content : entry.getAppContents()) {
            final String title = content.getTitle();
            final MainContentPanel mainContent = page.getMaincontentPanel();
            final AppContentLinkDO link = new AppContentLinkDO(content, title, mainContent, entry.getText());
            list.add(link);
        }
        page.getMaincontentPanel().getBreadcrumpPanel().update(list, entry.getText());
    }

    private ListView<ApplicationPanel> getSecondLevelLinks(final SubnavigationEntry entry, final Basepage page) {
        final List<ApplicationPanel> list = new ArrayList<>();
        for (ApplicationPanel panel : entry.getAppContents()) {
            if (panel != null) {
                list.add(panel);
            }
        }

        final ListView<ApplicationPanel> listView = new ListView<ApplicationPanel>("list2", list) {

            private static final long serialVersionUID = -2846282830860399739L;

            @Override
            protected void populateItem(final ListItem<ApplicationPanel> item) {
                final ApplicationPanel panel = item.getModelObject();
                final AjaxLink<Void> link = new AjaxLink<Void>("link2") {

                    private static final long serialVersionUID = 4310847746805700700L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        target.add(page);
                        replaceAppContent(page, panel, entry);
                    }
                };

                link.add(new Label("text2", panel.getTitle()));
                item.add(link);

            }
        };
        return listView;
    }

    private AjaxLink<Void> getContactLink(final Basepage page) {
        final AjaxLink<Void> link = new AjaxLink<Void>("contact") {

            private static final long serialVersionUID = 4310847746805700700L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                target.add(page);
                replaceAppContent(page, ContactPanel.getInstance(), null);
            }
        };

        return link;
    }

    private void addServletApplicationPage() {

        final WebMarkupContainer list3Container = new WebMarkupContainer("list3Container");
        final ResourceReference ref = new PackageResourceReference(SubNavigation.class, "appDefaultIcon.png");
        Image img = new Image("appDefaultIcon", ref);
        list3Container.add(img);

        final ListView<SubnavigationAppEntry> list = new ListView<SubnavigationAppEntry>("list3", servletPageList) {
            private static final long serialVersionUID = 85640243570559041L;

            @Override
            protected void populateItem(final ListItem<SubnavigationAppEntry> item) {
                final SubnavigationAppEntry external = item.getModelObject();
//                ExternalLink link = new ExternalLink("link3", external.getPath(), external.getName());
//                item.add(link);
                final AjaxLink<Void> link = new AjaxLink<Void>("link3") {

                    private static final long serialVersionUID = 4310847746805700700L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        target.add(page);
                        String url = external.getPath();
                        IFramePanel panel = new IFramePanel(url);
                        replaceAppContent(page, panel, null);
                        page.getMaincontentPanel().getBreadcrumpPanel().update(Collections.EMPTY_LIST, external.getName());
                    }
                };

                link.add(new Label("text3", external.getName()));
                item.add(link);
            }

        };

        list3Container.add(list);
        add(list3Container);
    }

    public boolean isEmpty() {
        return navigationEntries.isEmpty();
    }

    public static SubNavigation getInstance() {
        return SubNavigation.instance;
    }

    public List<SubnavigationEntry> getNavigationEntries() {
        return navigationEntries;
    }

    public Basepage getBasePage() {
        return page;
    }

}
