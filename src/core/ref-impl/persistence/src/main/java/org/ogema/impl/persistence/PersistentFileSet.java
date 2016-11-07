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
				tmpCounter = Integer.valueOf(suffix);
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
				logger.debug("Deleting file: " + name1);
			f.delete();
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
				logger.debug("Deleting file: " + fileNew.getName());
			fileNew.delete();
		}
		if (fileOld != null) {
			fileOld.setWritable(true);
			if (Configuration.LOGGING)
				logger.debug("Deleting file: " + fileOld.getName());
			fileOld.delete();
		}
	}

	void backup() {
		System.gc();
		if (fileNew != null) {
			fileNew.setWritable(true);
			if (fileNew.length() == 0)
				fileNew.delete();
			else {
				fileNew.renameTo(new File(directory, "backup_" + fileNew.getName() + System.currentTimeMillis()));
			}
		}
		if (fileOld != null) {
			fileOld.setWritable(true);
			if (fileOld.length() != 0) {
				fileOld.delete();
			}
			else {
				fileOld.renameTo(new File(directory, "backup_" + fileOld.getName() + System.currentTimeMillis()));
			}
		}
		fileOld = fileNew = null;
	}

	public void shiftB() {
		if (Configuration.LOGGING)
			logger.debug("Deleting file: " + fileNew.getName());
		fileNew.delete();
		fileNew = fileOld;
		fileOld = null;
	}

	public void setAsCurrentFile(String name) {
		File f = getFileByName(name);
		fileNew = f;
	}

	public void shiftF() {
		if (Configuration.LOGGING)
			logger.debug("Create new file: " + namePrefix);
		File newFile = new File(directory, namePrefix + String.valueOf(nameSuffix));
		nameSuffix++;
		/*
		 * Delete the old File
		 */
		if (fileOld != null) {
			if (Configuration.LOGGING)
				logger.debug("Deleting file: " + fileOld.getName());
			fileOld.delete();
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
