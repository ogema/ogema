//License
/***
 * Java Modbus Library (jamod)
 * Copyright (c) 2002-2004, jamod development team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ***/
package com.ghgande.j2mod.modbus.io;

import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.msg.ModbusMessage;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.util.ModbusUtil;

/**
 * Class that implements the ModbusRTU transport flavor.
 * 
 * @author John Charlton
 * @author Dieter Wimberger
 * 
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author Julie Haugh
 * @version 1.05
 * 
 *          20140426 - Implement serial slave support
 */
public class ModbusRTUTransport extends ModbusSerialTransport {

	private InputStream m_InputStream; // wrap into filter input
	private OutputStream m_OutputStream; // wrap into filter output

	private byte[] m_InBuffer;
	private BytesInputStream m_ByteIn; // to read message from
	private BytesOutputStream m_ByteInOut; // to buffer message to
	private BytesOutputStream m_ByteOut; // write frames
	private byte[] lastRequest = null;
	private boolean osIsKnown = false;
	private boolean osIsWindows = false;

	public ModbusTransaction createTransaction() {
		ModbusSerialTransaction transaction = new ModbusSerialTransaction();
		transaction.setTransport(this);

		return transaction;
	}

	public void writeMessage(ModbusMessage msg) throws ModbusIOException {
		try {
			int len;
			synchronized (m_ByteOut) {
				// first clear any input from the receive buffer to prepare
				// for the reply since RTU doesn't have message delimiters
				clearInput();
				// write message to byte out
				m_ByteOut.reset();
				msg.setHeadless();
				msg.writeTo(m_ByteOut);
				len = m_ByteOut.size();
				int[] crc = ModbusUtil.calculateCRC(m_ByteOut.getBuffer(), 0,
						len);
				m_ByteOut.writeByte(crc[0]);
				m_ByteOut.writeByte(crc[1]);
				// write message
				len = m_ByteOut.size();
				byte buf[] = m_ByteOut.getBuffer();
				m_OutputStream.write(buf, 0, len); // PDU + CRC
				m_OutputStream.flush();
				if (Modbus.debug)
					System.err
							.println("Sent: " + ModbusUtil.toHex(buf, 0, len));
				// clears out the echoed message
				// for RS485
				if (m_Echo) {
					readEcho(len);
				}
				lastRequest = new byte[len];
				System.arraycopy(buf, 0, lastRequest, 0, len);
			}
		} catch (Exception ex) {
			throw new ModbusIOException("I/O failed to write");
		}
	}

	/**
	 * readRequestData - Read the data for a request of a given, fixed size
	 * @throws IOException 
	 */
	private void readRequestData(int byteCount, byte inpBuf[], BytesOutputStream out) throws IOException {
		int remaining = byteCount + 2;
		int read = 0;
		int loopCount = 0;

		int tmOut = m_CommPort.getReceiveTimeout();
		long timedOut = System.currentTimeMillis() + tmOut;
		if (tmOut == 0) {
			try {
				m_CommPort.enableReceiveTimeout(250);
				timedOut += 250;
			} catch (UnsupportedCommOperationException e) {
				// Who cares ...
			}
		}
	
		/*
		 * Windows may have an issue when reading more than 16 bytes at
		 * a time.  The receive threshold will need to be changed as the
		 * number of remaining bytes declines.
		 */
		if (!osIsWindows)
			setReceiveThreshold(remaining);
		else
			setReceiveThreshold(remaining > 16 ? 16 : remaining);
		
		/*
		 * Read in the expected number of bytes.  loopCount is the
		 * number of consecutive reads with no data.  The loop will
		 * also end on a timeout.
		 */
		while (remaining > 0 && loopCount++ < 5) {
			int readCount = m_InputStream.read(inpBuf, 0, remaining);
			if (readCount > 0) {
				out.write(inpBuf, 0, readCount);
				read += readCount;
				remaining -= readCount;
				loopCount = 0;
				
				/*
				 * Determine if the remaining bytes to read is now below
				 * 16 for a Windows machine.
				 */
				if (osIsWindows && remaining > 0 && remaining < 16)
					setReceiveThreshold(remaining);
			}
			if (readCount == 0
					&& System.currentTimeMillis() > timedOut)
				break;

			if (remaining > 0 && ! (osIsWindows && readCount == 16))
				Thread.yield();
		}
		if (Modbus.debug && remaining > 0) {
			System.err.println("Error: looking for "
					+ (byteCount + 2) + " bytes, received " + read);
		}
		m_CommPort.disableReceiveThreshold();
	}
	
