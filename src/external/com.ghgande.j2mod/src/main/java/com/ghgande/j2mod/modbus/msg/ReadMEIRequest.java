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
 * Java Modbus Library (jamod)
 * Copyright 2010, greenHouse Computers, LLC
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
import java.io.EOFException;
import java.io.IOException;

import com.ghgande.j2mod.modbus.Modbus;

/**
 * Class implementing a <tt>Read MEI Data</tt> request.
 * 
 * @author jfhaugh (jfh@ghgande.com)
 * @version jamod-1.2rc1-ghpc
 * 
 * @version @version@ (@date@)
 */
public final class ReadMEIRequest extends ModbusRequest {

	// instance attributes
	private int m_SubCode;
	private int m_FieldLevel;
	private int m_FieldId;

	/**
	 * Constructs a new <tt>Read MEI Data request</tt> instance.
	 */
	public ReadMEIRequest() {
		super();

		setFunctionCode(Modbus.READ_MEI);
		m_SubCode = 0x0E;

		// 3 bytes (unit id and function code is excluded)
		setDataLength(3);
	}

	/**
	 * Constructs a new <tt>Read MEI Data request</tt> instance with a given
	 * reference and count of coils (i.e. bits) to be read.
	 * <p>
	 * 
	 * @param ref
	 *            the reference number of the register to read from.
	 * @param count
	 *            the number of bits to be read.
	 */
	public ReadMEIRequest(int level, int id) {
		super();

		setFunctionCode(Modbus.READ_MEI);
		m_SubCode = 0x0E;
		
		// 3 bytes (unit id and function code is excluded)
		setDataLength(3);
		setLevel(level);
		setFieldId(id);
	}

	public ModbusResponse getResponse() {
		ReadMEIResponse response = null;
		
		/*
		 * Any other sub-function is an error.
		 */
		if (getSubCode() != 0x0E) {
			IllegalFunctionExceptionResponse error =
					new IllegalFunctionExceptionResponse();
			
			error.setUnitID(getUnitID());
			error.setFunctionCode(getFunctionCode());
			
			return error;
		}

		response = new ReadMEIResponse();

		// transfer header data
		if (! isHeadless()) {
			response.setTransactionID(getTransactionID());
			response.setProtocolID(getProtocolID());
		} else {
			response.setHeadless();
		}
		response.setUnitID(getUnitID());
		response.setFunctionCode(Modbus.READ_MEI);

		return response;
	}

	/**
	 * The ModbusCoupler interface doesn't have a method for defining MEI for a
	 * device.
	 */
	public ModbusResponse createResponse() {
		return createExceptionResponse(Modbus.ILLEGAL_FUNCTION_EXCEPTION);
	}

	/**
	 * Gets the MEI subcode associated with this request.
	 */
	public int getSubCode() {
		return m_SubCode;
	}

	/**
	 * Sets the reference of the register to start reading from with this
	 * <tt>ReadCoilsRequest</tt>.
	 * <p>
	 * 
	 * @param ref
	 *            the reference of the register to start reading from.
	 */
	public void setLevel(int level) {
		m_FieldLevel = level;
	}

	/**
	 * Returns the reference of the register to to start reading from with this
	 * <tt>ReadCoilsRequest</tt>.
	 * <p>
	 * 
	 * @return the reference of the register to start reading from as
	 *         <tt>int</tt>.
	 */
	public int getLevel() {
		return m_FieldLevel;
	}

	/**
	 * Sets the number of bits (i.e. coils) to be read with this
	 * <tt>ReadCoilsRequest</tt>.
	 * <p>
	 * 
	 * @param count
	 *            the number of bits to be read.
	 */
	public void setFieldId(int id) {
		m_FieldId = id;
	}

	/**
	 * Returns the number of bits (i.e. coils) to be read with this
	 * <tt>ReadCoilsRequest</tt>.
	 * <p>
	 * 
	 * @return the number of bits to be read.
	 */
	public int getFieldId() {
		return m_FieldId;
	}

	public void writeData(DataOutput dout) throws IOException {
		byte results[] = new byte[3];

		results[0] = (byte) m_SubCode;
		results[1] = (byte) m_FieldLevel;
		results[2] = (byte) m_FieldId;

		dout.write(results);
	}

	public void readData(DataInput din) throws IOException {
		m_SubCode = din.readUnsignedByte();

		if (m_SubCode != 0xE) {
			try {
				while (din.readByte() >= 0)
					;
			} catch (EOFException x) {
				// do nothing.
			}
			return;
		}
		m_FieldLevel = din.readUnsignedByte();
		m_FieldId = din.readUnsignedByte();
	}

	public byte[] getMessage() {
		byte results[] = new byte[3];

		results[0] = (byte) m_SubCode;
		results[1] = (byte) m_FieldLevel;
		results[2] = (byte) m_FieldId;

		return results;
	}
}