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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
