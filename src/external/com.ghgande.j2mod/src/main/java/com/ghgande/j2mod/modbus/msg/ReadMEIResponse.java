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
 * Java Modbus Library (jamod)
 * Copyright (c) 2010, greenHouse Computers, LLC
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


/**
 * Class implementing a <tt>ReadMEIResponse</tt>.
 * 
 * Derived from similar class for Read Coils response.
 * 
 * @author Julie Haugh (jfh@ghgande.com)
 * @version 1.2rc1-ghpc (09/27/2010)
 */
public final class ReadMEIResponse
    extends ModbusResponse {

  //instance attributes
	private	int		m_FieldLevel = 0;
	private	int		m_Conformity = 1;
	private	int		m_FieldCount = 0;
	private	String	m_Fields[] = new String[64];
	private	int		m_FieldIds[] = new int[64];
	private	boolean	m_MoreFollows = false;
	private	int		m_NextFieldId;

  /**
   * Constructs a new <tt>ReadMEIResponse</tt>
   * instance.
   */
  public ReadMEIResponse() {
    super();
    setFunctionCode(Modbus.READ_MEI);
  }//constructor(int)


  /**
   * Returns the number of fields
   * read with the request.
   * <p>
   * @return the number of fields that have been read.
   */
  public int getFieldCount() {
    if(m_Fields == null) {
      return 0;
    } else{
      return m_Fields.length;
    }
  }//getBitCount


  /**
   * Returns the array of strings that were read
   */
  public String[] getFields() {
    return m_Fields;
  }//getFields

  /**
   * Convenience method that returns the field
   * at the requested index
   * <p>
   * @param index the index of the field which
   *       should be returned.
   *
   * @return requested field
   *
   * @throws IndexOutOfBoundsException if the
   *         index is out of bounds
   */
  public String getField(int index)
      throws IndexOutOfBoundsException {
	  return m_Fields[index];
  }//getCoilStatus

  /**
   * Convenience method that returns the field
   * ID at the given index.
   * <p>
   * @param index the index of the field for which
   *        the ID should be returned.
   *
   * @return field ID
   *
   * @throws IndexOutOfBoundsException if the
   *         index is out of bounds
   */
  public int getFieldId(int index)
      throws IndexOutOfBoundsException {
	  return m_FieldIds[index];
  }//getFieldId
  
  public void setFieldLevel(int level) {
	  m_FieldLevel = level;
  }
  
  public void addField(int id, String text) {
	  m_FieldIds[m_FieldCount] = id;
	  m_Fields[m_FieldCount] = text;
	  m_FieldCount++;
  }

  public void writeData(DataOutput dout)
      throws IOException {
	  dout.write(getMessage());
  }//writeData

  public void readData(DataInput din)
      throws IOException {
	  int	byteCount = 0;
	  
	  int	subCode = din.readUnsignedByte();
	  if (subCode != 0xE) {
		  throw new IOException("Invalid sub code");
	  }
	  
	  m_FieldLevel = din.readUnsignedByte();
	  m_Conformity = din.readUnsignedByte();
	  m_MoreFollows = din.readUnsignedByte() == 0xFF;
	  m_NextFieldId = din.readUnsignedByte();
	  
	  m_FieldCount = din.readUnsignedByte();
	  
	  byteCount = 6;
	  
	  if (m_FieldCount > 0) {
		  m_Fields = new String[m_FieldCount];
		  m_FieldIds = new int[m_FieldCount];
		  
		  for (int i = 0;i < m_FieldCount;i++) {
			  m_FieldIds[i] = din.readUnsignedByte();
			  int len = din.readUnsignedByte();
			  byte data[] = new byte[len];
			  din.readFully(data);
			  m_Fields[i] = new String(data);
			  
			  byteCount += 2 + len;
		  }
		  setDataLength(byteCount);
		  return;
	  } else {
		  setDataLength(byteCount);
		  return;
	  }
  }//readData
  
  public byte[] getMessage() {
	  int	size = 6;
	  
	  for (int i = 0;i < m_FieldCount;i++) {
		  /*
		   * Add the field ID
		   */
		  size++;
		  
		  /*
		   * Add the string length byte and the
		   * actual string length.
		   */
		  size++;
		  size += m_Fields[i].length();
	  }
	  
	  byte result[] = new byte[size];
	  int offset = 0;
	  
	  result[offset++] = 0x0E;
	  result[offset++] = (byte) m_FieldLevel;
	  result[offset++] = (byte) m_Conformity;
	  result[offset++] = (byte) (m_MoreFollows ? 0xFF:0);
	  result[offset++] = (byte) m_NextFieldId;
	  result[offset++] = (byte) m_FieldCount;
	  
	  for (int i = 0;i < m_FieldCount;i++) {
		  result[offset++] = (byte) m_FieldIds[i];
		  result[offset++] = (byte) m_Fields[i].length();
		  System.arraycopy(m_Fields[i].getBytes(), 0,
				  result, offset, m_Fields[i].length());
		  offset += m_Fields[i].length();
	  }
	  
	  return result;
  }

}//class ReadMEIResponse
