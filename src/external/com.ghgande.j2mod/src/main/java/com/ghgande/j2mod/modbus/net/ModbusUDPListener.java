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

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.ModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusUDPTransport;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;

/**
 * Class that implements a ModbusUDPListener.<br>
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author Julie Haugh
 * @version 0.97 (8/11/2012) Major code cleanup. Change to listen on wildcard
 *          address.
 */
public class ModbusUDPListener implements ModbusListener {
	private int m_Port = Modbus.DEFAULT_PORT;
	private boolean m_Listening = false;
	private boolean m_Continue = false;
	private InetAddress m_Interface;
	private UDPSlaveTerminal m_Terminal;
	private ModbusTransport m_Transport;
	private	int m_Unit = 0;

	public int getUnit() {
		return m_Unit;
	}
	
	public void setUnit(int unit) {
		m_Unit = unit;
	}
	
	/**
	 * Returns the number of the port this <tt>ModbusUDPListener</tt> is
	 * listening to.
	 * 
	 * @return the number of the IP port as <tt>int</tt>.
	 */
	public int getPort() {
		return m_Port;
	}

	/**
	 * Sets the number of the port this <tt>ModbusUDPListener</tt> is listening
	 * to.
	 * 
	 * @param port
	 *            the number of the IP port as <tt>int</tt>.
	 */
	public void setPort(int port) {
		m_Port = ((port > 0) ? port : Modbus.DEFAULT_PORT);
	}

	/**
	 * Starts this <tt>ModbusUDPListener</tt>.
	 */
	public void run() {
		try {
			if (m_Interface == null) {
				m_Terminal = new UDPSlaveTerminal(
						InetAddress.getByName("0.0.0.0"));
			} else {
				m_Terminal = new UDPSlaveTerminal(m_Interface);
			}
			m_Terminal.setLocalPort(m_Port);
			m_Terminal.activate();

			m_Transport = new ModbusUDPTransport(m_Terminal);
		} catch (Exception e) {
			/*
			 * TODO -- Make sure the methods in the try block are throwing
			 * reasonable exemptions and not just "Exception".
			 */
			if (Modbus.debug)
				e.printStackTrace();

			m_Listening = false;
			return;
		}

		m_Listening = true;
		m_Continue = true;

		try {
			while (m_Continue) {

				/*
				 * Get the request from the transport. It will be processed
				 * using an associated process image.
				 */
				ModbusRequest request = m_Transport.readRequest();
				ModbusResponse response = null;

				/*
				 * Make sure there is a process image to handle the request.
				 */
				if (ModbusCoupler.getReference().getProcessImage() == null) {
					response = request
							.createExceptionResponse(Modbus.ILLEGAL_FUNCTION_EXCEPTION);
				} else {
					response = request.createResponse();
				}
				/* DEBUG */
				if (Modbus.debug) {
					System.err.println("Request:" + request.getHexMessage());

					System.err.println("Response:" + response.getHexMessage());
				}
				m_Transport.writeMessage(response);
			}
		} catch (ModbusIOException ex) {
			if (!ex.isEOF()) {
				// FIXME: other troubles, output for debug
				ex.printStackTrace();
			}
		} finally {
			try {
				m_Terminal.deactivate();
				m_Transport.close();
			} catch (Exception ex) {
				// ignore
			}
		}
	}

	/**
	 * Stops this <tt>ModbusUDPListener</tt>.
	 */
	public void stop() {
		m_Terminal.deactivate();
		m_Listening = false;
		m_Continue = false;
	}

	/**
	 * Sets if this <tt>ModbusUDPListener</tt> is listening and accepting
	 * incoming connections.
	 * 
	 * @param listen
	 *            true if the <tt>ModbusUDPListener</tt> should listen, false
	 *            otherwise.
	 */
	public void setListening(boolean listen) {
		m_Listening = listen;
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
	 * Start the listener thread for this serial interface.
	 */
	public Thread listen() {
		m_Listening = true;
		Thread result = new Thread(this);
		result.start();
		
		return result;
	}

	/**
	 * Create a new <tt>ModbusUDPListener</tt> instance listening to the given
	 * interface address.
	 * 
	 * @param ifc
	 *            an <tt>InetAddress</tt> instance.
	 */
	public ModbusUDPListener(InetAddress ifc) {
		m_Interface = ifc;
		m_Listening = true;
	}

	/**
	 * Constructs a new ModbusUDPListener instance. The address will be set to a
	 * default value of the wildcard local address and the default Modbus port.
	 */
	public ModbusUDPListener() {
		try {
			m_Interface = InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 });
		} catch (UnknownHostException e) {
			// Can't happen -- length is fixed by code.
		}
	}
}
