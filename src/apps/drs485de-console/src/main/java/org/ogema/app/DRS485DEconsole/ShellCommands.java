/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.app.DRS485DEconsole;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.service.command.Descriptor;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.security.WebAccessManager;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;

//The annotations encapsulate the OSGi required. They expose the service Application
//to OSGi, which the OGEMA framework uses to detect this piece of code as an
//OGEMA application.
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
@Properties( { @Property(name = "osgi.command.scope", value = "drs485"),
		@Property(name = "osgi.command.function", value = { "list", "add", "remove", "set", "print" }) })
/**
 * Console OGEMA application that allows configuration, control and readout 
 * of the DRS485DE driver.
 * 
 * This application can manage several instances of the DRS485DE meter.
 * 
 * This class is implemented as a plug-in to the gogo shell. 
 * 
 * The properties to the service registration are published through 
 * the scr annotations above. 
 * 
 * 
 * 
 * @author pau
 *
 */
public class ShellCommands implements Application {

	// private Map<Integer, Device> devices = new HashMap<Integer, Device>();
	private Manager manager;
	private Servlet drs485deServlet;
	private WebAccessManager wam;
	private boolean counter = false;
	private ResourceAccess ra;

	// private ApplicationManager appManager;

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
		// this.appManager = appManager;
		manager = new Manager(appManager);
		ra = appManager.getResourceAccess();
		drs485deServlet = new Servlet(this);
		wam = appManager.getWebAccessManager();
		wam.registerWebResource("/drs485de", drs485deServlet);
		wam.registerWebResource("/drs485de_web", "/web");
	}

	@Override
	public void stop(AppStopReason reason) {
		manager.stop();
	}

	public JSONObject getGraphData() {

		JSONObject json = new JSONObject();
		try {
			//			if(counter == false) 
			//				{json.put("current_drs485de", 10);
			//				 counter = true;
			//				} else{ json.put("current_drs485de", 50);
			//				        counter = false;
			//				      }
			Resource res = ra.getResource("DRS485DE_0/energyReading");
			if (res == null || !(res instanceof EnergyResource)) {
				throw new RuntimeException();
			}

			EnergyResource eneres = (EnergyResource) res;
			json.put("current_drs485de", eneres.getValue() / 3600);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
