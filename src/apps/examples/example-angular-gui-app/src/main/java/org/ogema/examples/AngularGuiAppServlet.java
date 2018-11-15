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
package org.ogema.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceAccess;

public class AngularGuiAppServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ApplicationManager appMan;
    private final ResourceAccess resAcc;
    private final OgemaLogger logger;

	public AngularGuiAppServlet(ApplicationManager appMan) {
		this.appMan = appMan;
		this.resAcc = appMan.getResourceAccess();
		this.logger = appMan.getLogger();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// get a list of all OGEMA resources in the system, put them in a map suitable for display in tabular format, 
		// and return the serialized map as response to the GET request
		
		// this map takes the table entries; the outer keys are row identifiers, the inner key column identifiers
		Map<String,Map<String,String>> resultMap = new LinkedHashMap<>();
		List<Resource> ogemaResources = resAcc.getToplevelResources(Resource.class);
		for (Resource resource: ogemaResources) {
			Map<String,String> columns = new LinkedHashMap<>();
			columns.put("Resource location",resource.getLocation());
			
			String readableName = "n.a.";
			StringResource str = resource.getSubResource("name", StringResource.class);
			if (str.isActive()) {
				readableName = str.getValue();
			}
			columns.put("Human readable name",readableName);
	
			columns.put("Resource type", resource.getResourceType().getSimpleName());
			columns.put("Status",resource.isActive() ? "active" : "inactive");
			resultMap.put(resource.getLocation(),columns);
		}
		// return map as string
		ObjectWriter objWriter = new ObjectMapper().writer();
		String response = objWriter.writeValueAsString(resultMap);
		resp.getWriter().write(response);
		resp.setStatus(200);
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
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jo = mapper.readTree(request); // assume request string comes in JSON format
		if (!jo.has("status") || !jo.has("location")) {
			logger.warn("Received invalid POST request: " + request);
			String response = "Bad request: fields \"status\" and/or \"location\" missing.";
			resp.getWriter().write(response);
			resp.setStatus(400);
			return;
		}
		String resourceLocation = jo.get("location").getTextValue();
		boolean status = jo.get("status").getBooleanValue();
		Resource resource = resAcc.getResource(resourceLocation);
		if (status) resource.activate(false);
		else resource.deactivate(false);		
		String response = "OK";
		resp.getWriter().write(response);
		resp.setStatus(200);

	}


}