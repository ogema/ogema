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
/**
 *  * Java Modbus Library 2 (j2mod)
 * Copyright (c) 2010-2014, greenHouse Gas and Electric
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
 */
package com.ghgande.j2mod.modbus.msg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.io.NonWordDataHandler;
import com.ghgande.j2mod.modbus.procimg.*;

/**
 * Class implementing a <tt>WriteMultipleRegistersRequest</tt>. The
 * implementation directly correlates with the class 0 function <i>write
 * multiple registers (FC 16)</i>. It encapsulates the corresponding request
 * message.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author jfhaugh
 * @version 1.05
 * 
 *          20140426 - Refactor and minor bug fix.
 */
public final class WriteMultipleRegistersRequest extends ModbusRequest {
	private int m_Reference;
	private Register[] m_Registers;
	private NonWordDataHandler m_NonWordDataHandler = null;

	public ModbusResponse getResponse() {
		WriteMultipleRegistersResponse response = new WriteMultipleRegistersResponse();

		response.setHeadless(isHeadless());
		if (!isHeadless()) {
			response.setProtocolID(getProtocolID());
			response.setTransactionID(getTransactionID());
		}
		response.setFunctionCode(getFunctionCode());
		response.setUnitID(getUnitID());

		return response;
	}

	/**
	 * createResponse - Returns the <tt>WriteMultipleRegistersResponse</tt> that
	 * represents the answer to this <tt>WriteMultipleRegistersRequest</tt>.
	 * 
	 * The implementation should take care about assembling the reply to this
	 * <tt>WriteMultipleRegistersRequest</tt>.
	 * 
	 * This method is used to create responses from the process image associated
	 * with the <tt>ModbusCoupler</tt>. It is commonly used to implement Modbus
	 * slave instances.
	 * 
	 * @returns the corresponding ModbusResponse.
	 *          <p>
	 * 
	 *          createResponse() must be able to handle the case where the word
	 *          data that is in the response is actually non-word data. That is,
	 *          where the slave device has data which are not actually
	 *          <tt>short</tt> values in the range of registers being processed.
	 */
	public ModbusResponse createResponse() {
		WriteMultipleRegistersResponse response = null;

		if (m_NonWordDataHandler == null) {
			Register[] regs = null;
			// 1. get process image
			ProcessImage procimg = ModbusCoupler.getReference().getProcessImage();
			// 2. get registers
			try {
				regs = procimg.getRegisterRange(getReference(), getWordCount());
				// 3. set Register values
				for (int i = 0; i < regs.length; i++)
					regs[i].setValue(this.getRegister(i).getValue());
			} catch (IllegalAddressException iaex) {
				return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
			}
			response = (WriteMultipleRegistersResponse) getResponse();

			response.setReference(getReference());
			response.setWordCount(getWordCount());
		} else {
			int result = m_NonWordDataHandler.commitUpdate();
			if (result > 0)
				return createExceptionResponse(result);

			response = (WriteMultipleRegistersResponse) getResponse();

			response.setReference(getReference());
			response.setWordCount(m_NonWordDataHandler.getWordCount());
		}

		return response;
	}

	/**
	 * setReference - Sets the reference of the register to write to with this
	 * <tt>WriteMultipleRegistersRequest</tt>.
	 * <p>
	 * 
	 * @param ref
	 *            the reference of the register to start writing to as an
	 *            <tt>int</tt>.
	 */
	public void setReference(int ref) {
		m_Reference = ref;
	}

	/**
	 * setReference - Returns the reference of the register to start writing to
	 * with this <tt>WriteMultipleRegistersRequest</tt>.
	 * <p>
	 * 
	 * @return the reference of the register to start writing to as <tt>int</tt>
	 *         .
	 */
	public int getReference() {
		return m_Reference;
	}

	/**
	 * setRegisters - Sets the registers to be written with this
	 * <tt>WriteMultipleRegistersRequest</tt>.
	 * <p>
	 * 
	 * @param registers
	 *            the registers to be written as <tt>Register[]</tt>.
	 */
	public void setRegisters(Register[] registers) {
		m_Registers = registers;
	}

	/**
	 * getRegisters - Returns the registers to be written with this
	 * <tt>WriteMultipleRegistersRequest</tt>.
	 * <p>
	 * 
	 * @return the registers to be written as <tt>Register[]</tt>.
	 */
	public Register[] getRegisters() {
		return m_Registers;
	}

