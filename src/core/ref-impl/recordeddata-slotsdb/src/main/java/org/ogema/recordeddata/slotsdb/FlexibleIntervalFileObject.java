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

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;

public class FlexibleIntervalFileObject extends FileObject {

	private long lastTimestamp;
	private static final long headerend = 16;

	protected FlexibleIntervalFileObject(File file, RecordedDataCache cache) throws IOException {
		super(file, cache);
		lastTimestamp = startTimeStamp;
	}

	protected FlexibleIntervalFileObject(String fileName, RecordedDataCache cache) throws IOException {
		super(fileName, cache);
		lastTimestamp = startTimeStamp;
	}

	@Override
	void readHeader(DataInputStream dis) throws IOException {
		startTimeStamp = dis.readLong();
		storagePeriod = dis.readLong(); /* is -1 for disabled storagePeriod */
		lastTimestamp = startTimeStamp;
		// line below should be obsolete, since flexible interval needs no rounded timestamp
		//startTimeStamp = FileObjectProxy.getRoundedTimestamp(startTimeStamp, storagePeriod);
	}

	@Override
	public void append(double value, long timestamp, byte flag) throws IOException {
		// long writePosition = dataFile.length();
		if (!canWrite) {
			enableOutput();
		}

		// FIXME really? only write to time series if new values timestamp is greater than last one? -> this is for log data, so it makes sense
		if (timestamp > lastTimestamp) {
			dos.writeLong(timestamp);
			dos.writeDouble(value);
			dos.writeByte(flag);
			lastTimestamp = timestamp;
		}

	}

	@Override
	protected long getTimestampForLatestValueInternal() {
		// FIXME: this won't work ... if read(String, long, long) is invoked a new
		// FileObject is created and lastTimestamp is set to startTimeStamp ...
		// return lastTimestamp;

		// this is only a quickfix so that it works... @author of this class: if there
		// is a better solution pls fix this... otherwise delete all comments in here
		// and lets stick to this solution for now:
		int dataSetCount = getDataSetCountInternal();
		if (dataSetCount > 1) {
			try {
				if (!canRead) {
					enableInput();
				}
				fis.getChannel().position(headerend);
				byte[] b = new byte[(int) (dataFile.length() - headerend)];
				dis.read(b, 0, b.length);
				ByteBuffer bb = ByteBuffer.wrap(b);
				// set position to last entries timestamp (Long, Double and Byte size in bits
				// so divide through Byte.SIZE to get size in bytes)
				// casting is a hack to avoid incompatibility when building this on Java 9 and run on Java 8
				// ByteBuffer#position used to return a Buffer in Jdk8, but from Java 9 on returns a ByteBuffer
				((Buffer) bb).position((dataSetCount - 1) * getDataSetSize());
				long l = bb.getLong();
				return l;
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				// FIXME return negative value to signalize error? for now simply
				// return startTimeStamp ...
			}
		}
		return startTimeStamp;
	}

	@Override
	protected List<SampledValue> readInternal(long start, long end) throws IOException {

//		List<SampledValue> toReturn = new Vector<SampledValue>();
		final List<SampledValue> toReturn = new ArrayList<>(getDataSetCount());
		if (!canRead) {
			enableInput();
		}

		long startpos = headerend;

		fis.getChannel().position(startpos);
		byte[] b = new byte[(int) (dataFile.length() - headerend)];
		dis.read(b, 0, b.length);
		ByteBuffer bb = ByteBuffer.wrap(b);
		// casting is a hack to avoid incompatibility when building this on Java 9 and run on Java 8
		// ByteBuffer#rewind used to return a Buffer in Jdk8, but from Java 9 on returns a ByteBuffer
		((Buffer) bb).rewind();

		for (int i = 0; i < getDataSetCountInternal(); i++) {
			long timestamp = bb.getLong();
			double d = bb.getDouble();
			Quality s = Quality.getQuality(bb.get());
			if (!Double.isNaN(d)) {
				if (timestamp >= start && timestamp <= end) {
					toReturn.add(new SampledValue(DoubleValues.of(d), timestamp, s));
				}

			}
		}

		// TODO iterate through file and extract the required values.
		return toReturn;
	}

