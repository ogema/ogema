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
import com.ghgande.j2mod.modbus.io.NonWordDataHandler;
import com.ghgande.j2mod.modbus.procimg.IllegalAddressException;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

/**
 * Class implementing a <tt>Read / Write Multiple Registers</tt> request.
 * 
 * @author Julie Haugh
 * @version jamod-1.2rc1-ghpc
 * 
 * @author Julie Haugh
 * @version 1.05
 */
public final class ReadWriteMultipleRequest extends ModbusRequest {
	private NonWordDataHandler m_NonWordDataHandler;
	private int m_ReadReference;
	private int m_ReadCount;
	private int m_WriteReference;
	private int m_WriteCount;
	private Register m_WriteRegisters[];

	/**
	 * createResponse -- create an empty response for this request.
	 */
	public ModbusResponse getResponse() {
		ReadWriteMultipleResponse response = null;

		response = new ReadWriteMultipleResponse();

		/*
		 * Copy any header data from the request.
		 */
		response.setHeadless(isHeadless());
		if (!isHeadless()) {
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

	public ModbusResponse createResponse() {
		ReadWriteMultipleResponse response = null;
		InputRegister[] readRegs = null;
		Register[] writeRegs = null;

		// 1. get process image
		ProcessImage procimg = ModbusCoupler.getReference().getProcessImage();
		// 2. get input registers range
		try {
			readRegs = procimg.getRegisterRange(getReadReference(),
					getReadWordCount());

			InputRegister[] dummy = new InputRegister[readRegs.length];
			for (int i = 0; i < readRegs.length; i++)
				dummy[i] = new SimpleInputRegister(readRegs[i].getValue());

			readRegs = dummy;

			writeRegs = procimg.getRegisterRange(getWriteReference(),
					getWriteWordCount());

			for (int i = 0; i < writeRegs.length; i++)
				writeRegs[i].setValue(getRegister(i).getValue());
		} catch (IllegalAddressException e) {
			return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
		}
		response = (ReadWriteMultipleResponse) getResponse();
		response.setRegisters(readRegs);

		return response;
	}

	/**
	 * setReadReference - Sets the reference of the register to writing to with
	 * this <tt>ReadWriteMultipleRequest</tt>.
	 * <p>
	 * 
	 * @param ref
	 *            the reference of the register to start writing to as
	 *            <tt>int</tt>.
	 */
	public void setReadReference(int ref) {
		m_ReadReference = ref;
	}

	/**
	 * getReadReference - Returns the reference of the register to start writing
	 * to with this <tt>ReadWriteMultipleRequest</tt>.
	 * <p>
	 * 
	 * @return the reference of the register to start writing to as <tt>int</tt>
	 *         .
	 */
	public int getReadReference() {
		return m_ReadReference;
	}

	/**
	 * setWriteReference - Sets the reference of the register to write to with
	 * this <tt>ReadWriteMultipleRequest</tt>.
	 * <p>
	 * 
	 * @param ref
	 *            the reference of the register to start writing to as
	 *            <tt>int</tt>.
	 */
	public void setWriteReference(int ref) {
		m_WriteReference = ref;
	}

	/**
	 * getWriteReference - Returns the reference of the register to start
	 * writing to with this <tt>ReadWriteMultipleRequest</tt>.
	 * <p>
	 * 
	 * @return the reference of the register to start writing to as <tt>int</tt>
	 *         .
	 */
	public int getWriteReference() {
		return m_WriteReference;
	}

	/**
	 * setRegisters - Sets the registers to be written with this
	 * <tt>ReadWriteMultipleRequest</tt>.
	 * <p>
	 * 
	 * @param registers
	 *            the registers to be written as <tt>Register[]</tt>.
	 */
	public void setRegisters(Register[] registers) {
		m_WriteRegisters = registers;
		m_WriteCount = registers != null ? registers.length : 0;
	}

	/**
	 * getRegisters - Returns the registers to be written with this
	 * <tt>ReadWriteMultipleRequest</tt>.
	 * <p>
	 * 
	 * @return the registers to be read as <tt>Register[]</tt>.
	 */
	public Register[] getRegisters() {
		return m_WriteRegisters;
	}

	/**
	 * getRegister - Returns the specified <tt>Register</tt>.
	 * 
	 * @param index
	 *            the index of the <tt>Register</tt>.
	 * 
	 * @return the register as <tt>Register</tt>.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of bounds.
	 */
	public Register getRegister(int index) throws IndexOutOfBoundsException {
		if (index < 0)
			throw new IndexOutOfBoundsException(index + " < 0");

		if (index >= getWriteWordCount())
			throw new IndexOutOfBoundsException(index + " > "
					+ getWriteWordCount());

		return m_WriteRegisters[index];
	}

	/**
	 * getReadRegisterValue - Returns the value of the specified register
	 * interpreted as unsigned short.
	 * 
	 * @param index
	 *            the relative index of the register for which the value should
	 *            be retrieved.
	 * 
	 * @return the value as <tt>int</tt>.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of bounds.
	 */
	public int getReadRegisterValue(int index) throws IndexOutOfBoundsException {
		return getRegister(index).toUnsignedShort();
	}

	/**
	 * getByteCount - Returns the number of bytes representing the values to be
	 * written.
	 * 
	 * @return the number of bytes to be written as <tt>int</tt>.
	 */
	public int getByteCount() {
		return getWriteWordCount() * 2;
	}

	/**
	 * getWriteWordCount - Returns the number of words to be written.
	 * 
	 * @return the number of words to be written as <tt>int</tt>.
	 */
	public int getWriteWordCount() {
		return m_WriteCount;
	}

	/**
	 * setWriteWordCount - Sets the number of words to be written.
	 * 
	 * @param count
	 *            the number of words to be written as <tt>int</tt>.
	 */
	public void setWriteWordCount(int count) {
		m_WriteCount = count;
	}

	/**
	 * getReadWordCount - Returns the number of words to be read.
	 * 
	 * @return the number of words to be read as <tt>int</tt>.
	 */
	public int getReadWordCount() {
		return m_ReadCount;
	}

	/**
	 * setReadWordCount - Sets the number of words to be read.
	 * 
	 * @param count
	 *            the number of words to be read as <tt>int</tt>.
	 */
	public void setReadWordCount(int count) {
		m_ReadCount = count;
	}

	/**
	 * setNonWordDataHandler - Sets a non word data handler. A non-word data
	 * handler is responsible for converting words from a Modbus packet into the
	 * non-word values associated with the actual device's registers.
	 * 
	 * @param dhandler
	 *            a <tt>NonWordDataHandler</tt> instance.
	 */
	public void setNonWordDataHandler(NonWordDataHandler dhandler) {
		m_NonWordDataHandler = dhandler;
	}

	/**
	 * getNonWordDataHandler - Returns the actual non word data handler.
	 * 
	 * @return the actual <tt>NonWordDataHandler</tt>.
	 */
	public NonWordDataHandler getNonWordDataHandler() {
		return m_NonWordDataHandler;
	}

	/**
	 * writeData -- output this Modbus message to dout.
	 */
	public void writeData(DataOutput dout) throws IOException {
		dout.write(getMessage());
	}

	/**
	 * readData -- read the values of the registers to be written, along with
	 * the reference and count for the registers to be read.
	 */
	public void readData(DataInput input) throws IOException {
		m_ReadReference = input.readShort();
		m_ReadCount = input.readShort();
		m_WriteReference = input.readShort();
		m_WriteCount = input.readUnsignedShort();
		int byteCount = input.readUnsignedByte();

		if (m_NonWordDataHandler == null) {
			byte buffer[] = new byte[byteCount];
			input.readFully(buffer, 0, byteCount);

			int offset = 0;
			m_WriteRegisters = new Register[m_WriteCount];

			for (int register = 0; register < m_WriteCount; register++) {
				m_WriteRegisters[register] = new SimpleRegister(buffer[offset],
						buffer[offset + 1]);
				offset += 2;
			}
		} else {
			m_NonWordDataHandler
					.readData(input, m_WriteReference, m_WriteCount);
		}
	}

	/**
	 * getMessage -- return a prepared message.
	 */
	public byte[] getMessage() {
		byte results[] = new byte[9 + 2 * getWriteWordCount()];

		results[0] = (byte) (m_ReadReference >> 8);
		results[1] = (byte) (m_ReadReference & 0xFF);
		results[2] = (byte) (m_ReadCount >> 8);
		results[3] = (byte) (m_ReadCount & 0xFF);
		results[4] = (byte) (m_WriteReference >> 8);
		results[5] = (byte) (m_WriteReference & 0xFF);
		results[6] = (byte) (m_WriteCount >> 8);
		results[7] = (byte) (m_WriteCount & 0xFF);
		results[8] = (byte) (m_WriteCount * 2);

		int offset = 9;
		for (int i = 0; i < m_WriteCount; i++) {
			Register reg = getRegister(i);
			byte[] bytes = reg.toBytes();

			results[offset++] = bytes[0];
			results[offset++] = bytes[1];
		}
		return results;
	}

	/**
	 * Constructs a new <tt>Read/Write Multiple Registers Request</tt> instance.
	 */
	public ReadWriteMultipleRequest(int unit, int readRef, int readCount,
			int writeRef, int writeCount) {
		super();

		setUnitID(unit);
		setFunctionCode(Modbus.READ_WRITE_MULTIPLE);

		/*
		 * There is no additional data in this request.
		 */
		setDataLength(9 + writeCount * 2);

		m_ReadReference = readRef;
		m_ReadCount = readCount;
		m_WriteReference = writeRef;
		m_WriteCount = writeCount;
		m_WriteRegisters = new Register[writeCount];
		for (int i = 0; i < writeCount; i++)
			m_WriteRegisters[i] = new SimpleRegister(0);
	}

	/**
	 * Constructs a new <tt>Read/Write Multiple Registers Request</tt> instance.
	 */
	public ReadWriteMultipleRequest(int unit) {
		super();

		setUnitID(unit);
		setFunctionCode(Modbus.READ_WRITE_MULTIPLE);

		/*
		 * There is no additional data in this request.
		 */
		setDataLength(9);
	}

	/**
	 * Constructs a new <tt>Read/Write Multiple Registers Request</tt> instance.
	 */
	public ReadWriteMultipleRequest() {
		super();

		setFunctionCode(Modbus.READ_WRITE_MULTIPLE);

		/*
		 * There is no additional data in this request.
		 */
		setDataLength(9);
	}
}
