/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */

package org.ogema.drivers.lemoneg;

import org.ogema.core.application.Application;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;

/**
 * OSGi Activator. Register an LemonegDriver instance as an org.ogema.Application.
 * 
 * @author pau, bjg
 * 
 */
public class Activator implements BundleActivator {
	private ServiceRegistration<?> serviceRegistration;
	private HttpService httpService;

	@Override
	public void start(BundleContext context) throws Exception {
		LemonegDriver application = new LemonegDriver();
		serviceRegistration = context.registerService(Application.class.getName(), application, null);

		Servlet servlet = new Servlet(application);
		ShellCommands commands = new ShellCommands(context, application);

		try {
			httpService = (HttpService) context.getService(context
					.getServiceReference("org.osgi.service.http.HttpService"));

			if (httpService == null)
				System.out.println("The httpservice is null");
			else {
				httpService.registerServlet("/servlet", servlet, null, null);
				httpService.registerResources("/html_5_app", "/html_5_app", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		serviceRegistration.unregister();
	}

}
