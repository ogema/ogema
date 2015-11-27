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

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Vector;

import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;

public class FlexibleIntervalFileObject extends FileObject {

	private long lastTimestamp = startTimeStamp;
	private static final long headerend = 16;

	public FlexibleIntervalFileObject(File file) throws IOException {
		super(file);

	}

	public FlexibleIntervalFileObject(String fileName) throws IOException {
		super(fileName);
	}

	@Override
	void readHeader(DataInputStream dis) throws IOException {
		startTimeStamp = dis.readLong();
		storagePeriod = dis.readLong(); /* is -1 for disabled storagePeriod */

		// line below should be obsolete, since flexible interval needs no rounded timestamp
		//startTimeStamp = FileObjectProxy.getRoundedTimestamp(startTimeStamp, storagePeriod);
	}

	@Override
	public void append(double value, long timestamp, byte flag) throws IOException {
		// long writePosition = dataFile.length();

		if (!canWrite) {
			enableOutput();
		}

		// FIXME really? only write to time series if new values timestamp is greater than last one?
		if (timestamp > lastTimestamp) {
			dos.writeLong(timestamp);
			dos.writeDouble(value);
			dos.writeByte(flag);
			lastTimestamp = timestamp;
		}

	}

	@Override
	public long getTimestampForLatestValue() {
		// FIXME: this won't work ... if read(String, long, long) is invoked a new
		// FileObject is created and lastTimestamp is set to startTimeStamp ...
		// return lastTimestamp;

		// this is only a quickfix so that it works... @author of this class: if there
		// is a better solution pls fix this... otherwise delete all comments in here
		// and lets stick to this solution for now:
		int dataSetCount = getDataSetCount();
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
				bb.position((dataSetCount - 1) * getDataSetSize());
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
	public List<SampledValue> read(long start, long end) throws IOException {

		List<SampledValue> toReturn = new Vector<SampledValue>();
		if (!canRead) {
			enableInput();
		}

		long startpos = headerend;

		fis.getChannel().position(startpos);
		byte[] b = new byte[(int) (dataFile.length() - headerend)];
		dis.read(b, 0, b.length);
		ByteBuffer bb = ByteBuffer.wrap(b);
		bb.rewind();

		for (int i = 0; i < getDataSetCount(); i++) {
			long timestamp = bb.getLong();
			double d = bb.getDouble();
			Quality s = Quality.getQuality(bb.get());
			if (!Double.isNaN(d)) {
				if (timestamp >= start && timestamp <= end) {
					toReturn.add(new SampledValue(new DoubleValue(d), timestamp, s));
				}

			}
		}

		// TODO iterate through file and extract the required values.
		return toReturn;
	}

	@Override
	public List<SampledValue> readFully() throws IOException {
		List<SampledValue> toReturn = new Vector<SampledValue>();

		if (!canRead) {
			enableInput();
		}

		long startpos = headerend;

		fis.getChannel().position(startpos);
		byte[] b = new byte[(int) (dataFile.length() - headerend)];
		dis.read(b, 0, b.length);
		ByteBuffer bb = ByteBuffer.wrap(b);
		bb.rewind();
		int countOfDataSets = (int) ((dataFile.length() - headerend) / getDataSetSize());
		for (int i = 0; i < countOfDataSets; i++) {
			long timestamp = bb.getLong();
			double d = bb.getDouble();
			Quality s = Quality.getQuality(bb.get());

			if (!Double.isNaN(d)) {
				toReturn.add(new SampledValue(new DoubleValue(d), timestamp, s));
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
		bb.rewind();
		int countOfDataSets = (int) ((dataFile.length() - headerend) / 17);
		for (int i = 0; i < countOfDataSets; i++) {
			long timestamp2 = bb.getLong();
			double d = bb.getDouble();
			Quality s = Quality.getQuality(bb.get());
			if (timestamp < timestamp2) {
				return null;
			}
			if (!Double.isNaN(d) && timestamp == timestamp2) {
				return new SampledValue(new DoubleValue(d), timestamp2, s);
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
		// TODO Auto-generated method stub
		if (!canRead) {
			enableInput();
		}

		long startpos = headerend;

		fis.getChannel().position(startpos);
		byte[] b = new byte[(int) (dataFile.length() - headerend)];
		dis.read(b, 0, b.length);
		ByteBuffer bb = ByteBuffer.wrap(b);
		bb.rewind();
		int countOfDataSets = (int) ((dataFile.length() - headerend) / getDataSetSize());
		for (int i = 0; i < countOfDataSets; i++) {
			long timestamp2 = bb.getLong();
			double d = bb.getDouble();
			Quality s = Quality.getQuality(bb.get());
			if (!Double.isNaN(d) && timestamp <= timestamp2) {
				return new SampledValue(new DoubleValue(d), timestamp2, s);
			}
		}
		return null;
	}

	private int getDataSetCount() {
		return (int) ((dataFile.length() - headerend) / getDataSetSize());
	}

	private int getDataSetSize() {
		return (Long.SIZE + Double.SIZE + Byte.SIZE) / Byte.SIZE;
	}

}
