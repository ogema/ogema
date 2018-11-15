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
package org.ogema.accesscontrol;

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.Permission;
import java.security.ProtectionDomain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.core.application.ApplicationManager;

/**
 * This service is registered by the framework (security bundle). It can be used by trusted applications
 * to authenticate remote (machine-)users or applications in a servlet request, and to retrieve an 
 * {@link ApplicationManager} whose ResourcePermissions reflect the user and/or app permissions. <br>
 * It is used in particular by the OGEMA REST interface.
 */
public interface RestAccess {

	/**
	 * Authenticate either an app via its one time password, or a REST user via its 
	 * credentials or by means of a registered {@link Authenticator} service. If the user
	 * cannot be authenticated, a {@link HttpServletResponse#SC_FORBIDDEN} error is 
	 * sent, the response is submitted and null is returned. In this case the consumer of this
	 * service must not write any further data to the response.<br>
	 * If the authentication is successful, an {@link ApplicationManager} is returned which can be
	 * used to perform resource operations on behalf of the app and the logged-in natural-user 
	 * (in case of app authentication) or of the remote machine-user. 
	 *   
	 * @param req
	 * @param resp
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	ApplicationManager authenticate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
	
	/**
	 * Like {@link #authenticate(HttpServletRequest, HttpServletResponse)}, but returns the {@link AccessControlContext}
	 * applicable to the app or user that initiated the request.
	 * @param req
	 * @param resp
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	AccessControlContext getAccessContext(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
	
}
