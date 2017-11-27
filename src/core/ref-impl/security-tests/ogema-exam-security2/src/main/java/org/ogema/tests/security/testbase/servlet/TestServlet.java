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
package org.ogema.tests.security.testbase.servlet;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.core.security.WebAccessManager;

/**
 * A simple servlet, that accepts only GET requests and returns the same string
 * in every request ({@link #getSecretPassword()}). 
 */
public class TestServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final static AtomicInteger cnt = new AtomicInteger(0);
	
	private final String secretPassword = "mySecretPassword:" + new Random(System.currentTimeMillis()).nextInt();
	private final String path;
	private final WebAccessManager wam;
	
	public TestServlet(WebAccessManager webManager) {
		this.wam = webManager;
		path = webManager.registerWebResource("/securitytestservlet" + cnt.getAndIncrement(), this);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().write(secretPassword);
		resp.setStatus(HttpServletResponse.SC_OK);
	}
	
	public String getPath() {
		return path;
	}
	
	public String getSecretPassword() {
		return secretPassword;
	}
	
	public void close() {
		try {
			wam.unregisterWebResource(path);
		} catch (Exception ignore) {} 
	}

}
