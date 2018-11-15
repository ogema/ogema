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
package ogema.logging.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.*;

class LoggingAppServlet extends HttpServlet {

	private static final long serialVersionUID = -623919478854332527L;
	private final static Logger logger = LoggerFactory.getLogger(LoggingApp.class);
	private final ResourceAccess ra;
	private final LoggingApp app;

	LoggingAppServlet(LoggingApp app) {
		this.app = app;
		this.ra = app.am.getResourceAccess();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		StringBuilder sb = new StringBuilder();
		BufferedReader reader = req.getReader();

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} finally {
			reader.close();
		}
		String request = sb.toString();
		String response = "";
		int status;
		try {
			JSONObject json = new JSONObject(request);
			logger.debug("  JSON object: {}",json);
			if (json.has("target")) {
				handleActivateRequests(json, resp);
				return;
			}
			String location = json.getString("resource");
			boolean record = json.getBoolean("record");
//			String interval = String.valueOf(json.getInt("interval"));
			String logType = json.getString("logType");
			long intv = 30000l;
			try {
//				intv = Long.parseLong(interval) * 1000l;
				intv = json.getLong("interval") * 1000l;
			} catch (Exception ee) {
			}

			Resource resource = ra.getResource(location);
			RecordedData rd = LoggingApp.getRecordedData(resource);
			if (rd == null) {
				response = "An error occurred. Could not change log settings.";
				resp.getWriter().write(response);
				resp.setStatus(200);
				return;
			}
			RecordedDataConfiguration configuration = new RecordedDataConfiguration();
			if (record) {
				configuration.setStorageType(StorageType.valueOf(logType));
				configuration.setFixedInterval(intv);
				rd.setConfiguration(configuration);
			}
			else {
				rd.setConfiguration(null);
			}
			response = response + "New configuration for " + location + ":";
			try {
				response = response + " interval " + String.valueOf(rd.getConfiguration().getFixedInterval())
						+ "ms, storage type: " + rd.getConfiguration().getStorageType().toString();
			} catch (Exception ee) {
				response = response + " not logging.";
			}
			status = HttpServletResponse.SC_OK;
		} catch (Exception e) {
			response = response + "An error occurred. Could not change log settings: " + e.toString();
			status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		resp.getWriter().write(response);
		resp.setStatus(status);
	}
	
	private void handleActivateRequests(final JSONObject json, final HttpServletResponse resp) throws IOException {
		final String target = json.getString("target");
		final int activated;
		switch (target.toLowerCase()) {
		case "logsensors":
			activated = app.logAllSensors();
			break;
		case "logactors":
			activated = app.logAllActors();
			break;
		default:
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown request target: " + target);
			return;
		}
		resp.setContentType("application/json");
		resp.getWriter().write("{\"activated\":" + activated + "}");
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//		System.out.println("  Processing GET request " + req.toString());
		//		System.out.println("  Sensors: " + sensors.toString());
//		String response = "[";
		JSONArray array = new JSONArray();
		List<SingleValueResource> resources = ra.getResources(SingleValueResource.class);
		Collections.sort(resources, new ResourcesComparator());
//		int nr = resources.size();
//		int cnt = 0;
		for (Resource res : resources) {
			if (res instanceof StringResource)
				continue;
			try {
				array.put(getResourceObject(res));
/*				cnt++;
				if (cnt == nr) {
					response = response + getResString2(res);
				}
				else {
					response = response + getResString2(res) + ",";
				} */
			} catch (Exception e) {
				logger.warn("  Exception caught during request: " + e.toString());
			}
		}
//		response = response + "]";
		logger.debug("  Answer: {}", array);
		resp.getWriter().write(array.toString());
		resp.setStatus(200);
	}

	private static class ResourcesComparator implements Comparator<Resource> {

		@Override
		public int compare(Resource o1, Resource o2) {
			return o1.getLocation().toLowerCase().compareTo(o2.getLocation().toLowerCase());
		}

	}
	
	private static JSONObject getResourceObject(Resource resource) {
		JSONObject object = new JSONObject();
		object.put("Location", resource.getLocation());
		object.put("Type", resource.getResourceType().getSimpleName());
		object.put("active", String.valueOf(resource.isActive()));
		if (resource instanceof SingleValueResource) {
			String[] valueLog = getSimpleResString2(resource);
			object.put("value",  valueLog[0]);
			boolean logg = !(valueLog[1].equals(""));
			object.put("logging", String.valueOf(logg));
			if (logg) {
				object.put("log interval/s", valueLog[2]);
				object.put("logging type", valueLog[1]);
			}
		}
		
		return object;
	}

/*	private String getResString2(Resource resource) {
		String result = "{\"Location\":\"" + resource.getLocation() + "\"";
		result = result + ",\"Type\":\"" + resource.getResourceType().getSimpleName() + "\"";
		result = result + ",\"active\":\"" + String.valueOf(resource.isActive()) + "\"";
		if (resource instanceof SingleValueResource) {
			String[] valueLog = getSimpleResString2(resource);
			result = result + ",\"value\":\"" + valueLog[0] + "\"";
			//boolean logg = Boolean.parseBoolean(valueLog[3]);
			boolean logg = !(valueLog[1].equals(""));
			result = result + ",\"logging\":\"" + String.valueOf(logg) + "\"";
			if (logg) {
				result = result + ",\"log interval/s\":\"" + valueLog[2] + "\"";
				result = result + ",\"logging type\":\"" + valueLog[1] + "\"";
			}
		}
		result = result + "}";
		return result;
	} */

	private static String[] getSimpleResString2(Resource sres) {
		String value = "";
		String loggingType = "";
		String loggingDur = "";
		boolean logging = false;
		RecordedDataConfiguration config = null;
		try {
			if (sres instanceof StringResource) {
				StringResource strRes = (StringResource) sres;
				value = strRes.getValue();
			}
			else if (sres instanceof FloatResource) {
				FloatResource floatRes = (FloatResource) sres;
				value = String.valueOf(floatRes.getValue());
				try {
					config = floatRes.getHistoricalData().getConfiguration();
				} catch (Exception e) {
				}
			}
			else if (sres instanceof IntegerResource) {
				IntegerResource intRes = (IntegerResource) sres;
				value = String.valueOf(intRes.getValue());
				try {
					config = intRes.getHistoricalData().getConfiguration();
				} catch (Exception e) {
				}
			}
			else if (sres instanceof BooleanResource) {
				BooleanResource boolRes = (BooleanResource) sres;
				value = String.valueOf(boolRes.getValue());
				try {
					config = boolRes.getHistoricalData().getConfiguration();
				} catch (Exception e) {
				}
			}
			else if (sres instanceof TimeResource) {
				TimeResource timeRes = (TimeResource) sres;
				value = String.valueOf(timeRes.getValue());
				try {
					config = timeRes.getHistoricalData().getConfiguration();
				} catch (Exception e) {
				}
			}
			try {
				loggingType = config.getStorageType().name();
				loggingDur = String.valueOf(config.getFixedInterval() / 1000);
				if (config.getStorageType().equals(StorageType.FIXED_INTERVAL)) {
					if (config.getFixedInterval() > 0) {
						logging = true;
					}
				}
				else {
					logging = true;
				}
			} catch (Exception e) {
			}
		} catch (Exception e) {
			logger.warn("Exception caught: " + e.toString());
		}
		String[] result = { value, loggingType, loggingDur, String.valueOf(logging) };
		return result;
	}
}
