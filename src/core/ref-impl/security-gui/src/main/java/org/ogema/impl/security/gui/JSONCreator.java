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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.AppID;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.security.AppPermission;
import org.ogema.persistence.ResourceDB;
import org.ogema.resourcetree.SimpleResourceData;
import org.ogema.resourcetree.TreeElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.permissionadmin.PermissionInfo;

public class JSONCreator {

	private PermissionManager pman;

	private SecurityGui admin;

	ResourceDB db;

	private ResourceAccess resMngr;

	private static final String BLC = "org.osgi.service.condpermadmin.BundleLocationCondition";

	JSONCreator(PermissionManager pm, SecurityGui adminapp) {
		this.admin = adminapp;
		this.pman = pm;
		db = adminapp.db;
	}

	String frameworkStartLevel2JSON() {
		JSONObject startLevelObj = new JSONObject();
		Bundle systemBundle = admin.osgi.getBundle(0);
		FrameworkStartLevel level = systemBundle.adapt(FrameworkStartLevel.class);

		try {
			startLevelObj.put("defaultlvl", System.getProperty("org.osgi.framework.storage.fromlevel"));

			startLevelObj.put("currentlvl", level.getInitialBundleStartLevel());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return startLevelObj.toString();
	}

	String startLevel2JSON(int id) {
		JSONObject permObj = new JSONObject();

		try {
			if (compareStartLevels(id)) {
				permObj.put("editable", true);
			}
			else {
				permObj.put("editable", false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return permObj.toString();
	}

	boolean compareStartLevels(int id) {
		Bundle thisBundle = admin.osgi.getBundle(id);
		int startLevel = -1;
		String defaultStartLevel = null;

		BundleStartLevel level = thisBundle.adapt(BundleStartLevel.class);
		startLevel = level.getStartLevel();
		defaultStartLevel = System.getProperty("org.osgi.framework.storage.fromlevel", "5");

		return (startLevel < Integer.parseInt(defaultStartLevel) ? false : true);
	}

	String localPerms2JSON(List<String> perms, String name) {
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
					perm = perm.replaceAll("(.*<<[a-zA-Z_0-9]*) *(.*)", "$1_$2"); // replace
				// permissions
				// names
				// like
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

	StringBuffer bundleInfos2JSON(int id) {
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
		Map<String, ConditionalPermissionInfo> granted = pman.getGrantedPerms(b.getLocation());
		// Map<String, ConditionalPermissionInfo> granted =
		// ap.getGrantedPerms();
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

	String listAll2JSON(int id, List<String> perms, String name) {
		JSONObject listAll = new JSONObject();
		JSONObject grantedObj;
		JSONObject demandedObj;
		try {
			grantedObj = new JSONObject(grantedPerms2JSON(id));
			demandedObj = new JSONObject(localPerms2JSON(perms, name));

			listAll.put("granted", grantedObj);
			listAll.put("demanded", demandedObj);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return listAll.toString();

	}

	JSONObject filtered2JSON(String mode, String permname, String[] args) {
		JSONObject thisFiltered = new JSONObject();
		try {
			thisFiltered.put("mode", mode);
			thisFiltered.put("permname", permname);
			if (args != null) {
				if (args[0] != null)
					thisFiltered.put("filter", args[0]);
				if (args[1] != null)
					thisFiltered.put("actions", args[1]);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return thisFiltered;
	}

	String grantedPerms2JSON(int id) {
		StringBuffer sb = new StringBuffer();
		/*
		 * Put bundle id
		 */
		sb.append("{\"policies\":[");
		Map<String, ConditionalPermissionInfo> granted;
		Bundle b = null;
		// AppID aid = pman.getAdminManager().getAppByBundle(b);
		// AppPermission ap = pman.createAppPermission(b.getLocation());//
		// pman.getPolicies(aid);
		/*
		 * Put policies info
		 */
		if (id != -1) {
			b = admin.osgi.getBundle(id);
			granted = pman.getGrantedPerms(b.getLocation());
		}
		else {
			granted = pman.getGrantedPerms("defaultPolicy");
		}
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
				sb.append('"');
				String tmp = pi.getName();
				if (tmp != null) {
					sb.append(",\"filter\":\"");
					tmpStr = tmp.replace("\\", "\\\\");
					sb.append(tmpStr);
					sb.append('"');
				}

				tmpStr = pi.getActions();
				if (tmpStr != null) {
					sb.append(",\"actions\":\"");
					sb.append(tmpStr);
					sb.append('"');
				}
				sb.append("}");
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
		if (id != -1) {
			sb.append(b.getSymbolicName());
			sb.append("\"");
			sb.append(",\"editable\":\"");
			sb.append(compareStartLevels(id));
		}
		else {
			sb.append("defaultPolicy");
		}
		sb.append("\"}");
		return sb.toString();
	}

	StringBuffer appsList2JSON() {
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

	StringBuffer bundlesList2JSON() {
		StringBuffer sb = new StringBuffer();
		String descr = "";
		Bundle[] bundles = admin.osgi.getBundles();
		sb.append('[');
		int index = 0;
		for (Bundle entry : bundles) {
			if (index++ != 0)
				sb.append(',');
			sb.append("{\"name\":\"");
			if (entry.getSymbolicName() == null) {
				int i = entry.getLocation().lastIndexOf("/");
				int j = entry.getLocation().indexOf(".jar");
				sb.append(entry.getLocation().substring(i + 1, j));
			}
			else {
				sb.append(entry.getSymbolicName());
			}
			sb.append('"');
			sb.append(',');
			sb.append("\"id\":\"");
			sb.append(entry.getBundleId());
			sb.append('"');
			sb.append(',');
			sb.append("\"description\":\"");
			Dictionary<String, String> metaInf = entry.getHeaders();

			descr = metaInf.get("Bundle-Description");
			if (descr == "" || descr == null) {
				sb.append("");
			}
			else {
				sb.append(descr);
			}
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

	StringBuffer webResourceTree2JSON(int id, String path, String alias) {
		int index = 0;
		StringBuffer sb;
		sb = new StringBuffer();
		if (path.equals("#")) {
			AppID appid = pman.getAdminManager().getAppByBundle(admin.osgi.getBundle(id));
			if (appid == null) // probably not yet running
				return sb;
			@SuppressWarnings("deprecation")
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

	String newOgemaPolicy() {
		String directory = System.getProperty("user.dir");
		directory += "\\ogema";
		String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
		boolean success = false;
		String nl = "\n";
		StringBuffer sb = new StringBuffer();
		Map<String, ConditionalPermissionInfo> granted = pman.getGrantedPerms("all");
		Set<Entry<String, ConditionalPermissionInfo>> tlrs = granted.entrySet();

		for (Map.Entry<String, ConditionalPermissionInfo> entry : tlrs) {
			ConditionalPermissionInfo info = entry.getValue();

			// add the BundleLocationCondition to the StringBuffer if the
			// ConditionInfo[] is not empty and add the AccessDecision
			if (info.getConditionInfos().length != 0) {
				for (ConditionInfo ci : info.getConditionInfos()) {
					for (String loc : ci.getArgs()) {
						sb.append(info.getAccessDecision());
						sb.append(" { [" + BLC + " \"" + loc + "\"] ");
					}
				}
			}
			else {
				sb.append(info.getAccessDecision());
				sb.append(" { ");
			}
			// add the Permissions
			PermissionInfo[] permInfos = info.getPermissionInfos();
			for (PermissionInfo pInfo : permInfos) {
				sb.append("(");
				sb.append(pInfo.getType());
				if (pInfo.getName() != null) {
					if (pInfo.getName().indexOf("urp") != -1) {
						continue;
					}
					sb.append(" \"" + pInfo.getName() + "\"");
				}
				if (pInfo.getActions() != null) {
					sb.append(" \"" + pInfo.getActions() + "\"");
				}
				sb.append(") ");
			}
			sb.append("} ");
			sb.append("\"" + entry.getKey() + "\"");
			sb.append(nl + nl);
		}
		// create the new File
		File newPol = new File(directory, "ogema_" + date + ".policy");
		try {
			BufferedWriter bwr = new BufferedWriter(new FileWriter(newPol));
			bwr.write(sb.toString());
			bwr.close();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (success) {
			return ("A new Policy File \"ogema_" + date + ".policy\" was created at: " + nl + directory);
		}
		else {
			return ("There was an ERROR creating a new ogema.policy File");
		}

	}

	String newOgemaConfig() {
		boolean success = false;
		String directory = System.getProperty("user.dir");
		directory += "\\ogema";
		String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
		String nl = "\n";
		StringBuffer sb = new StringBuffer();
		ArrayList<String> bundleList = new ArrayList<String>();
		int startLevel = 0;
		HashMap<Integer, ArrayList<String>> bundleMap = new HashMap<Integer, ArrayList<String>>();
		Bundle[] bundles = admin.osgi.getBundles();

		for (Bundle b : bundles) {

			if ((b.getLocation().contains("urp") && b.getSymbolicName() != "urps")
					|| b.getLocation().equals("System Bundle")) {
				continue;
			}
			// Add the Startlevel of the Bundle and the Bundle Location to the
			// bundleMap
			BundleStartLevel level = b.adapt(BundleStartLevel.class);
			startLevel = level.getStartLevel();
			bundleList = bundleMap.get(startLevel);
			if (bundleList == null) {
				bundleList = new ArrayList<String>();
				bundleList.add(b.getLocation());
				bundleMap.put(startLevel, bundleList);
			}
			else {
				bundleList.add(b.getLocation());
				bundleMap.put(startLevel, bundleList);
			}

		}
		Set<Integer> keyset = bundleMap.keySet();
		ArrayList<Integer> keylist = new ArrayList<Integer>(new TreeSet<Integer>(keyset));
		java.util.Collections.sort(keylist);

		for (Integer key : keylist) {
			// Add the Startlevels and their Bundles to the StringBuffer
			sb.append("felix.auto.start.");
			sb.append(key);
			sb.append("=");
			bundleList = bundleMap.get(key);
			for (String s : bundleList) {
				sb.append(s);
				sb.append(" \\" + nl);
			}
			sb.deleteCharAt(sb.lastIndexOf("\\") - 1);
			sb.deleteCharAt(sb.lastIndexOf("\\"));
		}
		// create the new File
		File prop = new File(directory + "\\config_tmp.properties");
		try {
			String lines = FileUtils.readFileToString(prop);
			sb.append(lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
		File newConfig = new File(directory, "config_" + date + ".properties");

		try {
			BufferedWriter bwr = new BufferedWriter(new FileWriter(newConfig));
			bwr.write(sb.toString());
			bwr.close();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (success) {
			return ("A new config File \"config_" + date + ".properties\" was created at: " + nl + directory);
		}
		else {
			return ("There was an ERROR creating a new config.properties File");
		}
	}
}
