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
package org.ogema.apps.cs;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.ElectricCurrentResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.PhysicalUnit;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.model.units.VoltageResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceNotFoundException;
import org.ogema.persistence.DBConstants;
import org.ogema.persistence.ResourceDB;
import org.ogema.resourcetree.SimpleResourceData;
import org.ogema.resourcetree.TreeElement;
import org.slf4j.Logger;

public class CommonServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4482025468018275688L;

	private static final boolean DEBUG = false;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	ResourceDB db;

	private ResourceAccess resMngr;

	CommonServlet(ResourceDB db, ResourceAccess resAcc) {
		this.db = db;
		this.resMngr = resAcc;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pi = req.getPathInfo();
		StringBuffer sb = null;
		if (pi == null) {
			if (DEBUG)
				logger.info("CommonServices no path URI specified");
			return;
		}

		// OutputStream bout = resp.getOutputStream();
		String data = null;
		if (DEBUG)
			logger.info("CommonServices path URI is " + pi);

		int id = -1;

		/*
		 * List of locations where App-files archived (Appstores)
		 */
		switch (pi) {
		case "/resourceview":
			resp.setContentType("application/json");
			String idStr = req.getParameter("id");
			if (idStr != null && !idStr.equals("#"))
				id = Integer.valueOf(idStr);
			sb = resourceTreeView2JSON(id);
			data = sb.toString();
			printResponse(resp, data);
			break;
		case "/filteredresources":
			resp.setContentType("application/json");
			try {
				sb = filteredResourceTree2JSON(req);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			data = sb.toString();
			printResponse(resp, data);
			break;
		case "/resourceperm":
			resp.setContentType("application/json");
			idStr = req.getParameter("id");
			if (idStr != null && !idStr.equals("#"))
				id = Integer.valueOf(idStr);
			sb = resourceTreeView2JSON(id);
			data = sb.toString();
			printResponse(resp, data);
			break;

		case "/resourcevalue":
			resp.setContentType("application/json");
			idStr = req.getParameter("id");
			if (idStr != null && !idStr.equals("#"))
				id = Integer.valueOf(idStr);
			sb = simpleResourceValue2JSON(id);
			data = sb.toString();
			printResponse(resp, data);
			break;
		}
	}

	private void printResponse(HttpServletResponse resp, String data) throws IOException {
		PrintWriter pw = resp.getWriter();
		pw.print(data);
	}

	StringBuffer resourceTreeView2JSON(int id) {
		Collection<TreeElement> childs;

		if (id == -1)
			childs = db.getAllToplevelResources();
		else
			childs = db.getByID(id).getChildren();
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		int index = 0;
		for (TreeElement te : childs) {
			// if the node is an internal one skip it
			if (te.getPath().startsWith("@"))
				continue;
			if (index++ != 0)
				sb.append(',');
			sb.append("{\"text\":\"");
			sb.append(te.getName());
			// sb.append(" - ");
			// sb.append(te.getType().getName());
			sb.append('"');
			sb.append(',');
			sb.append("\"id\":\"");
			sb.append(te.getResID());
			sb.append('"');
			sb.append(',');
			sb.append("\"method\":\"\","); // method and recursive are container that are user on the client side
			sb.append("\"recursive\":\"\",");
			sb.append("\"value\":\"");
			readResourceValue(te, sb);
			sb.append('"');
			sb.append(',');
			sb.append("\"readOnly\":\"");
			sb.append(true);
			sb.append('"');
			sb.append(',');
			sb.append("\"type\":\"");
			if (te.getTypeKey() == DBConstants.TYPE_KEY_COMPLEX_ARR) {
				sb.append(ResourceList.class.getName());
			}
			else {
				Class<?> cls = te.getType();
				if (cls != null)
					sb.append(cls.getName());
				else
					sb.append("Unknown model class");
			}
			sb.append('"');
			sb.append(',');
			List<TreeElement> children = te.getChildren();
			boolean hasChildren = true;
			if (children.size() == 0)
				hasChildren = false;
			sb.append("\"children\":");
			sb.append(hasChildren);
			sb.append('}');
		}
		sb.append(']');
		return sb;
	}

	StringBuffer filteredResourceTree2JSON(HttpServletRequest req) {
		Collection<TreeElement> childs;

		String type = req.getParameter("type");
		String path = req.getParameter("path");
		String owner = req.getParameter("owner");
		String id = req.getParameter("id");
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("type", type);
		map.put("path", path);
		map.put("owner", owner);
		map.put("id", id);

		childs = db.getFilteredNodes(map);

		StringBuffer sb = new StringBuffer();
		sb.append('[');
		int index = 0;
		for (TreeElement te : childs) {
			// if the node is an internal one skip it
			if (te.getPath().startsWith("@"))
				continue;
			String text = te.getName();
			// If the node is a reference extend the name string with the path of the referee
			if (te.isReference())
				text = text + "->" + te.getLocation();
			else
				text = te.getLocation();
			if (index++ != 0)
				sb.append(',');
			sb.append("{\"text\":\"");
			sb.append(text);
			sb.append('"');
			sb.append(',');
			sb.append("\"id\":\"");
			sb.append(te.getResID());
			sb.append('"');
			sb.append(',');
			sb.append("\"method\":\"\",");
			sb.append("\"value\":\"");
			readResourceValue(te, sb);
			sb.append('"');
			sb.append(',');
			sb.append("\"readOnly\":\"");
			sb.append(true);
			sb.append('"');
			sb.append(',');
			sb.append("\"type\":\"");
			assignNodeType(te, sb);
			sb.append('"');
			sb.append(',');
			sb.append("\"restype\":\"");
			if (te.getTypeKey() == DBConstants.TYPE_KEY_COMPLEX_ARR) {
				sb.append(ResourceList.class.getName());
			}
			else {
				Class<?> cls = te.getType();
				if (cls != null)
					sb.append(cls.getName());
				else
					sb.append("Unknown model class");
			}
			sb.append('"');
			sb.append(',');
			List<TreeElement> children = te.getChildren();
			boolean hasChildren = true;
			if (children.size() == 0)
				hasChildren = false;
			sb.append("\"children\":");
			sb.append(hasChildren);
			sb.append('}');
		}
		sb.append(']');
		return sb;
	}

	private void assignNodeType(TreeElement te, StringBuffer sb) {
		if (te.isReference())
			sb.append("reference");
		else if (te.isToplevel())
			sb.append("toplevel");
		else {
			try {
				sb.append("leaf");
			} catch (ResourceNotFoundException e) {
			} catch (UnsupportedOperationException e) {
				sb.append("default");
			}

		}
	}

	StringBuffer simpleResourceValue2JSON(int id) {

		StringBuffer sb = new StringBuffer();
		TreeElement te = db.getByID(id);

		sb.append('[');
		sb.append("{\"text\":\"");
		sb.append(te.getName());
		sb.append('"');
		sb.append(',');
		sb.append("\"id\":\"");
		sb.append(te.getResID());
		sb.append('"');
		sb.append(',');
		sb.append("\"method\":\"\",");
		sb.append("\"value\":\"");
		boolean readOnly = readResourceValue(te, sb);
		sb.append('"');
		sb.append(',');
		String unit = readResourceUnit(te);
		if (unit != null) {
			sb.append("\"unit\":\"");
			sb.append(unit);
			sb.append('"');
			sb.append(',');
		}
		sb.append("\"readOnly\":\"");
		sb.append(readOnly);
		sb.append('"');
		sb.append(',');
		sb.append("\"owner\":\"");
		sb.append(te.getAppID());
		sb.append('"');
		sb.append(',');
		sb.append("\"type\":\"");
		{
			Class<? extends Resource> cls = te.getType();
			if (te.isComplexArray())
				sb.append("List of ");
			if (cls == null)
				sb.append("not yet specified Resource");
			else
				sb.append(cls.getName());
		}
		sb.append('"');
		sb.append(',');
		sb.append("\"path\":\"");
		sb.append(te.getPath().replace('.', '/'));
		sb.append('"');
		if (te.isReference()) {
			sb.append(',');
			sb.append("\"reference\":\"");
			sb.append(te.getReference().getPath());
			sb.append('"');
		}
		sb.append('}');
		sb.append(']');
		return sb;
	}

	private boolean isReadOnly(TreeElement node, StringBuffer sb) {
		String name = node.getPath().replace('.', '/');
		Resource res = resMngr.getResource(name);
		boolean readOnly = true;
		if (res.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_LOWEST))
			readOnly = false;
		return readOnly;
	}

	boolean readResourceValue(TreeElement node, StringBuffer sb) {
		boolean result = true;
		int typeKey = node.getTypeKey();
		if (node.isComplexArray()) {
			sb.append("Instance of ");
			sb.append(node.getType());
		}
		else {
			switch (typeKey) {
			// read simple resource
			case SimpleResourceData.TYPE_KEY_BOOLEAN:
				sb.append(node.getData().getBoolean());
				result = isReadOnly(node, sb);
				break;
			case SimpleResourceData.TYPE_KEY_FLOAT:
				sb.append(node.getData().getFloat());
				result = isReadOnly(node, sb);
				break;
			case SimpleResourceData.TYPE_KEY_INT:
				sb.append(node.getData().getInt());
				result = isReadOnly(node, sb);
				break;
			case SimpleResourceData.TYPE_KEY_STRING:
				sb.append(StringEscapeUtils.escapeHtml4(node.getData().getString()));
				result = isReadOnly(node, sb);
				break;
			case SimpleResourceData.TYPE_KEY_LONG:
				sb.append(node.getData().getLong());
				result = isReadOnly(node, sb);
				break;
			// read array resource
			case SimpleResourceData.TYPE_KEY_OPAQUE:
				sb.append(node.getData().getByteArr());
				break;
			case SimpleResourceData.TYPE_KEY_INT_ARR:
				sb.append(node.getData().getIntArr());
				break;
			case SimpleResourceData.TYPE_KEY_LONG_ARR:
				sb.append(node.getData().getLongArr());
				break;
			case SimpleResourceData.TYPE_KEY_FLOAT_ARR:
				sb.append(node.getData().getFloatArr());
				break;
			case SimpleResourceData.TYPE_KEY_COMPLEX_ARR:
				sb.append("Instance of ");
				sb.append(node.getType());
				break;
			case SimpleResourceData.TYPE_KEY_BOOLEAN_ARR:
				sb.append(node.getData().getBooleanArr());
				break;
			case SimpleResourceData.TYPE_KEY_STRING_ARR:
				final String[] arr = node.getData().getStringArr();
				final String[] arrCopy = new String[arr.length];
				int cnt = 0;
				for (String str : arr)
					arrCopy[cnt++]=StringEscapeUtils.escapeHtml4(str);
				sb.append(arrCopy);
				break;
			case SimpleResourceData.TYPE_KEY_COMPLEX:
				sb.append("Instance of ");
				sb.append(node.getType());
				break;
			default:
			}
		}
		return result;
	}

	String readResourceUnit(TreeElement node) {
		Class<?> type = node.getType();
		PhysicalUnit pu = null;
		if (type == TemperatureResource.class) {
			pu = PhysicalUnit.KELVIN;
		}
		else if (type == VoltageResource.class)
			pu = PhysicalUnit.VOLTS;
		else if (type == ElectricCurrentResource.class)
			pu = PhysicalUnit.AMPERES;
		else if (type == PowerResource.class)
			pu = PhysicalUnit.WATTS;
		else if (type == EnergyResource.class)
			pu = PhysicalUnit.JOULES;

		if (pu != null)
			return pu.toString();
		else
			return null;
	}

	void writeResourceValue(TreeElement node, String value) {
		int typeKey = node.getTypeKey();
		switch (typeKey) {
		// write simple resource
		case SimpleResourceData.TYPE_KEY_BOOLEAN:
			String name = node.getPath().replace('.', '/');
			BooleanResource res = resMngr.getResource(name);
			if (res.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_LOWEST))
				res.setValue(Boolean.valueOf(value));
			break;
		case SimpleResourceData.TYPE_KEY_FLOAT:
			String float_name = node.getPath().replace('.', '/');
			FloatResource float_res = resMngr.getResource(float_name);
			if (float_res.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_LOWEST))
				float_res.setValue(Float.valueOf(value));
			break;
		case SimpleResourceData.TYPE_KEY_INT:
			String int_name = node.getPath().replace('.', '/');
			IntegerResource int_res = resMngr.getResource(int_name);
			if (int_res.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_LOWEST))
				int_res.setValue(Integer.valueOf(value));
			break;
		case SimpleResourceData.TYPE_KEY_STRING:
			String string_name = node.getPath().replace('.', '/');
			StringResource string_res = resMngr.getResource(string_name);
			if (string_res.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_LOWEST))
				string_res.setValue(String.valueOf(value));
			break;
		case SimpleResourceData.TYPE_KEY_LONG:
			String long_name = node.getPath().replace('.', '/');
			TimeResource long_res = resMngr.getResource(long_name);
			if (long_res.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_LOWEST))
				long_res.setValue(Long.valueOf(value));
			break;
		// write array resource, to do
		case SimpleResourceData.TYPE_KEY_OPAQUE:
			break;
		case SimpleResourceData.TYPE_KEY_INT_ARR:
			break;
		case SimpleResourceData.TYPE_KEY_LONG_ARR:
			break;
		case SimpleResourceData.TYPE_KEY_FLOAT_ARR:
			break;
		case SimpleResourceData.TYPE_KEY_COMPLEX_ARR:
			break;
		case SimpleResourceData.TYPE_KEY_BOOLEAN_ARR:
			break;
		case SimpleResourceData.TYPE_KEY_STRING_ARR:
			break;
		case SimpleResourceData.TYPE_KEY_COMPLEX:
			break;
		default:
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map params = req.getParameterMap();
		String info = req.getPathInfo(); // pathinfo /permissions, permission data is application/x-www-form-urlencoded,
		// so they are reached over the parameters list.
		logger.info("POST: Pathinfo: " + info);

		String currenturi = req.getRequestURI();
		StringBuffer url = req.getRequestURL();
		if (DEBUG) {
			logger.info("Current URI: " + currenturi); // URI: /service/permissions
			logger.info("Current URL: " + url);
		}
		Set<Entry<String, String[]>> paramsEntries = params.entrySet();

		for (Map.Entry<String, String[]> e : paramsEntries) {
			String key = e.getKey();
			String[] val = e.getValue();
			if (DEBUG)
				logger.info(key + "\t: ");
			for (String s : val)
				if (DEBUG)
					logger.info(s);
		}
		switch (info) {
		case "/writeresource":
			System.out.println("/writeresource");
			String resource_id = req.getParameter("resourceId");
			int resource_id_int = Integer.parseInt(resource_id);
			String write_val = req.getParameter("writeValue");
			writeResourceValue(db.getByID(resource_id_int), write_val);
			System.out.println(resource_id);
			System.out.println(write_val);
			resp.setContentType("application/json");
			printResponse(resp, "test");
			break;
		default:
			break;
		}
	}
}