	/**
	 * getRegister - Returns the <tt>Register</tt> at the given position.
	 * 
	 * @param index
	 *            the relative index of the <tt>Register</tt>.
	 * 
	 * @return the register as <tt>Register</tt>.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of bounds.
	 */
	public Register getRegister(int index) throws IndexOutOfBoundsException {
		if (index < 0)
			throw new IndexOutOfBoundsException(index + " < 0");

		if (index >= getWordCount())
			throw new IndexOutOfBoundsException(index + " > " + getWordCount());

		return m_Registers[index];
	}

	/**
	 * getRegisterValue - Returns the value of the specified register.
	 * <p>
	 * 
	 * @param index
	 *            the index of the desired register.
	 * 
	 * @return the value as an <tt>int</tt>.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of bounds.
	 */
	public int getRegisterValue(int index) throws IndexOutOfBoundsException {
		return getRegister(index).toUnsignedShort();
	}

	/**
	 * getByteCount - Returns the number of bytes representing the values to be
	 * written.
	 * <p>
	 * 
	 * @return the number of bytes to be written as <tt>int</tt>.
	 */
	public int getByteCount() {
		return getWordCount() * 2;
	}

	/**
	 * getWordCount - Returns the number of words to be written.
	 * 
	 * @return the number of words to be written as <tt>int</tt>.
	 */
	public int getWordCount() {
		if (m_Registers == null)
			return 0;

		return m_Registers.length;
	}

	/**
	 * setNonWordHandler - Sets a non word data handler. A non-word data handler
	 * is responsible for converting words from a Modbus packet into the
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

	public void writeData(DataOutput output) throws IOException {
		byte data[] = getMessage();
		if (data == null)
			return;

		output.write(data);
	}

	public void readData(DataInput input) throws IOException {
		m_Reference = input.readShort();
		int registerCount = input.readUnsignedShort();
		int byteCount = input.readUnsignedByte();

		if (m_NonWordDataHandler == null) {
			byte buffer[] = new byte[byteCount];
			input.readFully(buffer, 0, byteCount);

			int offset = 0;
			m_Registers = new Register[registerCount];

			for (int register = 0; register < registerCount; register++) {
				m_Registers[register] = new SimpleRegister(buffer[offset],
						buffer[offset + 1]);
				offset += 2;
			}
		} else {
			m_NonWordDataHandler.readData(input, m_Reference, registerCount);
		}
	}

	public byte[] getMessage() {
		int len = 5;

		if (m_Registers != null)
			len += m_Registers.length * 2;

		byte result[] = new byte[len];
		int registerCount = m_Registers != null ? m_Registers.length : 0;

		result[0] = (byte) ((m_Reference >> 8) & 0xff);
		result[1] = (byte) (m_Reference & 0xff);
		result[2] = (byte) ((registerCount >> 8) & 0xff);
		result[3] = (byte) (registerCount & 0xff);
		result[4] = (byte) (registerCount * 2);

		int offset = 5;

		if (m_NonWordDataHandler == null) {
			for (int i = 0; i < registerCount; i++) {
				byte bytes[] = m_Registers[i].toBytes();
				result[offset++] = bytes[0];
				result[offset++] = bytes[1];
			}
		} else {
			m_NonWordDataHandler.prepareData(m_Reference, registerCount);
			byte bytes[] = m_NonWordDataHandler.getData();
			if (bytes != null) {
				int nonWordBytes = bytes.length;
				if (nonWordBytes > registerCount * 2)
					nonWordBytes = registerCount * 2;

				System.arraycopy(bytes, 0, result, offset, nonWordBytes);
			}
		}
		return result;
	}

	/**
	 * Constructs a new <tt>WriteMultipleRegistersRequest</tt> instance with a
	 * given starting reference and values to be written.
	 * <p>
	 * 
	 * @param first
	 *            -- the address of the first register to write to.
	 * @param registers
	 *            -- the registers to be written.
	 */
	public WriteMultipleRegistersRequest(int first, Register[] registers) {
		setFunctionCode(Modbus.WRITE_MULTIPLE_REGISTERS);

		setReference(first);
		setRegisters(registers);
	}

	/**
	 * Constructs a new <tt>WriteMultipleRegistersRequest</tt> instance.
	 */
	public WriteMultipleRegistersRequest() {
		setFunctionCode(Modbus.WRITE_MULTIPLE_REGISTERS);
	}
}
