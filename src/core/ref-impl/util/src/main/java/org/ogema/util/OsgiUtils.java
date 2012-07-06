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
package org.ogema.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

public class OsgiUtils {
	static final String PERMS_ENTRY_NAME = "OSGI-INF/permissions.perm";

	public String getBundleNameFromHeader(Bundle bundle) {
		Dictionary<String, ?> dic = bundle.getHeaders();
		String[] name = null;
		if (dic == null) {
			System.out.println("Could not read Admin Manifest");
			return null;
		}
		else {
			if (dic.get(Constants.BUNDLE_NAME) != null && dic.get(Constants.BUNDLE_NAME).getClass().isArray()) {
				name = (String[]) dic.get(Constants.BUNDLE_NAME);
			}
			else {
				name = new String[1];
				name[0] = (String) dic.get(Constants.BUNDLE_NAME);
			}
		}
		if (name[0] == null)
			return "Unknown bundle";
		return name[0];
	}

	ArrayList<String> getLocalePerms(String location) {
		JarFile jar = null;
		try {
			jar = new JarFile(location);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JarEntry perms = null;
		perms = jar.getJarEntry(PERMS_ENTRY_NAME);

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(jar.getInputStream(perms)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<String> permList = new ArrayList<String>();
		String line;
		try {
			line = br.readLine();
			while (line != null) {
				line = line.trim();
				if (line.startsWith("#") || line.startsWith("//") || line.equals(""))
					continue;
				permList.add(line);
			}
			jar.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return permList;
	}

}
