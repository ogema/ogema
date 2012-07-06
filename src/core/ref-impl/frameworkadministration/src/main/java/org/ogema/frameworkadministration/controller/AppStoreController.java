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
package org.ogema.frameworkadministration.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.AppID;
import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.InstallableApplication;
import org.ogema.core.installationmanager.InstallationManagement;
import org.ogema.core.security.AppPermission;
import org.ogema.frameworkadministration.json.AppsJsonAppFile;
import org.ogema.frameworkadministration.json.AppsJsonGetAppFiles;
import org.ogema.frameworkadministration.json.AppsJsonWebResource;
import org.ogema.frameworkadministration.json.get.AppsJsonGet;
import org.ogema.frameworkadministration.utils.AppCompare;
import org.ogema.frameworkadministration.utils.AppStoreUtils;
import org.ogema.persistence.ResourceDB;
import org.ogema.resourcetree.TreeElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.permissionadmin.PermissionInfo;
import org.slf4j.Logger;

/**
 * 
 * @author tgries
 */
public class AppStoreController {

	private static AppStoreController instance = null;

	private AdministrationManager administrationManager;
	private PermissionManager permissionManager;
	private InstallationManagement installationManager;
	private ResourceDB resourceDB;
	private BundleContext bundleContext;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	/**
	 * Maximum file size in bytes.
	 */
	public final int MAX_FILE_SIZE = 1024 * 1024 * 1024;

	public static AppStoreController getInstance() {
		if (instance == null) {
			instance = new AppStoreController();
		}
		return instance;
	}

	private AppStoreController() {
	}

	public void startAppInstall(HttpServletRequest req, HttpServletResponse resp, String address, String name)
			throws IOException {
		if (!permissionManager.handleSecurity(new AdminPermission(AdminPermission.APP)))
			throw new SecurityException("Permission to install Application denied!" + address + name);
		InstallableApplication app = installationManager.createInstallableApp(address, name);
		req.getSession().setAttribute(AppStoreUtils.INSTALLATION_STATE_ATTR_NAME, app);
		// In this case an app is chosen for the installation
		// Start the state machine for the installation process

		try {
			String data = getDesiredPerms(app);
			printResponse(resp, data);
		} catch (Exception e) {
			e.printStackTrace(resp.getWriter());
		}
	}