	/**
	 * getRequest - Read a request, after the unit and function code
	 * 
	 * @param function
	 *            - Modbus function code
	 * @param out
	 *            - Byte stream buffer to hold actual message
	 */
	private void getRequest(int function, BytesOutputStream out)
			throws IOException {
		int byteCount = -1;
		byte inpBuf[] = new byte[256];
	
		int tmOut = m_CommPort.getReceiveTimeout();
		if (tmOut == 0) {
			try {
				m_CommPort.enableReceiveTimeout(250);
			} catch (UnsupportedCommOperationException e) {
				// Who cares ...
			}
		}
	
		if (!osIsKnown) {
			String osName = System.getProperty("os.name");
			if (osName.toLowerCase().startsWith("win"))
				osIsWindows = true;
	
			osIsKnown = true;
		}
	
		try {
			if ((function & 0x80) == 0) {
				switch (function) {
				case Modbus.READ_EXCEPTION_STATUS:
				case Modbus.READ_COMM_EVENT_COUNTER:
				case Modbus.READ_COMM_EVENT_LOG:
				case Modbus.REPORT_SLAVE_ID:
					readRequestData(0, inpBuf, out);
					break;
				case Modbus.READ_FIFO_QUEUE:
					readRequestData(2, inpBuf, out);
					break;
				case Modbus.READ_MEI:
					readRequestData(3, inpBuf, out);
					break;
				case Modbus.READ_COILS:
				case Modbus.READ_INPUT_DISCRETES:
				case Modbus.READ_MULTIPLE_REGISTERS:
				case Modbus.READ_INPUT_REGISTERS:
				case Modbus.WRITE_COIL:
				case Modbus.WRITE_SINGLE_REGISTER:
					readRequestData(4, inpBuf, out);
					break;
				case Modbus.MASK_WRITE_REGISTER:
					readRequestData(6, inpBuf, out);
					break;
				case Modbus.READ_FILE_RECORD:
				case Modbus.WRITE_FILE_RECORD:
					byteCount = m_InputStream.read();
					out.write(byteCount);
					readRequestData(byteCount, inpBuf, out);
					break;
				case Modbus.WRITE_MULTIPLE_COILS:
				case Modbus.WRITE_MULTIPLE_REGISTERS:
					byteCount = m_InputStream.read(inpBuf, 0, 4);
					if (byteCount > 0)
						out.write(inpBuf, 0, byteCount);
					
					byteCount = m_InputStream.read();
					out.write(byteCount);
					readRequestData(byteCount, inpBuf, out);
					break;
				case Modbus.READ_WRITE_MULTIPLE:
					readRequestData(8, inpBuf, out);
					byteCount = m_InputStream.read();
					out.write(byteCount);
					readRequestData(byteCount, inpBuf, out);
					break;
				}
			}
		} catch (IOException e) {
			m_CommPort.disableReceiveThreshold();
			throw new IOException("getResponse serial port exception");
		}
		if (tmOut == 0)
			m_CommPort.disableReceiveTimeout();
	}

