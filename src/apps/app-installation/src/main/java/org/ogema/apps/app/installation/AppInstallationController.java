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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ogema.apps.app.installation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
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
import org.ogema.apps.app.installation.json.AppsJsonAppFile;
import org.ogema.apps.app.installation.json.AppsJsonGetAppFiles;
import org.ogema.apps.app.installation.utils.AppStoreUtils;
import org.ogema.apps.app.installation.utils.Utils;
import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.InstallableApplication;
import org.ogema.core.installationmanager.InstallationManagement;

/**
 *
 * @author tgries
 */
public class AppInstallationController {

	private PermissionManager permissionManager;
	private InstallationManagement installationManager;

	public final int MAX_FILE_SIZE = 1024 * 1024 * 1024;

	AppInstallationController(PermissionManager permissionManager, InstallationManagement installationManager) {
		this.permissionManager = permissionManager;
		this.installationManager = installationManager;
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
			Utils.printResponse(resp, data);
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
		Utils.log(json.toString(), this.getClass());
		return json.toString();
	}

	public File receiveFile(HttpServletRequest req, HttpServletResponse resp) {

		String path = "./temp";
		path = installationManager.getDefaultAppStore().getAddress();

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

}
