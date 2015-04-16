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
package org.ogema.impl.administration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.ogema.core.application.AppID;
import org.ogema.core.installationmanager.InstallableApplication;
import org.ogema.core.security.AppPermission;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;

public class InstallableApp implements InstallableApplication {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	static final String PERMS_ENTRY_NAME = "OSGI-INF/permissions.perm";
	static final String allPerm = "java.security.AllPermission";
	private static final String tmpDir = "./";

	String path;
	InstallState state;

	AppID appid;
	protected Bundle bundle;
	String name;
	String location;
	private AppPermission appPerms;
	private boolean isLocale;

	InstallableApp(String path, String name) {
		this.path = path;
		this.name = name;
		this.state = InstallState.APPCHOSEN;
		this.location = createLocation();
		this.isLocale = false;
	}

	public InstallableApp(final Bundle b) {
		this.location = AccessController.doPrivileged(new PrivilegedAction<String>() {
			public String run() {
				return b.getLocation();
			}
		});
		this.isLocale = false;
		this.bundle = b;
	}

	@Override
	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public Bundle getBundle() {
		return bundle;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public AppID getAppid() {
		return appid;
	}

	@Override
	public void setAppid(AppID appid) {
		this.appid = appid;
	}

	@Override
	public InstallState getState() {
		return state;
	}

	@Override
	public void setState(InstallState state) {
		this.state = state;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPresentationURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPresentationSnippetURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getPermissionDemand() {
		List<String> permsArray = new ArrayList<>();
		File f = new File(path, name);
		JarFile jar = null;
		try {
			jar = new JarFile(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JarEntry perms = null;
		perms = jar.getJarEntry(PERMS_ENTRY_NAME);

		BufferedReader br = null;
		try {
			if (perms != null) {
				br = new BufferedReader(new InputStreamReader(jar.getInputStream(perms)));
			}
		} catch (IOException e) {
			logger.warn("", e);
		}
		String line;
		if (br == null) {// If the jar entry doesn't exist, AllPermission is desired.
			permsArray.add(allPerm);
		}
		else {
			try {
				line = br.readLine();
				while (line != null) {
					line = line.trim();
					if (line.startsWith("#") || line.startsWith("//") || line.equals("")) {
						continue;
					}
					permsArray.add(line);
					line = br.readLine();
				}
				jar.close();
			} catch (IOException e) {
				logger.warn("", e);
			}
		}
		state = InstallState.DESIRED_PERMS_SENT;
		return permsArray;
	}

	@Override
	public AppPermission getGrantedPermissions() {
		return appPerms;
	}

	@Override
	public void setGrantedPermissions(AppPermission perms) {
		this.appPerms = perms;
	}

	@Override
	public String getDirectory() {
		return path;
	}

	String createLocation() {
		if (path.startsWith(".") || path.startsWith("/") || path.startsWith("file:")) {
			isLocale = true;
		}
		String loc = path + name;
		if (!isLocale) {
			try {
				return (new URL(loc)).toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "file:".concat(loc);
	}

	@Override
	public void prepare() {
		if (isLocale) // only if the file is not in the locale file system copy it
			return;
		InputStream ins = null;
		try {
			ins = new URL(location).openStream();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		int pos = location.lastIndexOf(File.separatorChar);
		if (pos == -1)
			pos = location.lastIndexOf('/');
		String filename = location.substring(pos + 1);

		// new JAR file in bundle's dir:
		File newfile = new File(tmpDir, filename);
		try {
			location = newfile.getCanonicalPath();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		FileOutputStream outStream = null;
		try {
			// copy JAR file
			outStream = new FileOutputStream(newfile);
			byte arrbyte[] = new byte[10240];
			int len;
			while ((len = ins.read(arrbyte, 0, arrbyte.length)) != -1) {
				outStream.write(arrbyte, 0, len);
			}
			ins.close();
			ins = null;
			outStream.close();
			outStream = null;

		} catch (Throwable ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
			// close all connections
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
