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
package org.ogema.logdatavisualisation;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.ResourceAccess;

public class LogDataVisualizationServlet extends HttpServlet {

	private static final long serialVersionUID = 550753654103033620L;
	private ResourceAccess resourceAccess = null;

	public LogDataVisualizationServlet(ApplicationManager applicationManager) {
		this.resourceAccess = applicationManager.getResourceAccess();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		final String requestedDataType = req.getParameter("dataType");
		String result = "no data";

		//depending on the requested dataType get all logged resources data...
		if (requestedDataType.equals("resourceData")) {
			result = this.generateFlottChartResourceData().generateJSON();
		}
		//.. or all schedule data
		if (requestedDataType.equals("scheduleData")) {
			result = this.generateFlottChartScheduleData().generateJSON();
		}

		//Write serialized JSON to the response
		resp.getWriter().write(result);
		resp.setStatus(200);
	}

	/**
	 * Get the RecordedData (logged Resource values) of all matching Resources 
	 * and bring it to a flottChart compatible JSON-Format.
	 * @return 
	 */
	private DataGen generateFlottChartResourceData() {

		/*
		  Get the RecordedData (logged Resource values) of all matching Resources 
		  and bring it to a flottChart compatible JSON-Format. Not very elegant
		  because we have to do the same check for all SimpleResources that 
		  could have RecordedData which are currently Float, Integer and Bollean 
		  Resources (state 21.11.2014).
		 */
		final DataGen flottChartData = new DataGen();

		//add all loggdata from all FloatResources
		for (FloatResource resource : resourceAccess.getResources(FloatResource.class)) {
			flottChartData.addRecordedDataForResource(resource.getHistoricalData(), resource);
		}
		//...and all logged data from all IntegerResources
		for (IntegerResource resource : resourceAccess.getResources(IntegerResource.class)) {
			flottChartData.addRecordedDataForResource(resource.getHistoricalData(), resource);
		}
		//...and all from Booleans
		for (BooleanResource resource : resourceAccess.getResources(BooleanResource.class)) {
			flottChartData.addRecordedDataForResource(resource.getHistoricalData(), resource);
		}

		return flottChartData;
	}

	/**
	 * Analog to generateFlottChartResourceData() for Schedules
	 * @return 
	 */
	private DataGen generateFlottChartScheduleData() {

		final DataGen flottChartData = new DataGen();
		for (Schedule schedule : resourceAccess.getResources(Schedule.class)) {
			flottChartData.addSchedule(schedule);
		}
		return flottChartData;
	}
}
