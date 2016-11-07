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
 * Java Modbus Library (j2mod)
 * Copyright (c) 2012, Julianne Frances Haugh
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
 */
package com.ghgande.j2mod.modbus.msg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

/**
 * Class implementing a <tt>ReadFileRecordResponse</tt>.
 * 
 * @author Julie (jfh@ghgande.com)
 * @version @version@ (@date@)
 */
public final class ReadFileRecordResponse extends ModbusResponse {

	public class RecordResponse {
		private int m_WordCount;
		private byte[] m_Data;

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
		 * 
		 * The response is a byte count, a function code, then wordCount
		 * words (2 bytes).
		 */
		public int getResponseSize() {
			return 2 + (m_WordCount * 2);
		}

		/**
		 * getResponse - return the response data for this record
		 * 
		 * The response data is the byte size of the response, minus this
		 * byte, the function code (6), then the raw byte data for the
		 * registers (m_WordCount * 2 bytes).
		 * 
		 * @param request
		 * @param offset
		 */
		public void getResponse(byte[] request, int offset) {
			request[offset] = (byte) (1 + (m_WordCount * 2));
			request[offset + 1] = 6;
			
			System.arraycopy(m_Data, 0, request, offset + 2, m_Data.length);
		}

		public byte[] getResponse() {
			byte[] request = new byte[getResponseSize()];

			getResponse(request, 0);

			return request;
		}

		public RecordResponse(short data[]) {
			m_WordCount = data.length;
			m_Data = new byte[m_WordCount * 2];

			int offset = 0;
			for (int i = 0; i < m_WordCount; i++) {
				m_Data[offset++] = (byte) (data[i] >> 8);
				m_Data[offset++] = (byte) (data[i] & 0xFF);
			}
		}
	}

	private int m_ByteCount;
	private	RecordResponse[] m_Records = null;

	/**
	 * Returns the number of bytes needed for the response.
	 * 
	 * The response is 1 byte for the total response size, plus
	 * the sum of the sizes of all the records in the response.
	 * 
	 * @return the number of bytes in the response.
	 */
	public int getByteCount() {
		if (m_Records == null)
			return 1;
		
		int size = 1;
		for (int i = 0;i < m_Records.length;i++)
			size += m_Records[i].getResponseSize();
		
		return size;
	}
	
	/**
	 * getRecordCount -- return the number of records in the response.
	 * 
	 * @return count of records in response.
	 */
	public int getRecordCount() {
		if (m_Records == null)
			return 0;
		
		return m_Records.length;
	}

	/**
	 * getRecord -- return the record response indicated by the reference
	 */
	public RecordResponse getRecord(int index) {
		return m_Records[index];
	}
	
	/**
	 * addResponse -- add a new record response.
	 */
	public void addResponse(RecordResponse response) {
		if (m_Records == null)
			m_Records = new RecordResponse[1];
		else {
			RecordResponse old[] = m_Records;
			m_Records = new RecordResponse[old.length + 1];
			
			System.arraycopy(old, 0, m_Records, 0, old.length);
		}
		m_Records[m_Records.length - 1] = response;
	}

	public void writeData(DataOutput dout) throws IOException {
		dout.writeByte(getByteCount() - 1);

		if (m_Records == null)
			return;
		
		for (int i = 0; i < m_Records.length; i++)
			dout.write(m_Records[i].getResponse());
	}

	public void readData(DataInput din) throws IOException {
		m_ByteCount = (din.readUnsignedByte() & 0xFF);

		int remainder = m_ByteCount;
		while (remainder > 0) {
			int length = din.readUnsignedByte();
			remainder--;
			
			int function = din.readByte();
			remainder--;
			
			if (function != 6 || (length - 1) > remainder) {
				throw new IOException("Invalid response format");
			}
			short[] data = new short[(length - 1) / 2];
			for (int i = 0;i < data.length;i++) {
				data[i] = din.readShort();
				remainder -= 2;
			}
			RecordResponse response = new RecordResponse(data);
			addResponse(response);
		}
		setDataLength(m_ByteCount + 1);
	}

	public byte[] getMessage() {
		byte result[] = null;

		result = new byte[getByteCount()];

		int offset = 0;
		result[offset++] = (byte) (result.length - 1);

		for (int i = 0; i < m_Records.length; i++) {
			m_Records[i].getResponse(result, offset);
			offset += m_Records[i].getWordCount() * 2;
		}
		return result;
	}

	/**
	 * Constructs a new <tt>ReadFileRecordResponse</tt> instance.
	 */
	public ReadFileRecordResponse() {
		super();

		setFunctionCode(Modbus.READ_FILE_RECORD);
	}
}