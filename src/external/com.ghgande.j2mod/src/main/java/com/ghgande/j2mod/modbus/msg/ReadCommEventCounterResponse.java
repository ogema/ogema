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
public final class ReadCommEventCounterResponse extends ModbusResponse {

	/*
	 * Message fields.
	 */
	private int m_Status;
	private int m_Events;

	/**
	 * Constructs a new <tt>ReportSlaveIDResponse</tt> instance.
	 */
	public ReadCommEventCounterResponse() {
		super();

		setFunctionCode(Modbus.READ_COMM_EVENT_COUNTER);
		setDataLength(4);
	}

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
		if (status != 0 && status != 0xFFFF)
			throw new IllegalArgumentException("Illegal status value: " + status);
		
		m_Status = status;
	}

	/**
	 * getEvents -- get device's event counter.
	 */
	public int getEventCount() {
		return m_Events;
	}

	/**
	 * setEvents -- set the device's event counter.
	 */
	public void setEventCount(int count) {
		m_Events = count;
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
		m_Status = din.readShort();
		m_Events = din.readShort();
	}

	/**
	 * getMessage -- format the message into a byte array.
	 */
	public byte[] getMessage() {
		byte result[] = new byte[4];

		result[0] = (byte) (m_Status >> 8);
		result[1] = (byte) (m_Status & 0xFF);
		result[2] = (byte) (m_Events >> 8);
		result[3] = (byte) (m_Events & 0xFF);

		return result;
	}
}
