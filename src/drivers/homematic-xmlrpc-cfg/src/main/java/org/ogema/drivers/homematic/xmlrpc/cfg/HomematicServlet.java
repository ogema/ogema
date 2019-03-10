/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package org.ogema.drivers.homematic.xmlrpc.cfg;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONObject;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.HomeMaticClientCli;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HomeMatic;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription.TYPES;
import org.ogema.drivers.homematic.xmlrpc.ll.xmlrpc.MapXmlRpcStruct;
import org.ogema.drivers.homematic.xmlrpc.ll.api.XmlRpcStruct;
import org.ogema.model.locations.Room;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.tools.resource.util.ResourceUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.LoggerFactory;


/** 
 * Config servlet for the OGEMA Homematic XML-RPC driver (ll)
 */
@SuppressWarnings("serial")
@Component(service=Application.class)
public class HomematicServlet extends HttpServlet implements Application {

	private static final String SERVLET_PATH = "/ogemadrivers/homematicxmlrpc/cfgservlet";
	private static final String WEB_PATH = "/ogemadrivers/homematicxmlrpc/config";
	private ApplicationManager appMan;
	
	private final Map<HomeMaticClientCli, HmInterface> clients = new ConcurrentHashMap<>(4);

	private final Method getUrl = getUrlMethod(); // new in 2.2.1
	
	@Reference(
			cardinality=ReferenceCardinality.AT_LEAST_ONE,
			policy=ReferencePolicy.DYNAMIC,
			policyOption=ReferencePolicyOption.GREEDY,
			unbind="removeClient"
	)
	protected void addClient(HomeMaticClientCli clientCli) {
		clients.put(clientCli, getInterface(clientCli));
	}
	
	protected void removeClient(HomeMaticClientCli cli) {
		clients.remove(cli);
	}
	
	private static Method getUrlMethod() {
		try {
			return HomeMatic.class.getMethod("getServerUrl");
		} catch (Exception e) {
			LoggerFactory.getLogger(HomematicServlet.class).warn("Failed to load url method",e);
			return null;
		}
	}
	
	private HmInterface getInterface(HomeMaticClientCli cli) {
		final HomeMatic hm = getClient(cli);
		if (getUrl != null) {
			try {
				final URL url = (URL) getUrl.invoke(hm);
				return new HmInterface(hm, url);
			} catch (Exception e) {}
		}
		return new HmInterface(hm);
	}
	
	@Override
	public void start(ApplicationManager appMan) {
		this.appMan = appMan;
		appMan.getWebAccessManager().registerWebResource(SERVLET_PATH, this);
		appMan.getWebAccessManager().registerWebResource(WEB_PATH, "webresources");
	}

	@Override
	public void stop(AppStopReason i) {
		appMan.getWebAccessManager().unregisterWebResource(SERVLET_PATH);
		appMan.getWebAccessManager().unregisterWebResource(WEB_PATH);
	}

