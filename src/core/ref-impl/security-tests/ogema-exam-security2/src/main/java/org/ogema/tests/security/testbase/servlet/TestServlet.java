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
public class TestServlet extends HttpServlet implements AutoCloseable {

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
