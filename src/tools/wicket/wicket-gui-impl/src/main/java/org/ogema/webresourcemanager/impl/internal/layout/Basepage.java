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
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.ogema.apps.wicket.ApplicationPage;
import org.ogema.apps.wicket.ApplicationPanel;
import org.ogema.apps.wicket.ComponentProvider;
import org.ogema.webresourcemanager.impl.internal.LoginPanel;
import org.ogema.webresourcemanager.impl.internal.MyWebResourceManager;
import org.ogema.webresourcemanager.impl.internal.layout.css.OgemaCustomCSS;
import org.ogema.webresourcemanager.impl.internal.websession.OgemaAuthentificatedWebsession;

public final class Basepage extends WebPage {

    private static final long serialVersionUID = -3472170461402384126L;

    private final OgemaNavigationPanel navigation = new OgemaNavigationPanel("OgemaNavigationPanel");
    private final SubNavigation subNavigation;
    private final MainContentPanel content = new MainContentPanel("MainContentPanel");
    private final FooterPanel footerPanel = new FooterPanel("FooterPanel");
    private final Label titleLabel;
    

    @Named("componentProvider")
    @Inject
    private volatile List<ComponentProvider> componentProvider;

    public Basepage() {
        super();
        titleLabel = new Label("title", "OGEMA");
        add(titleLabel);
        final Label customCSS = new Label("customCSS", "");
        customCSS.add(new AttributeModifier("href", OgemaCustomCSS.getCSSUrl()));
        add(customCSS);
        subNavigation = new SubNavigation("SubNavigation", this);
      
        add(navigation);
        add(subNavigation);
        add(content);
        add(footerPanel);

        if (OgemaAuthentificatedWebsession.get().isSignedIn()) {
            initBundleNavigation();
            ApplicationPanel welcome = ServletAppOverviewPanel.getInstance();
            welcome.initContent();
            content.replaceContent(welcome);
        } else {
            LoginPanel login = new LoginPanel();
            login.initContent();
            content.replaceContent(login);
        }
    }

    public void initBundleNavigation() {
        getSubNavigation().removeAllEntries();

        if (OgemaAuthentificatedWebsession.get().isSignedIn() == false) {
            return;
        }
        if (componentProvider == null) {
            componentProvider = new ArrayList<>();
        }

        for (ComponentProvider cp : componentProvider) {
            if (isAppPermitted(cp) == false) {
                continue;
            }
            try {
                String title = cp.getTitle();
                ResourceReference image = cp.getImage();
                List<ApplicationPanel> panels = cp.getPanel();
                ApplicationPage page = cp.getPage();
                final SubnavigationEntry entry = new SubnavigationEntry(image, title, page, panels);
                getSubNavigation().addSubMenuEntry(entry);
            } catch (Exception e) {
                logAppException(e.getMessage(), e);
            }
        }
 
    }

    private boolean isAppPermitted(ComponentProvider cp) {
        if (cp == null) {
            logAppException("ComponentProvider is null", null);
            return false;
        }
        String title = cp.getTitle();
        if (null == title || title.isEmpty()) {
            logAppException("AppTitle is Empty || null", null);
            return false;
        }
        ResourceReference image = cp.getImage();
        if (null == image) {
            logAppException("AppImage is null from (" + title + ")", null);
            return false;
        }
        List<ApplicationPanel> panels = null;
        try {
            panels = cp.getPanel();
        } catch (Throwable t) {
            if (null == panels || panels.isEmpty()) {
                logAppException("App has no AppPanels: " + title, t);
            }
            logAppException(t.getMessage(), t);
            t.printStackTrace();
            return false;
        }
        return true;
    }

    private void logAppException(String msg, Exception e) {
        if (MyWebResourceManager.getInstance() != null) {
            MyWebResourceManager.getInstance().getLogger().error(msg);
        }

        if (e != null) {
            System.err.print(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void logAppException(String msg, Throwable e) {
        if (MyWebResourceManager.getInstance() != null) {
            MyWebResourceManager.getInstance().getLogger().error(msg);
        }

        System.err.print(e.getLocalizedMessage());
        if (null != e) {
            e.printStackTrace();
        }
    }

    
    public void updateTitle(String title){
        IModel<String> t = new Model<>(title);
        titleLabel.setDefaultModel(t);        
    }

    protected SubNavigation getSubNavigation() {
        return subNavigation;
    }

    protected MainContentPanel getMaincontentPanel() {
        return content;
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        String file = "jquery-ui-1.10.3.custom/js/jquery-ui-1.10.3.custom.js";
        JavaScriptResourceReference ref = new JavaScriptResourceReference(Basepage.class, file);
        String url = urlFor(ref, null).toString();
        url = getRequestCycle().getUrlRenderer().renderFullUrl(Url.parse(url));
        IHeaderResponse response = container.getHeaderResponse();
        response.render(new JQueryUIResourceReference(url));

        response.render(CssHeaderItem.forReference(new CssResourceReference(Basepage.class,
                "jquery/flot/designFlot.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(Basepage.class,
                "jquery/flot/jquery.flot.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(Basepage.class,
                "jquery/flot/jquery.flot.time.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(Basepage.class,
                "jquery/flot/jquery.flot.selection.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(Basepage.class,
                "jquery/flot/jquery.flot.threshold.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(Basepage.class,
                "jquery/flot/jquery.flot.crosshair.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(Basepage.class,
                "jquery/flot/jquery.flot.stack.min.js")));

    }

}
