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
package com.ghgande.j2mod.modbus.procimg;

/**
 * Interface defining implementation specific details of the
 * <tt>ProcessImage</tt>, adding mechanisms for creating and modifying the
 * actual "process image".
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public interface ProcessImageImplementation extends ProcessImage {

	/**
	 * Sets a new <tt>DigitalOut</tt> instance at the given reference.
	 * 
	 * @param ref
	 *            the reference as <tt>int</tt>.
	 * @param _do
	 *            the new <tt>DigitalOut</tt> instance to be set.
	 * 
	 * @throws IllegalAddressException
	 *             if the reference is invalid.
	 */
	public void setDigitalOut(int ref, DigitalOut out)
			throws IllegalAddressException;

	/**
	 * Adds a new <tt>DigitalOut</tt> instance.
	 * 
	 * @param out
	 *            the <tt>DigitalOut</tt> instance to be added.
	 */
	public void addDigitalOut(DigitalOut out);

	/**
	 * Adds a new <tt>DigitalOut</tt> instance at the given reference.
	 * 
	 * @param ref - the reference for the instance.
	 * @param out - the <tt>DigitalOut</tt> instance to be added.
	 */
	public void addDigitalOut(int ref, DigitalOut out);

	/**
	 * Removes a given <tt>DigitalOut</tt> instance.
	 * 
	 * @param out
	 *            the <tt>DigitalOut</tt> instance to be removed.
	 */
	public void removeDigitalOut(DigitalOut out);

	/**
	 * Sets a new <tt>DigitalIn</tt> instance at the given reference.
	 * 
	 * @param ref
	 *            the reference as <tt>int</tt>.
	 * @param di
	 *            the new <tt>DigitalIn</tt> instance to be set.
	 * 
	 * @throws IllegalAddressException
	 *             if the reference is invalid.
	 */
	public void setDigitalIn(int ref, DigitalIn di)
			throws IllegalAddressException;

	/**
	 * Adds a new <tt>DigitalIn</tt> instance.
	 * 
	 * @param di
	 *            the <tt>DigitalIn</tt> instance to be added.
	 */
	public void addDigitalIn(DigitalIn di);

	/**
	 * Adds a new <tt>DigitalIn</tt> instance at the given reference, possibly
	 * creating a hole between the last existing reference and the new object.
	 * 
	 * @param ref - the reference for the new instance.
	 * @param di
	 *            the <tt>DigitalIn</tt> instance to be added.
	 */
	public void addDigitalIn(int ref, DigitalIn di);

	/**
	 * Removes a given <tt>DigitalIn</tt> instance.
	 * 
	 * @param di
	 *            the <tt>DigitalIn</tt> instance to be removed.
	 */
	public void removeDigitalIn(DigitalIn di);

	/**
	 * Sets a new <tt>InputRegister</tt> instance at the given reference.
	 * 
	 * @param ref
	 *            the reference as <tt>int</tt>.
	 * @param reg
	 *            the new <tt>InputRegister</tt> instance to be set.
	 * 
	 * @throws IllegalAddressException
	 *             if the reference is invalid.
	 */
	public void setInputRegister(int ref, InputRegister reg)
			throws IllegalAddressException;

	/**
	 * Adds a new <tt>InputRegister</tt> instance.
	 * 
	 * @param reg
	 *            the <tt>InputRegister</tt> instance to be added.
	 */
	public void addInputRegister(InputRegister reg);

	/**
	 * Adds a new <tt>InputRegister</tt> instance, possibly
	 * creating a hole between the last existing reference and the new object.
	 * 
	 * @param ref - The reference for the new instance.
	 * @param reg
	 *            the <tt>InputRegister</tt> instance to be added.
	 */
	public void addInputRegister(int ref, InputRegister reg);

	/**
	 * Removes a given <tt>InputRegister</tt> instance.
	 * 
	 * @param reg
	 *            the <tt>InputRegister</tt> instance to be removed.
	 */
	public void removeInputRegister(InputRegister reg);

	/**
	 * Sets a new <tt>Register</tt> instance at the given reference.
	 * 
	 * @param ref
	 *            the reference as <tt>int</tt>.
	 * @param reg
	 *            the new <tt>Register</tt> instance to be set.
	 * 
	 * @throws IllegalAddressException
	 *             if the reference is invalid.
	 */
	public void setRegister(int ref, Register reg)
			throws IllegalAddressException;

	/**
	 * Adds a new <tt>Register</tt> instance.
	 * 
	 * @param reg
	 *            the <tt>Register</tt> instance to be added.
	 */
	public void addRegister(Register reg);

	/**
	 * Adds a new <tt>Register</tt> instance, possibly
	 * creating a hole between the last existing reference and the new object.
	 * 
	 * @param ref - the reference for the new instance.
	 * @param reg
	 *            the <tt>Register</tt> instance to be added.
	 */
	public void addRegister(int ref, Register reg);

	/**
	 * Removes a given <tt>Register</tt> instance.
	 * 
	 * @param reg
	 *            the <tt>Register</tt> instance to be removed.
	 */
	public void removeRegister(Register reg);

	/**
	 * Sets a new <tt>File</tt> instance at the given reference.
	 * 
	 * @param ref
	 *            the reference as <tt>int</tt>.
	 * @param reg
	 *            the new <tt>File</tt> instance to be set.
	 * 
	 * @throws IllegalAddressException
	 *             if the reference is invalid.
	 */
	public void setFile(int ref, File reg)
			throws IllegalAddressException;

	/**
	 * Adds a new <tt>File</tt> instance.
	 * 
	 * @param reg
	 *            the <tt>File</tt> instance to be added.
	 */
	public void addFile(File reg);

	/**
	 * Adds a new <tt>File</tt> instance, possibly
	 * creating a hole between the last existing reference and the new object.
	 * 
	 * @param ref - the reference for the new isntance.
	 * @param reg
	 *            the <tt>File</tt> instance to be added.
	 */
	public void addFile(int ref, File reg);

	/**
	 * Removes a given <tt>File</tt> instance.
	 * 
	 * @param reg
	 *            the <tt>File</tt> instance to be removed.
	 */
	public void removeFile(File reg);

	/**
	 * Sets a new <tt>FIFO</tt> instance at the given reference.
	 * 
	 * @param ref
	 *            the reference as <tt>int</tt>.
	 * @param reg
	 *            the new <tt>FIFO</tt> instance to be set.
	 * 
	 * @throws IllegalAddressException
	 *             if the reference is invalid.
	 */
	public void setFIFO(int ref, FIFO reg)
			throws IllegalAddressException;

	/**
	 * Adds a new <tt>FIFO</tt> instance.
	 * 
	 * @param reg
	 *            the <tt>FIFO</tt> instance to be added.
	 */
	public void addFIFO(FIFO reg);

	/**
	 * Adds a new <tt>FIFO</tt> instance, possibly
	 * creating a hole between the last existing reference and the new object.
	 * 
	 * @param ref - the reference for the new instance.
	 * @param reg
	 *            the <tt>FIFO</tt> instance to be added.
	 */
	public void addFIFO(int ref, FIFO reg);

	/**
	 * Removes a given <tt>FIFO</tt> instance.
	 * 
	 * @param reg
	 *            the <tt>FIFO</tt> instance to be removed.
	 */
	public void removeFIFO(FIFO reg);

	/**
	 * Defines the set state (i.e. <b>true</b>) of a digital input or output.
	 */
	public static final byte DIG_TRUE = 1;

	/**
	 * Defines the unset state (i.e. <b>false</b>) of a digital input or output.
	 */
	public static final byte DIG_FALSE = 0;

	/**
	 * Defines the invalid (unset, neither true nor false) state of a digital
	 * input or output.
	 */
	public static final byte DIG_INVALID = -1;
}
