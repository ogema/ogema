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
package org.ogema.rest.shutdown.servlet;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.Constants;
import org.ogema.accesscontrol.PermissionManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component
@Property(name = HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, value = AdminServlet.alias)
@Service(Servlet.class)
public class AdminServlet extends HttpServlet  {

	private static final long serialVersionUID = 1L;

	final static String alias = "/rest/admin";
	
	@Reference
	private PermissionManager permMan;
	
	private volatile BundleContext ctx;
	
	@Activate
	protected void activate(BundleContext ctx) {
		this.ctx = ctx;
	}
	
	@Deactivate
	protected void deactivate() {
		this.ctx = null;
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final String usr = req.getParameter(Constants.OTUNAME);
		final String pw = req.getParameter(Constants.OTPNAME);
		if (usr == null || pw == null || !permMan.getAccessManager().authenticate(usr, pw, false)) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		final SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			try {
				sm.checkPermission(new AdminPermission(AdminPermission.SYSTEM));
			} catch (SecurityException e) {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		}
		final String target = req.getParameter("target");
		if (target == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Target missing");
			return;
		}
		switch (target.trim().toLowerCase()) {
		case "shutdown":
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ignore) {}
					try {
						final CountDownLatch latch = new CountDownLatch(1);
						final FrameworkListener listener = new FrameworkListener() {
							
							@Override
							public void frameworkEvent(FrameworkEvent event) {
								if (event.getType() == FrameworkEvent.STOPPED)
									latch.countDown();
							}
						};
						ctx.addFrameworkListener(listener);
						ctx.getBundle(0).stop();
						latch.await(5, TimeUnit.SECONDS);
					} catch (Throwable e) {
						System.err.println("Failed to shutdown framework smoothly... exiting");
						e.printStackTrace();
					}
					System.exit(0);
				}
			}).start();
			resp.setStatus(HttpServletResponse.SC_OK);
			break;
		default:
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown target " + target);
		}
	}
	
	
}
