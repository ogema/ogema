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
package org.ogema.drivers.arduino;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;

/**
 * Class that implements a serial connection which can be used for master and slave implementations.
 * 
 * @author Dieter Wimberger
 * @author John Charlton
 * @version @version@ (@date@)
 */
public class SerialConnection implements SerialPortEventListener {

	private static final boolean debug = true;
	private SerialParameters m_Parameters;
	// private ModbusSerialTransport m_Transport;
	private CommPortIdentifier m_PortIdentifyer;
	private SerialPort m_SerialPort;
	private boolean m_Open;
	private InputStream m_SerialIn;

	/**
	 * Creates a SerialConnection object and initilizes variables passed in as params.
	 * 
	 * @param parameters
	 *            A SerialParameters object.
	 */
	public SerialConnection(SerialParameters parameters) {
		m_Parameters = parameters;
		m_Open = false;
	}// constructor

	/**
	 * Returns the reference to the SerialPort instance.
	 * 
	 * @return a reference to the <tt>SerialPort</tt>.
	 */
	public SerialPort getSerialPort() {
		return m_SerialPort;
	}// getSerialPort

	InputStream getInputStream() {
		return m_SerialIn;
	}

	/**
	 * Returns the <tt>ModbusTransport</tt> instance to be used for receiving and sending messages.
	 * 
	 * @return a <tt>ModbusTransport</tt> instance.
	 */
	// public ModbusTransport getModbusTransport() {
	// return m_Transport;
	// }//getModbusTransport

	/**
	 * Opens the communication port.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void open() throws Exception {

		// 1. obtain a CommPortIdentifier instance
		try {
			m_PortIdentifyer = CommPortIdentifier.getPortIdentifier(m_Parameters.getPortName());
		} catch (NoSuchPortException e) {
			if (debug)
				System.out.println(e.getMessage());
			e.printStackTrace();
			throw new Exception(e);
		}
		System.out.println("Got Port Identifier: " + m_PortIdentifyer);

		// 2. open the port, wait for given timeout
		try {
			m_SerialPort = (SerialPort) m_PortIdentifyer.open("Modbus Serial Master", 30000);
		} catch (PortInUseException e) {
			if (debug)
				System.out.println(e.getMessage());

			throw new Exception(e.getMessage());
		}
		if (debug)
			System.out.println("Got Serial Port");

		// 3. set the parameters
		try {
			setConnectionParameters();
		} catch (Exception e) {
			// ensure it is closed
			m_SerialPort.close();
			if (debug)
				System.out.println(e.getMessage());
			throw e;
		}

		// if (Modbus.SERIAL_ENCODING_ASCII.equals(m_Parameters.getEncoding())) {
		// m_Transport = new ModbusASCIITransport();
		// } else if (Modbus.SERIAL_ENCODING_RTU.equals(m_Parameters.getEncoding())) {
		// m_Transport = new ModbusRTUTransport();
		// setReceiveTimeout(m_Parameters.getReceiveTimeout()); //just here for the moment.
		// } else if (Modbus.SERIAL_ENCODING_BIN.equals(m_Parameters.getEncoding())) {
		// m_Transport = new ModbusBINTransport();
		// }
		// m_Transport.setEcho(m_Parameters.isEcho());

		// Open the input and output streams for the connection. If they won't
		// open, close the port before throwing an exception.
		try {
			m_SerialIn = m_SerialPort.getInputStream();
			// m_Transport.setCommPort(m_SerialPort);
			// m_Transport.prepareStreams(m_SerialIn,
			// m_SerialPort.getOutputStream());
		} catch (IOException e) {
			m_SerialPort.close();
			if (debug)
				System.out.println(e.getMessage());

			throw new Exception("Error opening i/o streams");
		}
		if (debug)
			System.out.println("i/o Streams prepared");

		// Add this object as an event listener for the serial port.
		try {
			m_SerialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			m_SerialPort.close();
			if (debug)
				System.out.println(e.getMessage());
			throw new Exception("too many listeners added");
		}

		// Set notifyOnBreakInterrup to allow event driven break handling.
		m_SerialPort.notifyOnBreakInterrupt(true);

		m_Open = true;
		if (debug)
			System.out.println("Port opened.");
	}// open

	public void setReceiveTimeout(int ms) {
		// Set receive timeout to allow breaking out of polling loop during
		// input handling.
		try {
			m_SerialPort.enableReceiveTimeout(ms);
		} catch (UnsupportedCommOperationException e) {
			if (debug)
				System.out.println(e.getMessage());
		}
	}// setReceiveTimeout

	/**
	 * Sets the connection parameters to the setting in the parameters object. If set fails return the parameters object
	 * to origional settings and throw exception.
	 * 
	 * @throws Exception
	 *             if the configured parameters cannot be set properly on the port.
	 */
	public void setConnectionParameters() throws Exception {

		// Save state of parameters before trying a set.
		int oldBaudRate = m_SerialPort.getBaudRate();
		int oldDatabits = m_SerialPort.getDataBits();
		int oldStopbits = m_SerialPort.getStopBits();
		int oldParity = m_SerialPort.getParity();
		// int oldFlowControl = m_SerialPort.getFlowControlMode();

		// Set connection parameters, if set fails return parameters object
		// to original state.
		try {
			m_SerialPort.setSerialPortParams(m_Parameters.getBaudRate(), m_Parameters.getDatabits(), m_Parameters
					.getStopbits(), m_Parameters.getParity());
		} catch (UnsupportedCommOperationException e) {
			m_Parameters.setBaudRate(oldBaudRate);
			m_Parameters.setDatabits(oldDatabits);
			m_Parameters.setStopbits(oldStopbits);
			m_Parameters.setParity(oldParity);
			if (debug)
				System.out.println(e.getMessage());

			throw new Exception("Unsupported parameter");
		}

		// Set flow control.
		try {
			m_SerialPort.setFlowControlMode(m_Parameters.getFlowControlIn() | m_Parameters.getFlowControlOut());
		} catch (UnsupportedCommOperationException e) {
			if (debug)
				System.out.println(e.getMessage());

			throw new Exception("Unsupported flow control");
		}
	}// setConnectionParameters

