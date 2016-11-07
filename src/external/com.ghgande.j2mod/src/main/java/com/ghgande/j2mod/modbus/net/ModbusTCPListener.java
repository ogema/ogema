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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.util.ThreadPool;

/**
 * Class that implements a ModbusTCPListener.
 * 
 * <p>
 * If listening, it accepts incoming requests passing them on to be handled.
 * If not listening, silently drops the requests.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author Julie Haugh
 * @version 0.97 (8/11/2012)
 */
public class ModbusTCPListener implements ModbusListener {
	private ServerSocket m_ServerSocket = null;
	private ThreadPool m_ThreadPool;
	private Thread m_Listener;
	private int m_Port = Modbus.DEFAULT_PORT;
	private int m_Unit = 0;
	private int m_FloodProtection = 5;
	private boolean m_Listening;
	private InetAddress m_Address;

	/**
	 * Sets the port to be listened to.
	 * 
	 * @param port
	 *            the number of the IP port as <tt>int</tt>.
	 */
	public void setPort(int port) {
		m_Port = port;
	}
	
	/**
	 * Gets the unit number supported by this Modbus/TCP connection. A
	 * Modbus/TCP connection, by default, supports unit 0, but may also support
	 * a fixed unit number, or a range of unit numbers if the device is a
	 * Modbus/TCP gateway.  If the unit number is non-zero, all packets for
	 * any other unit number should be discarded.
	 * 
	 * @returns unit number supported by this interface.
	 */
	public int getUnit() {
		return m_Unit;
	}

	/**
	 * Sets the unit number to be listened for.  A Modbus/TCP connection, by
	 * default, supports unit 0, but may also support a fixed unit number, or a
	 * range of unit numbers if the device is a Modbus/TCP gateway.
	 * 
	 * @param unit
	 *            the number of the Modbus unit as <tt>int</tt>.
	 */
	public void setUnit(int unit) {
		m_Unit = unit;
	}

	/**
	 * Sets the address of the interface to be listened to.
	 * 
	 * @param addr
	 *            an <tt>InetAddress</tt> instance.
	 */
	public void setAddress(InetAddress addr) {
		m_Address = addr;
	}

	/**
	 * Starts this <tt>ModbusTCPListener</tt>.
	 * 
	 * @deprecated
	 */
	public void start() {
		m_Listening = true;
		
		m_Listener = new Thread(this);
		m_Listener.start();
	}

	/**
	 * Stops this <tt>ModbusTCPListener</tt>.
	 */
	public void stop() {
		m_Listening = false;
		try {
			m_ServerSocket.close();
			m_Listener.join();
		} catch (Exception ex) {
			// ?
		}
	}

	/**
	 * Accepts incoming connections and handles then with
	 * <tt>TCPConnectionHandler</tt> instances.
	 */
	public void run() {
		try {
			/*
			 * A server socket is opened with a connectivity queue of a size
			 * specified in int floodProtection. Concurrent login handling under
			 * normal circumstances should be allright, denial of service
			 * attacks via massive parallel program logins can probably be
			 * prevented.
			 */
			m_ServerSocket = new ServerSocket(m_Port, m_FloodProtection,
					m_Address);
			if (Modbus.debug)
				System.out.println("Listenening to "
						+ m_ServerSocket.toString() + "(Port " + m_Port + ")");

			/*
			 * Infinite loop, taking care of resources in case of a lot of
			 * parallel logins
			 */
			
			m_Listening = true;
			while (m_Listening) {
				Socket incoming = m_ServerSocket.accept();
				if (Modbus.debug)
					System.out.println("Making new connection "
							+ incoming.toString());

				if (m_Listening) {
					// FIXME: Replace with object pool due to resource issues
					m_ThreadPool.execute(new TCPConnectionHandler(
							new TCPSlaveConnection(incoming)));
				} else {
					incoming.close();
				}
			};
		} catch (SocketException iex) {
			if (! m_Listening) {
				return;
			} else {
				if (Modbus.debug)
					iex.printStackTrace();
			}
		} catch (IOException e) {
			// FIXME: this is a major failure, how do we handle this
		}
	}
	
	/**
	 * Set the listening state of this <tt>ModbusTCPListener</tt> object.
	 * A <tt>ModbusTCPListener</tt> will silently drop any requests if the
	 * listening state is set to <tt>false</tt>.
	 * 
	 * @param b
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
	 * Start the listener thread for this serial interface.
	 */
	public Thread listen() {
		m_Listening = true;
		Thread result = new Thread(this);
		result.start();
		
		return result;
	}

	/**
	 * Constructs a ModbusTCPListener instance.<br>
	 * 
	 * @param poolsize
	 *            the size of the <tt>ThreadPool</tt> used to handle incoming
	 *            requests.
	 * @param addr
	 *            the interface to use for listening.
	 */
	public ModbusTCPListener(int poolsize, InetAddress addr) {
		m_ThreadPool = new ThreadPool(poolsize);
		m_Address = addr;
	}

	/**
	/**
	 * Constructs a ModbusTCPListener instance.  This interface is created
	 * to listen on the wildcard address, which will accept TCP packets
	 * on all available interfaces.
	 * 
	 * @param poolsize
	 *            the size of the <tt>ThreadPool</tt> used to handle incoming
	 *            requests.
	 */
	public ModbusTCPListener(int poolsize) {
		m_ThreadPool = new ThreadPool(poolsize);
		try {
			/*
			 * TODO -- Check for an IPv6 interface and listen on that
			 * interface if it exists.
			 */
			m_Address = InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 } );
		} catch (UnknownHostException ex) {
			// Can't happen -- size is fixed.
		}
	}
}
