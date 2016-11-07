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
package com.ghgande.j2mod.modbus.cmd;

import java.io.IOException;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.ModbusRTUTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransport;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

/**
 * Class that implements a simple command line tool for writing to an analog
 * output over a Modbus/TCP connection.
 * 
 * <p>
 * Note that if you write to a remote I/O with a Modbus protocol stack, it will
 * most likely expect that the communication is <i>kept alive</i> after the
 * first write message.
 * 
 * <p>
 * This can be achieved either by sending any kind of message, or by repeating
 * the write message within a given period of time.
 * 
 * <p>
 * If the time period is exceeded, then the device might react by turning off
 * all signals of the I/O modules. After this timeout, the device might require
 * a reset message.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author jfhaugh
 * @version @version@ (@date@)
 */
public class WriteHoldingRegisterTest {

	private static void printUsage() {
		System.out.println("java com.ghgande.j2mod.modbus.cmd.WriteHoldingRegisterTest"
				+ " <connection [String]>"
				+ " <register [int]> <value [int]> {<repeat [int]>}");
	}

	public static void main(String[] args) {

		ModbusRequest req = null;
		ModbusTransaction trans = null;
		ModbusTransport transport = null;
		int ref = 0;
		int value = 0;
		int repeat = 1;
		int unit = 0;

		// 1. Setup parameters
		if (args.length < 3) {
			printUsage();
			System.exit(1);
		}

		try {
			try {
				transport = ModbusMasterFactory.createModbusMaster(args[0]);
				
				if (transport instanceof ModbusSerialTransport) {
					((ModbusSerialTransport) transport).setReceiveTimeout(500);
					if (System.getProperty("com.ghgande.j2mod.modbus.baud") != null)
						((ModbusSerialTransport) transport).setBaudRate(Integer.parseInt(System.getProperty("com.ghgande.j2mod.modbus.baud")));
					else
						((ModbusSerialTransport) transport).setBaudRate(19200);
					
					/*
					 * Some serial devices are slow to wake up.
					 */
					Thread.sleep(2000);
				}
								
				ref = Integer.parseInt(args[1]);
				value = Integer.parseInt(args[2]);

				if (args.length == 4)
					repeat = Integer.parseInt(args[3]);
				
				if (transport instanceof ModbusTCPTransport) {
					String	parts[] = args[0].split(":");
					if (parts.length >= 4)
						unit = Integer.parseInt(parts[3]);
				} else if (transport instanceof ModbusRTUTransport) {
					String parts[] = args[0].split(":");
					if (parts.length >= 3)
						unit = Integer.parseInt(parts[2]);
					
					String baud = System.getProperty("com.ghgande.j2mod.modbus.baud");
					if (baud != null) {
						((ModbusRTUTransport) transport).setBaudRate(Integer.parseInt(baud));
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				printUsage();
				System.exit(1);
			}

			req = new WriteSingleRegisterRequest(ref,
					new SimpleRegister(value));

			req.setUnitID(unit);

			// 3. Prepare the transaction
			trans = transport.createTransaction();
			trans.setRequest(req);
			req.setHeadless(trans instanceof ModbusSerialTransaction);

			if (Modbus.debug)
				System.out.println("Request: " + req.getHexMessage());

			// 4. Execute the transaction repeat times

			for (int count = 0; count < repeat; count++) {
				trans.execute();

				if (Modbus.debug)
					System.out.println("Response: "
							+ trans.getResponse().getHexMessage());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				transport.close();
			} catch (IOException e) {
				// Do nothing.
			}
		}
		System.exit(0);
	}
}
