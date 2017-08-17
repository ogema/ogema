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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DataFile {

	static final int BUFFER_SIZE = 1024;

	String fileName;
	RandomAccessFile raf;
	DataOutputStream out;

	private File file;

	FileOutputStream fos;

	public DataFile(File dataFile) {
		this.fileName = dataFile.getName();
		this.file = dataFile;
		try {
			this.raf = new RandomAccessFile(dataFile, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public DataFile(File f, boolean b) {
		this.fileName = f.getName();
		this.file = f;
		initOutput();
	}

	public void close() {
		try {
			if (raf != null) {
				FileDescriptor fd = raf.getFD();
				fd.sync();
				raf.close();
			}
			if (out != null) {
				FileDescriptor fd = fos.getFD();
				fd.sync();
				out.close();
				fos.close();
			}
		} catch (IOException e) {
		}
	}

	public void initOutput() {
		try {
			this.fos = new FileOutputStream(file, true);
			this.out = new DataOutputStream(new BufferedOutputStream(fos, BUFFER_SIZE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
