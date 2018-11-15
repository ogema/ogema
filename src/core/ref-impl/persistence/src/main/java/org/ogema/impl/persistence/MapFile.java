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
