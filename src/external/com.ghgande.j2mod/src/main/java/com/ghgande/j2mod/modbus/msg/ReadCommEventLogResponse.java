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
 * Copyright (c) 2010-2012, greenHouse Gas and Electric
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
package com.ghgande.j2mod.modbus.msg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.ghgande.j2mod.modbus.Modbus;

/**
 * Class implementing a <tt>ReadCommEventCounterResponse</tt>.
 * 
 * @author Julie Haugh (jfh@ghgande.com)
 * 
 * @version @version@ (@date@)
 */
public final class ReadCommEventLogResponse extends ModbusResponse {

	/*
	 * Message fields.
	 */
	private int m_ByteCount;
	private int m_Status;
	private int m_EventCount;
	private int m_MessageCount;
	private byte[] m_Events;

	/**
	 * getStatus -- get the device's status.
	 * 
	 * @return
	 */
	public int getStatus() {
		return m_Status;
	}

	/**
	 * setStatus -- set the device's status.
	 * 
	 * @param status
	 */
	public void setStatus(int status) {
		m_Status = status;
	}

	/**
	 * getEvents -- get device's event counter.
	 */
	public int getEventCount() {
		return m_EventCount;
	}

	/**
	 * setEventCount -- set the device's event counter.
	 */
	public void setEventCount(int count) {
		m_EventCount = count;
	}
	
	/**
	 * getMessageCount -- get device's message counter.
	 */
	public int getMessageCount() {
		return m_MessageCount;
	}
	
	/**
	 * setMessageCount -- set device's message counter.
	 */
	public void setMessageCount(int count) {
		m_MessageCount = count;
	}

	/**
	 * getEvent -- get an event from the event log.
	 */
	public int getEvent(int index) {
		if (m_Events == null || index < 0 || index >= m_Events.length)
			throw new IndexOutOfBoundsException("index = " + index
					+ ", limit = " + m_Events.length);
		
		return m_Events[index] & 0xFF;
	}
	
	public byte[] getEvents() {
		if (m_Events == null)
			return null;
		
		byte[] result = new byte[m_Events.length];
		System.arraycopy(m_Events, 0, result, 0, m_Events.length);
		
		return result;
	}
	
	/**
	 * setEvent -- store an event number in the event log
	 */
	public void setEvent(int index, int event) {
		if (m_Events == null || index < 0 || index >= m_Events.length)
			throw new IndexOutOfBoundsException("index = " + index
					+ ", limit = " + m_Events.length);
		
		m_Events[index] = (byte) event;	
	}
	
	public void setEvents(byte[] events) {
		if (events.length > 64)
			throw new IllegalArgumentException("events list too big (> 64 bytes)");

		m_Events = new byte[events.length];
		if (m_Events.length > 0)
			System.arraycopy(events, 0, m_Events, 0, events.length);
	}
	
	public void setEvents(int count) {
		if (count < 0 || count > 64)
			throw new IllegalArgumentException("invalid event list size (0 <= count <= 64)");
		
		m_Events = new byte[count];
	}

	/**
	 * writeData -- output the completed Modbus message to dout
	 */
	public void writeData(DataOutput dout) throws IOException {
		dout.write(getMessage());
	}

	/**
	 * readData -- input the Modbus message from din. If there was a header,
	 * such as for Modbus/TCP, it will have been read already.
	 */
	public void readData(DataInput din) throws IOException {
		m_ByteCount = din.readByte();
		m_Status = din.readShort();
		m_EventCount = din.readShort();
		m_MessageCount = din.readShort();
		
		m_Events = new byte[m_ByteCount - 6];
		
		if (m_Events.length > 0)
			din.readFully(m_Events, 0, m_Events.length);
	}

	/**
	 * getMessage -- format the message into a byte array.
	 */
	public byte[] getMessage() {
		byte result[] = new byte[m_Events.length + 7];

		result[0] = (byte) (m_ByteCount = m_Events.length + 6);
		result[1] = (byte) (m_Status >> 8);
		result[2] = (byte) (m_Status & 0xFF);
		result[3] = (byte) (m_EventCount >> 8);
		result[4] = (byte) (m_EventCount & 0xFF);
		result[5] = (byte) (m_MessageCount >> 8);
		result[6] = (byte) (m_MessageCount & 0xFF);
		
		for (int i = 0;i < m_Events.length;i++)
			result[7 + i] = m_Events[i];

		return result;
	}
	
	/**
	 * Constructs a new <tt>ReadCommEventLogResponse</tt> instance.
	 */
	public ReadCommEventLogResponse() {
		super();

		setFunctionCode(Modbus.READ_COMM_EVENT_LOG);
		setDataLength(7);
	}
}
