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
package org.ogema.recordeddata.slotsdb;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileObject {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected long startTimeStamp; // byte 0-7 in file (cached)
	protected long storagePeriod; // byte 8-15 in file (cached)
	protected final File dataFile;
	protected DataOutputStream dos;
	protected BufferedOutputStream bos;
	protected FileOutputStream fos;
	protected DataInputStream dis;
	protected FileInputStream fis;
	protected boolean canWrite;
	protected boolean canRead;

	/*
	 * File length will be cached to avoid system calls an improve I/O Performance
	 */
	protected long length = 0;

	public FileObject(String filename) throws IOException {
		canWrite = false;
		canRead = false;
		dataFile = new File(filename);
		length = dataFile.length();
		if (dataFile.exists() && length >= 16) {
			/*
			 * File already exists -> get file Header (startTime and step-frequency) TODO: compare to starttime and
			 * frequency in constructor! new file needed? update to file-array!
			 */
			try {
				fis = new FileInputStream(dataFile);
				try {
					dis = new DataInputStream(fis);
					try {
						readHeader(dis);
					} finally {
						if (dis != null) {
							dis.close();
							dis = null;
						}
					}
				} finally {
					if (dis != null) {
						dis.close();
						dis = null;
					}
				}
			} finally {
				if (fis != null) {
					fis.close();
					fis = null;
				}
			}
		}
	}

	/**
	 * Read 16 bytes of File Header.
	 * 
	 * @param dis2
	 */
	abstract void readHeader(DataInputStream dis) throws IOException;

	public FileObject(File file) throws IOException {
		canWrite = false;
		canRead = false;
		dataFile = file;
		length = dataFile.length();
		if (dataFile.exists() && length >= 16) {
			/*
			 * File already exists -> get file Header (startTime and step-frequency)
			 */
			fis = new FileInputStream(dataFile);
			try {
				dis = new DataInputStream(fis);
				try {
					readHeader(dis);
				} finally {
					if (dis != null) {
						dis.close();
						dis = null;
					}
				}
			} finally {
				if (fis != null) {
					fis.close();
					fis = null;
				}
			}

		}
	}

	protected void enableOutput() throws IOException {
		/*
		 * Close Input Streams, for enabling output.
		 */
		if (dis != null) {
			dis.close();
			dis = null;
		}
		if (fis != null) {
			fis.close();
			fis = null;
		}

		/*
		 * enabling output
		 */
		if (fos == null || dos == null || bos == null) {
			fos = new FileOutputStream(dataFile, true);
			bos = new BufferedOutputStream(fos);
			dos = new DataOutputStream(bos);
		}
		canRead = false;
		canWrite = true;
	}

	protected void enableInput() throws IOException {
		/*
		 * Close Output Streams for enabling input.
		 */
		if (dos != null) {
			dos.flush();
			dos.close();
			dos = null;
		}
		if (bos != null) {
			bos.close();
			bos = null;
		}
		if (fos != null) {
			fos.close();
			fos = null;
		}

		/*
		 * enabling input
		 */
		if (fis == null || dis == null) {
			fis = new FileInputStream(dataFile);
			dis = new DataInputStream(fis);
		}
		canWrite = false;
		canRead = true;
	}

	/**
	 * creates the file, if it doesn't exist.
	 * 
	 * @param startTimeStamp
	 *            for file header
	 */
	public void createFileAndHeader(long startTimeStamp, long stepIntervall) throws IOException {
		if (!dataFile.exists() || length < 16) {
			dataFile.getParentFile().mkdirs();
			if (dataFile.exists() && length < 16) {
				dataFile.delete(); // file corrupted (header shorter that 16
			}
			// bytes)
			dataFile.createNewFile();
			this.startTimeStamp = startTimeStamp;
			this.storagePeriod = stepIntervall;

			/*
			 * Do not close Output streams, because after writing the header -> data will follow!
			 */
			fos = new FileOutputStream(dataFile);
			bos = new BufferedOutputStream(fos);
			dos = new DataOutputStream(bos);
			dos.writeLong(startTimeStamp);
			dos.writeLong(stepIntervall);
			dos.flush();
			length += 16; /* wrote 2*8 Bytes */
			canWrite = true;
			canRead = false;
		}
	}

	public abstract void append(double value, long timestamp, byte flag) throws IOException;

	public abstract long getTimestampForLatestValue();

	/**
	 * Returns a List of Value Objects containing the measured Values between provided start and end timestamp
	 * 
	 * @param start
	 * @param end
	 * @throws IOException
	 */

	public abstract List<SampledValue> read(long start, long end) throws IOException;

	public abstract List<SampledValue> readFully() throws IOException;

	public abstract SampledValue read(long timestamp) throws IOException;

	public abstract SampledValue readNextValue(long timestamp) throws IOException;

	public abstract long getStoringPeriod();

	/**
	 * Closes and Flushes underlying Input- and OutputStreams
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		canRead = false;
		canWrite = false;
		if (dos != null) {
			dos.flush();
			dos.close();
			dos = null;
		}
		if (fos != null) {
			fos.close();
			fos = null;
		}
		if (dis != null) {
			dis.close();
			dis = null;
		}
		if (fis != null) {
			fis.close();
			fis = null;
		}
	}

	/**
	 * Flushes the underlying Data Streams.
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException {
		if (dos != null) {
			dos.flush();
		}
	}

	/**
	 * Return the Timestamp of the first stored Value in this File.
	 */
	public long getStartTimeStamp() {
		return startTimeStamp;
	}

	public static FileObject getFileObject(String fileName) throws IOException {
		if (fileName.startsWith("c")) {
			return new ConstantIntervalFileObject(fileName);
		}
		else if (fileName.startsWith("f")) {
			return new FlexibleIntervalFileObject(fileName);
		}
		else {
			throw new IOException("Invalid filename for SlotsDB-File");
		}
	}

	public static FileObject getFileObject(File file) throws IOException {
		if (file.getName().startsWith("c")) {
			return new ConstantIntervalFileObject(file);
		}
		else if (file.getName().startsWith("f")) {
			return new FlexibleIntervalFileObject(file);
		}
		else {
			throw new IOException("Invalid file for SlotsDB-File. Invalid filename.");
		}
	}
}
