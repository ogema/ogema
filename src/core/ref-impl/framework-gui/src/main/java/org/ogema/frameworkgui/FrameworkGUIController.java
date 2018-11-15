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
package org.ogema.frameworkgui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.AppID;
import org.ogema.frameworkgui.json.AppsJsonGet;
import org.ogema.frameworkgui.json.AppsJsonWebResource;
import org.ogema.frameworkgui.utils.AppCompare;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author tgries
 */
public class FrameworkGUIController {

	private final AdministrationManager administrationManager;
	private final BundleContext bundleContext;
	private final AccessManager accessManager;
	private final PermissionManager permissionManager;
//	private static final String DONT_APPEND = "no_index_html";

	public FrameworkGUIController(AdministrationManager administrationManager, BundleContext bundleContext,
			AccessManager accessManager, PermissionManager permissionManager) {
		this.administrationManager = administrationManager;
		this.bundleContext = bundleContext;
		this.accessManager = accessManager;
		this.permissionManager = permissionManager;
	}

	public StringBuffer appsList2JSON(String user) {

		StringBuffer sb = new StringBuffer();
		ArrayList<AdminApplication> apps = (ArrayList<AdminApplication>) administrationManager.getAllApps();

		// this causes problems if there is more than one app in a single bundle...
		// they have the same name, hence will be displayed the same way on the GUI -> filter by using a map
		//		List<AppsJsonGet> list = new ArrayList<AppsJsonGet>();			
		Map<String, AppsJsonGet> map = new LinkedHashMap<String, AppsJsonGet>();

		ObjectMapper mapper = new ObjectMapper();

		String result = "{}";

		for (AdminApplication entry : apps) {
			String name = entry.getID().getBundle().getSymbolicName();
//			AppID appId = entry.getID();
			if (!accessManager.isAppPermitted(user, entry.getID())) {
				continue;
			}

			String fileName = entry.getID().getBundle().getLocation();
			int lastSeperator = fileName.lastIndexOf("/");
			fileName = fileName.substring(lastSeperator + 1, fileName.length());

			//			if (!Utils.DEBUG) {
			//				boolean needFilter = false;
			//				for (String filter : Utils.FILTERED_APPS) {
			//					if (name.contains(filter)) {
			//						needFilter = true;
			//						break;
			//					}
			//				}
			//				if (needFilter) {
			//					continue;
			//				}
			//			}

			long id = entry.getBundleRef().getBundleId();
			Map<String, String> metainfo = new HashMap<String, String>();
			metainfo.put("File_Name", fileName);

			Bundle bundle = entry.getBundleRef();

			Dictionary<String, String> bundleDictionary = bundle.getHeaders();
			Enumeration<String> dictionaryEnums = bundleDictionary.keys();
			while (dictionaryEnums.hasMoreElements()) {
				String key = dictionaryEnums.nextElement();
				String element = bundleDictionary.get(key);

				if (!("Import-Package".equals(key) || "Export-Package".equals(key))) {
					String formattedKey = key.replace('-', '_');
					metainfo.put(formattedKey, element);
				}
			}

			AppsJsonGet singleApp = new AppsJsonGet();
			singleApp.setName(name);
			singleApp.setId(id);
			singleApp.setMetainfo(metainfo);

			//			StringBuffer jsonBuffer = webResourceTree2JSON((long) id, "#", null);
			StringBuffer jsonBuffer = webResourceTree2JSON(entry, "#", null);
			String jsonString = jsonBuffer.toString();
			List<AppsJsonWebResource> webResourcesApp = new ArrayList<AppsJsonWebResource>();

			try {
				webResourcesApp = mapper.readValue(jsonString, mapper.getTypeFactory().constructCollectionType(
						List.class, AppsJsonWebResource.class));

			} catch (IOException ex) {
				LoggerFactory.getLogger(getClass()).error(ex.toString());
			}

			//TODO: remove map/list type

			if (webResourcesApp.get(0).getAlias().equals("null")) {
				singleApp.setHasWebResources(false);
				continue;
			}
			else {
				singleApp.setHasWebResources(true);
				singleApp.getWebResourcePaths().add(webResourcesApp.get(0).getAlias());

			}

			//			if (webResourcesApp.isEmpty()) {
			//				singleApp.setHasWebResources(false);
			//				continue;
			//			}
			//			else {
			//				singleApp.setHasWebResources(true);
			//                                for (AppsJsonWebResource singleWebResource : webResourcesApp) {
			//					String path = singleWebResource.getAlias();
			//					String index = "";
			//					String append = singleWebResource.getId();
			//					if (!append.equals(DONT_APPEND)) {
			//						index = "/index.html";
			//					}
			//					singleApp.getWebResourcePaths().add(path + index);
			//				}
			//			}

			//			list.add(singleApp);
			map.put(name, singleApp);
		}
		List<AppsJsonGet> list = new ArrayList<AppsJsonGet>(map.values());
		Collections.sort(list, new AppCompare());

		try {
			result = mapper.writeValueAsString(list);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		sb.append(result);

		return sb;
	}

	public StringBuffer webResourceTree2JSON(AdminApplication app, String path, String alias) {
		// JSONObject permObj = new JSONObject();
		// JSONArray permsArray = new JSONArray();
		int index = 0;
		StringBuffer sb;
		sb = new StringBuffer();
		if (path.equals("#")) {

			//			AppID appid = administrationManager.getAppByBundle(bundleContext.getBundle(id));
			AppID appid = app.getID();
			String baseUrl = permissionManager.getWebAccess(appid).getStartUrl();
			Map<String, String> entries = new HashMap<String, String>();
			entries.put(baseUrl, appid.getIDString());
			//			if (baseUrl == null) {
			//				entries = permissionManager.getWebAccess().getRegisteredResources(appid);
			//			} else {
			//				entries = new HashMap<String, String>();
			//				entries.put(baseUrl, DONT_APPEND);
			//			}
			//			if (entries == null) {
			//				sb.append("[]");
			//				return sb;
			//			}
			Set<Map.Entry<String, String>> entrySet = entries.entrySet();
			sb.append('[');
			for (Map.Entry<String, String> e : entrySet) {
				String key = e.getKey();
				String name = e.getValue();
				if (index++ != 0) {
					sb.append(',');
				}
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
		//		Bundle b = bundleContext.getBundle(id);
		Bundle b = app.getBundleRef();
		Enumeration<URL> entries = b.findEntries(path, null, false);
//		String replace = path + "/";
		if (entries != null) {
			// permObj.put("webResources", permsArray);
			sb.append('[');
			while (entries.hasMoreElements()) {
				URL url = entries.nextElement();
				// String query ;//= url.getQuery();
				String file = url.getFile();
				boolean isDir = true;
				if (b.findEntries(file, null, false) == null) // if (query != null)
				{
					isDir = false;
				}

				if (index++ != 0) {
					sb.append(',');
				}
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
				if (isDir) {
					sb.append("\"children\":true");
				}
				else {
					sb.append("\"children\":false");
				}
				sb.append('}');
			}
			sb.append(']');
			return sb;
		}
		else {
			return null;
		}
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

}
