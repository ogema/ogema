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
import java.io.InputStream;
import java.io.FilterInputStream;

/**
 * Class implementing a specialized <tt>InputStream</tt> which
 * handles binary transmitted messages.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 *
 * @see ModbusBINTransport#FRAME_START
 * @see ModbusBINTransport#FRAME_END
 */
public class BINInputStream
    extends FilterInputStream {

  /**
   * Constructs a new <tt>BINInputStream</tt> instance
   * reading from the given <tt>InputStream</tt>.
   *
   * @param in a base input stream to be wrapped.
   */
  public BINInputStream(InputStream in) {
    super(in);
    if(!in.markSupported()) {
      throw new RuntimeException("Accepts only input streams that support marking.");
    }
  }//constructor


  /**
   * Reads a byte from the BIN encoded stream.
   *
   * @return int the byte read from the stream.
   * @throws java.io.IOException if an I/O error occurs.
   */
  public int read() throws IOException {
    int ch = in.read();
    if(ch == -1) {
      return -1;
    } else if (ch == ModbusBINTransport.FRAME_START_TOKEN) {
      in.mark(1);
      //read next
      ch = in.read();
      if(ch == ModbusBINTransport.FRAME_START_TOKEN) {
        return ch;
      } else {
        in.reset();
        return ModbusBINTransport.FRAME_START;
      }
    } else if(ch == ModbusBINTransport.FRAME_END_TOKEN) {
      in.mark(1);
      //read next
      ch = in.read();
      if(ch == ModbusBINTransport.FRAME_END_TOKEN) {
        return ch;
      } else {
        in.reset();
        return ModbusBINTransport.FRAME_END;
      }
    } else {
      return ch;
    }
  }//read



}//class BINInputStream
