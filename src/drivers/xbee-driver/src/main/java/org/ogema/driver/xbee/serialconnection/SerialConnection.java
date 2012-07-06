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

import org.slf4j.Logger;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * This class initializes the other classes in the serialconnection package and implements functions from the Interface.
 * 
 * @author puschas
 * 
 */
public class SerialConnection implements ISerialConnection {
	private SerialPort serialPort;
	private AbstractSerialPortReader serialPortReader;
	private SerialPortWriter serialPortWriter;
	private Thread serialPortWriterThread;
	private int baudrate = SerialPort.BAUDRATE_9600;
	private int databits = SerialPort.DATABITS_8;
	private int stopbits = SerialPort.STOPBITS_1;
	private int parity = SerialPort.PARITY_NONE;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");

	public SerialConnection(String port) throws SerialPortException {
		this.serialPort = new SerialPort(port);

		try {
			this.serialPort.openPort();
			this.serialPort.setParams(baudrate, databits, stopbits, parity);
		} catch (SerialPortException e) {
			logger.error(String.format("Failed to open serial port %s \n %s", port, e.getMessage()));
		}
		serialPortWriter = new SerialPortWriter(this.serialPort);
		serialPortWriterThread = new Thread(serialPortWriter, "SerialPortWriter");
		serialPortWriterThread.start();
		serialPortReader = new SerialPortReaderAp1(this.serialPort);
		serialPort.addEventListener(serialPortReader);
	}

	/**
	 * This lock object will receive a notification once new frames have been read and put into the FiFo.
	 */
	@Override
	public Object getInputEventLock() {
		return serialPortReader.getInputEventLock();
	}

	/**
	 * @return the oldest frame in the FiFo as a byte array and NULL if empty
	 */
	@Override
	public byte[] getReceivedFrame() {
		return serialPortReader.getInputFifo().get();
	}

	/**
	 * @returns true if the FiFo contains frames
	 */
	@Override
	public boolean hasFrames() {
		return serialPortReader.getInputFifo().count > 0 ? true : false;
	}

	/**
	 * @param a
	 *            complete frame (including start delimiter and checksum)
	 */
	@Override
	public void sendFrame(byte[] frame) {
		// TODO Auto-generated method stub
		serialPortWriter.sendData(frame);
	}

	/**
	 * Closes the jSSC connection and stops the serialPortWriter Thread
	 */
	@Override
	public void closeConnection() throws SerialPortException {
		serialPortWriter.stop();
		serialPort.closePort();
	}
}
