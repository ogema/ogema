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

import java.io.Serializable;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.SessionAuth;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;

/**
 *
 * @author skarge
 */
public class MySessionAuth extends SessionAuth implements Serializable{
    
	private final User user;
    private static final long serialVersionUID = 4475654685476354162L;

    public MySessionAuth(Authorization auth, AccessManager accMan, User user, HttpSession ses) {
        super(auth, accMan, ses);
        this.user = user;
    }
    
    public User getUsr() {
    	return user;
    }
    
}
