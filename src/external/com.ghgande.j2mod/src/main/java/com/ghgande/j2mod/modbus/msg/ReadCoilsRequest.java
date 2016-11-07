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
package com.ghgande.j2mod.modbus.msg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.procimg.DigitalOut;
import com.ghgande.j2mod.modbus.procimg.IllegalAddressException;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;

/**
 * Class implementing a <tt>ReadCoilsRequest</tt>. The implementation directly
 * correlates with the class 1 function <i>read coils (FC 1)</i>. It
 * encapsulates the corresponding request message.
 * 
 * <p>
 * Coils are understood as bits that can be manipulated (i.e. set or unset).
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author jfhaugh
 * @version @version@ (@date@)
 */
public final class ReadCoilsRequest extends ModbusRequest {

	// instance attributes
	private int m_Reference;
	private int m_BitCount;

	/**
	 * Constructs a new <tt>ReadCoilsRequest</tt> instance.
	 */
	public ReadCoilsRequest() {
		super();
		
		setFunctionCode(Modbus.READ_COILS);
		setDataLength(4);
	}

	/**
	 * Constructs a new <tt>ReadCoilsRequest</tt> instance with a given
	 * reference and count of coils (i.e. bits) to be read.
	 * <p>
	 * 
	 * @param ref
	 *            the reference number of the register to read from.
	 * @param count
	 *            the number of bits to be read.
	 */
	public ReadCoilsRequest(int ref, int count) {
		super();
		
		setFunctionCode(Modbus.READ_COILS);
		setDataLength(4);
		
		setReference(ref);
		setBitCount(count);
	}

	public ReadCoilsResponse getResponse() {
		ReadCoilsResponse response = null;
		response = new ReadCoilsResponse(m_BitCount);

		// transfer header data
		if (!isHeadless()) {
			response.setTransactionID(getTransactionID());
			response.setProtocolID(getProtocolID());
		} else {
			response.setHeadless();
		}
		response.setUnitID(getUnitID());

		return response;
	}
	
	public ModbusResponse createResponse() {
		ModbusResponse response = null;
		DigitalOut[] douts = null;

		// 1. get process image
		ProcessImage procimg = ModbusCoupler.getReference().getProcessImage();
		// 2. get input discretes range
		try {
			douts = procimg.getDigitalOutRange(getReference(),
					getBitCount());
		} catch (IllegalAddressException e) {
			response = new IllegalAddressExceptionResponse();
			response.setUnitID(getUnitID());
			response.setFunctionCode(getFunctionCode());
			
			return response;
		}
		response = getResponse();
		
		/*
		 * Populate the discrete values from the process image.
		 */
		for (int i = 0; i < douts.length; i++)
			((ReadCoilsResponse) response).setCoilStatus(i, douts[i].isSet());

		return response;		
	}

	/**
	 * Sets the reference of the register to start reading from with this
	 * <tt>ReadCoilsRequest</tt>.
	 * <p>
	 * 
	 * @param ref
	 *            the reference of the register to start reading from.
	 */
	public void setReference(int ref) {
		m_Reference = ref;
	}

	/**
	 * Returns the reference of the register to to start reading from with this
	 * <tt>ReadCoilsRequest</tt>.
	 * <p>
	 * 
	 * @return the reference of the register to start reading from as
	 *         <tt>int</tt>.
	 */
	public int getReference() {
		return m_Reference;
	}

	/**
	 * Sets the number of bits (i.e. coils) to be read with this
	 * <tt>ReadCoilsRequest</tt>.
	 * <p>
	 * 
	 * @param count
	 *            the number of bits to be read.
	 */
	public void setBitCount(int count) {
		if (count > Modbus.MAX_BITS) {
			throw new IllegalArgumentException("Maximum bitcount exceeded.");
		} else {
			m_BitCount = count;
		}
	}

	/**
	 * Returns the number of bits (i.e. coils) to be read with this
	 * <tt>ReadCoilsRequest</tt>.
	 * <p>
	 * 
	 * @return the number of bits to be read.
	 */
	public int getBitCount() {
		return m_BitCount;
	}

	public void writeData(DataOutput dout) throws IOException {
		dout.write(getMessage());
	}

	public void readData(DataInput din) throws IOException {
		m_Reference = din.readUnsignedShort();
		m_BitCount = din.readUnsignedShort();
	}

	public byte[] getMessage() {
		byte result[] = new byte[4];

		result[0] = (byte) ((m_Reference >> 8) & 0xff);
		result[1] = (byte) ((m_Reference & 0xff));
		result[2] = (byte) ((m_BitCount >> 8) & 0xff);
		result[3] = (byte) ((m_BitCount & 0xff));

		return result;
	}
}
