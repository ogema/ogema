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
