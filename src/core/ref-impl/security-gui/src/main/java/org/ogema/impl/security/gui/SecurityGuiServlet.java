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
package org.ogema.impl.security.gui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.AppID;
import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.InstallableApplication;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.security.AppPermission;
import org.ogema.core.security.WebAccessManager;
import org.ogema.persistence.ResourceDB;
import org.ogema.resourcetree.SimpleResourceData;
import org.ogema.resourcetree.TreeElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.permissionadmin.PermissionInfo;
import org.slf4j.Logger;

public class SecurityGuiServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7370224231398359148L;

	private static final String GRANTED_PERMS_NAME = "permission";

	private static final boolean DEBUG = false;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	private PermissionManager pman;

	private SecurityGui admin;

	ResourceDB db;

	private ResourceAccess resMngr;

	SecurityGuiServlet(WebAccessManager wam, PermissionManager pm, SecurityGui adminapp) {
		this.admin = adminapp;
		this.pman = pm;
		wam.registerWebResource("/security-gui", "/admin");
		wam.registerWebResource("/security/config", this);
		db = adminapp.db;
	}

	synchronized public String getAppstoresData() throws Exception {
		JSONArray appStoresData = new JSONArray();
		int i = 0;
		JSONObject json = new JSONObject();
		List<ApplicationSource> appStores = admin.instMan.getConnectedAppSources();
		for (ApplicationSource entry : appStores) {
			appStoresData.put(i++, entry.getName());
		}
		json.put("appstores", appStoresData);
		if (DEBUG)
			logger.info(json.toString());
		return json.toString();
	}

	private String getAppFiles(ApplicationSource src) throws Exception {
		JSONArray appStoresData = new JSONArray();
		JSONObject json = new JSONObject();
		int index = 0;
		List<InstallableApplication> apps = src.getAppsAvailable();
		for (InstallableApplication app : apps) {
			appStoresData.put(index++, app.getName());
		}
		json.put("apps", appStoresData);
		return json.toString();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pi = req.getPathInfo();
		StringBuffer sb = null;
		if (pi == null) {
			if (DEBUG)
				logger.info("SecurityGuiServlet no path URI specified");
			return;
		}

		// OutputStream bout = resp.getOutputStream();
		String data = null;
		if (DEBUG)
			logger.info("SecurityGuiServlet path URI is " + pi);

		int id = -1;

		/*
		 * List of locations where App-files archived (Appstores)
		 */
		switch (pi) {
		case "/appstores":
			if (DEBUG)
				logger.info("Get Appstores");
			try {
				data = getAppstoresData();
				printResponse(resp, data);
			} catch (Exception e1) {
				e1.printStackTrace(resp.getWriter());
			}
			break;
		/*
		 * List of the apps in a specific location.
		 */
		case "/apps": // The path is in this case /serletName/path1
			String appStore = req.getParameter("name");
			ApplicationSource appSource = admin.instMan.connectAppSource(appStore);
			if (DEBUG)
				logger.info("Get Apps in " + appSource.getAddress());
			if (appStore.equals(admin.instMan.getLocalStore().getName())) {
				try {
					data = getAppFiles(appSource);
					printResponse(resp, data);
				} catch (Exception e) {
					e.printStackTrace(resp.getWriter());
				}
			}
			else {
				printResponse(resp, "Only local appstores supported yet");
			}
			break;
		case "/app":
			String name = req.getParameter("name");
			appStore = req.getParameter("appstore");
			appSource = admin.instMan.connectAppSource(appStore);
			if (appStore == null || name == null) {
				printResponse(resp, "No appstore or app is selected.");
				;
				break;
			}
			startAppInstall(req, resp, appSource.getAddress(), name);
			break;
		case "/localepermissions":
			resp.setContentType("application/json");
			String idStr = req.getParameter("id");
			if (idStr != null)
				id = Integer.valueOf(idStr);
			if (id != -1) {
				Bundle b = admin.osgi.getBundle(id);
				if (b != null) {
					List<String> perms = SecurityGui.getLocalPerms(b.getLocation());
					try {
						data = localPerms2JSON(perms, b.getSymbolicName());
						printResponse(resp, data);
					} catch (Exception e) {
						e.printStackTrace(resp.getWriter());
					}
				}
				else {
					printResponse(resp, "Invalid bundle id " + idStr);
				}
			}
			break;
		case "/grantedpermissions":
			resp.setContentType("application/json");
			idStr = req.getParameter("id");
			if (idStr != null)
				id = Integer.valueOf(idStr);
			if (id != -1) {
				Bundle b = admin.osgi.getBundle(id);
				if (b != null) {
					try {
						data = grantedPerms2JSON(id);
						if (data == null)
							data = "Please check if the application with the ID " + id + "";
						printResponse(resp, data);
					} catch (Exception e) {
						e.printStackTrace(resp.getWriter());
					}
				}
				else {
					printResponse(resp, "Invalid bundle id " + idStr);
				}
			}
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
		case "/installedapps":
			resp.setContentType("application/json");
			String action = req.getParameter("action");
			idStr = req.getParameter("app");
			if (idStr != null)
				id = Integer.valueOf(idStr);

			switch (action) {
			case "getInfo":
				if (id != -1) {
					sb = bundleInfos2JSON(id);
					data = sb.toString();
					printResponse(resp, data);
				}
				else
					printResponse(resp, "Invalid app id");
				break;
			case "getIcon":
				printResponse(resp, "Not yet supported.");
				break;
			case "update":
				id = Integer.valueOf(idStr);
				/*
				 * if this bundle tries to update itself, its not supported. In this case return without doing anything.
				 */
				if (admin.bundleID == id) {
					printResponse(resp,
							"{\"statusInfo\":\"Update of the admin app is not supported over this interface.\"}");
					return;
				}
				Bundle b = admin.osgi.getBundle(id);
				InstallableApplication app = admin.instMan.createInstallableApp(b);
				admin.instMan.install(app);
				// admin.osgi.getBundle(id).update();
				printResponse(resp, "{\"statusInfo\":\"Update Succeded\"}");
				break;
			case "start":
				id = Integer.valueOf(idStr);

				b = admin.osgi.getBundle(id);
				app = admin.instMan.createInstallableApp(b);
				try {
					admin.osgi.getBundle(id).start();
				} catch (BundleException e1) {
					printResponse(resp, "{\"statusInfo\":\"");
					e1.printStackTrace(resp.getWriter());
					printResponse(resp, "\"}");
					break;
				}
				printResponse(resp, "{\"statusInfo\":\"Start Succeded\"}");
				break;
			case "stop":
				id = Integer.valueOf(idStr);

				b = admin.osgi.getBundle(id);
				app = admin.instMan.createInstallableApp(b);
				try {
					admin.osgi.getBundle(id).stop();
				} catch (BundleException e1) {
					printResponse(resp, "{\"statusInfo\":\"");
					e1.printStackTrace(resp.getWriter());
					printResponse(resp, "\"}");
					break;
				}
				printResponse(resp, "{\"statusInfo\":\"Stop Succeded\"}");
				break;
			case "delete":
				id = Integer.valueOf(idStr);
				/*
				 * if this bundle tries to update itself, its not supported. In this case return without doing anything.
				 */
				if (admin.bundleID == id) {
					printResponse(resp,
							"{\"statusInfo\":\"Uninstall of the admin app is not supported over this interface.\"}");
					return;
				}
				try {
					admin.osgi.getBundle(id).uninstall();
					printResponse(resp, "{\"statusInfo\":\"Uninstall Succeded\"}");
				} catch (BundleException e) {
					printResponse(resp, "{\"statusInfo\":\"");
					e.printStackTrace(resp.getWriter());
					printResponse(resp, "\"}");
				}
				break;
			case "listApps":
				sb = appsList2JSON();
				data = sb.toString();
				printResponse(resp, data);
				break;
			case "listAll":
				sb = bundlesList2JSON();
				data = sb.toString();
				printResponse(resp, data);
				break;
			case "webResources":
				resp.setContentType("application/json");
				String path = req.getParameter("id");
				String appid = req.getParameter("app");
				String alias = null;
				if (!path.equals("#"))
					alias = req.getParameter("text");
				id = Integer.valueOf(appid);
				sb = webResourceTree2JSON(id, path, alias);
				if (sb != null) {
					data = sb.toString();
					printResponse(resp, data);
				}
				break;
			}
			break;
		}
	}

	private void startAppInstall(HttpServletRequest req, HttpServletResponse resp, String address, String name)
			throws IOException {
		if (!pman.handleSecurity(new AdminPermission(AdminPermission.APP)))
			throw new SecurityException("Permission to install Application denied!" + address + name);
		InstallableApplication app = admin.instMan.createInstallableApp(address, name);
		// req.getSession().setAttribute(INSTALLATION_STATE_ATTR_NAME, app);

		Bundle b = null;
		try {
			b = admin.osgi.installBundle(app.getLocation());
		} catch (BundleException e1) {
			e1.printStackTrace();
		}
		if (b != null) {
			app.setState(InstallableApplication.InstallState.BUNDLE_INSTALLED);
			app.setBundle(b);
			logger.info("Bundle installed from " + b.getLocation());
		}
		else {
			logger.info("Bundle installation failed!");
		}
		// In this case an app is chosen for the installation
		// Start the state machine for the installation process

		try {
			// String data = getDesiredPerms(app);
			String data = "{\"name\":\"" + b.getSymbolicName() + "\",\"id\":" + b.getBundleId() + "}";
			printResponse(resp, data);
		} catch (Exception e) {
			e.printStackTrace(resp.getWriter());
		}
	}

	private String getDesiredPerms(InstallableApplication app) {
		JSONObject permObj = new JSONObject();
		JSONArray permsArray = new JSONArray();

		List<String> locals = app.getPermissionDemand();
		for (String perm : locals) {
			perm = perm.trim();
			if (perm.startsWith("#") || perm.startsWith("//") || perm.equals(""))
				continue;
			permsArray.put(perm);

		}
		try {
			permObj.put("permissions", permsArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return permObj.toString();
	}

	private String localPerms2JSON(List<String> perms, String name) {
		JSONObject permObj = new JSONObject();
		JSONArray permsArray = new JSONArray();

		List<String> locals = perms;
		for (String perm : locals) {
			perm = perm.trim();
			if (perm.startsWith("#") || perm.startsWith("//") || perm.equals(""))
				continue;
			// filter permissions that are granted as default
			if (!isGrantedAsDefault(perm)) {
				if (perm.indexOf('<') != -1)
					perm = perm.replaceAll("(.*<<[a-zA-Z_0-9]*) *(.*)", "$1_$2"); // replace permissions names like
				// <<ALL
				// FILES>>
				permsArray.put(perm);
			}
		}
		try {
			permObj.put("permissions", permsArray);
			permObj.put("name", name);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return permObj.toString();
	}

	private boolean isGrantedAsDefault(String perm) {
		perm = perm.replaceAll("[()\"]", "");
		String[] permElems = perm.split(" ");
		int index = 0;
		for (String str : permElems) {
			permElems[index++] = str.trim();
		}
		if (index == 1)
			return pman.isDefaultPolicy(permElems[0], null, null);
		else if (index == 2)
			return pman.isDefaultPolicy(permElems[0], permElems[1], null);
		else
			return pman.isDefaultPolicy(permElems[0], permElems[1], permElems[2]);

	}

	private void printResponse(HttpServletResponse resp, String data) throws IOException {
		PrintWriter pw = resp.getWriter();
		pw.print(data);
	}

	private StringBuffer appInfos2JSON(int id) {
		StringBuffer sb = new StringBuffer();
		/*
		 * Put bundle id
		 */
		sb.append("{\"bundleID\":\"");
		sb.append(id);
		sb.append("\",\"policies\":[");
		Bundle b = admin.osgi.getBundle(id);
		AppID aid = pman.getAdminManager().getAppByBundle(b);
		AppPermission ap = pman.getPolicies(aid);
		/*
		 * Put policies info
		 */
		Map<String, ConditionalPermissionInfo> granted = ap.getGrantedPerms();
		Set<Entry<String, ConditionalPermissionInfo>> tlrs = granted.entrySet();
		int index = 0, j = 0;
		for (Map.Entry<String, ConditionalPermissionInfo> entry : tlrs) {
			ConditionalPermissionInfo info = entry.getValue();
			if (j++ != 0)
				sb.append(',');
			sb.append("{\"mode\":\"");
			sb.append(info.getAccessDecision());
			sb.append("\",\"permissions\":[");
			/*
			 * Put Permissions
			 */
			index = 0;
			PermissionInfo pinfos[] = info.getPermissionInfos();
			for (PermissionInfo pi : pinfos) {
				String tmpStr = null;
				if (index++ != 0)
					sb.append(',');
				sb.append("{\"type\":\"");
				tmpStr = pi.getType();
				if (tmpStr != null)
					sb.append(tmpStr);
				sb.append("\",\"filter\":\"");
				String tmp = pi.getName();
				if (tmp != null) {
					tmpStr = tmp.replace("\\", "\\\\");
					sb.append(tmpStr == null ? "" : tmpStr);
				}
				sb.append("\",\"actions\":\"");
				tmpStr = pi.getActions();
				if (tmpStr != null)
					sb.append(tmpStr);
				sb.append("\"}");
			}

			sb.append("],\"conditions\":[");
			/*
			 * Put Conditions
			 */
			index = 0;
			ConditionInfo cinfos[] = info.getConditionInfos();
			for (ConditionInfo ci : cinfos) {
				String tmpStr = null;
				if (index++ != 0)
					sb.append(',');
				sb.append("{\"type\":\"");
				tmpStr = ci.getType();
				sb.append(tmpStr == null ? "" : tmpStr);
				String args[] = ci.getArgs();
				sb.append("\",\"arg1\":\"");
				try {
					if (tmpStr != null) {
						tmpStr = args[0].replace("\\", "\\\\");
						sb.append(tmpStr);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					// sb.append("\"\"");
				}
				sb.append("\",\"arg2\":\"");
				try {
					if (tmpStr != null) {
						tmpStr = args[1].replace("\\", "\\\\");
						sb.append(tmpStr);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					// sb.append("\"\"");
				}
				sb.append("\"}");
			}
			sb.append("]}");
		}
		sb.append("]}");
		return sb;
	}

	private StringBuffer bundleInfos2JSON(int id) {
		StringBuffer sb = new StringBuffer();
		/*
		 * Put bundle id
		 */
		sb.append("{\"bundleID\":\"");
		sb.append(id);
		sb.append("\",\"policies\":[");
		Bundle b = admin.osgi.getBundle(id);
		// AppID aid = pman.getAdminManager().getAppByBundle(b);
		// AppPermission ap = pman.createAppPermission(b.getLocation());
		/*
		 * Put policies info
		 */
		Map<String, ConditionalPermissionInfo> granted = pman.getGrantedPerms(b);
		// Map<String, ConditionalPermissionInfo> granted = ap.getGrantedPerms();
		Set<Entry<String, ConditionalPermissionInfo>> tlrs = granted.entrySet();
		int index = 0, j = 0;
		for (Map.Entry<String, ConditionalPermissionInfo> entry : tlrs) {
			ConditionalPermissionInfo info = entry.getValue();
			if (j++ != 0)
				sb.append(',');
			sb.append("{\"mode\":\"");
			sb.append(info.getAccessDecision());
			sb.append("\",\"permissions\":[");
			/*
			 * Put Permissions
			 */
			index = 0;
			PermissionInfo pinfos[] = info.getPermissionInfos();
			for (PermissionInfo pi : pinfos) {
				String tmpStr = null;
				if (index++ != 0)
					sb.append(',');
				sb.append("{\"type\":\"");
				tmpStr = pi.getType();
				if (tmpStr != null)
					sb.append(tmpStr);
				sb.append("\",\"filter\":\"");
				String tmp = pi.getName();
				if (tmp != null) {
					tmpStr = tmp.replace("\\", "\\\\");
					sb.append(tmpStr == null ? "" : tmpStr);
				}
				sb.append("\",\"actions\":\"");
				tmpStr = pi.getActions();
				if (tmpStr != null)
					sb.append(tmpStr);
				sb.append("\"}");
			}

			sb.append("],\"conditions\":[");
			/*
			 * Put Conditions
			 */
			index = 0;
			ConditionInfo cinfos[] = info.getConditionInfos();
			for (ConditionInfo ci : cinfos) {
				String tmpStr = null;
				if (index++ != 0)
					sb.append(',');
				sb.append("{\"type\":\"");
				tmpStr = ci.getType();
				sb.append(tmpStr == null ? "" : tmpStr);
				String args[] = ci.getArgs();
				sb.append("\",\"arg1\":\"");
				try {
					if (tmpStr != null) {
						tmpStr = args[0].replace("\\", "\\\\");
						sb.append(tmpStr);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					// sb.append("\"\"");
				}
				sb.append("\",\"arg2\":\"");
				try {
					if (tmpStr != null) {
						tmpStr = args[1].replace("\\", "\\\\");
						sb.append(tmpStr);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					// sb.append("\"\"");
				}
				sb.append("\"}");
			}
			sb.append("]}");
		}
		sb.append("]}");
		return sb;
	}

	private String grantedPerms2JSON(int id) {
		StringBuffer sb = new StringBuffer();
		/*
		 * Put bundle id
		 */
		sb.append("{\"policies\":[");
		Bundle b = admin.osgi.getBundle(id);
		// AppID aid = pman.getAdminManager().getAppByBundle(b);
		// AppPermission ap = pman.createAppPermission(b.getLocation());// pman.getPolicies(aid);
		/*
		 * Put policies info
		 */
		Map<String, ConditionalPermissionInfo> granted = pman.getGrantedPerms(b);
		Set<Entry<String, ConditionalPermissionInfo>> tlrs = granted.entrySet();
		int index = 0, j = 0;
		for (Map.Entry<String, ConditionalPermissionInfo> entry : tlrs) {
			ConditionalPermissionInfo info = entry.getValue();
			if (j++ != 0)
				sb.append(',');
			sb.append("{\"mode\":\"");
			sb.append(info.getAccessDecision());
			sb.append("\",\"permissions\":[");
			/*
			 * Put Permissions
			 */
			index = 0;
			PermissionInfo pinfos[] = info.getPermissionInfos();
			for (PermissionInfo pi : pinfos) {
				String tmpStr = null;
				if (index++ != 0)
					sb.append(',');
				sb.append("{\"type\":\"");
				tmpStr = pi.getType();
				if (tmpStr != null)
					sb.append(tmpStr);
				sb.append("\",\"filter\":\"");
				String tmp = pi.getName();
				if (tmp != null) {
					tmpStr = tmp.replace("\\", "\\\\");
					sb.append(tmpStr == null ? "" : tmpStr);
				}
				sb.append("\",\"actions\":\"");
				tmpStr = pi.getActions();
				if (tmpStr != null)
					sb.append(tmpStr);
				sb.append("\"}");
			}

			sb.append("],\"conditions\":[");
			/*
			 * Put Conditions
			 */
			index = 0;
			ConditionInfo cinfos[] = info.getConditionInfos();
			for (ConditionInfo ci : cinfos) {
				String tmpStr = null;
				if (index++ != 0)
					sb.append(',');
				sb.append("{\"type\":\"");
				tmpStr = ci.getType();
				sb.append(tmpStr == null ? "" : tmpStr);
				String args[] = ci.getArgs();
				sb.append("\",\"arg1\":\"");
				try {
					if (tmpStr != null) {
						tmpStr = args[0].replace("\\", "\\\\");
						sb.append(tmpStr);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				sb.append("\",\"arg2\":\"");
				try {
					if (tmpStr != null) {
						tmpStr = args[1].replace("\\", "\\\\");
						sb.append(tmpStr);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				sb.append("\"}");
			}
			sb.append("]");
			sb.append(",\"name\":\"");
			sb.append(info.getName());
			sb.append("\"}");
		}
		sb.append("],\"bundlename\":\"");
		sb.append(b.getSymbolicName());
		sb.append("\"}");
		return sb.toString();
	}

	private StringBuffer appsList2JSON() {
		StringBuffer sb = new StringBuffer();
		ArrayList<AdminApplication> apps = (ArrayList<AdminApplication>) pman.getAdminManager().getAllApps();
		sb.append('[');
		int index = 0;
		for (AdminApplication entry : apps) {
			if (index++ != 0)
				sb.append(',');
			sb.append("{\"name\":\"");
			sb.append(entry.getID().getBundle().getSymbolicName());
			sb.append('"');
			sb.append(',');
			sb.append("\"id\":\"");
			sb.append(entry.getBundleRef().getBundleId());
			sb.append('"');
			sb.append('}');
		}
		sb.append(']');
		return sb;
	}

	private StringBuffer bundlesList2JSON() {
		StringBuffer sb = new StringBuffer();
		Bundle[] bundles = admin.osgi.getBundles();
		sb.append('[');
		int index = 0;
		for (Bundle entry : bundles) {
			if (index++ != 0)
				sb.append(',');
			sb.append("{\"name\":\"");
			sb.append(entry.getSymbolicName());
			sb.append('"');
			sb.append(',');
			sb.append("\"id\":\"");
			sb.append(entry.getBundleId());
			sb.append('"');
			sb.append('}');
		}
		sb.append(']');
		return sb;
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
			Class<?> cls = te.getType();
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
		if (this.resMngr == null)
			this.resMngr = admin.appMngr.getResourceAccess();
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
				sb.append(node.getData().getString());
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
				sb.append(node.getData().getStringArr());
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

	void writeResourceValue(TreeElement node, String value) {
		if (this.resMngr == null)
			this.resMngr = admin.appMngr.getResourceAccess();
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

	StringBuffer webResourceTree2JSON(int id, String path, String alias) {
		int index = 0;
		StringBuffer sb;
		sb = new StringBuffer();
		if (path.equals("#")) {
			AppID appid = pman.getAdminManager().getAppByBundle(admin.osgi.getBundle(id));
			if (appid == null)// probably not yet running
				return sb;
			Map<String, String> entries = pman.getWebAccess().getRegisteredResources(appid);
			if (entries == null) {
				sb.append("[]");
				return sb;
			}
			Set<Entry<String, String>> entrySet = entries.entrySet();
			sb.append('[');
			for (Entry<String, String> e : entrySet) {
				String key = e.getKey();
				String name = e.getValue();
				if (index++ != 0)
					sb.append(',');
				sb.append("{\"text\":\"");
				sb.append(key);
				sb.append('"');
				sb.append(',');
				sb.append("\"id\":\"");
				sb.append(name);
				sb.append('"');
				sb.append(',');
				sb.append("\"alias\":\"");
				sb.append(key);
				sb.append('"');
				sb.append(',');
				sb.append("\"children\":true");
				sb.append('}');
			}
			sb.append(']');
			return sb;
		}

		// path = "/";
		Bundle b = admin.osgi.getBundle(id);
		Enumeration<URL> entries = b.findEntries(path, null, false);
		if (entries != null) {
			// permObj.put("webResources", permsArray);
			sb.append('[');
			while (entries.hasMoreElements()) {
				URL url = entries.nextElement();
				// String query ;//= url.getQuery();
				String file = url.getFile();
				boolean isDir = true;
				if (b.findEntries(file, null, false) == null)
					// if (query != null)
					isDir = false;

				if (index++ != 0)
					sb.append(',');
				sb.append("{\"text\":\"");
				// sb.append(file.replaceFirst(replace, ""));
				sb.append(file.replaceFirst(path, alias));
				sb.append('"');
				sb.append(',');
				sb.append("\"id\":\"");
				sb.append(file);
				sb.append('"');
				sb.append(',');
				sb.append("\"alias\":\"");
				sb.append(file.replaceFirst(path, alias));
				sb.append('"');
				sb.append(',');
				if (isDir)
					sb.append("\"children\":true");
				else
					sb.append("\"children\":false");
				sb.append('}');
			}
			sb.append(']');
			return sb;
		}
		else
			return null;
	}

	@SuppressWarnings( { "unchecked", "rawtypes" })
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();

		Map params = req.getParameterMap();
		String info = req.getPathInfo(); // pathinfo /permissions, permission data is application/x-www-form-urlencoded,
		// so they are reached over the parameters list.
		System.out.println("POST: Pathinfo: " + info);

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
		case "/permissions":
			resp.setContentType("text/html");
			int id = -1;
			String idStr = req.getParameter("id");
			if (idStr != null)
				id = Integer.valueOf(idStr);
			if (id != -1) {
				Bundle b = admin.osgi.getBundle(id);

				AppPermission ap = null;
				ap = pman.createAppPermission(b.getLocation());
				// The granted permissions are coded as a json object received as part of a HTTP form.
				String perms = req.getParameter(GRANTED_PERMS_NAME);
				if (perms != null) {
					try {
						JSONObject json = new JSONObject(perms);
						JSONArray granteds = (JSONArray) json.get("permissions");
						int len = granteds.length();
						int index = 0;
						while (len > 0) {
							JSONObject permEntry = granteds.getJSONObject(index++);
							String mode = permEntry.getString("mode");
							String permname = permEntry.getString("name");
							String[] args = new String[2];
							try {
								args[0] = permEntry.getString("filter");
							} catch (JSONException e1) {
								// The filter entry not present, set it to empty string
								args[0] = null;
							}
							try {
								args[1] = permEntry.getString("action");
							} catch (JSONException e1) {
								// The action entry not present, set it to null.
								// if no argument is given to the permission, the args array should be null.
								args[1] = null;
								if (args[0] == null && args[1] == null)
									args = null;
							}
							// Check if a permission or an exception is specified.
							if (mode.toLowerCase().equals("allow"))
								ap.addPermission(permname, args, null);
							else if (mode.toLowerCase().equals("deny"))
								ap.addException(permname, args, null);
							len--;
						}
					} catch (JSONException e1) {
						pw.print("App installation failed! Invalid permission definitions received");
						e1.printStackTrace(pw);
						break;
					}
					try {
						if (pman.installPerms(ap))
							printResponse(resp, "Policies commited successfully!");
						else
							printResponse(resp,
									"Commit of the policies did not occur, because the policy table has been modified externally!");
					} catch (SecurityException e1) {
						printResponse(resp, "Commit of the policies did not occur, because AllPermission is required!");
					} catch (IllegalStateException e2) {
						printResponse(resp, "Commit of the policies did not occur, because of inconsistent table!");
					}
				}
			}
			break;
		case "/uploadApp":
			String path = admin.instMan.getLocalStore().getAddress();
			File file = receiveFile(req, resp, path);
			String name = file.getName();
			startAppInstall(req, resp, path, name);
			break;
		case "/removepermission":
			id = -1;
			idStr = req.getParameter("id");
			if (idStr != null)
				id = Integer.valueOf(idStr);
			if (id != -1) {
				Bundle b = admin.osgi.getBundle(id);
				String perms = req.getParameter("remove");
				if (perms != null) {
					JSONObject json;
					try {
						json = new JSONObject(perms);
						JSONObject toBeremoved = (JSONObject) json.get("permission");
						String permType = toBeremoved.getString("type");
						String filter = toBeremoved.getString("filter");
						String actions = toBeremoved.getString("actions");
						if (actions.length() == 0)
							actions = null;
						pman.removePermission(b, permType, filter, actions);
					} catch (JSONException e1) {
						e1.printStackTrace();
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}

			break;
		default:
			break;
		}
	}

	private File receiveFile(HttpServletRequest req, HttpServletResponse resp, String path) {
		// String filePath = req.getParameter("filename");
		boolean isMultipart;
		int maxFileSize = 1024 * 1024;
		int maxMemSize = 16 * 1024;
		File file = null;
		// Check that we have a file upload request
		isMultipart = ServletFileUpload.isMultipartContent(req);
		resp.setContentType("text/html");
		java.io.PrintWriter out = null;
		try {
			out = resp.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!isMultipart) {
			out.println("<html>");
			out.println("<head>");
			out.println("<title>Servlet upload</title>");
			out.println("</head>");
			out.println("<body>");
			out.println("<p>No file uploaded</p>");
			out.println("</body>");
			out.println("</html>");
			return null;
		}
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// maximum size that will be stored in memory
		factory.setSizeThreshold(maxMemSize);
		// Location to save data that is larger than maxMemSize.
		factory.setRepository(new File(path));

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		// maximum file size to be uploaded.
		upload.setSizeMax(maxFileSize);

		try {
			// Parse the request to get file items.
			List<FileItem> fileItems = upload.parseRequest(req);

			// Process the uploaded file items
			Iterator<FileItem> i = fileItems.iterator();

			while (i.hasNext()) {
				FileItem fi = i.next();
				if (!fi.isFormField()) {
					// Get the uploaded file parameters
					String fileName = fi.getName();
					// Write the file
					if (fileName.lastIndexOf("\\") >= 0) {
						file = new File(path, fileName.substring(fileName.lastIndexOf("\\")));
					}
					else {
						File tempPath = new File(path);
						tempPath.mkdirs();
						file = new File(tempPath, fileName);
					}
					fi.write(file);
				}
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return file;
	}
}
