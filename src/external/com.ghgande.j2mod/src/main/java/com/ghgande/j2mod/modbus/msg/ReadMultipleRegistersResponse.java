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
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

/**
 * Class implementing a <tt>ReadMultipleRegistersResponse</tt>. The
 * implementation directly correlates with the class 0 function <i>read multiple
 * registers (FC 3)</i>. It encapsulates the corresponding response message.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author Julie (jfh@ghgande.com)
 * @version 2012-03-07 Added setFunctionCode() to constructors.
 */
public final class ReadMultipleRegistersResponse extends ModbusResponse {

	// instance attributes
	private int m_ByteCount;
	private Register[] m_Registers;

	/**
	 * Constructs a new <tt>ReadMultipleRegistersResponse</tt> instance.
	 */
	public ReadMultipleRegistersResponse() {
		super();
		setFunctionCode(Modbus.READ_MULTIPLE_REGISTERS);
	}// constructor

	/**
	 * Constructs a new <tt>ReadInputRegistersResponse</tt> instance.
	 * 
	 * @param registers
	 *            the Register[] holding response registers.
	 */
	public ReadMultipleRegistersResponse(Register[] registers) {
		super();
		
		setFunctionCode(Modbus.READ_MULTIPLE_REGISTERS);
		setDataLength(registers.length * 2 + 1);
		
		m_Registers = registers;
		m_ByteCount = registers.length * 2;
	}

	/**
	 * Returns the number of bytes that have been read.
	 * 
	 * @return the number of bytes that have been read as <tt>int</tt>.
	 */
	public int getByteCount() {
		return m_ByteCount;
	}

	/**
	 * Returns the number of words that have been read. The returned value
	 * should be half of the the byte count of this
	 * <tt>ReadMultipleRegistersResponse</tt>.
	 * 
	 * @return the number of words that have been read as <tt>int</tt>.
	 */
	public int getWordCount() {
		return m_ByteCount / 2;
	}// getWordCount

	/**
	 * Returns the <tt>Register</tt> at the given position (relative to the
	 * reference used in the request).
	 * 
	 * @param index
	 *            the relative index of the <tt>Register</tt>.
	 * 
	 * @return the register as <tt>Register</tt>.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of bounds.
	 */
	public Register getRegister(int index) {
		if (m_Registers == null)
			throw new IndexOutOfBoundsException("No registers defined!");			
			
		if (index < 0)
			throw new IndexOutOfBoundsException("Negative index: " + index);

		if (index >= getWordCount())
			throw new IndexOutOfBoundsException(index + " > " + getWordCount());

		return m_Registers[index];
	}

	/**
	 * Returns the value of the register at the given position (relative to the
	 * reference used in the request) interpreted as unsigned short.
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
	public int getRegisterValue(int index) throws IndexOutOfBoundsException {
		return getRegister(index).toUnsignedShort();
	}

	/**
	 * Returns the reference to the array of registers read.
	 * 
	 * @return a <tt>Register[]</tt> instance.
	 */
	public Register[] getRegisters() {
		return m_Registers;
	}

	/**
	 * Sets the entire block of registers for this response
	 */
	public void setRegisters(Register[] registers) {
		m_ByteCount = registers.length * 2;
		setDataLength(m_ByteCount + 1);

		m_Registers = registers;
	}

	public void writeData(DataOutput dout) throws IOException {
		dout.writeByte(m_ByteCount);

		for (int k = 0; k < getWordCount(); k++)
			dout.write(m_Registers[k].toBytes());
	}

	public void readData(DataInput din) throws IOException {
		m_ByteCount = din.readUnsignedByte();

		m_Registers = new Register[getWordCount()];

		for (int k = 0; k < getWordCount(); k++)
			m_Registers[k] = new SimpleRegister(din.readByte(), din.readByte());

		setDataLength(m_ByteCount + 1);
	}

	public byte[] getMessage() {
		byte result[] = null;

		result = new byte[getWordCount() * 2 + 1];
		
		int offset = 0;
		result[offset++] = (byte) m_ByteCount;

		for (int i = 0; i < m_Registers.length; i++) {
			byte[] data = m_Registers[i].toBytes();
			
			result[offset++] = data[0];
			result[offset++] = data[1];
		}
		return result;
	}
}