	private HomeMatic getClient(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		final String ifc = req.getParameter("interface");
		if (ifc == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "interface parameter missing");
			return null;
		}
		for (HmInterface hm : clients.values()) {
			if (ifc.equalsIgnoreCase(hm.getId()))
				return hm.getHm();
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, "interface " + ifc + " not found");
		return null;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final String target = req.getParameter("target");
		if (target == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "target parameter missing");
			return;
		}
		resp.setCharacterEncoding("UTF-8");
		if ("interfaces".equalsIgnoreCase(target)) {
			final PrintWriter writer = resp.getWriter();
			writer.write('[');
			boolean first = true;
			for (HmInterface hm : clients.values()) {
				if (!first)
					writer.write(',');
				writer.write(hm.toJson());
				first = false;
			}
			writer.write(']');
			resp.setContentType("application/json");
			resp.setStatus(HttpServletResponse.SC_OK);
			return ;
		}
		final HomeMatic hm = getClient(req, resp);
		if (hm == null)
			return;
		try {
			switch (target.toLowerCase()) {
			case "devices":
				/*
				resp.getWriter().write(mapToJson(hm.listDevices().stream()
					.collect(Collectors.toMap(device -> JSONObject.quote(device.getAddress()), this::serialize)).toString()));
					*/
				final List<DeviceDescription> dd = hm.listDevices();
				final Map<String, Map<String, Object>> devices = new HashMap<>(dd.size());
				for (DeviceDescription d : dd) {
					devices.put(d.getAddress(), serialize(d));
				}
				writeMapAsJson(resp.getWriter(), devices);
				break;
			case "links":
 				writeCollectionAsJson(resp.getWriter(), hm.getLinks("", 0));
				break;
			case "paramread":
				final XmlRpcStruct struct = hm.getParamset(req.getParameter("address"), req.getParameter("param"));
				writeMapAsJson(resp.getWriter(), struct.toMap());
				break;
			case "paramdescr":
				final Map<String, ParameterDescription<?>> desc = hm.getParamsetDescription(req.getParameter("address"), req.getParameter("param"));
				writeMapAsJson(resp.getWriter(), desc);
				break;
			case "readvalue":
				final Object o = hm.getValue(req.getParameter("address"), req.getParameter("key"));
				final PrintWriter writer = resp.getWriter();
				writer.write("{\"result\":");
				writeObjectAsJson(writer, o);
				writer.write('}');
				break;
			case "installmode":
				final int remainingSeconds = hm.getInstallMode();
				final PrintWriter writer2 = resp.getWriter();
				writer2.write("{\"seconds\":");
				writer2.write(remainingSeconds + "");
				writer2.write('}');
				break;
			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown target " + target);
				return;
			}
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			appMan.getLogger().warn("Error in request",e);
			return;
		}
		resp.setContentType("application/json");
		resp.setStatus(HttpServletResponse.SC_OK);
	}
	
	private void getLinkInfos(HomeMatic hm) {
		final List<Map<String, Object>> links;
		try {
			links = hm.getLinks("", 0);
		} catch (XmlRpcException e) {
			throw new RuntimeException(e);
		}
		System.out.println("   ALL links " + links);
		/*
		for (DeviceDescription d : hm.listDevices()) {
			System.out.println(" Links for " + d.getAddress());
			System.out.println("    Peers: " + hm.getLinkPeers(d.getAddress()));
			System.out.println("    " + hm.getLinks(address, 0));
			
		}
		*/
			
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final HomeMatic hm = getClient(req, resp);
		if (hm == null)
			return;
		final String target = req.getParameter("target");
		if (target == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "target parameter missing");
			return;
		}
		final char[] arr = new char[1024];
		final StringBuilder buffer = new StringBuilder();
		int read = 0;
		int cnt = 0;
		while ((read = req.getReader().read(arr, 0, arr.length)) != -1) {
			buffer.append(arr, 0, read);
			cnt += read;
			if (cnt > 10000) {
				resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
				return;
			}
		}
		final String request = buffer.toString();
		final JSONObject json = new JSONObject(request);
		resp.setCharacterEncoding("UTF-8");
		final PrintWriter writer = resp.getWriter();
		try {
			if ("addlink".equalsIgnoreCase(target)) {
				final String sender = json.getString("sender");
				final String receiver = json.getString("receiver");
				final String name = json.has("name") ? json.getString("name") : "";
				final String description = json.has("description") ? json.getString("description") : "";
				hm.addLink(sender, receiver, name, description);
				final Map<String, Object> info = hm.getLinkInfo(sender, receiver);
				if (info == null) {
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Link has not been created");
					return;
				}
				writeMapAsJson(writer, info);
				resp.setContentType("application/json");
				return;
			}
			if ("installmode".equalsIgnoreCase(target)) {
				hm.setInstallMode(json.getBoolean("mode"), 30, 1);
				final int remainingSeconds = hm.getInstallMode();
				final PrintWriter writer2= resp.getWriter();
				writer2.write("{\"seconds\":");
				writer2.write(remainingSeconds + "");
				writer2.write('}');
				resp.setContentType("application/json");
				return;
			}
			final String channel = json.getString("channel");
			final String paramset_key = json.getString("paramset_key");
			final String value = json.getString("value");
			final String key = json.getString("key");
			switch (target.toLowerCase()) {
			case "setvalue":
				/*
				final XmlRpcStruct struct = hm.getParamset(channel, paramset_key);
				if (!struct.containsKey(key)) {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid key " + key + " for parameter " + paramset_key + " at address " + channel);
					return;
				}
				*/
				final Map<String, ParameterDescription<?>> descriptions = hm.getParamsetDescription(channel, paramset_key);
				final int idxDot = key.indexOf('.');
				final TYPES type = descriptions.get(idxDot >= 0 ? key.substring(idxDot + 1) : key).getType();
//				set(value, type, key, struct);
				hm.setValue(channel, key, convert(value, type));
//				hm.putParamset(channel, paramset_key, struct);
				final Object o = hm.getValue(channel, key);//hm.getParamset(channel, paramset_key).getValue(key);
				writer.write('{');
				writer.write(JSONObject.quote(key));
				writer.write(':');
				writeObjectAsJson(writer, o);
				writer.write('}');
				break;
			case "setparam":
				/*
				final XmlRpcStruct struct = hm.getParamset(channel, paramset_key);
				if (!struct.containsKey(key)) {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid key " + key + " for parameter " + paramset_key + " at address " + channel);
					return;
				}
				*/
				final XmlRpcStruct struct = new MapXmlRpcStruct(new HashMap<String, Object>(2));
				final Map<String, ParameterDescription<?>> descriptions2 = hm.getParamsetDescription(channel, paramset_key);
				final TYPES type2 = descriptions2.get(key).getType();
				descriptions2.get(key).getOperations();
				set(value, type2, key, struct);
				hm.putParamset(channel, paramset_key, struct);
				final Object o2 = hm.getParamset(channel, paramset_key).getValue(key);
				writer.write('{');
				writer.write(JSONObject.quote(key));
				writer.write(':');
				writeObjectAsJson(writer, o2);
				writer.write('}');
				break;
			default:
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown target " + target);
				return;
			}
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			appMan.getLogger().warn("Error in request",e);
			return;
		}
		resp.setContentType("application/json");
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final HomeMatic hm = getClient(req, resp);
		if (hm == null)
			return;
		final String target = req.getParameter("target");
		if (target == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "target parameter missing");
			return;
		}
		final char[] arr = new char[1024];
		final StringBuilder buffer = new StringBuilder();
		int read = 0;
		int cnt = 0;
		while ((read = req.getReader().read(arr, 0, arr.length)) != -1) {
			buffer.append(arr, 0, read);
			cnt += read;
			if (cnt > 10000) {
				resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
				return;
			}
		}
		final String request = buffer.toString();
		final JSONObject json = new JSONObject(request);
		resp.setCharacterEncoding("UTF-8");
		final PrintWriter writer = resp.getWriter();
		try {
			switch (target.toLowerCase()) {
			case "link":
				final String receiver = json.getString("receiver");
				final String sender = json.getString("sender");
				hm.removeLink(sender, receiver);
				break;
			default: 
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown target " + target);
				return;
			}
		} catch (XmlRpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			appMan.getLogger().warn("Error in request",e);
			return;
		}
		writer.write('{');
		writer.write('}');
		resp.setStatus(HttpServletResponse.SC_OK);
	}
	
	private static HomeMatic getClient(final HomeMaticClientCli clientCli) {
		try {
			final Field f = clientCli.getClass().getDeclaredField("client");
			f.setAccessible(true);
			return (HomeMatic) f.get(clientCli);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Object convert(final String value, final TYPES type) {
		switch (type) {
		case INTEGER:
			return Integer.parseInt(value);
		case FLOAT:
			Float.parseFloat(value);
		case STRING:
		case ENUM:
			return value;
		case BOOL:
			return Boolean.parseBoolean(value);
		default:
			throw new UnsupportedOperationException("Set not implemented for type " + type);
		}
		/*
		if (template instanceof Object[]) { // StringArray
			final int start = value.indexOf('[');
			final int end = value.indexOf(']');
			final String[] arr = value.substring(start+1, end).split(",");
			for (int i = 0; i<arr.length; i++) {
				arr[i] = arr[i].trim();
			}
			struct.setStringArray(key, arr);
		}
		*/
	}
	
	private static void set(final String value, final TYPES type, final String key, final XmlRpcStruct struct) {
		switch (type) {
		case INTEGER:
			struct.setInt(key, Integer.parseInt(value));
			break;
		case FLOAT:
			struct.setValue(key, Float.parseFloat(value));
			break;
		case STRING:
			struct.setString(key, value);
			break;
		case BOOL:
			struct.setBoolean(key, Boolean.parseBoolean(value));
			break;
		default:
			throw new UnsupportedOperationException("Set not implemented for type " + type);
		}
		/*
		if (template instanceof Object[]) { // StringArray
			final int start = value.indexOf('[');
			final int end = value.indexOf(']');
			final String[] arr = value.substring(start+1, end).split(",");
			for (int i = 0; i<arr.length; i++) {
				arr[i] = arr[i].trim();
			}
			struct.setStringArray(key, arr);
		}
		*/
	}

	private static void writeCollectionAsJson(final PrintWriter writer, final Collection<?> set) {
		writer.write('[');
		boolean first = true;
		for (Object o : set) {
			if (!first)
				writer.write(',');
			writeObjectAsJson(writer, o);
			first = false;
		}
		writer.write(']');
	}
	
	private static void writeArrayAsJson(final PrintWriter writer, final Object[] arr) {
		writer.write('[');
		boolean first = true;
		for (Object o : arr) {
			if (!first)
				writer.write(',');
			writeObjectAsJson(writer, o);
			first = false;
		}
		writer.write(']');
	}
	
	private static void writeMapAsJson(final PrintWriter writer, final Map<?,?> map) {
		writer.write('{');
		boolean first = true;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (!first)
				writer.write(',');
			final Object key = entry.getKey();
			final Object value = entry.getValue();
			writeObjectAsJson(writer, key);
			writer.write(':');
			writeObjectAsJson(writer, value);
			first = false;
		}
		writer.write('}');
	}
	
	private static void writeObjectAsJson(final PrintWriter writer, final Object in) {
		if (in instanceof Number) {
			writer.write(in.toString());
		} else if (in instanceof Map) {
			writeMapAsJson(writer, (Map<?, ?>) in);
		} else if (in instanceof Collection) {
			writeCollectionAsJson(writer, (Collection<?>) in);
		}
		else if (in == null) {
			writer.write("null");
		}
		else if (in instanceof ParameterDescription<?>) {
			writeMapAsJson(writer, ((ParameterDescription) in).toMap());
		}
		else if (in instanceof Object[]) {
			writeArrayAsJson(writer, (Object[]) in);
		}
		else {
			writer.write(JSONObject.quote(in.toString()));
		}
	}
	
	private static String mapToJson(final String in) {
		return in.replace("={", ":{").replace("=\"", ":\"").replace("=[", ":[");
	}
	
	private Map<String, Object> serialize(final DeviceDescription device) {
		final Map<String, Object> map = new HashMap<>(device.toMap());
		/*
		final Map<String, Object> map = new HashMap<>(4);
		map.put("\"address\"", JSONObject.quote(device.getAddress()));
		map.put("\"type\"", JSONObject.quote(device.getType()));
		final String[] children  =device.getChildren();
		if (children != null && children.length > 0)
			map.put("\"children\"", quote(children));
		final String parent = device.getParent();
		if (parent != null && !parent.isEmpty())
			map.put("\"parent\"", JSONObject.quote(parent));
		final String[] params = device.getParamsets();
		if (params != null && params.length > 0)
			map.put("\"params\"", quote(params));
			*/
		final List<HmDevice> devices = appMan.getResourceAccess().getResources(HmDevice.class);
		HmDevice resource = null;
		for (HmDevice d : devices) {
			if (Objects.equals(device.getAddress(), d.address().getValue())) {
				resource = d;
				break;
			}
		}
		/*
		final Optional<HmDevice> opt = appMan.getResourceAccess().getResources(HmDevice.class).stream()
			.filter(d -> Objects.equals(device.getAddress(), d.address().getValue()))
			.findAny();
			*/
		if (resource != null) {
			Resource target = resource;
			for (PhysicalElement sub : resource.getSubResources(PhysicalElement.class, false)) {
				target = sub;
				break;
			}
			try {
				final Room r = ResourceUtils.getDeviceLocationRoom(target);
				if (r != null)
					map.put("room", r.getLocation());
			} catch (SecurityException e) {}
			if (target != resource)
				map.put("restype", target.getResourceType().getSimpleName());
		}
		return map;
	}
	
	/*
	private static String quote(final String[] array) {
		return "[" + Arrays.stream(array)
			.map(JSONObject::quote)
			.collect(Collectors.joining(",")) + "]";
	}
	*/
	
}
