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
package org.ogema.base.security;

import java.io.FilePermission;

import org.ogema.permissions.OgemaFilePermission;

public class OgemaSecurityManager extends SecurityManager {
	@Override
	public void checkRead(String file) {
		try {
			super.checkPermission(new OgemaFilePermission(file, "read"));
		} catch (SecurityException e) {
			super.checkPermission(new FilePermission(file, "read"));
		}
	}

	@Override
	public void checkWrite(String file) {
		try {
			super.checkPermission(new OgemaFilePermission(file, "write"));
		} catch (SecurityException e) {
			super.checkPermission(new FilePermission(file, "write"));
		}
	}

	@Override
	public void checkRead(String file, Object ctx) {
		try {
			super.checkPermission(new OgemaFilePermission(file, "read"), ctx);
		} catch (SecurityException e) {
			super.checkPermission(new FilePermission(file, "read"));
		}
	}

	@Override
	public void checkDelete(String file) {
		try {
			super.checkPermission(new OgemaFilePermission(file, "delete"));
		} catch (SecurityException e) {
			super.checkPermission(new FilePermission(file, "delete"));
		}
	}
}
