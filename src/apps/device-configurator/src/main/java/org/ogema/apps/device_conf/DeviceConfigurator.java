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
package org.ogema.apps.device_conf;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.security.WebAccessManager;
import org.ogema.driverconfig.HLDriverInterface;
import org.ogema.driverconfig.LLDriverInterface;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class DeviceConfigurator implements Application {
	private BundleContext context;
	private ServiceTracker<HLDriverInterface, HLDriverInterface> hl_tracker;
	private ServiceTracker<LLDriverInterface, LLDriverInterface> ll_tracker;
	private HashMap<String, HLDriverInterface> hl_map;
	private HashMap<String, LLDriverInterface> ll_map;
	JSONArray not_available_json;
	JSONObject not_available_json_object;
	private WebAccessManager wam;

	public DeviceConfigurator(BundleContext ctx) {
		context = ctx;
		hl_map = new HashMap<String, HLDriverInterface>();
		ll_map = new HashMap<String, LLDriverInterface>();

		try {
			not_available_json = new JSONArray();
			JSONObject not_available = new JSONObject();

			not_available.put("Service/function", "not available");
			not_available_json.put(0, not_available);

		} catch (JSONException e) {
		}

	}

	@Override
	public void start(ApplicationManager appManager) {

		Servlet servlet = new Servlet(this);

		wam = appManager.getWebAccessManager();

		wam.registerWebResource("/servlet", servlet);
		wam.registerWebResource("/devconf", "/html_5_app");

		ServiceTrackerCustomizer<HLDriverInterface, HLDriverInterface> hl_tracker_customizer = new ServiceTrackerCustomizer<HLDriverInterface, HLDriverInterface>() {
			@Override
			public HLDriverInterface addingService(ServiceReference<HLDriverInterface> sr) {

				HLDriverInterface hl_interface = context.getService(sr);
				hl_map.put(hl_interface.whichID(), hl_interface);

				return hl_interface;
			}

			@Override
			public void modifiedService(ServiceReference<HLDriverInterface> sr, HLDriverInterface t) {
			}

			@Override
			public void removedService(ServiceReference<HLDriverInterface> sr, HLDriverInterface t) {
			}
		};

		hl_tracker = new ServiceTracker<>(context, HLDriverInterface.class, hl_tracker_customizer);
		hl_tracker.open();

		ServiceTrackerCustomizer<LLDriverInterface, LLDriverInterface> ll_tracker_customizer = new ServiceTrackerCustomizer<LLDriverInterface, LLDriverInterface>() {
			@Override
			public LLDriverInterface addingService(ServiceReference<LLDriverInterface> sr) {
				LLDriverInterface ll_interface = context.getService(sr);
				ll_map.put(ll_interface.whichID(), ll_interface);

				return ll_interface;
			}

			@Override
			public void modifiedService(ServiceReference<LLDriverInterface> sr, LLDriverInterface t) {
			}

			@Override
			public void removedService(ServiceReference<LLDriverInterface> sr, LLDriverInterface t) {
			}
		};

		ll_tracker = new ServiceTracker<>(context, LLDriverInterface.class, ll_tracker_customizer);
		ll_tracker.open();
	}

	@Override
	public void stop(AppStopReason reason) {
		wam.unregisterWebResource("/servlet");
		wam.unregisterWebResource("/devconf");
	}

	// **GET*****************************************************************************************************************************************************************************
	public JSONArray showCC(String hlDriverId, String deviceAddress) {

		if (hl_map.get(hlDriverId) != null)
			return hl_map.get(hlDriverId).showCreatedChannels(deviceAddress);
		else
			return not_available_json;
	}

	public JSONObject scan(String llDriverId) {

		if (ll_map.get(llDriverId) != null)
			return ll_map.get(llDriverId).scanForDevices();
		else {
			JSONObject json = new JSONObject();
			try {
				json.put("status", "unsuccessful");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return json;
		}
	}

	public JSONObject cache(String llDriverId) {

		if (ll_map.get(llDriverId) != null)
			return ll_map.get(llDriverId).cacheDevices();
		else {
			JSONObject json = new JSONObject();
			try {
				json.put("status", "unsuccessful");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return json;
		}
	}

	public JSONObject readC(String hlDriverId, String config_data_json) {

		try {
			JSONObject config_data = new JSONObject(config_data_json);

			if (hl_map.get(hlDriverId) != null) {

				return hl_map.get(hlDriverId).readChannel(config_data.get("interfaceId").toString(),
						config_data.get("deviceAddress").toString(), config_data.get("channelAddress").toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return not_available_json_object;
	}

	public JSONObject showCD(String llDriverId, String interfaceId, String device, String endpoint, String clusterId) {
		if (ll_map.get(llDriverId) != null)
			return ll_map.get(llDriverId).showClusterDetails(interfaceId, device, endpoint, clusterId);
		else
			return not_available_json_object;
	}

	public JSONArray showDD(String llDriverId, String interfaceId, String deviceAddress) {

		if (ll_map.get(llDriverId) != null)
			// return ll_map.get(llDriverId).showDeviceDetails(interfaceId, deviceAddress);
			return not_available_json;
		else
			return not_available_json;

		// JSONArray showDDArray = new JSONArray();
		// JSONObject manufactorerId = new JSONObject();
		// JSONArray clusters = new JSONArray();
		// JSONObject cluster_0 = new JSONObject();
		// JSONObject cluster_1 = new JSONObject();
		//
		// try {
		// manufactorerId.put("manufactorerId", 0);
		// showDDArray.put(0, manufactorerId);
		// cluster_0.put("cluster_0", "cluster_0");
		// clusters.put(0, cluster_0);
		// cluster_1.put("cluster_1", "cluster_1");
		// clusters.put(1, cluster_1);
		// showDDArray.put(1, clusters);
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }
		// return showDDArray;
	}

	public JSONArray showACC(String llDriverId) {
		if (ll_map.get(llDriverId) != null)
			return ll_map.get(llDriverId).showAllCreatedChannels();
		else
			return not_available_json;
	}

	public JSONObject showH(String llDriverId) {

		// if (ll_map.get(llDriverId) != null)
		// return ll_map.get(llDriverId).showHardware();
		// else
		// return not_available_json;

		return not_available_json_object;

		// JSONArray showHArray = new JSONArray();
		// JSONArray hardwareIdentifier0Array = new JSONArray();
		// JSONArray hardwareIdentifier1Array = new JSONArray();
		//
		// JSONObject hardwareIdentifier0 = new JSONObject();
		// JSONObject PortName0 = new JSONObject();
		// JSONObject hardwareIdentifier1 = new JSONObject();
		// JSONObject PortName1 = new JSONObject();
		//
		// try {
		// hardwareIdentifier0.put("hardwareIdentifier0", "0");
		// hardwareIdentifier0Array.put(0, hardwareIdentifier0);
		// PortName0.put("PortName0", "0");
		// hardwareIdentifier0Array.put(1, PortName0);
		//
		// hardwareIdentifier1.put("hardwareIdentifier1", "1");
		// hardwareIdentifier1Array.put(0, hardwareIdentifier1);
		// PortName1.put("PortName1", "1");
		// hardwareIdentifier1Array.put(1, PortName1);
		//
		// showHArray.put(0, hardwareIdentifier0Array);
		// showHArray.put(1, hardwareIdentifier1Array);
		//
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }
		//
		// return showHArray;
	}

	public JSONObject showN(String llDriverId) {
		if (ll_map.get(llDriverId) != null)
			return ll_map.get(llDriverId).showNetwork("-l");
		else
			return not_available_json_object;
	}

	public JSONArray showAllHLDrivers() {
		if (hl_map.keySet().size() > 0) {

			JSONArray hlDrivers = new JSONArray();
			JSONObject hlDriver;
			int i = 0;
			String interface_key;

			for (Iterator<String> iterator = hl_map.keySet().iterator(); iterator.hasNext();) {
				try {
					hlDriver = new JSONObject();
					interface_key = iterator.next();
					hlDriver.put("hlDriverId", interface_key);
					hlDrivers.put(i, hlDriver);
				} catch (Exception e) {
					e.printStackTrace();
				}
				i++;
			}
			return hlDrivers;
		}
		else
			return not_available_json;
	}

	public JSONArray showAllLLDrivers() {
		if (ll_map.keySet().size() > 0) {

			JSONArray llDrivers = new JSONArray();
			JSONObject llDriver;
			int i = 0;
			String interface_key;

			for (Iterator<String> iterator = ll_map.keySet().iterator(); iterator.hasNext();) {
				try {
					llDriver = new JSONObject();
					interface_key = iterator.next();
					llDriver.put("llDriverId", interface_key);
					llDriver.put("tech", ll_map.get(interface_key).whichTech());
					llDrivers.put(i, llDriver);
				} catch (Exception e) {
					e.printStackTrace();
				}
				i++;
			}
			return llDrivers;
		}
		else
			return not_available_json;
	}

	// **POST*****************************************************************************************************************************************************************************
	public void createC(String hlDriverId, String config_data_json) {
		System.out.println(config_data_json);
		try {
			JSONObject config_data = new JSONObject(config_data_json);
			if (hl_map.get(hlDriverId) != null) {
				hl_map.get(hlDriverId).createChannel(config_data.get("interfaceId").toString(),
						config_data.get("deviceAddress").toString(), config_data.get("channelAddress").toString(),
						Long.parseLong(config_data.get("timeout").toString()),
						config_data.get("resourceName").toString(), config_data.get("deviceID").toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void writeC(String hlDriverId, String config_data_json) {
		System.out.println(config_data_json);

		try {
			JSONObject config_data = new JSONObject(config_data_json);
			if (hl_map.get(hlDriverId) != null) {
				hl_map.get(hlDriverId).writeChannel(config_data.get("interfaceId").toString(),
						config_data.get("deviceAddress").toString(), config_data.get("channelAddress").toString(),
						config_data.get("writeValue").toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void deleteC(String hlDriverId, String config_data_json) {

		try {
			JSONObject config_data = new JSONObject(config_data_json);
			if (hl_map.get(hlDriverId) != null) {
				hl_map.get(hlDriverId).deleteChannel(config_data.get("interfaceId").toString(),
						config_data.get("deviceAddress").toString(), config_data.get("channelAddress").toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void addC(String llDriverId, String hardwareIdentifier) {
		System.out.println(hardwareIdentifier);
	}

	public void addCVP(String llDriverId, String portName) {
		System.out.println(portName);
	}
}
