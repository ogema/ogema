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
package org.ogema.frameworkadministration.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ogema.frameworkadministration.controller.ResourceController;
import org.ogema.frameworkadministration.utils.Utils;

/**
 *
 * @author tgries
 */
public class FAServletResources extends HttpServlet {
    private static final long serialVersionUID = 1L;

	ResourceController resourceController;

	public FAServletResources() {
		resourceController = new ResourceController();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String requestJSON = Utils.readJsonFromRequest(req);
		resourceController.switchActive(requestJSON);

		resp.setStatus(200);
		resp.getWriter().write("OK");
		//super.doPost(req, resp); //To change body of generated methods, choose Tools | Templates.
	}

}
