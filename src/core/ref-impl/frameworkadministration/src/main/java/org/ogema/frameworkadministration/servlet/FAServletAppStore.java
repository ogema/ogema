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
package org.ogema.frameworkadministration.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.AppID;
import org.ogema.core.security.AppPermission;
import org.ogema.core.security.WebAccessManager;
import org.ogema.frameworkadministration.controller.AppStoreController;
import org.ogema.frameworkadministration.utils.AppStoreUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.InstallableApplication;
import org.ogema.frameworkadministration.json.AppsJsonAppConditions;
import org.ogema.frameworkadministration.json.AppsJsonAppPermissions;
import org.ogema.frameworkadministration.json.AppsJsonAppPolicies;
import org.ogema.frameworkadministration.json.AppsJsonAppPolicy;
import org.ogema.frameworkadministration.utils.BundleIcon;
import org.ogema.frameworkadministration.utils.Utils;
import org.osgi.service.condpermadmin.ConditionInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FAServletAppStore extends HttpServlet {

	private static final long serialVersionUID = 7370224231398359148L;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	private final PermissionManager permissionManager;
	private final BundleContext bundleContext;
	private final long bundleID;
	private final AdministrationManager administrationManager;
	private final AppStoreController appStoreController;

	private final BundleIcon defaultIcon = new BundleIcon(getClass().getResource(
			"/org/ogema/frameworkadministration/gui/img/svg/appdefaultlogo.svg"), BundleIcon.IconType.SVG);

	public FAServletAppStore(PermissionManager permissionManager, BundleContext bundleContext, long bundleID,
			AdministrationManager administrationManager, AppStoreController appStoreController) {
		this.permissionManager = permissionManager;
		this.bundleContext = bundleContext;
		this.bundleID = bundleID;
		this.administrationManager = administrationManager;
		this.appStoreController = appStoreController;
	}

	public void register(WebAccessManager wam) {
		wam.registerWebResource("/admin", "/admin");
		wam.registerWebResource("/install", this);
	}

	public void unregister(WebAccessManager wam) {
		wam.unregisterWebResource("/admin");
		wam.unregisterWebResource("/install");
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String logout = req.getParameter("action");
		if ("logout".equals(logout)) {
			req.getSession().invalidate();
			return;
		}

		String pi = req.getPathInfo();
		StringBuffer sb = null;
		if (pi == null) {
			logger.info("AdminServlet no path URI specified");
			return;
		}

		String data = null;

		int id = -1;

		/*
		 * List of locations where App-files archived (Appstores)
		 */
		switch (pi) {
		case "/installedapps":
			resp.setContentType("application/json");
			String action = req.getParameter("action");
			String idStr = req.getParameter("app");

			if (idStr != null) {
				//FIXME: remove this error in javascript rather than here (use of angular ng-markups could solve this)  
				if (idStr.equals("{{app.id}}")) {
					break;
				}
				id = Integer.valueOf(idStr);
			}
			switch (action) {
			case "getInfo":
				if (id != -1) {
					sb = appStoreController.appInfos2JSON(id);
					data = sb.toString();
					printResponse(resp, data);
				}
				else
					printResponse(resp, "Invalid app id");
				break;
			case "getIcon":
				id = Integer.valueOf(idStr);
				BundleIcon icon = BundleIcon.forBundle(bundleContext.getBundle(id), defaultIcon);
				icon.writeIcon(resp);
				break;
			case "update":
				id = Integer.valueOf(idStr);
				/*
				 * if this bundle tries to update itself, its not supported. In this case return without doing anything.
				 */
				if (bundleID == id) {
					printResponse(resp,
							"{\"statusInfo\":\"Update of the admin app is not supported over this interface.\"}");
					return;
				}
				try {
					bundleContext.getBundle(id).update();
					printResponse(resp, "{\"statusInfo\":\"Update Succeded\"}");
				} catch (BundleException e) {
					printResponse(resp, "{\"statusInfo\":\"");
					e.printStackTrace(resp.getWriter());
					printResponse(resp, "\"}");
				}
				break;
			case "delete":
				id = Integer.valueOf(idStr);
				/*
				 * if this bundle tries to update itself, its not supported. In this case return without doing anything.
				 */
				if (bundleID == id) {
					printResponse(resp,
							"{\"statusInfo\":\"Uninstall of the admin app is not supported over this interface.\"}");
					return;
				}
				try {
					bundleContext.getBundle(id).uninstall();
					printResponse(resp, "{\"statusInfo\":\"Uninstall Succeded\"}");
				} catch (BundleException e) {
					printResponse(resp, "{\"statusInfo\":\"");
					e.printStackTrace(resp.getWriter());
					printResponse(resp, "\"}");
				}
				break;
			case "listAll":
				sb = appStoreController.appsList2JSON();
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
				sb = appStoreController.webResourceTree2JSON(id, path, alias);
				if (sb != null) {
					data = sb.toString();
					printResponse(resp, data);
				}
				break;
			}
			break;
		}
	}

	private void printResponse(HttpServletResponse resp, String data) throws IOException {
		PrintWriter pw = resp.getWriter();
		pw.print(data);
	}

	@Override
	@SuppressWarnings( { "unchecked", "fallthrough" })
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();

		Map<String, String[]> params = req.getParameterMap();
		String info = req.getPathInfo(); // pathinfo /permissions, permission data is application/x-www-form-urlencoded,
		// so they are reached over the parameters list.
		System.out.println("POST: Pathinfo: " + info);

		String currenturi = req.getRequestURI();
		StringBuffer url = req.getRequestURL();

		logger.info("Current URI: " + currenturi); // URI: /install/permissions
		logger.info("Current URL: " + url);

		Set<Entry<String, String[]>> paramsEntries = params.entrySet();

		for (Map.Entry<String, String[]> e : paramsEntries) {
			String key = e.getKey();
			String[] val = e.getValue();
			logger.info(key + "\t: ");
			for (String s : val)
				logger.info(s);
		}
		switch (info) {
		case "/installedapps": {
			String action = req.getParameter("action");
			String app = req.getParameter("app");
			if ("setPermission".equals(action) && app != null) {
				String settings = req.getParameter("settings");

				//refactor into methos later on
				//put message into useable object
				ObjectMapper mapper = new ObjectMapper();
				AppsJsonAppPolicies appPolicies = mapper.readValue(settings, AppsJsonAppPolicies.class);

				//install permissions
				long id = Long.parseLong(app);
				Bundle bundle = bundleContext.getBundle(id);
				AppID appID = administrationManager.getAppByBundle(bundle);
				AppPermission ap = permissionManager.getPolicies(appID);

				List<AppsJsonAppPolicy> appPolicyList = appPolicies.getPolicies();
				for (AppsJsonAppPolicy appPolicy : appPolicyList) {

					String mode = appPolicy.getMode(); //allow,deny
					String uniqueName = appPolicy.getUniqueName();

					ap.removePermission(uniqueName);

					if (appPolicy.isDelete()) {
						continue;
					}
					List<AppsJsonAppConditions> conditions = appPolicy.getConditions();
					List<AppsJsonAppPermissions> permissions = appPolicy.getPermissions();

					@SuppressWarnings("unused")
					ConditionInfo conditionInfo = null;

					if (conditions != null && !conditions.isEmpty()) {

						//there is only one condition
						AppsJsonAppConditions appCondition = conditions.get(0);
						if (appCondition != null) {
							@SuppressWarnings("unused")
							String conditionType = appCondition.getType();
							String arg1 = appCondition.getArg1();
							String arg2 = appCondition.getArg2();
							String[] args = null;
							if (arg1.length() > 0 && arg2.length() > 0) {
								args = new String[2];
								args[0] = arg1;
								args[1] = arg2;
							}
							else if (arg1.length() > 0) {
								args = new String[1];
								args[0] = arg1;
							}
							else if (arg2.length() > 0) {
								args = new String[1];
								args[0] = arg2;
							}
							conditionInfo = new ConditionInfo(mode, args);
						}
					}
					for (AppsJsonAppPermissions appPermission : permissions) {

						String actions = appPermission.getActions(); //"read,write,addsub,create,activity,delete"
						String filter = appPermission.getFilter(); //path=*
						String type = appPermission.getType(); //"org.ogema.accesscontrol.ResourcePermission"

						String[] args = null;

						if (filter.length() > 0 && actions.length() > 0) {
							args = new String[2];
							args[0] = filter;
							args[1] = actions;
						}
						else if (filter.length() > 0) {
							args = new String[1];
							args[0] = filter;
						}
						else if (actions.length() > 0) {
							args = new String[1];
							args[0] = actions;
						}

						if (mode.toLowerCase().equals("allow")) {
							ap.addPermission(type, args, null);
						}
						else if (mode.toLowerCase().equals("deny")) {
							ap.addException(type, args, null);
						}

					}
				}

				permissionManager.installPerms(ap);

				String message = Utils.createMessage("OK", "new permissions set");
				resp.getWriter().write(message);
				resp.setStatus(200);

			}

			break;
		}
		case "/permissions":
			// check if the installation process has a valid state
			// Check if the parameter of the current installation process has changed
			String appname = req.getParameter("name");
			String appStore = req.getParameter("appstore");
			ApplicationSource appSource = administrationManager.getSources().connectAppSource(appStore);
			InstallableApplication installingApp = (InstallableApplication) req.getSession().getAttribute(
					AppStoreUtils.INSTALLATION_STATE_ATTR_NAME);
			if (!installingApp.getName().equals(appname)
					|| !installingApp.getDirectory().equals(appSource.getAddress())) {
				pw
						.print("Invalid installation parameter. Current installation is aborted. Please restart the installation.");
				req.getSession().setAttribute(AppStoreUtils.INSTALLATION_STATE_ATTR_NAME, null);
				break;
			}

			AppPermission ap = null;
			AppID id = null;
			id = installingApp.getAppid();

			// Action depends on the current state of the installation process
			switch (installingApp.getState()) {
			case APPCHOSEN:
				pw.print("Invalid state of installation process.");
				break;
			case DESIRED_PERMS_SENT:
				installingApp.setState(InstallableApplication.InstallState.RECEIVE_GRANTED);
				//XXX fall-through ?!?
			case RECEIVE_GRANTED:
				if (id != null && id.getLocation().endsWith(appname)) {
					ap = permissionManager.getPolicies(id);
				}
				else {
					ap = permissionManager.createAppPermission(installingApp.getLocation());
				}
				{
					// The granted permissions are coded as a json object received as part of a HTTP form.
					String perms = req.getParameter(AppStoreUtils.GRANTED_PERMS_NAME);
					if (perms != null) {
						try {
							JSONObject json = new JSONObject(perms);
							JSONArray granteds = (JSONArray) json.get("localePerms");
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
						// After all received permissions are stored in the AppPermission instance they could be
						// transfered
						// to the PermissionManager.
						permissionManager.installPerms(ap);

						// Now its time to install the bundle
						{
							installingApp.setState(InstallableApplication.InstallState.BUNDLE_INSTALLING);
							Bundle b = null;
							try {
								b = bundleContext.installBundle(installingApp.getLocation());
							} catch (BundleException e1) {
								e1.printStackTrace(pw);
							}
							if (b != null) {
								installingApp.setState(InstallableApplication.InstallState.BUNDLE_INSTALLED);
								installingApp.setBundle(b);
								logger.info("Bundle installed from " + b.getLocation());
							}
							else {
								installingApp.setState(InstallableApplication.InstallState.ABORTED);
								pw.print("Bundle installation failed!");
								break;
							}
						}

						try {
							installingApp.getBundle().start();
						} catch (BundleException e1) {
							pw.print("Bundle start failed!");
							e1.printStackTrace(pw);
							break;
						}
						// Wait until the Application service is reachable
						int tries = 20;
						AppID appid = null;
						while (appid == null && tries-- > 0) {
							appid = administrationManager.getAppByBundle(installingApp.getBundle());
							if (appid == null)
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
								}
						}
						if (appid != null) {
							installingApp.setAppid(appid);
							// if (Configuration.DEBUG) {
							// admin.printGrantedPerms(pman.getPolicies(appid));
							// }
						}
						else {
							installingApp.setState(InstallableApplication.InstallState.ABORTED);
							pw.print("App installation failed! Installed bundle is probably not an Application.");
							break;
						}

						// Remove the installation status object from the session
						req.getSession().setAttribute(AppStoreUtils.INSTALLATION_STATE_ATTR_NAME, null);
					}
				}
				break;
			case BUNDLE_INSTALLING:
			case ABORTED:
			default:
			}
			break;
		case "/uploadApp":
			File file = appStoreController.receiveFile(req, resp);
			if (file != null) {
				Utils.log("app uploaded to appstore", getClass());
				String message = Utils.createMessage("OK", "app uploaded to appstore");
				resp.getWriter().write(message);
				resp.setStatus(200);
			}
			else {
				Utils.log("app upload incomplete", getClass());
				String message = Utils.createMessage("ERROR", "app upload incomplete");
				resp.getWriter().write(message);
			}
			//String name = file.getName();
			//appStoreController.startAppInstall(req, resp, tmpFileUpload.getAddress(), name);
			break;
		default:
			break;
		}
	}

}
