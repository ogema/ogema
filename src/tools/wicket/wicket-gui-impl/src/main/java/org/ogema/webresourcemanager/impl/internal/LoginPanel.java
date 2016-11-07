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
package org.ogema.webresourcemanager.impl.internal;

import org.ogema.apps.wicket.ApplicationPanel;

public class LoginPanel extends ApplicationPanel {

    private static final long serialVersionUID = 7358041261408481720L;
    private final LoginForm FORM = new LoginForm("loginForm");

    @Override
    public void initContent() {
        add(FORM);
        setOutputMarkupId(true);
    }

    @Override
    public String getTitle() {
        return "Login";
    }

}