	private void printResponse(HttpServletResponse resp, String data) throws IOException {
		PrintWriter pw = resp.getWriter();
		pw.print(data);
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

			boolean needFilter = false;
			for (String filter : AppStoreUtils.FILTERED_APPS) {
				if (name.contains(filter) && !name.contains("framework-gui")
						&& !name.contains("framework-administration")) {
					needFilter = true;
					break;
				}
			}
			if (needFilter) {
				continue;
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

			StringBuffer jsonBuffer = AppStoreController.getInstance().webResourceTree2JSON((int) id, "#", null);
			String jsonString = jsonBuffer.toString();
			List<AppsJsonWebResource> webResourcesApp = new ArrayList<AppsJsonWebResource>();

			try {
				webResourcesApp = mapper.readValue(jsonString, mapper.getTypeFactory().constructCollectionType(
						List.class, AppsJsonWebResource.class));

			} catch (IOException ex) {
				java.util.logging.Logger.getLogger(AppStoreController.class.getName()).log(Level.SEVERE, null, ex);
			}

			if (webResourcesApp.isEmpty()) {
				singleApp.setHasWebResources(false);
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

	public StringBuffer resourceTree2JSON(int id) {
		Collection<TreeElement> childs;

		if (id == -1)
			childs = resourceDB.getAllToplevelResources();
		else
			childs = resourceDB.getByID(id).getChildren();
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		int index = 0;
		for (TreeElement te : childs) {
			if (index++ != 0)
				sb.append(',');
			sb.append("{\"text\":\"");
			sb.append(te.getName());
			sb.append(" - ");
			sb.append(te.getType().getName());
			sb.append('"');
			sb.append(',');
			sb.append("\"id\":\"");
			sb.append(te.getResID());
			sb.append('"');
			sb.append(',');
			sb.append("\"method\":\"\",");
			sb.append("\"children\":true");
			sb.append('}');
		}
		sb.append(']');
		return sb;
	}

	public StringBuffer webResourceTree2JSON(int id, String path, String alias) {
		// JSONObject permObj = new JSONObject();
		// JSONArray permsArray = new JSONArray();
		int index = 0;
		StringBuffer sb;
		sb = new StringBuffer();
		if (path.equals("#")) {
			AppID appid = permissionManager.getAdminManager().getAppByBundle(bundleContext.getBundle(id));
			Map<String, String> entries = permissionManager.getWebAccess().getRegisteredResources(appid);
			if (entries == null) {
				sb.append("[]");
				return sb;
			}
			Set<Map.Entry<String, String>> entrySet = entries.entrySet();
			sb.append('[');
			for (Map.Entry<String, String> e : entrySet) {
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

	public StringBuffer appInfos2JSON(int id) {
		StringBuffer sb = new StringBuffer();
		/*
		 * Put bundle id
		 */
		sb.append("{\"bundleID\":\"");
		sb.append(id);
		sb.append("\",\"policies\":[");
		Bundle b = bundleContext.getBundle(id);
		AppID aid = permissionManager.getAdminManager().getAppByBundle(b);
		AppPermission ap = permissionManager.getPolicies(aid);
		/*
		 * Put policies info
		 */
		Map<String, ConditionalPermissionInfo> granted = ap.getGrantedPerms();
		Set<Map.Entry<String, ConditionalPermissionInfo>> tlrs = granted.entrySet();
		int index = 0, j = 0;
		for (Map.Entry<String, ConditionalPermissionInfo> entry : tlrs) {
			ConditionalPermissionInfo info = entry.getValue();
			if (j++ != 0)
				sb.append(',');
			sb.append("{\"mode\":\"");
			sb.append(info.getAccessDecision());

			sb.append("\",\"uniqueName\":\"");
			sb.append(info.getName());

			sb.append("\",\"delete\":\"");
			sb.append("false");

			sb.append("\",\"change\":\"");
			sb.append("false");

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
			permObj.put("localePerms", permsArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return permObj.toString();
	}

	public String getAppFiles(File f) throws Exception {
		// JSONArray appStoresData = new JSONArray();
		// JSONObject json = new JSONObject();
		String[] files = f.list();
		String result = "{}";
		AppsJsonGetAppFiles appListJson = new AppsJsonGetAppFiles();
		// int index = 0;
		for (String name : files) {
			File file = new File(f, name);
			if (name.endsWith(".jar") && !file.isDirectory()) {
				// appStoresData.put(index++, name);
				AppsJsonAppFile singleAppJson = new AppsJsonAppFile();
				singleAppJson.setName(name);
				// find icon from jar
				ZipFile zipFile = new ZipFile(file);
				ZipEntry zipEntry = null;

				if (zipFile.getEntry("icon.svg") != null) {
					singleAppJson.setType("svg+xml");
					zipEntry = zipFile.getEntry("icon.svg");
				}
				else if (zipFile.getEntry("icon.png") != null) {
					singleAppJson.setType("png");
					zipEntry = zipFile.getEntry("icon.png");
				}
				else if (zipFile.getEntry("icon.jpg") != null) {
					singleAppJson.setType("jpg");
					zipEntry = zipFile.getEntry("icon.jpg");
				}

				if (zipEntry != null) {
					InputStream inputStream = zipFile.getInputStream(zipEntry);
					byte[] byteArray = IOUtils.toByteArray(inputStream);
					byte[] unformattedBase64byteArray = Base64.encodeBase64(byteArray);
					String utf8Base64 = new String(unformattedBase64byteArray, "UTF-8");
					singleAppJson.setIconBase64(utf8Base64);
				}
				appListJson.getApps().add(singleAppJson);
			}

		}

		ObjectMapper mapper = new ObjectMapper();
		result = mapper.writeValueAsString(appListJson);
		// json.put("apps", appStoresData);
		// return json.toString();
		return result;
	}

	public synchronized String getAppstoresData() throws Exception {
		JSONArray appStoresData = new JSONArray();
		int i = 0;
		JSONObject json = new JSONObject();
		// Set<Entry<String, AppStore>> appstrs = appStores.entrySet();
		List<ApplicationSource> appstrs = installationManager.getConnectedAppSources();
		for (ApplicationSource entry : appstrs) {
			appStoresData.put(i++, entry.getName());
		}
		json.put("appstores", appStoresData);
		logger.info(json.toString());
		return json.toString();
	}

	public File receiveFile(HttpServletRequest req, HttpServletResponse resp) {

		String path = "./temp";
		path = administrationManager.getInstallationManager().getDefaultAppStore().getAddress();

		// String filePath = req.getParameter("filename");
		boolean isMultipart;
		int maxFileSize = MAX_FILE_SIZE;
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
					// String fieldName = fi.getFieldName();
					String fileName = fi.getName();
					// String contentType = fi.getContentType();
					// boolean isInMemory = fi.isInMemory();
					// long sizeInBytes = fi.getSize();
					// Write the file
					if (fileName.lastIndexOf("\\") >= 0) {
						file = new File(path, fileName.substring(fileName.lastIndexOf("\\")));
					}
					else {
						file = new File(path, fileName);
					}
					fi.write(file);
				}
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return file;
	}

	public AdministrationManager getAdministrationManager() {
		return administrationManager;
	}

	public void setAdministrationManager(AdministrationManager administrationManager) {
		this.administrationManager = administrationManager;
	}

	public PermissionManager getPermissionManager() {
		return permissionManager;
	}

	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}

	public ResourceDB getResourceDB() {
		return resourceDB;
	}

	public void setResourceDB(ResourceDB resourceDB) {
		this.resourceDB = resourceDB;
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public void setInstallationManager(InstallationManagement instMan) {
		this.installationManager = instMan;
	}

}
