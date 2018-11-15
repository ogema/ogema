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
/**
 * 
 */
package org.ogema.permissions;

import java.io.FilePermission;
import java.security.Permission;
import java.security.Permissions;

import org.ogema.accesscontrol.Util;

/**
 * @author Zekeriya Mansuroglu zekeriya.mansuroglu@iis.fraunhofer.de
 *
 */
public class OgemaFilePermission extends Permission {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4883560809773506653L;

	private static final String TEMP_DIR_VAR_STRING = "<<TEMP>>";
	private static final String ALL_FILES_VAR_STRING = "<<ALL FILES>>";
	private static final String APP_STORAGE_DIR_VAR_STRING = "<<APP STORAGE>>";

	private FilePermission wrappedPerm;
	private Permissions perms;
	private String actions;

	/**
	 * 
	 */
	public OgemaFilePermission(String path, String actions) {
		super(path);
		String newPath = path;
		if (path.startsWith(APP_STORAGE_DIR_VAR_STRING) || path.startsWith(ALL_FILES_VAR_STRING)) {
			perms = new Permissions();
			StringBuilder sb = new StringBuilder();
			String storagePath = Util.getCurrentAppStoragePath();
			int len = storagePath.length();
			sb.append(storagePath);
			// first add permission for the main storage path
			newPath = sb.toString();
			FilePermission wrapped = new FilePermission(newPath, actions);
			perms.add(wrapped);
			// second permission is set for all sub files and directories
			sb.append("/-");
			newPath = sb.toString();
			wrapped = new FilePermission(newPath, actions);
			perms.add(wrapped);
		}
		else if (path.startsWith(TEMP_DIR_VAR_STRING)) {
			String tmpDirName = System.getProperty("java.io.tmpdir");
			newPath = path.replace(TEMP_DIR_VAR_STRING, tmpDirName);
			wrappedPerm = new FilePermission(newPath, actions);
		}
		else {
			wrappedPerm = new FilePermission(path, actions);
		}
		this.actions = actions;

	}

	@Override
	public boolean implies(Permission permission) {
		if (!(permission instanceof OgemaFilePermission))
			return false;
		OgemaFilePermission ofPerm = (OgemaFilePermission) permission;
		FilePermission fPerm = ofPerm.wrappedPerm;
		boolean result;
		if (perms != null)
			result = perms.implies(fPerm);
		else
			result = wrappedPerm.implies(fPerm);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof OgemaFilePermission) {
			OgemaFilePermission ofp = (OgemaFilePermission) obj;
			if (ofp.wrappedPerm != null && wrappedPerm != null)
				return (ofp.wrappedPerm.equals(wrappedPerm));
			else if (ofp.perms != null && perms != null)
				return (ofp.perms.equals(perms));
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (wrappedPerm != null)
			return wrappedPerm.hashCode();
		else
			return perms.hashCode();
	}

	@Override
	public String getActions() {
		return actions;
	}

}
