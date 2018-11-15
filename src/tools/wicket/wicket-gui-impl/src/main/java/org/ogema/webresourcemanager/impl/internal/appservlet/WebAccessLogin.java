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
package org.ogema.webresourcemanager.impl.internal.appservlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.ogema.accesscontrol.Constants;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.webresourcemanager.impl.internal.MyWebResourceManager;
import org.ogema.webresourcemanager.impl.internal.websession.OgemaAuthentificatedWebsession;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

public class WebAccessLogin {

    private static WebAccessLogin instance;
    public static String NOT_LOGGED_INT = "not_loggedIn";

    private WebAccessLogin() {

    }

    public static WebAccessLogin getInstance() {
        if (WebAccessLogin.instance == null) {
            WebAccessLogin.instance = new WebAccessLogin();
        }
        return WebAccessLogin.instance;
    }

    public boolean login(HttpServletRequest req, String usr, String pwd) {

        final String OLDREQ_ATTR_NAME = "requestBeforeLogin";

        final PermissionManager permissionManager = MyWebResourceManager.getInstance().getPermissionManager();
        final UserAdmin ua = MyWebResourceManager.getInstance().getUa();
        final boolean auth = permissionManager.getAccessManager().authenticate(usr, pwd, true);
        HttpSession ses = ((ServletWebRequest) RequestCycle.get().getRequest()).getContainerRequest().getSession();

        if (auth) {
            User user = (User) ua.getRole(usr);
            Authorization author = ua.getAuthorization(user);

            MySessionAuth sauth = new MySessionAuth(author, permissionManager.getAccessManager(), user, ses);
            req.getSession(false).invalidate();
            ses = req.getSession(true);
            ses.setAttribute(Constants.AUTH_ATTRIBUTE_NAME, sauth);
            ses.setAttribute(OLDREQ_ATTR_NAME, null);
            OgemaAuthentificatedWebsession.get().setAttribute(Constants.AUTH_ATTRIBUTE_NAME, sauth);
            OgemaAuthentificatedWebsession.get().setRoles(author.getRoles()); 
            return true;
        }

        return false;
    }
}
