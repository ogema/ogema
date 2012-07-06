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
package org.ogema.driver.xbee.serialconnection;

import java.nio.ByteBuffer;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;

import org.ogema.driver.xbee.Configuration;
import org.ogema.driver.xbee.Constants;
import org.slf4j.Logger;

/**
 * SerialPortReader for API Enable 1 configuration of the XBee (no escaped bytes).
 * 
 * @author puschas
 * 
 */
public class SerialPortReaderAp1 extends AbstractSerialPortReader {
	protected volatile Fifo<byte[]> inputFifo;
	protected final Object inputEventLock;
	private SerialPort serialPort;
	private volatile ParsingState state;
	private byte[] tempArray = null;
	private ByteBuffer buffer;
	private volatile int arrayIndex;
	private volatile int parsedCounter;
	private volatile int messageLength;
	private volatile int checksum;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");

	private enum ParsingState {
		DATA_RECEIVED, DELIMITER_PARSED, LENGTH_PARSED, MESSAGE_PARSED, CHECKSUM_PARSED;
	}

	public SerialPortReaderAp1(SerialPort serialPort) {
		this.serialPort = serialPort;
		inputFifo = new Fifo<byte[]>(8); // 1<<8=256
		inputEventLock = new Object();
		state = ParsingState.DATA_RECEIVED;
		buffer = ByteBuffer.allocate(256);
		messageLength = 0;
	}

	private void clear() {
		buffer.clear();
		parsedCounter = 0;
		checksum = 0;
		state = ParsingState.DATA_RECEIVED;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.isRXCHAR()) { // Data available
			try {
				tempArray = serialPort.readBytes();
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (tempArray == null) {
				return;
			}
			if (Configuration.DEBUG)
				logger.debug("new chunk: " + Constants.bytesToHex(tempArray));

			arrayIndex = 0;

			while (arrayIndex < tempArray.length) {
				switch (state) {
				case DATA_RECEIVED: // Wait for start delimiter
					for (; arrayIndex < tempArray.length; ++arrayIndex) {
						if ((byte) 0x7E == tempArray[arrayIndex]) {
							buffer.put(tempArray[arrayIndex]);
							++arrayIndex;
							++parsedCounter;
							state = ParsingState.DELIMITER_PARSED;
							break;
						}
					}
					break;
				case DELIMITER_PARSED: // Parse message length
					for (; arrayIndex < tempArray.length; ++arrayIndex) {
						if (Constants.MESSAGE_START == parsedCounter) { // Length has been parsed
							messageLength = (buffer.get(buffer.position() - 2) << 8)
									| ((buffer.get(buffer.position() - 1) & 0xff));
							messageLength &= 0xffff;
							if (messageLength > Constants.MTU) { // Length invalid, ignore frame
								clear();
								break;
							}
							state = ParsingState.LENGTH_PARSED;
							break;
						}
						else {
							buffer.put(tempArray[arrayIndex]);
							++parsedCounter;
						}
					}
					break;
				case LENGTH_PARSED: // Parse message
					for (; arrayIndex < tempArray.length; ++arrayIndex) {
						if (parsedCounter == messageLength + 3) { // The complete message (without checksum) has been
							// parsed
							state = ParsingState.MESSAGE_PARSED;
							break;
						}
						else {
							checksum += tempArray[arrayIndex] & 0xFF;
							buffer.put(tempArray[arrayIndex]);
							++parsedCounter;
						}
					}
					break;
				case MESSAGE_PARSED: // Parse checksum
				{
					checksum += tempArray[arrayIndex] & 0xFF;
					++parsedCounter;
					state = ParsingState.CHECKSUM_PARSED;
					++arrayIndex;
				}
					break;
				case CHECKSUM_PARSED: // Verify checksum, put Buffer in FIFO, notify and clean up
					if (0xFF == (checksum & 0xFF)) { // If checksum valid
						byte[] dst = new byte[parsedCounter - 4]; // Exclude start delimiter, length and checksum
						buffer.position(Constants.MESSAGE_START); // Start after length
						buffer.get(dst, 0, parsedCounter - 4); // parsedCounter - (delimiter + length + checksum)
						if (Configuration.DEBUG)
							logger.debug("Parsed message:\n" + Constants.bytesToHex(dst));
						synchronized (inputEventLock) {
							inputFifo.put(dst);
							inputEventLock.notify();
						}
					}
					else {
						if (Configuration.DEBUG)
							logger.info("Invalid checksum: " + Integer.toHexString(checksum & 0xFF));
					}
					clear();
					break;
				}
			}
		}
	}

	@Override
	Fifo<byte[]> getInputFifo() {
		return inputFifo;
	}

	@Override
	Object getInputEventLock() {
		return inputEventLock;
	}
}
