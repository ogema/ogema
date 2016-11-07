/**
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
 */
package com.ghgande.j2mod.modbus.cmd;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransport;
import com.ghgande.j2mod.modbus.msg.ExceptionResponse;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadFIFOQueueRequest;
import com.ghgande.j2mod.modbus.msg.ReadFIFOQueueResponse;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;

/**
 * ReadFIFOTest -- Exercise the "READ FIFO" Modbus
 * message.
 * 
 * @author Julie
 * @version 1.03
 */
public class ReadFIFOTest {

	/**
	 * usage -- Print command line arguments and exit.
	 */
	private static void usage() {
		System.out.println(
				"Usage: ReadFIFOTest connection unit fifo [repeat]");
		
		System.exit(1);
	}

	public static void main(String[] args) {
		ModbusTransport	transport = null;
		ReadFIFOQueueRequest request = null;
		ReadFIFOQueueResponse response = null;
		ModbusTransaction	trans = null;
		int			unit = 0;
		int			fifo = 0;
		int			requestCount = 1;

		/*
		 * Get the command line parameters.
		 */
		if (args.length < 3 || args.length > 4)
			usage();
		
		try {
			transport = ModbusMasterFactory.createModbusMaster(args[0]);
			if (transport instanceof ModbusSerialTransport) {
				((ModbusSerialTransport) transport).setReceiveTimeout(500);
				if (System.getProperty("com.ghgande.j2mod.modbus.baud") != null)
					((ModbusSerialTransport) transport).setBaudRate(Integer.parseInt(System.getProperty("com.ghgande.j2mod.modbus.baud")));
				else
					((ModbusSerialTransport) transport).setBaudRate(19200);
				
				Thread.sleep(2000);
			}
			unit = Integer.parseInt(args[1]);
			fifo = Integer.parseInt(args[2]);
			
			if (args.length > 3)
				requestCount = Integer.parseInt(args[3]);
		} catch (NumberFormatException x) {
			System.err.println("Invalid parameter");
			usage();
		} catch (Exception ex) {
			ex.printStackTrace();
			usage();
			System.exit(1);
		}

		try {
			for (int i = 0; i < requestCount; i++) {
				/*
				 * Setup the READ FILE RECORD request.  The record number
				 * will be incremented for each loop.
				 */
				request = new ReadFIFOQueueRequest();
				request.setUnitID(unit);
				request.setReference(fifo);
				
				if (Modbus.debug)
					System.out.println("Request: " + request.getHexMessage());

				/*
				 * Setup the transaction.
				 */
				trans = transport.createTransaction();
				trans.setRequest(request);

				/*
				 * Execute the transaction.
				 */
				try {
					trans.execute();
				} catch (ModbusSlaveException x) {
					System.err.println("Slave Exception: " +
							x.getLocalizedMessage());
					continue;
				} catch (ModbusIOException x) {
					System.err.println("I/O Exception: " +
							x.getLocalizedMessage());
					continue;					
				} catch (ModbusException x) {
					System.err.println("Modbus Exception: " +
							x.getLocalizedMessage());
					continue;					
				}

				ModbusResponse dummy = trans.getResponse();
				if (dummy == null) {
					System.err.println("No response for transaction " + i);
					continue;
				}
				if (dummy instanceof ExceptionResponse) {
					ExceptionResponse exception = (ExceptionResponse) dummy;

					System.err.println(exception);

					continue;
				} else if (dummy instanceof ReadFIFOQueueResponse) {
					response = (ReadFIFOQueueResponse) dummy;

					if (Modbus.debug)
						System.out.println("Response: "
								+ response.getHexMessage());

					int count = response.getWordCount();
					System.out.println(count + " values");
					
					for (int j = 0;j < count;j++) {
						short value = (short) response.getRegister(j);
						
						System.out.println("data[" + j + "] = " + value);
					}
					continue;
				}

				/*
				 * Unknown message.
				 */
				System.out.println(
						"Unknown Response: " + dummy.getHexMessage());
			}
			
			/*
			 * Teardown the connection.
			 */
			transport.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.exit(0);
	}
}
