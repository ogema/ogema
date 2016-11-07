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
import java.io.IOException;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.procimg.IllegalAddressException;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.procimg.Register;


/**
 * Class implementing a <tt>Mask Write Register</tt> request.
 * 
 * @author Julie Haugh (jfh@ghgande.com)
 * @version jamod-1.2rc1-ghpc
 * 
 * @author jfhaugh (jfh@ghgande.com)
 * @version @version@ (@date@)
 */
public final class MaskWriteRegisterRequest extends ModbusRequest {
	private int m_Reference;
	private int m_AndMask;
	private int m_OrMask;

	/**
	 * getReference -- return the reference field.
	 */
	public int getReference() {
		return m_Reference;
	}
	
	/**
	 * setReference -- set the reference field.
	 */
	public void setReference(int ref) {
		m_Reference = ref;
	}
	
	/**
	 * getAndMask -- return the AND mask value;
	 * @return
	 */
	public int getAndMask() {
		return m_AndMask;
	}
	
	/**
	 * setAndMask -- set AND mask
	 */
	public void setAndMask(int mask) {
		m_AndMask = mask;
	}
	
	/**
	 * getOrMask -- return the OR mask value;
	 * @return
	 */
	public int getOrMask() {
		return m_OrMask;
	}
	
	/**
	 * setOrMask -- set OR mask
	 */
	public void setOrMask(int mask) {
		m_OrMask = mask;
	}
	
	/**
	 * getResponse -- create an empty response for this request.
	 */
	public ModbusResponse getResponse() {
		MaskWriteRegisterResponse response = null;

		response = new MaskWriteRegisterResponse();

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

		return response;
	}
	
	/**
	 * The ModbusCoupler doesn't have a means of reporting the slave
	 * state or ID information.
	 */
	public ModbusResponse createResponse() {
		MaskWriteRegisterResponse response = null;

		/*
		 * Get the process image.
		 */
		ProcessImage procimg = ModbusCoupler.getReference().getProcessImage();
		try {
			Register register = procimg.getRegister(m_Reference);
			
			/*
			 * Get the original value.  The AND mask will first be
			 * applied to clear any bits, then the OR mask will be
			 * applied to set them.
			 */
			int value = register.getValue();
			
			value = (value & m_AndMask) | (m_OrMask & ~m_AndMask);
			
			/*
			 * Store the modified value back where it came from.
			 */
			register.setValue(value);
		} catch (IllegalAddressException iaex) {
			return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
		}
		response = (MaskWriteRegisterResponse) getResponse();
		
		response.setReference(m_Reference);
		response.setAndMask(m_AndMask);
		response.setOrMask(m_OrMask);

		return response;
	}

	/**
	 * writeData -- output this Modbus message to dout.
	 */
	public void writeData(DataOutput dout) throws IOException {
		dout.write(getMessage());
	}

	/**
	 * readData -- dummy function.  There is no data with the request.
	 */
	public void readData(DataInput din) throws IOException {
		m_Reference = din.readShort();
		m_AndMask = din.readShort();
		m_OrMask = din.readShort();
	}

	/**
	 * getMessage -- return an empty array as there is no data for
	 * 		this request.
	 */
	public byte[] getMessage() {
		byte	results[] = new byte[6];
		
		results[0] = (byte) (m_Reference >> 8);
		results[1] = (byte) (m_Reference & 0xFF);
		results[2] = (byte) (m_AndMask >> 8);
		results[3] = (byte) (m_AndMask & 0xFF);
		results[4] = (byte) (m_OrMask >> 8);
		results[5] = (byte) (m_OrMask & 0xFF);

		return results;
	}
	
	/**
	 * Constructs a new <tt>Mask Write Register</tt> request.
	 * 
	 * @param ref
	 * @param andMask
	 * @param orMask
	 */
	public MaskWriteRegisterRequest(int ref, int andMask, int orMask) {
		super();
		
		setFunctionCode(Modbus.MASK_WRITE_REGISTER);
		setReference(ref);
		setAndMask(andMask);
		setOrMask(orMask);
		
		setDataLength(6);
	}

	/**
	 * Constructs a new <tt>Mask Write Register</tt> request.
	 * instance.
	 */
	public MaskWriteRegisterRequest() {
		super();
		
		setFunctionCode(Modbus.MASK_WRITE_REGISTER);
		setDataLength(6);
	}
}
