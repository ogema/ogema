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
 * Copyright (c) 2010-2015, greenHouse Gas and Electric
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
 * Class implementing a <tt>ReadSerialDiagnosticsRequest</tt>.
 * 
 * @author Julie Haugh (jfh@ghgande.com)
 * 
 * @version @version@ (@date@)
 */
public final class ReadSerialDiagnosticsRequest extends ModbusRequest {

	/*
	 * Message fields.
	 */
	private int m_Function;
	private	short	m_Data;

	/**
	 * getFunction -- Get the DIAGNOSTICS sub-function.
	 * 
	 * @return
	 */
	public int getFunction() {
		return m_Function;
	}

	/**
	 * setFunction - Set the DIAGNOSTICS sub-function.
	 * 
	 * @param function - DIAGNOSTICS command sub-function.
	 */
	public void setFunction(int function) {
		m_Function = function;
		m_Data = 0;
	}
	
	/**
	 * getWordCount -- get the number of words in m_Data.
	 */
	public int getWordCount() {
		return 1;
	}
	
	/**
	 * getData -- return the first data item.
	 */
	public int getData() {
		return m_Data;
	}
	
	/**
	 * getData -- Get the data item at the index.
	 * 
	 * @param index - Unused, must be 0.
	 * 
	 * @deprecated
	 */
	public int getData(int index) {
		if (index != 0)
			throw new IndexOutOfBoundsException();
		
		return m_Data;
	}
	
	/**
	 * setData -- Set the optional data value
	 */
	public void setData(int value) {
		m_Data = (short) value;
	}

	/**
	 * setData -- Set the data item at the index
	 * 
	 * @param index - Unused, must be 0.
	 * @param value - Optional data value for function.
	 * 
	 * @deprecated
	 */
	public void setData(int index, int value) {
		if (index != 0)
			throw new IndexOutOfBoundsException();
		
		m_Data = (short) value;
	}

	/**
	 * createResponse -- create an empty response for this request.
	 */
	public ModbusResponse getResponse() {
		ReadSerialDiagnosticsResponse response = null;

		response = new ReadSerialDiagnosticsResponse();

		/*
		 * Copy any header data from the request.
		 */
		response.setHeadless(isHeadless());
		if (! isHeadless()) {
			response.setTransactionID(getTransactionID());
			response.setProtocolID(getProtocolID());
		}
		
		/*
		 * Copy the unit ID and function code.
		 */
		response.setUnitID(getUnitID());
		response.setFunctionCode(getFunctionCode());
		
		/*
		 * Copy the sub-function code.
		 */
		response.setFunction(getFunction());

		return response;
	}
	
	/**
	 * The ModbusCoupler doesn't have a means of reporting the slave
	 * state or ID information.
	 */
	public ModbusResponse createResponse() {
		return createExceptionResponse(Modbus.ILLEGAL_FUNCTION_EXCEPTION);
	}

	/**
	 * writeData -- output the completed Modbus message to dout
	 */
	public void writeData(DataOutput dout) throws IOException {
		dout.write(getMessage());
	}

	/**
	 * readData -- Read the function code and data value 
	 */
	public void readData(DataInput din) throws IOException {
		m_Function = din.readShort() & 0xFFFF;
		m_Data = (short) (din.readShort() & 0xFFFF);
	}

	/**
	 * getMessage -- Create the DIAGNOSTICS message paylaod.
	 */
	public byte[] getMessage() {
		byte result[] = new byte[4];

		result[0] = (byte) (m_Function >> 8);
		result[1] = (byte) (m_Function & 0xFF);
		result[2] = (byte) (m_Data >> 8);
		result[3] = (byte) (m_Data & 0xFF);

		return result;
	}

	/**
	 * Constructs a new <tt>Diagnostics</tt> request
	 * instance.
	 */
	public ReadSerialDiagnosticsRequest() {
		super();

		setFunctionCode(Modbus.READ_SERIAL_DIAGNOSTICS);
		setDataLength(4);
	}
}
