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
