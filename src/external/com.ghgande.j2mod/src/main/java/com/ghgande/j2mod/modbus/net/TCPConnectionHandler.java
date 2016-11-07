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
package com.ghgande.j2mod.modbus.net;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.ModbusTransport;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;

/**
 * Class implementing a handler for incoming Modbus/TCP requests.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class TCPConnectionHandler implements Runnable {
	private TCPSlaveConnection m_Connection;
	private ModbusTransport m_Transport;

	/**
	 * Constructs a new <tt>TCPConnectionHandler</tt> instance.
	 * 
	 * <p>
	 * The connections will be handling using the <tt>ModbusCouple</tt> class
	 * and a <tt>ProcessImage</tt> which provides the interface between the
	 * slave implementation and the <tt>TCPSlaveConnection</tt>.
	 * 
	 * @param con
	 *            an incoming connection.
	 */
	public TCPConnectionHandler(TCPSlaveConnection con) {
		setConnection(con);
	}

	/**
	 * Sets a connection to be handled by this <tt>
	 * TCPConnectionHandler</tt>.
	 * 
	 * @param con
	 *            a <tt>TCPSlaveConnection</tt>.
	 */
	public void setConnection(TCPSlaveConnection con) {
		m_Connection = con;
		m_Transport = m_Connection.getModbusTransport();
	}

	public void run() {
		try {
			do {
				// 1. read the request
				ModbusRequest request = m_Transport.readRequest();
				ModbusResponse response = null;

				/*
				 * test if Process image exists.
				 */
				ProcessImage image = ModbusCoupler.getReference()
						.getProcessImage();
				if (image == null) {
					/*
					 * Do nothing -- non-existent devices do not respond to
					 * messages.
					 */
					continue;
				}
				if (image.getUnitID() != 0
						&& request.getUnitID() != image.getUnitID()) {
					/*
					 * Do nothing -- non-existent units do not respond to
					 * message.
					 */
					continue;
				}

				// 2. create the response.
				response = request.createResponse();

				if (Modbus.debug) {
					System.out.println("Request:" + request.getHexMessage());
					System.out.println("Response:" + response.getHexMessage());
				}

				// 3. write the response message.
				m_Transport.writeMessage(response);
			} while (true);
		} catch (ModbusIOException ex) {
			if (!ex.isEOF() && Modbus.debug)
				ex.printStackTrace();
		} finally {
			try {
				m_Connection.close();
			} catch (Exception ex) {
				// ignore
			}
		}
	}
}