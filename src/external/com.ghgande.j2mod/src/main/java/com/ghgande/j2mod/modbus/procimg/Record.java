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

/**
 * @author Julie
 *
 * File -- an abstraction of a Modbus Record, as supported by the
 * READ FILE RECORD and WRITE FILE RECORD commands.
 */
public class Record {
	private	int	m_Record_Number;
	private	int	m_Register_Count;
	private	Register m_Registers[];
	
	public int getRecordNumber() {
		return m_Record_Number;
	}
	
	public int getRegisterCount() {
		return m_Register_Count;
	}
	
	public Register getRegister(int register) {
		if (register < 0 || register >= m_Register_Count)
			throw new IllegalAddressException();
		
		return m_Registers[register];
	}
	
	public Record setRegister(int ref, Register register) {
		if (ref < 0 || ref >= m_Register_Count)
			throw new IllegalAddressException();
		
		m_Registers[ref] = register;
		
		return this;
	}
	
	public Record(int recordNumber, int registers) {
		m_Record_Number = recordNumber;
		m_Register_Count = registers;
		m_Registers = new Register[registers];
		
		for (int i = 0;i < m_Register_Count;i++)
			m_Registers[i] = new SimpleRegister(0);
	}
}
