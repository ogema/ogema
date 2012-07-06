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
package org.ogema.recordeddata.slotsdb;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;

public class ConstantIntervalFileObject extends FileObject {

	public ConstantIntervalFileObject(File file) throws IOException {
		super(file);
	}

	public ConstantIntervalFileObject(String fileName) throws IOException {
		super(fileName);
	}

	/**
	 * @return step frequency in seconds
	 */
	@Override
	public long getStoringPeriod() {
		return storagePeriod;
	}

	@Override
	protected void readHeader(DataInputStream dis) throws IOException {
		startTimeStamp = dis.readLong();
		storagePeriod = dis.readLong();
	}

	@Override
	public void append(double value, long timestamp, byte flag) throws IOException {
		long writePosition = getBytePosition(timestamp);
		if (writePosition == length) {
			/*
			 * value for this timeslot has not been saved yet "AND" some value has been stored in last timeslot
			 */
			if (!canWrite) {
				enableOutput();
			}

			dos.writeDouble(value);
			dos.writeByte(flag);
			length += 9;
		}
		else {
			if (length > writePosition) {
				/*
				 * value has already been stored for this timeslot -> handle? AVERAGE, MIN, MAX, LAST speichern?!
				 */
			}
			else {
				/*
				 * there are missing some values missing -> fill up with NaN!
				 */
				if (!canWrite) {
					enableOutput();
				}
				long rowsToFillWithNan = (writePosition - length) / 9;// TODO:
				// stimmt
				// Berechnung?
				for (int i = 0; i < rowsToFillWithNan; i++) {
					dos.writeDouble(Double.NaN); // TODO: festlegen welcher Wert
					// undefined sein soll NaN
					// ok?
					dos.writeByte(Quality.BAD.getQuality()); // TODO:
					// festlegen
					// welcher Wert
					// undefined sein
					// soll 00 ok?
					length += 9;
				}
				dos.writeDouble(value);
				dos.writeByte(flag);
				length += 9;
			}
		}
		/*
		 * close(); OutputStreams will not be closed or flushed. Data will be written to disk after calling flush()
		 * method.
		 */
	}

	@Override
	public long getTimestampForLatestValue() {
		return startTimeStamp + (((length - 16) / 9) - 1) * storagePeriod;
	}

	/**
	 * calculates the position in a file for a certain timestamp
	 * 
	 * @param timestamp
	 * @return position
	 */
	private long getBytePosition(long timestamp) {
		if (timestamp >= startTimeStamp) {

			/*
			 * get position for timestamp 117 000: 117 000 - 100 000 = 17 000 17 * 000 / 5 000 = 3.4 Math.round(3.4) = 3
			 * 3*(8+1) = 27 27 + 16 = 43 = position to store to!
			 */
			// long pos = (Math.round((double) (timestamp - startTimeStamp) /
			// storagePeriod) * 9) + 16; /* slower */

			double pos = (double) (timestamp - startTimeStamp) / storagePeriod;
			if (pos % 1 != 0) { /* faster */
				pos = Math.round(pos);
			}
			return (long) (pos * 9 + 16);
		}
		else {
			// not in file! should never happen...
			return -1;
		}
	}

	/*
	 * Calculates the closest timestamp to wanted timestamp getByteposition does a similar thing (Math.round()), for
	 * byte position.
	 */
	private long getClosestTimestamp(long timestamp) {
		// return Math.round((double) (timestamp -
		// startTimeStamp)/storagePeriod)*storagePeriod+startTimeStamp; /*
		// slower */

		double ts = (double) (timestamp - startTimeStamp) / storagePeriod;
		if (ts % 1 != 0) {
			ts = Math.round(ts);
		}
		return (long) ts * storagePeriod + startTimeStamp;
	}

	@Override
	public SampledValue read(long timestamp) throws IOException {

		if ((timestamp - startTimeStamp) % storagePeriod == 0) {
			if (timestamp >= startTimeStamp && timestamp <= getTimestampForLatestValue()) {
				if (!canRead) {
					enableInput();
				}
				fis.getChannel().position(getBytePosition(timestamp));
				Double toReturn = dis.readDouble();
				if (!Double.isNaN(toReturn)) {

					return new SampledValue(new DoubleValue(toReturn), timestamp, Quality.getQuality(dis.readByte()));
				}
			}
		}
		return null;
	}

	/**
	 * Returns a List of Value Objects containing the measured Values between provided start and end timestamp
	 * 
	 * @param start
	 * @param end
	 * @return 
	 * @throws IOException
	 */
	@Override
	public List<SampledValue> read(long start, long end) throws IOException {
		start = getClosestTimestamp(start); // round to: startTimestamp +
		// n*stepIntervall
		long endRounded = getClosestTimestamp(end); // round to: startTimestamp +
		// n*stepIntervall

		List<SampledValue> toReturn = new Vector<>();

		if (start < end) {
			if (start < startTimeStamp) {
				// of this file.
				start = startTimeStamp;
			}
			if (endRounded > getTimestampForLatestValue()) {
				endRounded = getTimestampForLatestValue();
			}

			if (!canRead) {
				enableInput();
			}

			long timestampcounter = start;
			long startPos = getBytePosition(start);
			long endPos = getBytePosition(endRounded);

			fis.getChannel().position(startPos);

			byte[] b = new byte[(int) (endPos - startPos) + 9];
			dis.read(b, 0, b.length);
			ByteBuffer bb = ByteBuffer.wrap(b);
			bb.rewind();

			for (int i = 0; i <= (endPos - startPos) / 9; i++) {
				double d = bb.getDouble();
				Quality s = Quality.getQuality(bb.get());
				if (!Double.isNaN(d)) {
					if (timestampcounter <= end) {
						toReturn.add(new SampledValue(new DoubleValue(d), timestampcounter, s));
					}
				}
				timestampcounter += storagePeriod;
			}

		}
		else if (start == end) {
			toReturn.add(read(start));
			toReturn.removeAll(Collections.singleton(null));
		}
		return toReturn; // Always return a list -> might be empty -> never is
		// null, to avoid NP's
	}

	@Override
	public List<SampledValue> readFully() throws IOException {
		return read(startTimeStamp, getTimestampForLatestValue());
	}

	@Override
	public SampledValue readNextValue(long timestamp) throws IOException {
		// Calculate next Value, round Timestamp to next Value
		timestamp = timestamp + (storagePeriod - ((timestamp - startTimeStamp) % storagePeriod));
		long startPos = getBytePosition(timestamp);
		long endPos = getBytePosition(getTimestampForLatestValue());
		for (int i = 0; i <= (endPos - startPos) / 9; i++) {
			if (timestamp >= startTimeStamp && timestamp <= getTimestampForLatestValue()) {
				if (!canRead) {
					enableInput();
				}
				fis.getChannel().position(getBytePosition(timestamp));
				Double toReturn = dis.readDouble();
				if (!Double.isNaN(toReturn)) {
					return new SampledValue(new DoubleValue(toReturn), timestamp, Quality.getQuality(dis.readByte()));
				}
				timestamp += storagePeriod;
			}
		}
		return null;
	}
}
