//License
/***
 * Java Modbus Library (jamod)
 * Copyright 2010-2012, greenHouse Gas and Electric
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
import com.ghgande.j2mod.modbus.msg.WriteFileRecordResponse.RecordResponse;
import com.ghgande.j2mod.modbus.procimg.File;
import com.ghgande.j2mod.modbus.procimg.IllegalAddressException;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.procimg.Record;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;


/**
 * Class implementing a <tt>Write File Record</tt> request.
 * 
 * @author Julie Haugh (jfh@ghgande.com)
 * @version @version@ (@date@)
 */
public final class WriteFileRecordRequest extends ModbusRequest {
	private int m_ByteCount;
	private RecordRequest[] m_Records;
	
	public class RecordRequest {
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
				throw new IllegalAddressException("0 <= " +
						register + " < " + m_WordCount);
			}
			byte b1 = m_Data[register * 2];
			byte b2 = m_Data[register * 2 + 1];
			
			SimpleRegister result = new SimpleRegister(b1, b2);
			return result;
		}

		/**
		 * getRequestSize -- return the size of the response in bytes.
		 */
		public int getRequestSize() {
			return 7 + m_WordCount * 2;
		}

		public void getRequest(byte[] request, int offset) {
			request[offset++] = 6;
			request[offset++] = (byte) (m_FileNumber >> 8);
			request[offset++] = (byte) (m_FileNumber & 0xFF);
			request[offset++] = (byte) (m_RecordNumber >> 8);
			request[offset++] = (byte) (m_RecordNumber & 0xFF);
			request[offset++] = (byte) (m_WordCount >> 8);
			request[offset++] = (byte) (m_WordCount & 0xFF);
			
			System.arraycopy(m_Data, 0, request, offset, m_Data.length);
		}

		public byte[] getRequest() {
			byte[] request = new byte[7 + 2 * m_WordCount];

			getRequest(request, 0);

			return request;
		}

		public RecordRequest(int file, int record, short[] values) {
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
	public int getRequestSize() {
		if (m_Records == null)
			return 1;
		
		int size = 1;
		for (int i = 0;i < m_Records.length;i++)
			size += m_Records[i].getRequestSize();
		
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
	public RecordRequest getRecord(int index) {
		return m_Records[index];
	}
	
	/**
	 * addRequest -- add a new record request.
	 */
	public void addRequest(RecordRequest request) {
		if (request.getRequestSize() + getRequestSize() > 248)
			throw new IllegalArgumentException();
		
		if (m_Records == null)
			m_Records = new RecordRequest[1];
		else {
			RecordRequest old[] = m_Records;
			m_Records = new RecordRequest[old.length + 1];
			
			System.arraycopy(old, 0, m_Records, 0, old.length);
		}
		m_Records[m_Records.length - 1] = request;
		
		setDataLength(getRequestSize());
	}

	/**
	 * createResponse -- create an empty response for this request.
	 */
	public ModbusResponse getResponse() {
		WriteFileRecordResponse response = null;

		response = new WriteFileRecordResponse();

		/*
		 * Copy any header data from the request.
		 */
		response.setHeadless(isHeadless());
		if (! isHeadless()) {
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
	
	/**
	 * The ModbusCoupler doesn't have a means of writing file records.
	 */
	public ModbusResponse createResponse() {
		WriteFileRecordResponse response = null;
		response = (WriteFileRecordResponse) getResponse();

		/*
		 * Get the process image.
		 */
		ProcessImage procimg = ModbusCoupler.getReference().getProcessImage();
		
		/*
		 * There is a list of requests to be resolved.
		 */
		try {
			for (int i = 0;i < getRequestCount();i++) {
				RecordRequest recordRequest = getRecord(i);
				if (recordRequest.getFileNumber() < 0 ||
						recordRequest.getFileNumber() >= procimg.getFileCount())
					return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
					
				File file = procimg.getFileByNumber(recordRequest.getFileNumber());
				
				if (recordRequest.getRecordNumber() < 0 ||
						recordRequest.getRecordNumber() >= file.getRecordCount())
					return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
				
				Record record = file.getRecord(recordRequest.getRecordNumber());
				int registers = recordRequest.getWordCount();
				if (record == null && registers != 0)
					return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
									
				short data[] = new short[registers];
				for (int j = 0;j < registers;j++) {
					Register register = record.getRegister(j);
					if (register == null)
						return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);						
						
					register.setValue(recordRequest.getRegister(j).getValue());
					data[j] = recordRequest.getRegister(j).toShort();
				}
				RecordResponse recordResponse = response.new RecordResponse(
						file.getFileNumber(), record.getRecordNumber(), data);
				response.addResponse(recordResponse);
			}
		} catch (IllegalAddressException e) {
			return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
		}
		return response;
	}

	/**
	 * writeData -- output this Modbus message to dout.
	 */
	public void writeData(DataOutput dout) throws IOException {
		dout.write(getMessage());
	}

	/**
	 * readData -- convert the byte stream into a request.
	 */
	public void readData(DataInput din) throws IOException {
		m_ByteCount = din.readUnsignedByte();

		m_Records = new RecordRequest[0];

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
			RecordRequest dummy[] = new RecordRequest[m_Records.length + 1];
			if (m_Records.length > 0)
				System.arraycopy(m_Records, 0, dummy, 0, m_Records.length);
			
			m_Records = dummy;
			m_Records[m_Records.length - 1] =
					new RecordRequest(file, record, registers);
		}
	}

	/**
	 * getMessage -- return the raw binary message.
	 */
	public byte[] getMessage() {
		byte	results[] = new byte[getRequestSize()];

		results[0] = (byte) (getRequestSize() - 1);
		
		int offset = 1;
		for (int i = 0;i < m_Records.length;i++) {
			m_Records[i].getRequest(results, offset);
			offset += m_Records[i].getRequestSize();
		}
		return results;
	}

	/**
	 * Constructs a new <tt>Write File Record</tt> request
	 * instance.
	 */
	public WriteFileRecordRequest() {
		super();
		
		setFunctionCode(Modbus.WRITE_FILE_RECORD);
		
		/*
		 * Set up space for the initial header.
		 */
		setDataLength(1);
	}
}
