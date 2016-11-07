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
 * Copyright (c) 2010-2012, greenHouse Gas and Electric
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
import java.util.Arrays;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

/**
 * Class implementing a <tt>ReadFIFOQueueResponse</tt>.
 * 
 * @author Julie Haugh (jfh@ghgande.com)
 * 
 * @version @version@ (@date@)
 */
public final class ReadFIFOQueueResponse extends ModbusResponse {

	/*
	 * Message fields.
	 */
	private	int	m_Count;
	private	InputRegister m_Registers[];

	/**
	 * getWordCount -- get the queue size.
	 * 
	 * @return
	 */
	public int getWordCount() {
		return m_Count;
	}

	/**
	 * getWordCount -- set the queue size.
	 * 
	 * @return
	 */
	public synchronized void setWordCount(int ref) {
		if (ref < 0 || ref > 31)
			throw new IllegalArgumentException();
		
		int	oldCount = m_Count;
		InputRegister newRegisters[] = new InputRegister[ref];
		
		m_Count = ref;
		
		for (int i = 0;i < ref;i++) {
			if (i < oldCount)
				newRegisters[i] = m_Registers[i];
			else
				newRegisters[i] = new SimpleRegister(0);
		}
	}
	
	public int[] getRegisters() {
		int	values[] = new int[m_Count];
		
		for (int i = 0;i < m_Count;i++)
			values[i] = getRegister(i);
		
		return values;
	}
	
	public int getRegister(int index) {
		return m_Registers[index].getValue();
	}

	/**
	 * setRegisters -- set the device's status.
	 * 
	 * @param status
	 */
	public synchronized void setRegisters(InputRegister[] regs) {
		m_Registers = regs;
		if (regs == null) {
			m_Count = 0;
			return;
		}
		
		if (regs.length > 31)
			throw new IllegalArgumentException();
		
		m_Count = regs.length;
	}

	/**
	 * writeData -- output the completed Modbus message to dout
	 */
	public void writeData(DataOutput dout) throws IOException {
		dout.write(getMessage());
	}

	/**
	 * readData -- input the Modbus message from din. If there was a header,
	 * such as for Modbus/TCP, it will have been read already.
	 */
	public void readData(DataInput din) throws IOException {
		int byteCount;
		
		/*
		 * Read and discard the byte count.  There's no way to indicate
		 * the packet was inconsistent, other than throwing an I/O
		 * exception for an invalid packet format ...
		 */
		byteCount = din.readShort();
		
		/*
		 * The first register is the number of registers which
		 * follow.  Save that as m_Count, not as a register.
		 */
		m_Count = din.readShort();
		m_Registers = new InputRegister[m_Count];
		
		for (int i = 0;i < m_Count;i++)
			m_Registers[i] = new SimpleInputRegister(din.readShort());
	}

	/**
	 * getMessage -- format the message into a byte array.
	 */
	public byte[] getMessage() {
		byte result[] = new byte[m_Count * 2 + 4];

		int len = m_Count * 2 + 2;
		result[0] = (byte) (len >> 8);
		result[1] = (byte) (len & 0xFF);
		result[2] = (byte) (m_Count >> 8);
		result[3] = (byte) (m_Count & 0xFF);
		
		for (int i = 0;i < m_Count;i++) {
			byte value[] = m_Registers[i].toBytes();
			result[i * 2 + 4] = value[0];
			result[i * 2 + 5] = value[1];
		}
		return result;
	}
	
	/**
	 * Constructs a new <tt>ReadFIFOQueueResponse</tt> instance.
	 */
	public ReadFIFOQueueResponse() {
		super();

		setFunctionCode(Modbus.READ_FIFO_QUEUE);
		
		m_Count = 0;
		m_Registers = new InputRegister[0];
		
		setDataLength(7);
	}
}