	/**
	 * Close the port and clean up associated elements.
	 */
	public void close() {
		// If port is alread closed just return.
		if (!m_Open) {
			return;
		}

		// Check to make sure sPort has reference to avoid a NPE.
		if (m_SerialPort != null) {
			try {
				// m_Transport.close();
				m_SerialIn.close();
			} catch (IOException e) {
				System.err.println(e);
			}
			// Close the port.
			m_SerialPort.close();
		}

		m_Open = false;
	}// close

	/**
	 * Reports the open status of the port.
	 * 
	 * @return true if port is open, false if port is closed.
	 */
	public boolean isOpen() {
		return m_Open;
	}// isOpen

	public void serialEvent(SerialPortEvent e) {
		// Determine type of event.
		switch (e.getEventType()) {
		// This event is ignored, the application reads directly from
		// the serial input stream
		case SerialPortEvent.DATA_AVAILABLE:
			/*
			 * try { int amount = m_SerialIn.available(); while (amount > 0) { try { byte[] buffer = new byte[amount];
			 * if ((amount = m_SerialIn.read(buffer, 0, amount)) > 0) { m_Pipe.write (buffer, 0, amount); } amount =
			 * m_SerialIn.available(); } catch (IOException ex) { System.err.println("Error: Comm event read: " + ex);
			 * ex.printStackTrace(); return; } } } catch (Exception ex) { //handle
			 * 
			 * }
			 */
			// TODO parse data
			System.out.println("New data: " + m_SerialIn);
			break;
		case SerialPortEvent.BI:
			if (debug)
				System.out.println("Serial port break detected");
			break;
		default:
			if (debug)
				System.out.println("Serial port event: " + e.getEventType());
		}
	}// serialEvent

}// class SerialConnection
