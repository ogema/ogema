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
