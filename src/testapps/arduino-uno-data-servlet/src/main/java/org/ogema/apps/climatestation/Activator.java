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
package org.ogema.apps.climatestation;

import org.ogema.core.application.Application;
import org.ogema.drivers.arduino.data.ClimateData;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;

public class Activator implements BundleActivator {

	private ServiceRegistration<?> serviceRegistration;
	private HttpService httpService;
	private ServiceReference<ClimateData> climateDataReference;
	private static ClimateData climateData;
	private RoomClimateStationServlet application;

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("this is RoomClimateStation::Activator \n");

		climateDataReference = context.getServiceReference(ClimateData.class);
		if (climateDataReference != null) {
			climateData = context.getService(climateDataReference);
			application = new RoomClimateStationServlet(climateData);
		}
		else {
			application = new RoomClimateStationServlet();

		}

		serviceRegistration = context.registerService(Application.class.getName(), application, null);

		try {
			httpService = (HttpService) context.getService(context
					.getServiceReference("org.osgi.service.http.HttpService"));

			if (httpService == null)
				System.out.println("The httpservice is null");
			else {
				httpService.registerServlet("/climate_station_servlet", application, null, null);
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