	/**
	 * readRequest - Read a slave request.
	 * 
	 *  @return a <tt>ModbusRequest</tt> to be processed by the slave simulator
	 */
	public ModbusRequest readRequest() throws ModbusIOException {
		ModbusCoupler coupler = ModbusCoupler.getReference();

		if (coupler == null || coupler.isMaster())
			throw new RuntimeException("Operation not supported.");

		boolean done = false;
		ModbusRequest request = null;
		int dlength = 0;

		try {
			do {
				// 1. read to function code, create request and read function
				// specific bytes
				synchronized (m_ByteIn) {
					int uid = m_InputStream.read();
					if (uid != -1) {
						int fc = m_InputStream.read();
						m_ByteInOut.reset();
						m_ByteInOut.writeByte(uid);
						m_ByteInOut.writeByte(fc);

						// create response to acquire length of message
						request = ModbusRequest.createModbusRequest(fc);
						request.setHeadless();

						/*
						 * With Modbus RTU, there is no end frame. Either we
						 * assume the message is complete as is or we must do
						 * function specific processing to know the correct
						 * length. To avoid moving frame timing to the serial
						 * input functions, we set the timeout and to message
						 * specific parsing to read a response.
						 */
						getRequest(fc, m_ByteInOut);
						dlength = m_ByteInOut.size() - 2; // less the crc
						if (Modbus.debug)
							System.err.println("Response: "
									+ ModbusUtil.toHex(m_ByteInOut.getBuffer(),
											0, dlength + 2));

						m_ByteIn.reset(m_InBuffer, dlength);

						// check CRC
						int[] crc = ModbusUtil.calculateCRC(m_InBuffer, 0,
								dlength); // does not include CRC
						if (ModbusUtil.unsignedByteToInt(m_InBuffer[dlength]) != crc[0]
								&& ModbusUtil
										.unsignedByteToInt(m_InBuffer[dlength + 1]) != crc[1]) {
							if (Modbus.debug)
								System.err.println("CRC should be " + crc[0]
										+ ", " + crc[1]);
							
							/*
							 * Drain the input in case the frame was misread and more
							 * was to follow.
							 */
							clearInput();
							
							throw new IOException(
									"CRC Error in received frame: "
											+ dlength
											+ " bytes: "
											+ ModbusUtil.toHex(
													m_ByteIn.getBuffer(), 0,
													dlength));
						}
					} else {
						throw new IOException("Error reading response");
					}

					// read response
					m_ByteIn.reset(m_InBuffer, dlength);
					if (request != null) {
						request.readFrom(m_ByteIn);
					}
					done = true;
				}
			} while (!done);
			return request;
		} catch (Exception ex) {
			/*
			 * An exception mostly means there is no request. The master should
			 * retry the request.
			 */
			return null;
		}
	}

	/**
	 * clearInput - Clear the input if characters are found in the input stream.
	 * 
	 * @throws ModbusIOException
	 */
	public void clearInput() throws IOException {
		if (m_InputStream.available() > 0) {
			int len = m_InputStream.available();
			byte buf[] = new byte[len];
			m_InputStream.read(buf, 0, len);
			if (Modbus.debug)
				System.err.println("Clear input: "
						+ ModbusUtil.toHex(buf, 0, len));
		}
	}

