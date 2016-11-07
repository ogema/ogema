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
package com.ghgande.j2mod.modbus.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FilterOutputStream;


/**
 * Class implementing a specialized <tt>OutputStream</tt> which
 * duplicates bytes written to the stream that resemble a
 * frame token.
 *
 * Note that the "virtual" characters FRAME_START and FRAME_END
 * are exceptions, they are translated to the respective tokens
 * as given by the specification.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 *
 * @see ModbusBINTransport#FRAME_START
 * @see ModbusBINTransport#FRAME_END
 */
public class BINOutputStream
    extends FilterOutputStream {

  /**
   * Constructs a new <tt>BINOutputStream</tt> instance
   * writing to the given <tt>OutputStream</tt>.
   *
   * @param out a base output stream instance to be wrapped.
   */
  public BINOutputStream(OutputStream out) {
    super(out);
  }//constructor

  /**
   * Writes a byte to the raw output stream.
   * Bytes resembling a frame token will be duplicated.
   *
   * @param b the byte to be written as <tt>int</tt>.
   * @throws java.io.IOException if an I/O error occurs.
   */
  public void write(int b) throws IOException {
    if (b == ModbusASCIITransport.FRAME_START) {
      out.write(ModbusBINTransport.FRAME_START_TOKEN);
      return;
    } else if (b == ModbusASCIITransport.FRAME_END) {
      out.write(ModbusBINTransport.FRAME_END_TOKEN);
      return;
    } else if (b == ModbusBINTransport.FRAME_START_TOKEN || b == ModbusBINTransport.FRAME_END_TOKEN){
      out.write(b);
      out.write(b);
    }
  }//write


  /**
   * Writes an array of bytes to the raw output stream.
   * Bytes resembling a frame token will be duplicated.
   *
   * @param data the <tt>byte[]</tt> to be written.
   * @throws java.io.IOException if an I/O error occurs.
   */
  public void write(byte[] data) throws IOException {
    for (int i = 0; i < data.length; i++) {
      write(data[i]);
    }
  }//write(byte[])

  /**
   * Writes an array of bytes to the raw output stream.
   * Bytes resembling a frame token will be duplicated.  *
   *
   * @param data the <tt>byte[]</tt> to be written.
   * @param off the offset into the data to start writing from.
   * @param len the number of bytes to be written from off.
   *
   * @throws java.io.IOException if an I/O error occurs.
   */
  public void write(byte[] data, int off, int len) throws IOException {
    for (int i = off; i < len; i++) {
      write(data[i]);
    }
  }//write(byte[])


}//class BINOutputStream
