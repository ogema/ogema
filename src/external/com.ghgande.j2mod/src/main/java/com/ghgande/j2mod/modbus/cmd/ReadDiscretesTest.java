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
 ***/
package com.ghgande.j2mod.modbus.cmd;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransport;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesResponse;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;

/**
 * Class that implements a simple command line tool for reading a digital input.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class ReadDiscretesTest {

	private static void printUsage() {
		System.out.println(
				"java com.ghgande.j2mod.modbus.cmd.ReadDiscretesTest <connection [String]> <unit [int8]> <register [int16]> <bitcount [int16]> {<repeat [int]>}");
	}

	public static void main(String[] args) {
		ReadInputDiscretesRequest req = null;
		ReadInputDiscretesResponse res = null;
		ModbusTransport transport = null;
		ModbusTransaction trans = null;
		int ref = 0;
		int count = 0;
		int repeat = 1;
		int unit = 0;

		try {

			// 1. Setup the parameters
			if (args.length < 4 || args.length > 5) {
				printUsage();
				System.exit(1);
			} else {
				try {
					transport = ModbusMasterFactory.createModbusMaster(args[0]);
					
					if (transport instanceof ModbusSerialTransport) {
						((ModbusSerialTransport) transport).setReceiveTimeout(500);
						if (System.getProperty("com.ghgande.j2mod.modbus.baud") != null)
							((ModbusSerialTransport) transport).setBaudRate(Integer.parseInt(System.getProperty("com.ghgande.j2mod.modbus.baud")));
						else
							((ModbusSerialTransport) transport).setBaudRate(19200);
					}
									
					/*
					 * There are a number of devices which won't initialize immediately
					 * after being opened.  Take a moment to let them come up.
					 */
					Thread.sleep(2000);
					
					unit = Integer.parseInt(args[1]);
					ref = Integer.parseInt(args[2]);
					count = Integer.parseInt(args[3]);
					if (args.length == 5) {
						repeat = Integer.parseInt(args[4]);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					printUsage();
					System.exit(1);
				}
			}

			req = new ReadInputDiscretesRequest(ref, count);
			req.setUnitID(unit);
			if (Modbus.debug)
				System.out.println("Request: " + req.getHexMessage());

			// 4. Prepare the transaction
			trans = transport.createTransaction();
			trans.setRequest(req);

			if (trans instanceof ModbusTCPTransaction)
				((ModbusTCPTransaction) trans).setReconnecting(true);

			// 5. Execute the transaction repeat times
			int k = 0;
			do {
				trans.execute();

				res = (ReadInputDiscretesResponse) trans.getResponse();

				if (Modbus.debug)
					System.out.println("Response: " + res.getHexMessage());

				System.out.println("Input Discretes Status="
						+ res.getDiscretes().toString());

				k++;
			} while (k < repeat);

			// 6. Close the connection
			transport.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		System.exit(0);
	}
}