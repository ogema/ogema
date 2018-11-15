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
package org.ogema.recordeddata.slotsdb;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileObject {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected long startTimeStamp = Long.MIN_VALUE; // byte 0-7 in file (cached)
	protected long storagePeriod; // byte 8-15 in file (cached)
	protected final File dataFile;
	protected DataOutputStream dos;
	protected BufferedOutputStream bos;
	protected FileOutputStream fos;
	protected DataInputStream dis;
	protected FileInputStream fis;
	protected boolean canWrite;
	protected boolean canRead;
	
	private final RecordedDataCache cache;

	/*
	 * File length will be cached to avoid system calls and improve I/O Performance
	 */
	protected long length = 0;

	public FileObject(String filename, RecordedDataCache cache) throws IOException {
		this.cache = cache;
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
	 * @param dis
	 */
	abstract void readHeader(DataInputStream dis) throws IOException;

	public FileObject(File file, RecordedDataCache cache) throws IOException {
		this.cache = cache;
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
			cache.invalidate();
			assert cache.getCache() == null : "Invalidated cache is still alive";
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

			//OLD 
			//this.startTimeStamp = FileObjectProxy.getRoundedTimestamp(startTimeStamp, stepIntervall);

			this.startTimeStamp = startTimeStamp;

			this.storagePeriod = stepIntervall;

			/*
			 * Do not close Output streams, because after writing the header -> data will follow!
			 */
			fos = new FileOutputStream(dataFile);
			bos = new BufferedOutputStream(fos);
			dos = new DataOutputStream(bos);
			dos.writeLong(this.startTimeStamp);
			dos.writeLong(stepIntervall);
			dos.flush();
			length += 16; /* wrote 2*8 Bytes */
			canWrite = true;
			canRead = false;
		}
	}
	
	public List<SampledValue> readFully() throws IOException {
		List<SampledValue> values = cache.getCache();
		if (values != null)
			return values;
		values = readFullyInternal();
		// store until next write access
		cache.cache(Collections.unmodifiableList(values));
		return values;
	}
	
	public List<SampledValue> read(long start, long end) throws IOException {
		if (start <= startTimeStamp && end >= getTimestampForLatestValue()) {
			return readFully(); // caches values
		}
		final List<SampledValue> values = cache.getCache();
		if (values != null) {
			final List<SampledValue> copy = new ArrayList<>(values.size());
			for (final SampledValue sv : values) {
				final long t = sv.getTimestamp();
				if (t < start)
					continue;
				if (t > end)
					break;
				copy.add(sv);
			}
			return copy;
		}
		return readInternal(start, end);
	};


	public int getDataSetCount() {
		final List<SampledValue> values = cache.getCache();
		if (values != null) {	
			return values.size();
		}
		return getDataSetCountInternal();
	};
	
	public int getDataSetCount(long start, long end) throws IOException {
		final List<SampledValue> values = cache.getCache();
		if (values != null) {
			if (start <= startTimeStamp && !values.isEmpty()) {
				long endTimeStamp = values.get(values.size()-1).getTimestamp();
				if (end >= endTimeStamp) {
					return values.size();
				}
			}
			int cnt = 0;
			for (final SampledValue sv : values) {
				final long t = sv.getTimestamp();
				if (t < start)
					continue;
				if (t > end)
					break;
				cnt++;
			}
			return cnt;
		}
		return getDataSetCountInternal(start, end);
	};


	public long getTimestampForLatestValue() {
		final List<SampledValue> values = cache.getCache();
		if (values != null && !values.isEmpty()) {
			return values.get(values.size()-1).getTimestamp();
		}
		return getTimestampForLatestValueInternal();
	};

	public abstract void append(double value, long timestamp, byte flag) throws IOException;

	protected abstract List<SampledValue> readInternal(long start, long end) throws IOException;

	protected abstract List<SampledValue> readFullyInternal() throws IOException;
	
	public abstract SampledValue read(long timestamp) throws IOException;

	public abstract SampledValue readNextValue(long timestamp) throws IOException;
	
	public abstract SampledValue readPreviousValue(long timestamp) throws IOException;
	
	protected abstract long getTimestampForLatestValueInternal();
	
	protected abstract int getDataSetCountInternal();
	protected abstract int getDataSetCountInternal(long start, long end) throws IOException;
	
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
			cache.invalidate();
			assert cache.getCache() == null : "Invalidated cache is still alive";
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
			cache.invalidate();
			assert cache.getCache() == null : "Invalidated cache is still alive";
			dos.flush();
		}
	}

	/**
	 * Return the Timestamp of the first stored Value in this File.
	 */
	public long getStartTimeStamp() {
		return startTimeStamp;
	}

	public static FileObject getFileObject(String fileName, RecordedDataCache cache) throws IOException {
		if (fileName.startsWith("c")) {
			return new ConstantIntervalFileObject(fileName, cache);
		}
		else if (fileName.startsWith("f")) {
			return new FlexibleIntervalFileObject(fileName, cache);
		}
		else {
			throw new IOException("Invalid filename for SlotsDB-File");
		}
	}

	public static FileObject getFileObject(File file, RecordedDataCache cache) throws IOException {
		if (file.getName().startsWith("c")) {
			return new ConstantIntervalFileObject(file, cache);
		}
		else if (file.getName().startsWith("f")) {
			return new FlexibleIntervalFileObject(file, cache);
		}
		else {
			throw new IOException("Invalid file for SlotsDB-File. Invalid filename.");
		}
	}
}
