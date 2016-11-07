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
 * Copyright 2012-2014, Julianne Frances Haugh
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

import java.io.IOException;
import java.util.Arrays;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusRTUTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransport;
import com.ghgande.j2mod.modbus.msg.ExceptionResponse;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;
import com.ghgande.j2mod.modbus.procimg.InputRegister;

/**
 * Class that implements a simple command line tool for reading an analog
 * input over a Modbus/TCP connection.
 * 
 * <p>
 * Note that if you read from a remote I/O with a Modbus protocol stack, it will
 * most likely expect that the communication is <i>kept alive</i> after the
 * first read message.
 * 
 * <p>
 * This can be achieved either by sending any kind of message, or by repeating
 * the read message within a given period of time.
 * 
 * <p>
 * If the time period is exceeded, then the device might react by turning off
 * all signals of the I/O modules. After this timeout, the device might require
 * a reset message.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author Julie Haugh
 * @version 1.03 (1/18/2014)
 */
public class ReadInputRegistersTest {

	private static void printUsage() {
		System.out.println("java com.ghgande.j2mod.modbus.cmd.ReadInputRegistersTest"
				+ " <address{:port{:unit}} [String]>"
				+ " <base [int]> <count [int]> {<repeat [int]>}");
	}

	public static void main(String[] args) {
		ModbusTransport transport = null;
		ModbusRequest req = null;
		ModbusTransaction trans = null;
		int ref = 0;
		int count = 0;
		int repeat = 1;
		int unit = 0;

		// 1. Setup parameters
		if (args.length < 3) {
			printUsage();
			System.exit(1);
		}

		try {
			try {
				// 2. Open the connection.
				transport = ModbusMasterFactory.createModbusMaster(args[0]);
				if (transport == null) {
					System.err.println("Cannot open " + args[0]);
					System.exit(1);
				}
				
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
				
				ref = Integer.parseInt(args[1]);
				count = Integer.parseInt(args[2]);

				if (args.length > 3)
					repeat = Integer.parseInt(args[3]);
				
				if (transport instanceof ModbusTCPTransport) {
					String	parts[] = args[0].split(":");
					if (parts.length >= 4)
						unit = Integer.parseInt(parts[3]);
				} else if (transport instanceof ModbusRTUTransport) {
					String parts[] = args[0].split(":");
					if (parts.length >= 3)
						unit = Integer.parseInt(parts[2]);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				printUsage();
				System.exit(1);
			}

			// 5. Execute the transaction repeat times

			for (int k = 0;k < repeat;k++) {
				System.err.println("Request " + k);
				
				// 3. Create the command.
				req = new ReadInputRegistersRequest(ref, count);
				req.setUnitID(unit);
				
				// 4. Prepare the transaction
				trans = transport.createTransaction();
				trans.setRequest(req);
				trans.setRetries(1);
				req.setHeadless(trans instanceof ModbusSerialTransaction);
				
				if (Modbus.debug)
					System.out.println("Request: " + req.getHexMessage());

				if (trans instanceof ModbusSerialTransaction) {
					/*
					 * 10ms interpacket delay.
					 */
					((ModbusSerialTransaction) trans).setTransDelayMS(10);
				}
				
				if (trans instanceof ModbusTCPTransaction) {
					((ModbusTCPTransaction) trans).setReconnecting(true);
				}

				try {
					trans.execute();
				} catch (ModbusException x) {
					System.err.println(x.getMessage());
					continue;
				}
				ModbusResponse res = trans.getResponse();

				if (Modbus.debug) {
					if (res != null)
						System.out.println("Response: " + res.getHexMessage());
					else
						System.err.println("No response to READ INPUT request.");
				}
				
				if (res == null)
					continue;
				
				if (res instanceof ExceptionResponse) {
					ExceptionResponse exception = (ExceptionResponse) res;
					System.out.println(exception);
					continue;
				}

				if (! (res instanceof ReadInputRegistersResponse))
					continue;
				
				ReadInputRegistersResponse data = (ReadInputRegistersResponse) res;
				InputRegister values[] = data.getRegisters();
				
				System.out.println("Data: " + Arrays.toString(values));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		try {
			// 6. Close the connection
			if (transport != null)
				transport.close();
		} catch (IOException e) {
			// Do nothing.
		}
		System.exit(0);
	}
}
