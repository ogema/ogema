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

/**
 * Class implementing a <tt>WriteMultipleCoilsResponse</tt>. The implementation
 * directly correlates with the class 1 function <i>write multiple coils (FC
 * 15)</i>. It encapsulates the corresponding response message.
 * <p>
 * Coils are understood as bits that can be manipulated (i.e. set or cleared).
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author Julie Haugh
 * @version 1.05
 * 
 *          20140426 - Refactor and verify the new methods.<br>
 */
public final class WriteMultipleCoilsResponse
    extends ModbusResponse {

	// instance attributes
	private int m_Reference;
	private int m_BitCount;

	/**
	 * getReference - Returns the reference of the coil to start reading from
	 * with this <tt>WriteMultipleCoilsResponse</tt>.
	 * <p>
	 * 
	 * @return the reference of the coil to start reading from as <tt>int</tt>.
	 */
	public int getReference() {
		return m_Reference;
	}

	/**
	 * setReference - Sets the reference to the coil that is the first coil in
	 * this response.
	 * 
	 * @param ref
	 */
	public void setReference(int ref) {
		m_Reference = ref;
	}

	/**
	 * getBitCount - Returns the quantity of coils written with the request.
	 * <p>
	 * 
	 * @return the quantity of coils that have been written.
	 */
	public int getBitCount() {
		return m_BitCount;
	}

	/**
	 * setBitCount - Sets the number of coils that will be in a response.
	 * 
	 * @param count
	 *            the number of coils in the response.
	 */
	public void setBitCount(int count) {
		m_BitCount = count;
	}

	/**
	 * writeData - Copy the attribute values for this message to the output
	 * buffer.
	 */
	public void writeData(DataOutput dout) throws IOException {

		dout.writeShort(m_Reference);
		dout.writeShort(m_BitCount);
	}

	/**
	 * readData - Initialize the attribute values for this message from the
	 * input buffer.
	 */
	public void readData(DataInput din) throws IOException {

		m_Reference = din.readUnsignedShort();
		m_BitCount = din.readUnsignedShort();
	}

	public byte[] getMessage() {
		byte results[] = new byte[4];
		
		results[0] = (byte) ((m_Reference >> 8) & 0xff);
		results[1] = (byte) (m_Reference & 0xff);
		results[2] = (byte) ((m_BitCount >> 8) & 0xff);
		results[3] = (byte) (m_BitCount & 0xff);

		return results;
	}

	/**
	 * Constructs a new <tt>WriteMultipleCoilsResponse</tt> instance with a
	 * given count of coils and starting reference.
	 * <p>
	 * 
	 * @param ref
	 *            the offset to begin writing from.
	 * @param count
	 *            the number of coils to be written.
	 */
	public WriteMultipleCoilsResponse(int ref, int count) {
		super();

		m_Reference = ref;
		m_BitCount = count;

		setDataLength(4);
	}

	/**
	 * Constructs a new <tt>WriteMultipleCoilsResponse</tt> instance.
	 */
	public WriteMultipleCoilsResponse() {
		super();

		setDataLength(4);
	}
}
