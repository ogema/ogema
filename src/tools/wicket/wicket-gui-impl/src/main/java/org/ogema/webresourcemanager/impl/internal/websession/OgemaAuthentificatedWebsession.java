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
package org.ogema.webresourcemanager.impl.internal.websession;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

public class OgemaAuthentificatedWebsession extends AuthenticatedWebSession {

    private long loginTime = -1;
    private String username = null;
    private String password = null;
    private final Set<String> roles;

    public OgemaAuthentificatedWebsession(Request request) {
        super(request);
        roles = new HashSet<>();

    }

    private static final long serialVersionUID = 68751461687687867L;

    @Override
    public boolean authenticate(String username, String password) {
        this.username = username;
        this.password = password;
        loginTime = System.currentTimeMillis();
        return true;
    }

    public long getLoginTime() {
        return loginTime;
    }

    @Override
    public Roles getRoles() {
        Roles resultRoles = new Roles();
        if (isSignedIn()) {
            resultRoles.add(Roles.USER);
            resultRoles.addAll(roles);
            for (final String s : roles) {
                if ("ALL RESOURCES".equals(s)) {
                    resultRoles.add(Roles.ADMIN);
                    break;
                }
            }
        }

        return resultRoles;
    }

    public void setRoles(String[] roles) {
        this.roles.addAll(Arrays.asList(roles));

    }

    public void logout() {
        loginTime = -1;
        invalidate();
    }

    public static OgemaAuthentificatedWebsession get() {
        return (OgemaAuthentificatedWebsession) Session.get();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
