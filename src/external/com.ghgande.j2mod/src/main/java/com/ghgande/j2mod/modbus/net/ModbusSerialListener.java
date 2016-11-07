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
/***
 * Java Modbus Library (j2mod)
 * Copyright 2012, Julianne Frances Haugh
 * d/b/a greenHouse Gas and Electric
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
package com.ghgande.j2mod.modbus.net;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.ModbusTransport;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.util.SerialParameters;

/**
 * Class that implements a ModbusSerialListener.<br>
 * If listening, it accepts incoming requests passing them on to be handled.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author Julie Haugh Code cleanup in prep to refactor with ModbusListener
 *         interface
 */
public class ModbusSerialListener implements ModbusListener {
	private boolean m_Listening;
	private boolean m_Running = true;
	private SerialConnection m_SerialCon;
	private int m_Unit = 0;

	/**
	 * run
	 * 
	 * Listen for incoming messages and process.
	 */
	public void run() {
		try {
			m_Listening = true;
			m_SerialCon.open();

			ModbusTransport transport = m_SerialCon.getModbusTransport();

			while (m_Running) {
				if (m_Listening) {
					try {

						/*
						 * Read the request from the serial interface. If this
						 * instance has been assigned a unit number, it must be
						 * enforced.
						 */
						ModbusRequest request = transport.readRequest();
						if (request == null)
							continue;
						
						if (m_Unit != 0 && m_Unit != request.getUnitID())
							continue;

						/*
						 * Create the response using a ProcessImage. A Modbus
						 * ILLEGAL FUNCTION exception will be thrown if there is
						 * no ProcessImage.
						 */
						ModbusResponse response = null;
						if (ModbusCoupler.getReference().getProcessImage() == null) {
							response = request
									.createExceptionResponse(Modbus.ILLEGAL_FUNCTION_EXCEPTION);
						} else {
							response = request.createResponse();
						}

						/*
						 * Log the Request and Response messages.
						 */
						try {
							if (Modbus.debug) {
								System.out.println("Request (" + request.getClass().getName() + "): "
										+ request.getHexMessage());

								System.out.println("Response (" + response.getClass().getName() + "): "
										+ response.getHexMessage());
							}
						} catch (RuntimeException x) {
							// Ignore.
						}

						/*
						 * Write the response.
						 */
						transport.writeMessage(response);
					} catch (ModbusIOException ex) {
						if (Modbus.debug)
							ex.printStackTrace();

						continue;
					}
				} else {
					/*
					 * Not listening -- read and discard the request so the
					 * input doesn't get clogged up.
					 */
					transport.readRequest();
				}
			}
		} catch (Exception e) {
			/*
			 * TODO -- Make sure methods are throwing reasonable exceptions, and
			 * not just throwing "Exception".
			 */
			e.printStackTrace();
		} finally {
			m_Listening = false;

			if (m_SerialCon != null) {
				m_SerialCon.close();
			}
		}
	}

	/**
	 * Sets the Modbus unit number for this <tt>ModbusSerialListener</tt>
	 * 
	 * @param unit
	 *            Modbus unit number
	 */
	public void setUnit(int unit) {
		m_Unit = unit;
	}

	/**
	 * Gets the Modbus unit number for this <tt>ModbusSerialListener</tt>
	 * 
	 * @returns Modbus unit number
	 */
	public int getUnit() {
		return m_Unit;
	}

	/**
	 * Sets the listening flag of this <tt>ModbusTCPListener</tt>.
	 * 
	 * @param b
	 *            true if listening (and accepting incoming connections), false
	 *            otherwise.
	 */
	public void setListening(boolean b) {
		m_Listening = b;
	}

	/**
	 * Tests if this <tt>ModbusTCPListener</tt> is listening and accepting
	 * incoming connections.
	 * 
	 * @return true if listening (and accepting incoming connections), false
	 *         otherwise.
	 */
	public boolean isListening() {
		return m_Listening;
	}

	/**
	 * Stops this interface.
	 */
	public void stop() {
		m_Listening = false;
		m_Running = false;
	}
	
	/**
	 * Start the listener thread for this serial interface.
	 */
	public Thread listen() {
		m_Listening = true;
		Thread result = new Thread(this);
		result.start();
		
		return result;
	}

	/**
	 * Constructs a new <tt>ModbusSerialListener</tt> instance.
	 * 
	 * @param params
	 *            a <tt>SerialParameters</tt> instance.
	 */
	public ModbusSerialListener(SerialParameters params) {
		m_SerialCon = new SerialConnection(params);
	}
}
