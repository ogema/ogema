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
package org.ogema.accesscontrol;

import java.io.FilePermission;
import java.security.Permission;

/**
 * @author Zekeriya Mansuroglu zekeriya.mansuroglu@iis.fraunhofer.de
 *
 */
public class OgemaFilePermission extends Permission {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4883560809773506653L;
	private FilePermission wrappedPerm;

	/**
	 * 
	 */
	public OgemaFilePermission(String path, String actions) {
		super(path);

		switch (path) {
		case "<<ALL FILES>>":
		case "<<APP-STORAGE>>":
			String storagePath = Activator.admin.getInstallationManager().getCurrentInstallStoragePath();
			wrappedPerm = new FilePermission(storagePath, actions);
			break;
		default:
			wrappedPerm = new FilePermission(path, actions);
			break;
		}
	}

	@Override
	public boolean implies(Permission permission) {
		return wrappedPerm.implies(permission);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof OgemaFilePermission) {
			OgemaFilePermission ofp = (OgemaFilePermission) obj;
			return ofp.wrappedPerm.equals(wrappedPerm);
		}
		else if (obj instanceof FilePermission) {
			FilePermission fp = (FilePermission) obj;
			return fp.equals(wrappedPerm);
		}
		else
			return false;
	}

	@Override
	public int hashCode() {
		return wrappedPerm.hashCode();
	}

	@Override
	public String getActions() {
		return wrappedPerm.getActions();
	}

}
