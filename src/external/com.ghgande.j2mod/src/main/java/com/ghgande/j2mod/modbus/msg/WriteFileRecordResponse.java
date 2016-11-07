//License
/***
 * Java Modbus Library (a2mod)
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
 * Java Modbus Library (a2mod)
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
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

/**
 * Class implementing a <tt>WriteFileRecordResponse</tt>.
 * 
 * @author Julie
 * @version 0.96
 */
public final class WriteFileRecordResponse extends ModbusResponse {
	private int m_ByteCount;
	private RecordResponse[] m_Records;
	
	public class RecordResponse {
		private int m_FileNumber;
		private int m_RecordNumber;
		private int m_WordCount;
		private	byte	m_Data[];

		public int getFileNumber() {
			return m_FileNumber;
		}

		public int getRecordNumber() {
			return m_RecordNumber;
		}

		public int getWordCount() {
			return m_WordCount;
		}

		public SimpleRegister getRegister(int register) {
			if (register < 0 || register >= m_WordCount) {
				throw new IndexOutOfBoundsException("0 <= " +
						register + " < " + m_WordCount);
			}
			byte b1 = m_Data[register * 2];
			byte b2 = m_Data[register * 2 + 1];
			
			SimpleRegister result = new SimpleRegister(b1, b2);
			return result;
		}

		/**
		 * getResponseSize -- return the size of the response in bytes.
		 */
		public int getResponseSize() {
			return 7 + m_WordCount * 2;
		}

		public void getResponse(byte[] response, int offset) {
			response[offset++] = 6;
			response[offset++] = (byte) (m_FileNumber >> 8);
			response[offset++] = (byte) (m_FileNumber & 0xFF);
			response[offset++] = (byte) (m_RecordNumber >> 8);
			response[offset++] = (byte) (m_RecordNumber & 0xFF);
			response[offset++] = (byte) (m_WordCount >> 8);
			response[offset++] = (byte) (m_WordCount & 0xFF);
			
			System.arraycopy(m_Data, 0, response, offset, m_Data.length);
		}

		public byte[] getResponse() {
			byte[] response = new byte[7 + 2 * m_WordCount];

			getResponse(response, 0);

			return response;
		}

		public RecordResponse(int file, int record, short[] values) {
			m_FileNumber = file;
			m_RecordNumber = record;
			m_WordCount = values.length;
			m_Data = new byte[m_WordCount * 2];

			int offset = 0;
			for (int i = 0; i < m_WordCount; i++) {
				m_Data[offset++] = (byte) (values[i] >> 8);
				m_Data[offset++] = (byte) (values[i] & 0xFF);
			}
		}
	}

	/**
	 * getRequestSize -- return the total request size.  This is useful
	 * for determining if a new record can be added.
	 * 
	 * @returns size in bytes of response.
	 */
	public int getResponseSize() {
		if (m_Records == null)
			return 1;
		
		int size = 1;
		for (int i = 0;i < m_Records.length;i++)
			size += m_Records[i].getResponseSize();
		
		return size;
	}
	
	/**
	 * getRequestCount -- return the number of record requests in this
	 * message.
	 */
	public int getRequestCount() {
		if (m_Records == null)
			return 0;
		
		return m_Records.length;
	}
	
	/**
	 * getRecord -- return the record request indicated by the reference
	 */
	public RecordResponse getRecord(int index) {
		return m_Records[index];
	}
	
	/**
	 * addResponse -- add a new record response.
	 */
	public void addResponse(RecordResponse response) {
		if (response.getResponseSize() + getResponseSize() > 248)
			throw new IllegalArgumentException();
		
		if (m_Records == null)
			m_Records = new RecordResponse[1];
		else {
			RecordResponse old[] = m_Records;
			m_Records = new RecordResponse[old.length + 1];
			
			System.arraycopy(old, 0, m_Records, 0, old.length);
		}
		m_Records[m_Records.length - 1] = response;
		
		setDataLength(getResponseSize());
	}

	public void writeData(DataOutput dout) throws IOException {
		dout.write(getMessage());
	}

	public void readData(DataInput din) throws IOException {
		m_ByteCount = din.readUnsignedByte();

		m_Records = new RecordResponse[0];

		for (int offset = 1; offset + 7 < m_ByteCount;) {
			int function = din.readUnsignedByte();
			int file = din.readUnsignedShort();
			int record = din.readUnsignedShort();
			int count = din.readUnsignedShort();
			
			offset += 7;
			
			if (function != 6)
				throw new IOException();
				
			if (record < 0 || record >= 10000)
				throw new IOException();

			if (count < 0 || count >= 126)
				throw new IOException();

			short registers[] = new short[count];
			for (int j = 0;j < count;j++) {
				registers[j] = din.readShort();
				offset += 2;
			}
			RecordResponse dummy[] = new RecordResponse[m_Records.length + 1];
			if (m_Records.length > 0)
				System.arraycopy(m_Records, 0, dummy, 0, m_Records.length);
			
			m_Records = dummy;
			m_Records[m_Records.length - 1] =
					new RecordResponse(file, record, registers);
		}
	}

	public byte[] getMessage() {
		byte	results[] = new byte[getResponseSize()];

		results[0] = (byte) (getResponseSize() - 1);
		
		int offset = 1;
		for (int i = 0;i < m_Records.length;i++) {
			m_Records[i].getResponse(results, offset);
			offset += m_Records[i].getResponseSize();
		}
		return results;
	}

	/**
	 * Constructs a new <tt>WriteFileRecordResponse</tt> instance.
	 */
	public WriteFileRecordResponse() {
		super();
		
		setFunctionCode(Modbus.WRITE_FILE_RECORD);
		setDataLength(7);
	}
}