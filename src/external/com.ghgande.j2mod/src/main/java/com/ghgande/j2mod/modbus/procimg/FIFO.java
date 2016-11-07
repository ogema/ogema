/***
 * Java Modbus Library (j2mod)
 * Copyright 2012, Julianne Frances Haugh
 * d/b/a greenHouse Gas and Electric
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
package com.ghgande.j2mod.modbus.procimg;

import java.util.Vector;

/**
 * @author Julie
 *
 * FIFO -- an abstraction of a Modbus FIFO, as supported by the
 * READ FIFO command.
 * 
 * The FIFO class is only intended to be used for testing purposes and does
 * not reflect the actual behavior of a FIFO in a real Modbus device.  In an
 * actual Modbus device, the FIFO is mapped within a fixed address.
 */
public class FIFO {
	private int m_Address;
	private	int	m_Register_Count;
	private	Vector<Register> m_Registers;
	
	public synchronized int getRegisterCount() {
		return m_Register_Count;
	}
	
	public synchronized Register[] getRegisters() {
		Register result[] = new Register[m_Register_Count + 1];
		
		result[0] = new SimpleRegister(m_Register_Count);
		for (int i = 0;i < m_Register_Count;i++)
			result[i + 1] = m_Registers.get(i);
		
		return result;
	}
	
	public synchronized void pushRegister(Register register) {
		if (m_Register_Count == 31)
			m_Registers.remove(0);
		else
			m_Register_Count++;
		
		m_Registers.add(new SimpleRegister(register.getValue()));
	}
	
	public synchronized void resetRegisters() {
		m_Registers.removeAllElements();
		m_Register_Count = 0;
	}
	
	public int getAddress() {
		return m_Address;
	}
	
	public FIFO(int address) {
		m_Address = address;
		m_Register_Count = 0;
		m_Registers = new Vector<Register>();
	}
}
