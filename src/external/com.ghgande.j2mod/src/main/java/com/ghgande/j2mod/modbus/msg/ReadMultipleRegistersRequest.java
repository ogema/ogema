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
import com.ghgande.j2mod.modbus.procimg.IllegalAddressException;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.procimg.Register;

/**
 * Class implementing a <tt>ReadMultipleRegistersRequest</tt>. The
 * implementation directly correlates with the class 0 function <i>read multiple
 * registers (FC 3)</i>. It encapsulates the corresponding request message.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public final class ReadMultipleRegistersRequest extends ModbusRequest {

	// instance attributes
	private int m_Reference;
	private int m_WordCount;

	/**
	 * Constructs a new <tt>ReadMultipleRegistersRequest</tt> instance.
	 */
	public ReadMultipleRegistersRequest() {
		super();

		setFunctionCode(Modbus.READ_MULTIPLE_REGISTERS);
		setDataLength(4);
	}

	/**
	 * Constructs a new <tt>ReadMultipleRegistersRequest</tt> instance with a
	 * given reference and count of words to be read.  This message reads
	 * from holding (r/w) registers.
	 * 
	 * @see ReadInputRegistersRequest
	 * 
	 * @param ref
	 *            the reference number of the register to read from.
	 * @param count
	 *            the number of words to be read.
	 */
	public ReadMultipleRegistersRequest(int ref, int count) {
		super();

		setFunctionCode(Modbus.READ_MULTIPLE_REGISTERS);
		setDataLength(4);

		setReference(ref);
		setWordCount(count);
	}
	
	public ModbusResponse getResponse() {
		ReadMultipleRegistersResponse response = null;

		response = new ReadMultipleRegistersResponse();
		
		response.setUnitID(getUnitID());
		response.setHeadless(isHeadless());
		if (! isHeadless()) {
			response.setProtocolID(getProtocolID());
			response.setTransactionID(getTransactionID());
		}
		return response;
	}

	public ModbusResponse createResponse() {
		ReadMultipleRegistersResponse response = null;
		Register[] regs = null;

		// 1. get process image
		ProcessImage procimg = ModbusCoupler.getReference().getProcessImage();
		// 2. get input registers range
		try {
			regs = procimg.getRegisterRange(getReference(), getWordCount());
		} catch (IllegalAddressException e) {
			return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
		}
		response = (ReadMultipleRegistersResponse) getResponse();
		response.setRegisters(regs);
		
		return response;
	}

	/**
	 * Sets the reference of the register to start reading from with this
	 * <tt>ReadMultipleRegistersRequest</tt>.
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
	 * <tt>ReadMultipleRegistersRequest</tt>.
	 * <p>
	 * 
	 * @return the reference of the register to start reading from as
	 *         <tt>int</tt>.
	 */
	public int getReference() {
		return m_Reference;
	}

	/**
	 * Sets the number of words to be read with this
	 * <tt>ReadMultipleRegistersRequest</tt>.
	 * <p>
	 * 
	 * @param count
	 *            the number of words to be read.
	 */
	public void setWordCount(int count) {
		m_WordCount = count;
	}

	/**
	 * Returns the number of words to be read with this
	 * <tt>ReadMultipleRegistersRequest</tt>.
	 * <p>
	 * 
	 * @return the number of words to be read as <tt>int</tt>.
	 */
	public int getWordCount() {
		return m_WordCount;
	}

	public void writeData(DataOutput dout) throws IOException {
		dout.writeShort(m_Reference);
		dout.writeShort(m_WordCount);
	}

	public void readData(DataInput din) throws IOException {
		m_Reference = din.readUnsignedShort();
		m_WordCount = din.readUnsignedShort();
	}

	public byte[] getMessage() {
		byte result[] = new byte[4];

		result[0] = (byte) ((m_Reference >> 8) & 0xff);
		result[1] = (byte) (m_Reference & 0xff);
		result[2] = (byte) ((m_WordCount >> 8) & 0xff);
		result[3] = (byte) (m_WordCount & 0xff);

		return result;
	}
}
