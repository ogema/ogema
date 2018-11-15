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
