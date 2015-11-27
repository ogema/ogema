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
package org.ogema.driver.xbee.serialconnection;

import java.nio.ByteBuffer;

import org.ogema.driver.xbee.Configuration;
import org.ogema.driver.xbee.Constants;
import org.ogema.driver.xbee.manager.InputHandler;
import org.slf4j.Logger;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * SerialPortReader for API Enable 1 configuration of the XBee (no escaped bytes).
 * 
 * @author puschas
 * 
 */
public class SerialPortReaderAp1 {
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

	private InputHandler inputHandler;

	private enum ParsingState {
		DATA_RECEIVED, DELIMITER_PARSED, LENGTH_PARSED, MESSAGE_PARSED, CHECKSUM_PARSED;
	}

	public SerialPortReaderAp1(SerialPort serialPort, InputHandler ih) {
		this.serialPort = serialPort;
		this.inputHandler = ih;
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

	// @Override
	public void serialRead() {
		if (!serialPort.isOpened())
			return;
		try {
			tempArray = serialPort.readBytes();
		} catch (SerialPortException e) {
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
					buffer.position(Constants.MESSAGE_START); // Start after length
					buffer.limit(parsedCounter);
					if (Configuration.DEBUG)
						logger.debug("Parsed message:\n" + Constants.bytesToHex(buffer.array()));
					inputHandler.handleMessage(buffer);
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
