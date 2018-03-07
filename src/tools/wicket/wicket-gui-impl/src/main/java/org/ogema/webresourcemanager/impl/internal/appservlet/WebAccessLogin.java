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
package org.ogema.webresourcemanager.impl.internal.appservlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.ogema.accesscontrol.Constants;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.SessionAuth;
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