	/**
	 * getResponse - Read a <tt>ModbusResponse</tt> from a slave.
	 * 
	 * @param function
	 * @param out
	 * @throws IOException
	 */
	private void getResponse(int function, BytesOutputStream out)
			throws IOException {
		int byteCount = -1;
		int readCount = 0;
		byte inpBuf[] = new byte[256];

		int tmOut = m_CommPort.getReceiveTimeout();
		long timedOut = System.currentTimeMillis() + tmOut;
		if (tmOut == 0) {
			try {
				m_CommPort.enableReceiveTimeout(250);
				timedOut += 250;
			} catch (UnsupportedCommOperationException e) {
				// Who cares ...
			}
		}

		if (!osIsKnown) {
			String osName = System.getProperty("os.name");
			if (osName.toLowerCase().startsWith("win"))
				osIsWindows = true;

			osIsKnown = true;
		}

		try {
			if ((function & 0x80) == 0) {
				switch (function) {
				case Modbus.READ_COILS:
				case Modbus.READ_INPUT_DISCRETES:
				case Modbus.READ_MULTIPLE_REGISTERS:
				case Modbus.READ_INPUT_REGISTERS:
				case Modbus.READ_COMM_EVENT_LOG:
				case Modbus.REPORT_SLAVE_ID:
				case Modbus.READ_FILE_RECORD:
				case Modbus.WRITE_FILE_RECORD:
				case Modbus.READ_WRITE_MULTIPLE:
					/*
					 * Read the data payload byte count. There will be two
					 * additional CRC bytes afterwards.
					 */
					byteCount = m_InputStream.read();
					out.write(byteCount);

					int remaining = byteCount + 2;
					int read = 0;
					int loopCount = 0;
					// now get the specified number of bytes and the 2 CRC bytes
					while (remaining > 0 && loopCount++ < 5) {
						if (!osIsWindows)
							setReceiveThreshold(remaining);
						else
							setReceiveThreshold(remaining > 16 ? 16 : remaining);

						readCount = m_InputStream.read(inpBuf, 0, remaining);
						if (readCount > 0) {
							out.write(inpBuf, 0, readCount);
							read += readCount;
							remaining -= readCount;
							loopCount = 0;
						}
						if (readCount == 0
								&& System.currentTimeMillis() > timedOut)
							break;

						if (remaining > 0)
							Thread.yield();
					}
					if (Modbus.debug && remaining > 0) {
						System.err.println("Error: looking for "
								+ (byteCount + 2) + " bytes, received " + read);
					}
					m_CommPort.disableReceiveThreshold();
					break;
				case Modbus.WRITE_COIL:
				case Modbus.WRITE_SINGLE_REGISTER:
				case Modbus.READ_COMM_EVENT_COUNTER:
				case Modbus.WRITE_MULTIPLE_COILS:
				case Modbus.WRITE_MULTIPLE_REGISTERS:
				case Modbus.READ_SERIAL_DIAGNOSTICS:
					/*
					 * read status: only the CRC remains after the two data
					 * words.
					 */
					setReceiveThreshold(6);
					readCount = m_InputStream.read(inpBuf, 0, 6);
					out.write(inpBuf, 0, readCount);
					m_CommPort.disableReceiveThreshold();
					break;
				case Modbus.READ_EXCEPTION_STATUS:
					/*
					 * read status: only the CRC remains after exception status
					 * byte.
					 */
					setReceiveThreshold(3);
					readCount = m_InputStream.read(inpBuf, 0, 3);
					out.write(inpBuf, 0, readCount);
					m_CommPort.disableReceiveThreshold();
					break;
				case Modbus.MASK_WRITE_REGISTER:
					// eight bytes in addition to the address and function codes
					setReceiveThreshold(8);
					readCount = m_InputStream.read(inpBuf, 0, 8);
					out.write(inpBuf, 0, readCount);
					m_CommPort.disableReceiveThreshold();
					break;
				case Modbus.READ_FIFO_QUEUE:
					int b1,
					b2;
					b1 = (byte) (m_InputStream.read() & 0xFF);
					out.write(b1);
					b2 = (byte) (m_InputStream.read() & 0xFF);
					out.write(b2);

					byteCount = ModbusUtil.makeWord(b1, b2);

					/*
					 * now get the specified number of bytes and the 2 CRC bytes
					 */
					setReceiveThreshold(byteCount + 2);
					readCount = m_InputStream.read(inpBuf, 0, byteCount + 2);
					out.write(inpBuf, 0, readCount);
					m_CommPort.disableReceiveThreshold();
					break;
				case Modbus.READ_MEI:
					// read the subcode. We only support 0x0e.
					int sc = m_InputStream.read();
					if (sc != 0x0e)
						throw new IOException("Invalid subfunction code");

					out.write(sc);
					// next few bytes are just copied.
					setReceiveThreshold(5);
					int id,
					fieldCount;
					int cnt = m_InputStream.read(inpBuf, 0, 5);
					out.write(inpBuf, 0, cnt);
					id = (int) inpBuf[0];
					fieldCount = (int) inpBuf[4];
					for (int i = 0; i < fieldCount; i++) {
						setReceiveThreshold(1);
						id = m_InputStream.read();
						out.write(id);
						int len = m_InputStream.read();
						out.write(len);
						setReceiveThreshold(len);
						len = m_InputStream.read(inpBuf, 0, len);
						out.write(inpBuf, 0, len);
					}
					if (fieldCount == 0) {
						setReceiveThreshold(1);
						int err = m_InputStream.read();
						out.write(err);
					}
					// now get the 2 CRC bytes
					setReceiveThreshold(2);
					readCount = m_InputStream.read(inpBuf, 0, 2);
					out.write(inpBuf, 0, 2);
					m_CommPort.disableReceiveThreshold();
					m_CommPort.disableReceiveTimeout();
				}
			} else {
				// read the exception code, plus two CRC bytes.
				setReceiveThreshold(3);
				readCount = m_InputStream.read(inpBuf, 0, 3);
				out.write(inpBuf, 0, 3);
				m_CommPort.disableReceiveThreshold();

			}
		} catch (IOException e) {
			m_CommPort.disableReceiveThreshold();
			throw new IOException("getResponse serial port exception");
		}
		if (tmOut == 0)
			m_CommPort.disableReceiveTimeout();
	}

