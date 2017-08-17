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
package org.ogema.app.securityconsumer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.ogema.core.application.ApplicationManager;

public class OgemaFilePermissionTest {

	public OgemaFilePermissionTest(ApplicationManager appmngr) {
		testRelativePathNotInOsgiStorageOgemaFilePermissionWorks();
		testRelativePathNotInOsgiStorageFilePermissionDoesntWork();
		testRelativePathIsInOsgiStorage(appmngr);
	}

	public void testRelativePathNotInOsgiStorageOgemaFilePermissionWorks() {
		Path targetDir = FileSystems.getDefault().getPath("./data", "ogemafilepermissiontest");
		try {
			Files.createDirectories(targetDir);
		} catch (SecurityException e) {
			e.printStackTrace();
			assert false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void testRelativePathNotInOsgiStorageFilePermissionDoesntWork() {
		boolean result = false;
		Path targetDir = FileSystems.getDefault().getPath("./data", "filepermissiontest");
		try {
			Files.createDirectories(targetDir);
		} catch (SecurityException e) {
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert result;
	}

	public void testRelativePathIsInOsgiStorage(ApplicationManager mngr) {
		boolean result = true;
		// Accesses to the following file are implied by (org.ogema.accesscontrol.OgemaFilePermission "<<APP
		// STORAGE>>/test/-" "read,write")
		File f = mngr.getDataFile("test/storagefilepermissiontest");
		try {
			f.mkdirs();
			f.createNewFile();
		} catch (SecurityException e) {
			e.printStackTrace();
			result = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert result;

		// delete is allowed too
		try {
			f.delete();
		} catch (SecurityException e) {
			e.printStackTrace();
			result = false;
		}
		assert result;
	}

	public void createTempFileWorks() {
		try {
			File f = File.createTempFile(getClass().getName(), "tmp");
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(0x48);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
