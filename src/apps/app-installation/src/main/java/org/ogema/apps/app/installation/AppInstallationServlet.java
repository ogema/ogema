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
package org.ogema.apps.app.installation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.apps.app.installation.utils.AppStoreUtils;
import org.ogema.apps.app.installation.utils.Utils;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.AppID;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.InstallableApplication;
import org.ogema.core.security.AppPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.condpermadmin.ConditionInfo;

public class AppInstallationServlet extends HttpServlet {

	private AppInstallationController appController;
	private AdministrationManager administrationManager;
	private PermissionManager permissionManager;
	private BundleContext bundleContext;

	public AppInstallationServlet(AppInstallationController appController, AdministrationManager administrationManager,
			BundleContext bundleContext) {
		this.appController = appController;
		this.administrationManager = administrationManager;
		this.bundleContext = bundleContext;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String pi = req.getPathInfo();
		if (pi == null) {
			Utils.log("AdminServlet no path URI specified", this.getClass());
			return;
		}

		// OutputStream bout = resp.getOutputStream();
		String data = null;
		Utils.log("AdminServlet path URI is " + pi, this.getClass());

		/*
		 * List of locations where App-files archived (Appstores)
		 */
		switch (pi) {
		case "/appstores":
			Utils.log("Get Appstores", this.getClass());
			try {
				data = appController.getAppstoresData();
				Utils.printResponse(resp, data);
			} catch (Exception e1) {
				e1.printStackTrace(resp.getWriter());
			}
			break;
		/*
		 * List of the apps in a specific location.
		 */
		case "/apps": // The path is in this case /serletName/path1
			String appStore = req.getParameter("name");
			appStore = "localAppDirectory";
			// AppStore appSource = AppStoreController.getInstance().getAppStores().get(appStore);
			ApplicationSource appSource = administrationManager.getInstallationManager().connectAppSource(appStore);
			Utils.log("Get Apps in " + appSource.getAddress(), this.getClass());
			if (appStore.equals(administrationManager.getInstallationManager().getDefaultAppStore().getName())) {
				File f = new File(appSource.getAddress());
				if (!f.exists()) {
					Utils.printResponse(resp, "No Apps available");
					return;
				}
				else {
					try {
						data = appController.getAppFiles(f);
						Utils.printResponse(resp, data);
					} catch (Exception e) {
						e.printStackTrace(resp.getWriter());
					}
				}
			}
			else {
				Utils.printResponse(resp, "Only local appstores supported yet");
			}
			break;
		case "/app":
			String name = req.getParameter("name");
			appStore = req.getParameter("appstore");
			// appSource = AppStoreController.getInstance().getAppStores().get(appStore);
			appSource = administrationManager.getInstallationManager().connectAppSource(appStore);
			if (appStore == null || name == null) {
				Utils.printResponse(resp, "No appstore or app is selected.");
				break;
			}
			appController.startAppInstall(req, resp, appSource.getAddress(), name);
			break;
		default:
			break;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();

		Map<String, String[]> params = req.getParameterMap();
		String info = req.getPathInfo(); // pathinfo /permissions, permission data is application/x-www-form-urlencoded,
		// so they are reached over the parameters list.
		System.out.println("POST: Pathinfo: " + info);

		String currenturi = req.getRequestURI();
		StringBuffer url = req.getRequestURL();

		Set<Map.Entry<String, String[]>> paramsEntries = params.entrySet();

		for (Map.Entry<String, String[]> e : paramsEntries) {
			String key = e.getKey();
			String[] val = e.getValue();
		}
		switch (info) {
		case "/permissions":
			// check if the installation process has a valid state
			// Check if the parameter of the current installation process has changed
			String appname = req.getParameter("name");
			String appStore = req.getParameter("appstore");
			// AppStore appSource = AppStoreController.getInstance().getAppStores().get(appStore);
			ApplicationSource appSource = administrationManager.getInstallationManager().connectAppSource(appStore);
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
								//logger.info("Bundle installed from " + b.getLocation());
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
			File file = appController.receiveFile(req, resp);
			if (file != null) {
				Utils.log("app uploaded to appstore", getClass());
				//String message = Utils.createMessage("OK", "app uploaded to appstore");
				//resp.getWriter().write(message);
				resp.setStatus(200);
			}
			else {
				Utils.log("app upload incomplete", getClass());
				//String message = Utils.createMessage("ERROR", "app upload incomplete");
				//resp.getWriter().write(message);
			}
			//String name = file.getName();
			//AppStoreController.getInstance().startAppInstall(req, resp, tmpFileUpload.getAddress(), name);
			break;
		default:
			break;
		}
	}

}
