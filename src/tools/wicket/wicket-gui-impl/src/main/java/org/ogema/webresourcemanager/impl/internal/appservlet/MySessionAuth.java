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
