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
