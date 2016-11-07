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
import com.ghgande.j2mod.modbus.procimg.DigitalIn;
import com.ghgande.j2mod.modbus.procimg.IllegalAddressException;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;

/**
 * Class implementing a <tt>ReadInputDiscretesRequest</tt>. The implementation
 * directly correlates with the class 1 function <i>read input discretes (FC
 * 2)</i>. It encapsulates the corresponding request message.
 * <p>
 * Input Discretes are understood as bits that cannot be manipulated (i.e. set
 * or unset).
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author jfhaugh
 * @version @version@ (@date@)
 */
public final class ReadInputDiscretesRequest extends ModbusRequest {

	// instance attributes
	private int m_Reference;
	private int m_BitCount;

	/**
	 * Constructs a new <tt>ReadInputDiscretesRequest</tt> instance.
	 */
	public ReadInputDiscretesRequest() {
		super();
		
		setFunctionCode(Modbus.READ_INPUT_DISCRETES);
		
		/*
		 * Two bytes for count, two bytes for offset.
		 */
		setDataLength(4);
	}

	/**
	 * Constructs a new <tt>ReadInputDiscretesRequest</tt> instance with a given
	 * reference and count of input discretes (i.e. bits) to be read.
	 * <p>
	 * 
	 * @param ref
	 *            the reference number of the register to read from.
	 * @param count
	 *            the number of bits to be read.
	 */
	public ReadInputDiscretesRequest(int ref, int count) {
		super();
		
		setFunctionCode(Modbus.READ_INPUT_DISCRETES);
		// 4 bytes (unit id and function code is excluded)
		setDataLength(4);
		setReference(ref);
		setBitCount(count);
	}// constructor

	/**
	 * Constructs a response to match this request.
	 * 
	 * <p>Used by slave implementations to construct the appropriate
	 * response.
	 * 
	 * @return
	 */
	public ReadInputDiscretesResponse getResponse() {
		ReadInputDiscretesResponse response =
				new ReadInputDiscretesResponse(getBitCount());
		
		response.setUnitID(getUnitID());
		response.setFunctionCode(getFunctionCode());
		
		response.setHeadless(isHeadless());
		if (! isHeadless()) {
			response.setTransactionID(getTransactionID());
			response.setProtocolID(getProtocolID());
		}
		return response;
	}

	public ModbusResponse createResponse() {
		ReadInputDiscretesResponse response = null;
		DigitalIn[] dins = null;

		// 1. get process image
		ProcessImage procimg = ModbusCoupler.getReference().getProcessImage();
		// 2. get input discretes range
		try {
			dins = procimg.getDigitalInRange(getReference(),
					getBitCount());
		} catch (IllegalAddressException e) {
			return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
		}
		response = getResponse();
		
		/*
		 * Populate the discrete values from the process image.
		 */
		for (int i = 0; i < dins.length; i++)
			response.setDiscreteStatus(i, dins[i].isSet());

		return response;
	}

	/**
	 * Sets the reference of the register to start reading from with this
	 * <tt>ReadInputDiscretesRequest</tt>.
	 * <p>
	 * 
	 * @param ref
	 *            the reference of the register to start reading from.
	 */
	public void setReference(int ref) {
		if (ref < 0 || m_BitCount + ref >= 65536)
			throw new IllegalArgumentException();
		
		m_Reference = ref;
	}

	/**
	 * Returns the reference of the discrete to to start reading from with
	 * this <tt>ReadInputDiscretesRequest</tt>.
	 * 
	 * @return the reference of the discrete to start reading from as
	 *         <tt>int</tt>.
	 */
	public int getReference() {
		return m_Reference;
	}

	/**
	 * Sets the number of bits (i.e. input discretes) to be read with this
	 * <tt>ReadInputDiscretesRequest</tt>.
	 * 
	 * @param count
	 *            the number of bits to be read.
	 */
	public void setBitCount(int count) {
		if (count < 0 || count > 2000 || count + m_Reference >= 65536)
			throw new IllegalArgumentException();
		
		m_BitCount = count;
	}

	/**
	 * Returns the number of bits (i.e. input discretes) to be read with this
	 * <tt>ReadInputDiscretesRequest</tt>.
	 * <p>
	 * 
	 * @return the number of bits to be read.
	 */
	public int getBitCount() {
		return m_BitCount;
	}

	public void writeData(DataOutput dout) throws IOException {
		dout.writeShort(m_Reference);
		dout.writeShort(m_BitCount);
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
