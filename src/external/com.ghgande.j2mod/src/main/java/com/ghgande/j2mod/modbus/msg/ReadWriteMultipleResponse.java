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
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

/**
 * Class implementing a <tt>ReadWriteMultipleResponse</tt>.
 * 
 * @author Julie (jfh@ghgande.com)
 * 
 * @version @version@ (@date@)
 */
public final class ReadWriteMultipleResponse extends ModbusResponse {

	private int				m_ByteCount;
	private InputRegister[] m_Registers;

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
	 * <tt>ReadWriteMultipleResponse</tt>.
	 * 
	 * @return the number of words that have been read as <tt>int</tt>.
	 */
	public int getWordCount() {
		return m_ByteCount / 2;
	}

	/**
	 * Returns the <tt>Register</tt> at the given position (relative to the
	 * reference used in the request).
	 * 
	 * @param index
	 *            the relative index of the <tt>InputRegister</tt>.
	 * 
	 * @return the register as <tt>InputRegister</tt>.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of bounds.
	 */
	public InputRegister getRegister(int index) {
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
	 * @return a <tt>InputRegister[]</tt> instance.
	 */
	public InputRegister[] getRegisters() {
		return m_Registers;
	}

	/**
	 * Sets the entire block of registers for this response
	 */
	public void setRegisters(InputRegister[] registers) {
		m_ByteCount = registers.length * 2 + 1;
		setDataLength(m_ByteCount);

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

	/**
	 * Constructs a new <tt>ReadWriteMultipleResponse</tt> instance.
	 * 
	 * @param registers
	 *            the Register[] holding response registers.
	 */
	public ReadWriteMultipleResponse(InputRegister[] registers) {
		super();
		
		setFunctionCode(Modbus.READ_WRITE_MULTIPLE);
		setDataLength(registers.length * 2 + 1);
		
		m_Registers = registers;
		m_ByteCount = registers.length * 2 + 1;
	}

	/**
	 * Constructs a new <tt>ReadWriteMultipleResponse</tt> instance.
	 * 
	 * @param registers
	 *            the Register[] holding response registers.
	 */
	public ReadWriteMultipleResponse(int count) {
		super();
		
		setFunctionCode(Modbus.READ_WRITE_MULTIPLE);
		setDataLength(count * 2 + 1);
		
		m_Registers = new InputRegister[count];
		m_ByteCount = count * 2 + 1;
	}

	/**
	 * Constructs a new <tt>ReadWriteMultipleResponse</tt> instance.
	 */
	public ReadWriteMultipleResponse() {
		super();
		
		setFunctionCode(Modbus.READ_WRITE_MULTIPLE);
	}
}