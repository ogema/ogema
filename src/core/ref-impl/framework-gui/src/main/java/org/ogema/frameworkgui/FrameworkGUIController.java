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
package org.ogema.frameworkgui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.codehaus.jackson.map.ObjectMapper;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.AppID;
import org.ogema.core.security.WebAccessManager;
import org.ogema.frameworkgui.json.AppsJsonGet;
import org.ogema.frameworkgui.json.AppsJsonWebResource;
import org.ogema.frameworkgui.utils.AppCompare;
import org.ogema.frameworkgui.utils.Utils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 *
 * @author tgries
 */
public class FrameworkGUIController {

	private final AdministrationManager administrationManager;
	private final BundleContext bundleContext;

	private final WebAccessManager webAccessManager;

	public FrameworkGUIController(AdministrationManager administrationManager, BundleContext bundleContext,
			WebAccessManager webAccessManager) {
		this.administrationManager = administrationManager;
		this.bundleContext = bundleContext;
		this.webAccessManager = webAccessManager;
	}

	public StringBuffer appsList2JSON() {
		StringBuffer sb = new StringBuffer();
		ArrayList<AdminApplication> apps = (ArrayList<AdminApplication>) administrationManager.getAllApps();

		List<AppsJsonGet> list = new ArrayList<AppsJsonGet>();
		ObjectMapper mapper = new ObjectMapper();

		String result = "{}";

		for (AdminApplication entry : apps) {
			String name = entry.getID().getBundle().getSymbolicName();

			String fileName = entry.getID().getBundle().getLocation();
			int lastSeperator = fileName.lastIndexOf("/");
			fileName = fileName.substring(lastSeperator + 1, fileName.length());

			if (!Utils.DEBUG) {
				boolean needFilter = false;
				for (String filter : Utils.FILTERED_APPS) {
					if (name.contains(filter)) {
						needFilter = true;
						break;
					}
				}
				if (needFilter) {
					continue;
				}
			}

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

			StringBuffer jsonBuffer = webResourceTree2JSON((int) id, "#", null);
			String jsonString = jsonBuffer.toString();
			List<AppsJsonWebResource> webResourcesApp = new ArrayList<AppsJsonWebResource>();

			try {
				webResourcesApp = mapper.readValue(jsonString, mapper.getTypeFactory().constructCollectionType(
						List.class, AppsJsonWebResource.class));

			} catch (IOException ex) {
				java.util.logging.Logger.getLogger(FrameworkGUIController.class.getName()).log(Level.SEVERE, null, ex);
			}

			if (webResourcesApp.isEmpty()) {
				singleApp.setHasWebResources(false);
				continue;
			}
			else {
				singleApp.setHasWebResources(true);
				for (AppsJsonWebResource singleWebResource : webResourcesApp) {
					String path = singleWebResource.getAlias();
					String index = "/index.html";
					singleApp.getWebResourcePaths().add(path + index);
				}
			}

			list.add(singleApp);
		}

		Collections.sort(list, new AppCompare());

		try {
			result = mapper.writeValueAsString(list);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		sb.append(result);

		return sb;
	}

	public StringBuffer webResourceTree2JSON(int id, String path, String alias) {
		// JSONObject permObj = new JSONObject();
		// JSONArray permsArray = new JSONArray();
		int index = 0;
		StringBuffer sb;
		sb = new StringBuffer();
		if (path.equals("#")) {

			AppID appid = administrationManager.getAppByBundle(bundleContext.getBundle(id));
			Map<String, String> entries = webAccessManager.getRegisteredResources(appid);
			if (entries == null) {
				sb.append("[]");
				return sb;
			}
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
		Bundle b = bundleContext.getBundle(id);
		Enumeration<URL> entries = b.findEntries(path, null, false);
		String replace = path + "/";
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
