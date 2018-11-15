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
package org.ogema.webresourcemanager.impl.internal;

import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ogema.webresourcemanager.impl.internal.appservlet.WebAccessLogin;
import org.ogema.webresourcemanager.impl.internal.layout.Basepage;
import org.ogema.webresourcemanager.impl.internal.websession.OgemaAuthentificatedWebsession;

public class LoginForm extends Form<Void> {

    private static final long serialVersionUID = 314296470564830480L;
    private final IModel<String> name = new Model<>("");
    private final IModel<String> passwd = new Model<>("");
    private final FeedbackPanel feedback = new FeedbackPanel("feedback");

    public LoginForm(final String id) {
        super(id);
        TextField<String> firstnameField = new TextField<>("name", name);
        PasswordTextField lastameField = new PasswordTextField("passwd", passwd);
        add(firstnameField);
        add(lastameField);
        add(new Label("userName", "Username"));
        add(new Label("userPwd", "Password"));
        feedback.setOutputMarkupId(true);
        add(feedback);
        setOutputMarkupId(true);
        AjaxButton button = new AjaxButton("button", this) {

        	private static final long serialVersionUID = 1L;

			@Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                target.add(feedback);
                final String user = name.getObject();
                final String pwd = passwd.getObject();
                boolean loggedin;
                try {
                    HttpServletRequest request = ((HttpServletRequest) getRequest().getContainerRequest());
                    loggedin = WebAccessLogin.getInstance().login(request, user, pwd);
                    if (loggedin) {
                        OgemaAuthentificatedWebsession.get().signIn(user, pwd);
                    }

                } catch (Exception e) {
                    loggedin = false;
                    e.printStackTrace();
                    error("Der PermissionManager wurde nicht in den OSGI-Kontext geladen: " + e.getMessage());
                }
                if (loggedin) {
                    setResponsePage(Basepage.class);
                } else {
                    error("Username and/or Password wrong " );

                }
            }
        };
        add(button);
    }
}
