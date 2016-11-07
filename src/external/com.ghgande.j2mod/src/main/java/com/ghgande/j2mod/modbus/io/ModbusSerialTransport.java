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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.msg.ModbusMessage;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.util.ModbusUtil;

import gnu.io.*;

/**
 * Abstract base class for serial <tt>ModbusTransport</tt>
 * implementations.
 *
 * @author Dieter Wimberger
 * @author John Charlton
 *
 * @version 1.2rc1 (09/11/2004)
 */
abstract public class ModbusSerialTransport
    implements ModbusTransport {
  protected CommPort  m_CommPort;
  protected boolean   m_Echo = false;     // require RS-485 echo processing

  /**
   * <code>prepareStreams</code> prepares the input and output streams of this
   * <tt>ModbusSerialTransport</tt> instance.
   *
   * @param in the input stream to be read from.
   * @param out the output stream to write to.
   * @throws IOException if an I\O error occurs.
   */
  abstract public void prepareStreams(InputStream in, OutputStream out)
      throws IOException;

  /**
   *  <code>readResponse</code> reads a response message from the slave
   *  responding to a master writeMessage request.
   *
   * @return a <code>ModbusResponse</code> value
   * @exception ModbusIOException if an error occurs
   */
  abstract public ModbusResponse readResponse()
      throws ModbusIOException;
  
  /**
   * The <code>readRequest</code> method listens continuously on the serial
   * input stream for master request messages and replies if the request slave
   * ID matches its own set in ModbusCoupler.getUnitID().
   *
   * @return a <code>ModbusRequest</code> value
   * @exception ModbusIOException if an error occurs
   */
  abstract public ModbusRequest readRequest()
    throws ModbusIOException;
  
  /**
   * The <code>writeMessage</code> method writes a modbus serial message to
   * its serial output stream to a specified slave unit ID.
   *
   * @param msg a <code>ModbusMessage</code> value
   * @exception ModbusIOException if an error occurs
   */
  abstract public void writeMessage(ModbusMessage msg)
    throws ModbusIOException;


  /**
   * The <code>close</code> method closes the serial input/output streams.
   *
   * @exception IOException if an error occurs
   */
  abstract public void close() throws IOException;
  
  /**
   * <code>setCommPort</code> sets the comm port member and prepares the input
   * and output streams to be used for reading from and writing to.
   *
   * @param cp the comm port to read from/write to.
   * @throws IOException if an I/O related error occurs.
   */
  public void setCommPort(CommPort cp) throws IOException {
    m_CommPort = cp;
    if (cp != null) {
      prepareStreams(cp.getInputStream(), cp.getOutputStream());
    }
  }
  
  /**
   * <code>isEcho</code> method returns the output echo state.
   *
   * @return a <code>boolean</code> value
   */
  public boolean isEcho() {
    return m_Echo;
  }//isEcho

  /**
   * <code>setEcho</code> method sets the output echo state.
   *
   * @param b a <code>boolean</code> value
   */
  public void setEcho(boolean b) {
    this.m_Echo = b;
  }//setEcho


  /**
   * Describe <code>setReceiveThreshold</code> method here.
   *
   * @param th an <code>int</code> value
   */
  public void setReceiveThreshold(int th) {
    try {
      m_CommPort.enableReceiveThreshold(th); /* chars */
    } catch (UnsupportedCommOperationException e) {
      System.out.println(e.getMessage());
    }
  }
  
  /**
   * Describe <code>setReceiveTimeout</code> method here.
   *
   * @param ms an <code>int</code> value
   */
  public void setReceiveTimeout(int ms) {
    try {
      m_CommPort.enableReceiveTimeout(ms); /* milliseconds */
      
      int thresh = m_CommPort.getReceiveThreshold();
      if (thresh <= 0)
    	  m_CommPort.enableReceiveThreshold(2);
    } catch (UnsupportedCommOperationException e) {
      System.out.println(e.getMessage());
    }
  }
  
  /**
   * <code>setBaudRate</code> - Change the serial port baud rate
   * 
   * @param baud - an <code>int</code> value
   */
 public void setBaudRate(int baud) {
		try {
			SerialPort physicalPort = null;
			int	stop;
			int data;
			int parity;
			
			if (!(m_CommPort instanceof SerialPort))
				throw new UnsupportedCommOperationException(
						"Cannot change baud rate on non-serial device.");
			
			physicalPort = (SerialPort) m_CommPort;
			stop = physicalPort.getStopBits();
			data = physicalPort.getDataBits();
			parity = physicalPort.getParity();
			
			physicalPort.setSerialPortParams(baud, data, stop, parity);
			
			if (Modbus.debug)
				System.err.println("baud rate is now " + physicalPort.getBaudRate());
		} catch (UnsupportedCommOperationException x) {
			System.out.println(x.getMessage());
		}
 }

  /**
   * Reads the own message echo produced in RS485 Echo Mode
   * within the given time frame.
   *
   * @param len is the length of the echo to read.  Timeout will occur if the
   * echo is not received in the time specified in the SerialConnection.
   *
   * @throws IOException if a I/O error occurred.
   */
  public void readEcho(int len) throws IOException {

    byte echoBuf[] = new byte[len];
    setReceiveThreshold(len);
    int echoLen = m_CommPort.getInputStream().read(echoBuf, 0, len);
    if (Modbus.debug)
      System.out.println("Echo: " +
                         ModbusUtil.toHex(echoBuf, 0, echoLen));
    m_CommPort.disableReceiveThreshold();
    if (echoLen != len) {
      if (Modbus.debug)
        System.err.println("Error: Transmit echo not received.");
      throw new IOException("Echo not received.");
    }
  }//readEcho

  
}//interface ModbusSerialTransport