	@Override
	protected List<SampledValue> readFullyInternal() throws IOException {
//		List<SampledValue> toReturn = new Vector<SampledValue>();
		final List<SampledValue> toReturn = new ArrayList<>(getDataSetCountInternal());

		if (!canRead) {
			enableInput();
		}

		long startpos = headerend;

        //System.out.println("direct BB");
        /*
		fis.getChannel().position(startpos);
		byte[] b = new byte[(int) (dataFile.length() - headerend)];
		dis.read(b, 0, b.length);
		ByteBuffer bb = ByteBuffer.wrap(b);
		((Buffer) bb).rewind();
        */
        MappedByteBuffer bb = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, startpos, dataFile.length() - headerend);
		int countOfDataSets = (int) ((dataFile.length() - headerend) / getDataSetSize());
		for (int i = 0; i < countOfDataSets; i++) {
			long timestamp = bb.getLong();
			double d = bb.getDouble();
			Quality s = Quality.getQuality(bb.get());

			if (!Double.isNaN(d)) {
				toReturn.add(new SampledValue(DoubleValues.of(d), timestamp, s));
			}
		}
		return toReturn;
	}

	@Override
	public SampledValue read(long timestamp) throws IOException {
		// TODO Search the special value
		if (!canRead) {
			enableInput();
		}

		long startpos = headerend;

		fis.getChannel().position(startpos);
		byte[] b = new byte[(int) (dataFile.length() - headerend)];
		dis.read(b, 0, b.length);
		ByteBuffer bb = ByteBuffer.wrap(b);
		((Buffer) bb).rewind();
		int countOfDataSets = (int) ((dataFile.length() - headerend) / 17);
		for (int i = 0; i < countOfDataSets; i++) {
			long timestamp2 = bb.getLong();
			double d = bb.getDouble();
			Quality s = Quality.getQuality(bb.get());
			if (timestamp < timestamp2) {
				return null;
			}
			if (!Double.isNaN(d) && timestamp == timestamp2) {
				return new SampledValue(DoubleValues.of(d), timestamp2, s);
			}
		}
		return null;
	}

	@Override
	public long getStoringPeriod() {
		return -1;
	}

	@Override
	public SampledValue readNextValue(long timestamp) throws IOException {
		if (!canRead) {
			enableInput();
		}

		long startpos = headerend;

		fis.getChannel().position(startpos);
		byte[] b = new byte[(int) (dataFile.length() - headerend)];
		dis.read(b, 0, b.length);
		ByteBuffer bb = ByteBuffer.wrap(b);
		((Buffer) bb).rewind();
		int countOfDataSets = (int) ((dataFile.length() - headerend) / getDataSetSize());
		for (int i = 0; i < countOfDataSets; i++) {
			long timestamp2 = bb.getLong();
			double d = bb.getDouble();
			Quality s = Quality.getQuality(bb.get());
			if (!Double.isNaN(d) && timestamp <= timestamp2) {
				return new SampledValue(DoubleValues.of(d), timestamp2, s);
			}
		}
		return null;
	}

	@Override
	public SampledValue readPreviousValue(long timestamp) throws IOException {
		if (!canRead) {
			enableInput();
		}
		long startpos = headerend;

		fis.getChannel().position(startpos);
		byte[] b = new byte[(int) (dataFile.length() - headerend)];
		dis.read(b, 0, b.length);
		ByteBuffer bb = ByteBuffer.wrap(b);
		((Buffer) bb).rewind();
		int countOfDataSets = (int) ((dataFile.length() - headerend) / getDataSetSize());
		long tcand = Long.MIN_VALUE;
		double dcand = Double.NaN;
		Quality qcand = null;
		for (int i = 0; i < countOfDataSets; i++) {
			long timestamp2 = bb.getLong();
			double d = bb.getDouble();
			Quality s = Quality.getQuality(bb.get());
			if (!Double.isNaN(d) && timestamp >= timestamp2) {
				tcand = timestamp2;
				dcand = d;
				qcand = s;
//				candidate = new SampledValue(DoubleValues.of(d), timestamp2, s);
			}
			else if (timestamp < timestamp2)
				break;
		}
		if (!Double.isNaN(dcand))
			return new SampledValue(DoubleValues.of(dcand), tcand, qcand);
		return null;
	}

	@Override
	protected int getDataSetCountInternal() {
		return (int) ((dataFile.length() - headerend) / getDataSetSize());
	}

	@Override
	protected int getDataSetCountInternal(long start, long end) throws IOException {
		long fileEnd = getTimestampForLatestValueInternal();
		if (start <= startTimeStamp && end >= fileEnd)
			return getDataSetCountInternal();
		else if (start > fileEnd || end < startTimeStamp)
			return 0;
		if (!canRead) {
			enableInput();
		}
		long startpos = headerend;
		fis.getChannel().position(startpos);
		byte[] b = new byte[(int) (dataFile.length() - headerend)];
		dis.read(b, 0, b.length);
		ByteBuffer bb = ByteBuffer.wrap(b);
		((Buffer) bb).rewind();
		int cnt = 0;
		int countOfDataSets = getDataSetCountInternal();
		for (int i = 0; i < countOfDataSets; i++) {
			long timestamp2 = bb.getLong();
			double d = bb.getDouble();
			if (timestamp2 > end)
				return cnt;
			if (!Double.isNaN(d) && timestamp2 >= start) {
				cnt++;
			}
			bb.get();
		}
		return cnt;
	}

	private final static int getDataSetSize() {
		return (Long.SIZE + Double.SIZE + Byte.SIZE) / Byte.SIZE;
	}

}
