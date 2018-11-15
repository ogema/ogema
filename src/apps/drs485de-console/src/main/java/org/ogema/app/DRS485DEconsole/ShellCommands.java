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
package org.ogema.app.DRS485DEconsole;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.service.command.Descriptor;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.security.WebAccessManager;

//The annotations encapsulate the OSGi required. They expose the service Application
//to OSGi, which the OGEMA framework uses to detect this piece of code as an
//OGEMA application.
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
@Properties( { @Property(name = "osgi.command.scope", value = "drs485"),
		@Property(name = "osgi.command.function", value = { "list", "add", "remove", "set", "print" }) })
/**
 * Console OGEMA application that allows configuration, control and readout of the DRS485DE driver.
 * 
 * This application can manage several instances of the DRS485DE meter.
 * 
 * This class is implemented as a plug-in to the gogo shell.
 * 
 * The properties to the service registration are published through the scr annotations above.
 * 
 * 
 * 
 * @author pau
 *
 */
public class ShellCommands implements Application {

	private Manager manager;
	private Servlet drs485deServlet;
	private WebAccessManager wam;
	private ResourceAccess ra;
	private Resource res;
	private EnergyResource eneres;

	@Descriptor("list all instances")
	public void list() {

		List<Device> devices = manager.getDevices();

		for (int index = 0; index < devices.size(); index++) {
			Device device = devices.get(index);

			if (device != null)
				System.out.println(index + ": " + device.getProperties());
		}
	}

	@Descriptor("list specific instance")
	public void list(@Descriptor("index of instance") int index) {

		List<Device> devices = manager.getDevices();

		Device device = devices.get(index);

		if (device != null)
			System.out.println(index + ": " + device.getProperties());
		else
			System.out.println("no such device");
	}

	@Descriptor("add new instance with default parameters. Initially disabled.")
	public void add() {
		int index = manager.newDevice();

		System.out.println("added device " + index);
	}

	@Descriptor("delete specific instance")
	public void remove(@Descriptor("index of instance") int index) {

		Device device = manager.removeDevice(index);

		if (device != null) {
			device.delete();
			System.out.println("removed device " + index);
		}
		else
			System.out.println("no such device");
	}

	@Descriptor("set parameter of instance")
	public void set(@Descriptor("index of instance") int index,
			@Descriptor("parameter name (see list command)") String key,
			@Descriptor("new value of parameter ") String value) {
		Device device = manager.getDevice(index);

		if (device != null) {
			device.setProperty(key, value);
			System.out.println("device " + index + " " + key + "=" + value);
		}
		else
			System.out.println("no such device");
	}

	@Descriptor("print meter data (energy value)")
	public void print(@Descriptor("index of instance") int index) {
		Device device = manager.getDevice(index);

		if (device != null) {
			device.printReadings();
		}
		else
			System.out.println("no such device");
	}

	@Override
	public void start(ApplicationManager appManager) {
		manager = new Manager(appManager);
		ra = appManager.getResourceAccess();
		drs485deServlet = new Servlet(this);
		wam = appManager.getWebAccessManager();
		wam.registerWebResource("/drs485de", drs485deServlet);
		wam.registerWebResource("/drs485de_web", "/web");

		List<Device> devices = manager.getDevices();

		for (int index = 0; index < devices.size(); index++) {
			Device device = devices.get(index);
			if (device != null) {

				res = ra.getResource(device.getProperties().getProperty("name") + "/energyReading");
				eneres = (EnergyResource) res;
				if (eneres != null) {
					configureLogging(eneres);

					// System.out.println("with ReductionMode before: " + appManager.getFrameworkTime());
					// List<SampledValue> list = eneres.getHistoricalData().getValues(1423561200000L, 1423562400000L,
					// 20000, ReductionMode.AVERAGE);
					// System.out.println("with ReductionMode after: " + appManager.getFrameworkTime());
					// System.out.println("list with ReductionMode: " + list);

					System.out.println("without ReductionMode before: " + appManager.getFrameworkTime());
					List<SampledValue> list2 = eneres.getHistoricalData().getValues(1423561200000L, 1423562400000L,
							20000, ReductionMode.NONE);
					System.out.println("without ReductionMode after: " + appManager.getFrameworkTime());
					System.out.println("list without ReductionMode: " + list2);
				}
			}
		}
	}

	@Override
	public void stop(AppStopReason reason) {
		wam.unregisterWebResource("/drs485de");
		wam.unregisterWebResource("/drs485de_web");
		manager.stop();
	}

	public JSONArray getMeterList() {

		JSONArray jsonArr = new JSONArray();
		JSONObject json;

		List<Device> devices = manager.getDevices();

		for (int index = 0; index < devices.size(); index++) {
			Device device = devices.get(index);
			if (device != null) {
				json = new JSONObject();
				try {
					json.put("name", device.getProperties().getProperty("name"));
					jsonArr.put(json);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return jsonArr;
	}

	public JSONObject getGraphData() {

		JSONObject json = new JSONObject();
		if (eneres != null) {
			try {

				json.put("current_drs485de", eneres.getValue() / 3600);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return json;
	}

	public JSONArray getGraphDataHistory() {
		JSONArray json = new JSONArray();
		if (eneres != null) {
			DateTime now = DateTime.now();
			List<SampledValue> list = eneres.getHistoricalData().getValues(now.minusDays(1).getMillis(),
					now.getMillis(), 60000, ReductionMode.AVERAGE);
			System.out.println("historical list :" + list);

			try {

				for (int i = 0; i < list.size() - 1; i++) {
					json.put(list.get(i).getValue().getDoubleValue() / 3600);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return json;
	}

	public void configureLogging(EnergyResource eneres) {
		// configure meter for logging once per minute
		final RecordedDataConfiguration meterConfig = new RecordedDataConfiguration();
		meterConfig.setStorageType(StorageType.ON_VALUE_UPDATE);
		// meterConfig.setFixedInterval(3 * 1000l);
		eneres.getHistoricalData().setConfiguration(meterConfig);
	}
}