	/**
	 * readResponse - Read the bytes for the response from the slave.
	 * 
	 * @return a <tt>ModbusRespose</tt>
	 */
	public ModbusResponse readResponse() throws ModbusIOException {
		boolean done = false;
		ModbusResponse response = null;
		int dlength = 0;

		try {
			do {
				// 1. read to function code, create request and read function
				// specific bytes
				synchronized (m_ByteIn) {
					int uid = m_InputStream.read();
					if (uid != -1) {
						int fc = m_InputStream.read();
						m_ByteInOut.reset();
						m_ByteInOut.writeByte(uid);
						m_ByteInOut.writeByte(fc);

						// create response to acquire length of message
						response = ModbusResponse.createModbusResponse(fc);
						response.setHeadless();

						/*
						 * With Modbus RTU, there is no end frame. Either we
						 * assume the message is complete as is or we must do
						 * function specific processing to know the correct
						 * length. To avoid moving frame timing to the serial
						 * input functions, we set the timeout and to message
						 * specific parsing to read a response.
						 */
						getResponse(fc, m_ByteInOut);
						dlength = m_ByteInOut.size() - 2; // less the crc
						if (Modbus.debug)
							System.err.println("Response: "
									+ ModbusUtil.toHex(m_ByteInOut.getBuffer(),
											0, dlength + 2));

						m_ByteIn.reset(m_InBuffer, dlength);

						// check CRC
						int[] crc = ModbusUtil.calculateCRC(m_InBuffer, 0,
								dlength); // does not include CRC
						if (ModbusUtil.unsignedByteToInt(m_InBuffer[dlength]) != crc[0]
								&& ModbusUtil
										.unsignedByteToInt(m_InBuffer[dlength + 1]) != crc[1]) {
							if (Modbus.debug)
								System.err.println("CRC should be " + crc[0]
										+ ", " + crc[1]);
							throw new IOException(
									"CRC Error in received frame: "
											+ dlength
											+ " bytes: "
											+ ModbusUtil.toHex(
													m_ByteIn.getBuffer(), 0,
													dlength));
						}
					} else {
						throw new IOException("Error reading response");
					}

					// read response
					m_ByteIn.reset(m_InBuffer, dlength);
					if (response != null) {
						response.readFrom(m_ByteIn);
					}
					done = true;
				}
			} while (!done);
			return response;
		} catch (Exception ex) {
			if (Modbus.debug) {
				System.err.println("Last request: "
						+ ModbusUtil.toHex(lastRequest));
				System.err.println(ex.getMessage());
			}
			throw new ModbusIOException("I/O exception - failed to read");
		}
	}

	/**
	 * prepareStreams - Prepares the input and output streams of this
	 * <tt>ModbusRTUTransport</tt> instance.
	 * 
	 * @param in
	 *            the input stream to be read from.
	 * @param out
	 *            the output stream to write to.
	 * @throws IOException
	 *             if an I\O error occurs.
	 */
	public void prepareStreams(InputStream in, OutputStream out)
			throws IOException {
		m_InputStream = in; // new RTUInputStream(in);
		m_OutputStream = out;

		m_ByteOut = new BytesOutputStream(Modbus.MAX_MESSAGE_LENGTH);
		m_InBuffer = new byte[Modbus.MAX_MESSAGE_LENGTH];
		m_ByteIn = new BytesInputStream(m_InBuffer);
		m_ByteInOut = new BytesOutputStream(m_InBuffer);
	}

	public void close() throws IOException {
		m_InputStream.close();
		m_OutputStream.close();
	}
}
