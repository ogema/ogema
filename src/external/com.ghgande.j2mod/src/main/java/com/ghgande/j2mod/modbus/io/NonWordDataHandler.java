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
package com.ghgande.j2mod.modbus.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.DataInput;

/**
 * Interface implementing a non word data handler for the read/write multiple
 * register commands.
 * 
 * This interface can be used by any class which works with multiple words of
 * data for a non-standard data item. For example, message may involve data
 * items which are floating point values or string.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public interface NonWordDataHandler {

	/**
	 * Returns the intermediate raw non-word data.
	 * 
	 * <p>
	 * An implementation would need to provide a means of converting between the
	 * raw byte data and the registers that are present in actual messages.
	 * 
	 * @return the raw data as <tt>byte[]</tt>.
	 */
	public byte[] getData();

	/**
	 * Reads the non-word raw data based on an arbitrary implemented structure.
	 * 
	 * @param in
	 *            the <tt>DataInput</tt> to read from.
	 * @param reference
	 *            to specify the offset as <tt>int</tt>.
	 * @param count
	 *            to specify the amount of bytes as <tt>int</tt>.
	 * 
	 * @throws IOException
	 *             if I/O fails.
	 * @throws EOFException
	 *             if the stream ends before all data is read.
	 */
	public void readData(DataInput in, int reference, int count)
			throws IOException, EOFException;

	/**
	 * Returns the word count of the data. Note that this should be the length
	 * of the byte array divided by two.
	 * 
	 * @return the number of words the data consists of.
	 */
	public int getWordCount();

	/**
	 * Commits the data if it has been read into an intermediate repository.
	 * 
	 * <p>
	 * This method is called for a message (for example, a
	 * <tt>WriteMultipleRegistersRequest</tt> instance) when finished with
	 * reading, for creating a response.
	 * 
	 * @return -1 if the commit was successful, a Modbus exception code valid
	 *         for the read/write multiple registers commands otherwise.
	 */
	public int commitUpdate();

	/**
	 * Prepares the raw data, putting it together from a backing data store.
	 * 
	 * <p>
	 * This method is called for a message (for example,
	 * <tt>ReadMultipleRegistersRequest</tt>) when finished with reading, for
	 * creating a response.
	 * 
	 * @param reference
	 *            to specify the offset as <tt>int</tt>.
	 * @param count
	 *            to specify the number of bytes as <tt>int</tt>.
	 */
	public void prepareData(int reference, int count);
}