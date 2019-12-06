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
package org.ogema.impl.persistence;

import java.io.File;

import org.slf4j.Logger;

class PersistentFileSet {
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("persistence");

	File fileOld, fileNew;

	int nameSuffix;
	File directory;
	String namePrefix;

	/*
	 * Detect existing data base files, activate the newest files with valid content and delete older invalid backup
	 * files.
	 */
	void initFiles(File dir, String fileName) {
		directory = dir;
		namePrefix = fileName;

		String files[] = dir.list();
		String suffix = null, name1 = null, name2 = null;
		/*
		 * The suffix of the file is an counter and indicate the currentness of it. More higher the suffix more current
		 * the file. After the sorting algorithm below is counter1 < counter2: where is counter1 the suffix and name the
		 * name of the older file and counter2 the suffix and name 2 the name of the newer file.
		 */
		int tmpCounter = -1, counter1 = -1, counter2 = -1;
		for (String name : files) {
			if (name.startsWith(fileName)) {
				suffix = name.substring(fileName.length());
				try {
					tmpCounter = Integer.valueOf(suffix);
				} catch (NumberFormatException e) {
					continue;
				}
				if (tmpCounter > counter2) {
					/*
					 * Name is newer than name1 and name2. name1 can be deleted before it is overridden by the new name.
					 */
					if (name1 != null) {
						checkDelete(dir, name1);
					}
					counter1 = counter2;
					counter2 = tmpCounter;
					name1 = name2;
					name2 = name;
				}
				else if (tmpCounter < counter2 && tmpCounter > counter1) {
					counter1 = tmpCounter;
					/*
					 * Name is newer than name1 but older than name2. name1 can be deleted before it is overridden by
					 * the new name.
					 */
					if (name1 != null) {
						checkDelete(dir, name1);
					}
					name1 = name;
				}
				else {
					/*
					 * Name is older than name1 and name2. name can be deleted.
					 */
					checkDelete(dir, name);
				}
			}
		}
		if (name1 != null) {
			fileOld = new File(dir, name1);
		}
		if (name2 != null) {
			fileNew = new File(dir, name2);
		}
		if (counter2 != -1)
			nameSuffix = counter2 + 1;
	}

	private void checkDelete(File dir, String name1) {
		File f = new File(dir, name1);
		if (f.length() == 0) {
			if (Configuration.LOGGING)
				logger.debug("Deleting file: {}", name1);
			if (!f.delete())
				logger.info("File could not be deleted: " + f.getName());
		}
	}

	public File getFileByName(String name) {
		String fileName = null;
		if (fileNew != null) {
			fileName = fileNew.getName();
			if (fileName.equals(name))
				return fileNew;
		}
		if (fileOld != null) {
			fileName = fileOld.getName();
			if (fileName.equals(name))
				return fileOld;
		}
		return null;
	}

	/*
	 * Used by the tests only
	 */
	void reset() {
		System.gc();
		if (fileNew != null) {
			fileNew.setWritable(true);
			if (Configuration.LOGGING)
				logger.debug("Deleting file: ", fileNew.getName());
			if (!fileNew.delete())
				logger.info("File could not be deleted: " + fileNew.getName());
		}
		if (fileOld != null) {
			fileOld.setWritable(true);
			if (Configuration.LOGGING)
				logger.debug("Deleting file: {}", fileOld.getName());
			if (!fileOld.delete())
				logger.info("File could not be deleted: " + fileOld.getName());
		}
	}

	void backup() {
		System.gc();
		if (fileNew != null) {
			fileNew.setWritable(true);
			if (fileNew.length() == 0) {
				if (!fileNew.delete())
					logger.info("File could not be deleted: " + fileNew.getName());
			}
			else {
				fileNew.renameTo(new File(directory, "backup_" + fileNew.getName() + System.currentTimeMillis()));
			}
		}
		if (fileOld != null) {
			fileOld.setWritable(true);
			if (fileOld.length() == 0) {
				if (!fileOld.delete())
					logger.info("File could not be deleted: " + fileOld.getName());
			}
			else {
				fileOld.renameTo(new File(directory, "backup_" + fileOld.getName() + System.currentTimeMillis()));
			}
		}
		fileOld = fileNew = null;
	}

	public void shiftB() {
		if (Configuration.LOGGING)
			logger.info("Deleting file: " + fileNew.getName());
		if (!fileNew.delete())
			logger.info("File could not be deleted: " + fileNew.getName());
		fileNew = fileOld;
		fileOld = null;
	}

	public void setAsCurrentFile(String name) {
		File f = getFileByName(name);
		fileNew = f;
	}

	public void shiftF() {
		if (Configuration.LOGGING)
			logger.debug("Create new file: {}", namePrefix);
		File newFile = new File(directory, namePrefix + String.valueOf(nameSuffix));
		nameSuffix++;
		/*
		 * Delete the old File
		 */
		if (fileOld != null) {
			if (Configuration.LOGGING)
				logger.debug("Deleting file: {}", fileOld.getName());
			if (!fileOld.delete())
				logger.info("File could not be deleted: " + fileOld.getName());
		}
		fileOld = fileNew;
		fileNew = newFile;
	}

	public File getBiggest() {
		int maxlength = 0;
		File result = null;
		String files[] = directory.list();

		for (String name : files) {
			if (name.startsWith(namePrefix)) {
				File f = new File(directory, name);
				int length = (int) f.length();
				if (length > maxlength) {
					maxlength = length;
					result = f;
				}
			}
		}
		return result;
	}
}
