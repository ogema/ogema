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

public class MapFile {
	/**
	 * Initial size of buffer the type entry is set up therein.
	 */
	static final int BUFFER_SIZE = 1024;

	static final int MAGIC1 = 0xFEEDC0DE;

	static final int MAGIC2 = 0xFEEDFACE;

	private static final int ENTRY_COUNT_OFFSET = 12;

	@SuppressWarnings("unused")
	private File file;
	RandomAccessFile raf;
	DataOutputStream out;

	String dataFileName;

	int entryCount;

	@SuppressWarnings("unused")
	private int fileLen;

	private boolean valid;

	FileOutputStream fos;

	public MapFile(File f) {
		this.file = f;
		try {
			this.raf = new RandomAccessFile(f, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		init();
	}

	public MapFile(File f, String datafile) {
		this.file = f;
		try {
			this.fos = new FileOutputStream(f, true);
			this.out = new DataOutputStream(new BufferedOutputStream(fos, BUFFER_SIZE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.fileLen = 0;
		this.dataFileName = datafile;
		this.entryCount = 0;
	}

	void init() {
		int entryCount = 0, dataFileNameOffset;
		String fileName;
		int fileLen;
		try {
			fileLen = (int) raf.length();
			raf.seek(fileLen - ENTRY_COUNT_OFFSET);
			entryCount = raf.readInt();
			/*
			 * Read name of the data archive which is corresponding to the directory file.
			 */
			dataFileNameOffset = (entryCount << 3);
			raf.seek(dataFileNameOffset);
			fileName = raf.readUTF();

			// 8 bytes each entry + 4 bytes entry count + 8 bytes magic + utf8
			// data file name including 2 bytes for the length information
			if (((entryCount << 3) + 12 + fileName.length() + 2) != fileLen)
				this.valid = false;
			raf.seek(fileLen - 8);
			if ((raf.readInt() != MAGIC1) && (raf.readInt() != MAGIC2))
				this.valid = false;
		} catch (IOException e) {
			this.valid = false;
			return;
		}
		this.entryCount = entryCount;
		this.dataFileName = fileName;
		this.fileLen = fileLen;
		this.valid = true;
	}

	public boolean isValid() {
		return valid;
	}

	public int getLastOffset() {
		int mapOffset = ((entryCount - 1) << 3) + 4;
		int dataOffset = -1;
		try {
			raf.seek(mapOffset);
			dataOffset = raf.readInt();
		} catch (IOException e) {
		}
		return dataOffset;
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

}
