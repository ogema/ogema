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
import java.security.AllPermission;
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
import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.InstallableApplication;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.security.AppPermission;
import org.ogema.persistence.ResourceDB;
import org.ogema.resourcetree.SimpleResourceData;
import org.ogema.resourcetree.TreeElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.slf4j.Logger;

public class SecurityGuiServlet extends HttpServlet {

	/**
	 * 
	 */

	private final BundleIcon defaultIcon = new BundleIcon(getClass().getResource("/admin/images/appdefaultlogo.svg"),
			BundleIcon.IconType.SVG);

	private static final long serialVersionUID = 7370224231398359148L;

	private static final String GRANTED_PERMS_NAME = "permission";

	private static final boolean DEBUG = false;

	final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	private PermissionManager pman;

	private SecurityGui admin;

	ResourceDB db;

	private ResourceAccess resMngr;

	private JSONCreator json;

	private DefaultFilter filter;

	SecurityGuiServlet(PermissionManager pm, SecurityGui adminapp) {
		this.admin = adminapp;
		this.pman = pm;
		db = adminapp.db;
		this.json = new JSONCreator(pm, adminapp);
		this.filter = new DefaultFilter(logger);
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
			appStoresData.put(index++, app.getLocation());
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
		case "/geticon":
			id = Integer.valueOf(req.getParameter("id"));
			BundleIcon.forBundle(admin.osgi.getBundle(id), defaultIcon).writeIcon(resp);
			break;
		case "/getappstoreicon":
			String loc = req.getParameter("loc");
			BundleIcon.forNewBundle(loc, defaultIcon, resp);
			break;
		case "/frameworkstartlevel":
			resp.setContentType("application/json");
			data = json.frameworkStartLevel2JSON();
			if (data != null) {
				printResponse(resp, data);
			}
			else {
				printResponse(resp, "error");
			}
			break;
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
			try {
				startAppInstall(req, resp, appSource.getAddress(), name);
			} catch (Throwable t) {
				printResponse(resp, "Installation failed.");
				t.printStackTrace();

			}
			break;
		case "/startlevel":
			resp.setContentType("application/json");
			String idStr = req.getParameter("id");
			if (idStr != null)
				id = Integer.valueOf(idStr);
			if (id != -1) {
				try {
					data = json.startLevel2JSON(id);
					printResponse(resp, data);
				} catch (Exception e) {
					e.printStackTrace(resp.getWriter());
				}

			}
			else {
				printResponse(resp, "Invalid bundle id " + idStr);
			}
			break;
		case "/localepermissions":
			resp.setContentType("application/json");
			idStr = req.getParameter("id");
			if (idStr != null)
				id = Integer.valueOf(idStr);
			if (id != -1) {
				Bundle b = admin.osgi.getBundle(id);
				if (b != null) {
					List<String> perms = SecurityGui.getLocalPerms(b);
					try {
						data = json.localPerms2JSON(perms, b.getSymbolicName());
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
		case "/listpermissions":
			resp.setContentType("application/json");
			idStr = req.getParameter("id");
			if (idStr != null)
				id = Integer.valueOf(idStr);
			if (id != -1) {
				Bundle b = admin.osgi.getBundle(id);
				if (b != null) {
					List<String> perms = SecurityGui.getLocalPerms(b);
					try {
						data = json.listAll2JSON(id, perms, b.getSymbolicName());
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
		case "/defaultpolicy":
			resp.setContentType("application/json");
			try {
				data = json.grantedPerms2JSON(-1);
				printResponse(resp, data);
			} catch (Exception e) {
				e.printStackTrace(resp.getWriter());
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
						data = json.grantedPerms2JSON(id);
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
			sb = json.simpleResourceValue2JSON(id);
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
					sb = json.bundleInfos2JSON(id);
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
				if (admin.osgi.getBundle(id).getLocation().startsWith("urp")) {
					printResponse(resp,
							"{\"statusInfo\":\"Uninstall of User Rights is not supported over this interface.\"}");
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
				sb = json.appsList2JSON();
				data = sb.toString();
				printResponse(resp, data);
				break;
			case "listAll":
				sb = json.bundlesList2JSON();
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
				sb = json.webResourceTree2JSON(id, path, alias);
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
		admin.instMan.install(app);
		b = app.getBundle();
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

	private void printResponse(HttpServletResponse resp, String data) throws IOException {
		PrintWriter pw = resp.getWriter();
		pw.print(data);
	}

	@SuppressWarnings( { "unchecked", "rawtypes" })
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();

		Map params = req.getParameterMap();
		String info = req.getPathInfo(); // pathinfo /permissions, permission
		// data is
		// application/x-www-form-urlencoded,
		// so they are reached over the parameters list.
		System.out.println("POST: Pathinfo: " + info);

		String currenturi = req.getRequestURI();
		StringBuffer url = req.getRequestURL();
		if (DEBUG) {
			logger.info("Current URI: " + currenturi); // URI:
			// /service/permissions
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
		case "/newstartlevel":
			resp.setContentType("text/html");
			String levelstr = "";
			int newlevel = -1;
			levelstr = req.getParameter("level");
			if (levelstr.length() != 0)
				newlevel = Integer.valueOf(levelstr);

			Bundle systemBundle = admin.osgi.getBundle(0);
			FrameworkStartLevel level = systemBundle.adapt(FrameworkStartLevel.class);

			level.setInitialBundleStartLevel(newlevel);

			if (level.getInitialBundleStartLevel() == newlevel) {
				printResponse(resp, "The startlevel has successfully been edited");
			}
			else {
				printResponse(resp, "There was an error");
			}

			break;
		case "/newpolicy":
			resp.setContentType("text/html");
			String success = json.newOgemaConfig();
			success = success + " and " + json.newOgemaPolicy();
			printResponse(resp, success);
			break;
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
			int id = -2;
			boolean defPol = false;
			String idStr = req.getParameter("id");
			if (idStr != null)
				id = Integer.valueOf(idStr);
			if (id != -2) {

				Bundle b = null;
				AppPermission ap = null;
				if (id != -1) {
					b = admin.osgi.getBundle(id);
					ap = pman.createAppPermission(b.getLocation());
				}
				else {
					ap = pman.getDefaultPolicies();
					defPol = true;
				}
				String perms = req.getParameter(GRANTED_PERMS_NAME);
				String permType = "";
				String filterstr = "";
				String actions = "";

				JSONArray filtereds = new JSONArray();
				JSONObject filterReturn = new JSONObject();
				// The granted permissions are coded as a json object
				// received as part of a HTTP form.

				if (perms != null) {
					try {
						JSONObject jsonObj = new JSONObject(perms);
						JSONArray granteds = (JSONArray) jsonObj.get("permissions");
						JSONArray removeds;

						try {
							removeds = (JSONArray) jsonObj.get("oldpermissions");
						} catch (JSONException e1) {
							removeds = null;
						}
						int len = granteds.length();
						int index = 0;
						while (len > 0) {
							JSONObject permEntry = granteds.getJSONObject(index);
							if (removeds != null && removeds.length() > 0) {
								JSONObject toBeremoved = removeds.getJSONObject(index++);
								permType = toBeremoved.getString("name");
								try {
									filterstr = toBeremoved.getString("filter");
								} catch (JSONException e1) {
									filterstr = null;
								}
								try {
									actions = toBeremoved.getString("action");
								} catch (JSONException e1) {
									actions = null;
								}
							}
							else {
								index++;
							}
							String mode = permEntry.getString("mode");
							String permname = permEntry.getString("name");
							String[] args = new String[2];

							try {
								args[0] = permEntry.getString("filter");
							} catch (JSONException e1) {
								// The filter entry not present, set it to
								// empty string
								args[0] = null;
							}
							try {
								args[1] = permEntry.getString("action");
								// HACK TODO this is a workaround for a bug in the javascript sources. "Check,All" added
								// as action.
								if (args[1].indexOf("Check,All,") != -1)
									args[1] = args[1].substring(10, args[1].length());
							} catch (JSONException e1) {
								// The action entry not present, set it to
								// null.
								// if no argument is given to the
								// permission, the args array should be
								// null.
								args[1] = null;
								if (args[0] == null && args[1] == null)
									args = null;
							}
							// If AllPermission is granted remove all
							// granteds before
							if (permname.equals(AllPermission.class.getName())) {
								if (mode.toLowerCase().equals("allow")) {
									// If AllPermission is granted all other
									// permissions granted before stay
									// granted
									// In this case no AllPermission will be
									// granted but only the default
									// permission,
									// that are granted via the ogema.policy
									// file.
									// Don't break the loop because further
									// denies should't be ignored
									ap.removePermissions();
									// In this case all forbiddens should
									// added as exception
									filter.addDefaultExceptions(ap);
									addPolicy(ap, mode, permname, args);

									// Don't break the loop because further
									// denies should't be ignored
								}
								else {
									// If AllPermission is denied no other
									// policy has any effect
									ap.removePermissions();
									ap.removeExceptions();
									addPolicy(ap, mode, permname, args);

									break; // Break the loop further
									// allows/denies have no effect
								}
							}
							// Check if the permission match with the global
							// default policy
							else if (!filter.filterPermission(mode, permname, args, ap)) {
								addPolicy(ap, mode, permname, args);
								if (!(permname.equals(permType)
										&& args[0].equals(filterstr)
										&& ((args[1] == null && actions == null) || (args[1] != null && args[1]
												.equals(actions))) && removeds == null)) {
									pman.removePermission(b, permType, filterstr, actions);
								}
							}
							else {
								filtereds.put(json.filtered2JSON(mode, permname, args));
							}
							len--;
						}
					} catch (JSONException e1) {
						pw.print("App installation failed! Invalid permission definitions received");
						e1.printStackTrace(pw);
						break;
					}
					try {
						if (pman.installPerms(ap)) {
							if (defPol)
								pman.setDefaultPolicies();
							if (filtereds.length() == 0) {
								printResponse(resp, "Policies commited successfully!");
							}
							else {
								resp.setContentType("application/json");
								filterReturn.put("filtered", filtereds);
								printResponse(resp, filterReturn.toString());
							}
						}
						else
							printResponse(resp,
									"Commit of the policies did not occur, because the policy table has been modified externally!");
					} catch (SecurityException e1) {
						printResponse(resp, "Commit of the policies did not occur, because AllPermission is required!");
					} catch (IllegalStateException e2) {
						printResponse(resp, "Commit of the policies did not occur, because of inconsistent table!");
					} catch (JSONException e3) {
						e3.printStackTrace();
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
			id = -2;
			int count = -1;
			idStr = req.getParameter("id");
			String countStr = req.getParameter("count");
			if (idStr != null)
				id = Integer.valueOf(idStr);
			if (countStr != null)
				count = Integer.valueOf(countStr);
			if (id != -2) {
				Bundle b = admin.osgi.getBundle(id);
				String perms = req.getParameter("remove");
				if (perms != null) {
					JSONObject json;
					JSONObject toBeremoved = null;
					try {
						json = new JSONObject(perms);
						for (int i = 0; i < count; i++) {
							// get each of the Permissions that should be
							// removed
							toBeremoved = (JSONObject) json.get("permission" + i);
							String permType = toBeremoved.getString("type");
							String filter, actions;
							try {
								filter = toBeremoved.getString("filter");
							} catch (JSONException e1) {
								filter = null;
							}
							try {
								actions = toBeremoved.getString("actions");
							} catch (JSONException e1) {
								actions = null;
							}

							if (id != -1) {

								pman.removePermission(b, permType, filter, actions);
							}
							else {
								// remove one Default Permission
								pman.removePermission(null, permType, filter, actions);

								pman.setDefaultPolicies();
							}

						}
					} catch (JSONException e1) {
						logger.debug("To be removed permission: %s", toBeremoved);
					}
				}
			}

			break;
		case "/removeall":
			id = -2;
			Bundle b = null;
			defPol = false;
			idStr = req.getParameter("id");
			if (idStr != null)
				id = Integer.valueOf(idStr);
			if (id != -2) {
				AppPermission ap = null;
				if (id != -1) {
					b = admin.osgi.getBundle(id);
					ap = pman.createAppPermission(b.getLocation());
				}
				else {
					ap = pman.getDefaultPolicies();
					defPol = true;
				}
				ap.removePermissions();
				ap.removeExceptions();
			}
			if (defPol)
				pman.setDefaultPolicies();
			break;
		default:
			break;
		}
	}

	void addPolicy(AppPermission ap, String mode, String permname, String[] args) {
		// Check if a permission or an exception is specified.
		if (mode.toLowerCase().equals("allow"))
			ap.addPermission(permname, args, null);
		else if (mode.toLowerCase().equals("deny"))
			ap.addException(permname, args, null);
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

	private File receiveFile(HttpServletRequest req, HttpServletResponse resp, String path) {
		// String filePath = req.getParameter("filename");
		boolean isMultipart;
		int maxFileSize = 1024 * 1024 * 10;
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
