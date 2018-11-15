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
