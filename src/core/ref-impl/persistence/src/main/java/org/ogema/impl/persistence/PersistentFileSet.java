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
package org.ogema.impl.persistence;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

class PersistentFileSet {
	File fileOld, fileNew;

	DataOutputStream out;
	int nameSuffix;
	RandomAccessFile in;
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
					 * Name is newer then name1 and name2. name1 can be deleted before it is overridden by the new name.
					 */
					if (name1 != null) {
						if (DBResourceIO.DEBUG)
							System.out.println("Deleting file: " + name1);
						new File(dir, name1).delete();
					}
					counter1 = counter2;
					counter2 = tmpCounter;
					name1 = name2;
					name2 = name;
				}
				else if (tmpCounter < counter2 && tmpCounter > counter1) {
					counter1 = tmpCounter;
					/*
					 * Name is newer then name1 but older than name2. name1 can be deleted before it is overridden by
					 * the new name.
					 */
					if (name1 != null) {
						if (DBResourceIO.DEBUG)
							System.out.println("Deleting file: " + name);
						new File(dir, name1).delete();
					}
					name1 = name;
				}
				else {
					if (DBResourceIO.DEBUG)
						System.out.println("Deleting file: " + name);
					/*
					 * Name is older then name1 and name2. name can be deleted.
					 */
					new File(dir, name).delete();
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

	void updateNextOut() {
		DataOutputStream result = null;
		File newFile = new File(directory, namePrefix + String.valueOf(nameSuffix));
		nameSuffix++;
		try {
			FileOutputStream fos1 = new FileOutputStream(newFile);
			result = new DataOutputStream(new BufferedOutputStream(fos1, DBResourceIO.ENTRY_MAX_SIZE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		/*
		 * Delete the old File
		 */
		try {
			if (out != null)
				out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (fileOld != null) {
			if (DBResourceIO.DEBUG)
				System.out.println("Deleting file: " + fileOld.getName());
			fileOld.delete();
		}
		fileOld = fileNew;
		fileNew = newFile;

		out = result;
	}

	void updateCurrentOut() {
		DataOutputStream result = null;
		File f = fileNew;
		File delete = fileOld;
		if (f == null) {
			fileNew = fileOld;
			fileOld = null;
			f = fileNew;
			delete = null;
		}
		if (f != null) {
			try {
				FileOutputStream fos1 = new FileOutputStream(f, true);
				result = new DataOutputStream(new BufferedOutputStream(fos1, DBResourceIO.ENTRY_MAX_SIZE));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			/*
			 * Delete the old File
			 */
			try {
				if (out != null)
					out.close();
				if (delete != null) {
					if (DBResourceIO.DEBUG)
						System.out.println("Deleting file: " + delete.getName());
					delete.delete();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			out = result;
		}
		else
			updateNextOut();
	}

	void updateCurrentIn() {
		RandomAccessFile result = null;
		File f = fileNew;
		if (f == null) {
			f = fileOld;
			fileNew = fileOld;
			fileOld = null;
		}
		if (f != null)
			try {
				result = new RandomAccessFile(fileNew, "r");
			} catch (FileNotFoundException e) {
			}
		in = result;
	}

	/*
	 * Used by the tests only
	 */
	void reset() {
		closeAll();
		System.gc();
		if (fileNew != null) {
			fileNew.setWritable(true);
			if (DBResourceIO.DEBUG)
				System.out.println("Deleting file: " + fileNew.getName());
			fileNew.delete();
		}
		if (fileOld != null) {
			fileOld.setWritable(true);
			if (DBResourceIO.DEBUG)
				System.out.println("Deleting file: " + fileOld.getName());
			fileOld.delete();
		}
	}

	void backup() {
		closeAll();
		System.gc();
		if (fileNew != null) {
			fileNew.setWritable(true);
			fileNew.renameTo(new File(directory, "backup_new_" + namePrefix + System.currentTimeMillis()));
		}
		if (fileOld != null) {
			fileOld.setWritable(true);
			fileOld.renameTo(new File(directory, "backup_old_" + namePrefix + System.currentTimeMillis()));
		}
	}

	void closeAll() {
		try {
			if (out != null)
				out.close();
			out = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (in != null)
				in.close();
			in = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteNew() {
		if (DBResourceIO.DEBUG)
			System.out.println("Deleting file: " + fileNew.getName());
		fileNew.delete();
		fileNew = fileOld;
		fileOld = null;
	}

}
