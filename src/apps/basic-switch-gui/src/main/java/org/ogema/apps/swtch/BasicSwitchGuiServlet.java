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
package org.ogema.apps.swtch;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.apps.swtch.patterns.ControllableMultiPattern;
import org.ogema.apps.swtch.patterns.ControllableOnOffPattern;
import org.ogema.apps.swtch.patterns.ThermostatPattern;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.locations.Room;
import org.ogema.model.prototypes.PhysicalElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

class BasicSwitchGuiServlet extends HttpServlet {

	private static final long serialVersionUID = 6951206236759267713L;
	private final ApplicationManager appMan;

	BasicSwitchGuiServlet(ApplicationManager appMan) {
		this.appMan = appMan;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String,Map> typesMap = new LinkedHashMap<>();
		Map<String,Map> switchMap = new LinkedHashMap<>();
		Map<String,Map> multiSwitchMap = new LinkedHashMap<>();
		Map<String,Map> thermoSwitchMap = new LinkedHashMap<>();
		for (ControllableOnOffPattern pattern : appMan.getResourcePatternAccess().getPatterns(ControllableOnOffPattern.class, AccessPriority.PRIO_LOWEST)) {
			if (pattern.isReference()) continue; // otherwise devices are listed twice
			Map ptMap = new LinkedHashMap<>();
			ptMap.put("value", pattern.getValue());
			ptMap.put("name", pattern.getName());
			ptMap.put("type", pattern.getType());
			ptMap.put("loc", pattern.getRoom());
			if (pattern.model instanceof SingleSwitchBox && pattern.model.getSubResource("device") != null && pattern.model.getSubResource("device").isActive()) {
				Map dev = new LinkedHashMap<>();
				PhysicalElement device = pattern.model.getSubResource("device",PhysicalElement.class);
				String name = device.getName();
				if (device.name().isActive()) {
					name = device.name().getValue();
				}	
				dev.put("name", name);
				dev.put("type",device.getResourceType().getSimpleName());
				ptMap.put("device", dev);
				if (device.location().room().name().isActive()) ptMap.put("loc", device.location().room().name().getValue());
			}
			switchMap.put(pattern.model.getLocation(), ptMap);
		}
		for (ControllableMultiPattern pattern : appMan.getResourcePatternAccess().getPatterns(ControllableMultiPattern.class, AccessPriority.PRIO_LOWEST)) {
			Map ptMap = new LinkedHashMap<>();
			ptMap.put("value", pattern.getValue());
			ptMap.put("name", pattern.getName());
			ptMap.put("type", pattern.getType());
			ptMap.put("loc", pattern.getRoom());
			multiSwitchMap.put(pattern.model.getLocation(), ptMap);
		}
		for (ThermostatPattern pattern : appMan.getResourcePatternAccess().getPatterns(ThermostatPattern.class, AccessPriority.PRIO_LOWEST)) {
			Map ptMap = new LinkedHashMap<>();
			ptMap.put("value", cutValue(pattern.currentTemperature.getCelsius()));
			ptMap.put("name", pattern.model.getLocation());
			ptMap.put("crStp", cutValue(pattern.remoteDesiredTemperature.getCelsius()));
			ptMap.put("charge",cutValue(pattern.batteryCharge.getValue()));
			ptMap.put("valve", cutValue(pattern.valvePosition.getValue()));
			ptMap.put("loc", "");
			Room room = pattern.model.location().room();
			if (room.isActive()) {
				if (room.name().isActive()) {
					ptMap.put("loc", room.name().getValue());
				} else {
					ptMap.put("loc", room.getName());
				}			
			}
			thermoSwitchMap.put(pattern.model.getLocation(), ptMap);
		}
		typesMap.put("switches", switchMap);
		typesMap.put("multiswitches", multiSwitchMap);
		typesMap.put("thermostats", thermoSwitchMap);
		ObjectWriter ow = new ObjectMapper().writer();
		resp.getWriter().write(ow.writeValueAsString(typesMap));
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
		String msg = sb.toString();
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jo = mapper.readTree(msg);
			if (jo.has("swtch")) {
				String swtch = jo.get("swtch").asText();
				Resource res = appMan.getResourceAccess().getResource(swtch);
				/*if (res instanceof SingleSwitchBox) {
					SingleSwitchBox switchBox = (SingleSwitchBox) res;
					switchBox.onOffSwitch().stateControl().setValue(!switchBox.onOffSwitch().stateControl().getValue());
				}*/
				BooleanResource stateControl = res.getSubResource("onOffSwitch", OnOffSwitch.class).stateControl()
						.create();
				BooleanResource stateFeedback = res.getSubResource("onOffSwitch", OnOffSwitch.class).stateFeedback();
				if (stateControl.exists()) {
					if (stateControl.isActive()) 
						stateControl.setValue(!stateFeedback.getValue());
					else
						stateControl.setValue(true);
					stateControl.activate(false);
				}
			}
			else if (jo.has("mswtch")) {
				String swtch = jo.get("mswtch").asText();
				MultiSwitch res = appMan.getResourceAccess().getResource(swtch);
				float value = (float) jo.get("value").asDouble() / 100;
				if (value >= 0 && value <= 1) {
					FloatResource stateControl = res.stateControl().create();
					stateControl.setValue(value);
					stateControl.activate(false);
				}
			}
			else if (jo.has("thermo")) {
				String swtch = jo.get("thermo").asText();
				Thermostat res = appMan.getResourceAccess().getResource(swtch);
				float value = (float) jo.get("value").asDouble();
				TemperatureResource tr = res.temperatureSensor().settings().setpoint().create();
				tr.setCelsius(value);
				tr.activate(false);
			}
			Thread.sleep(400); // do this in order to allow drivers to transfer stateControl value to stateFeedback
		} catch (Exception e) {
			appMan.getLogger().warn("Error in POST method ", e);
		}

		resp.getWriter().write(sb.toString());
		resp.setStatus(200);

	}

	private String cutValue(float val) {
		String take1 = String.valueOf(val);
		int idx = take1.indexOf(".");
		if (idx < 0)
			return take1;
		if (take1.substring(idx).length() > 3) {
			return take1.substring(0, idx + 3);
		}
		return take1;
	}

}
