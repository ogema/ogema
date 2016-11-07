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
//////////////////////////////////////////////////////////////////////////
//
//  File:  SerialFacadeTest.java
//
//  Description: Unit test driver to exerecise the methods for
//  ModbusSerialMaster class.
//
//  Programmer:  JDC (CCC), Wed Feb  4 11:54:23 2004
//
//  Change History: 
//
//  $Log: SerialFacadeTest.java,v $
//  Revision 1.2  2004/10/21 16:44:36  wimpi
//  Please see status file for changes.
//
//  Revision 1.1  2004/09/30 01:45:38  jdcharlton
//  Test driver for ModbusSerialMaster facade
//
//
//
//////////////////////////////////////////////////////////////////////////

package com.ghgande.j2mod.modbus.cmd;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import com.ghgande.j2mod.modbus.util.SerialParameters;

public class SerialFacadeTest {

	private static void printUsage() {
		System.out.println("java com.ghgande.j2mod.modbus.cmd.SerialAITest"
				+ " <portname [String]>" + " <Unit Address [int8]>");
	}

	public static void main(String[] args) {
		int inChar = -1;
		int result = 0;
		boolean finished = false;
		int slaveId = 88;
		String portname = null;
		ModbusSerialMaster msm = null;

		// 1. Setup the parameters
		if (args.length < 2) {
			printUsage();
			System.exit(1);
		} else {
			try {
				portname = args[0];
				slaveId = Integer.parseInt(args[1]);
			} catch (Exception ex) {
				ex.printStackTrace();
				printUsage();
				System.exit(1);
			}
		}

		try {
			System.out.println("Sending test messages to slave: " + slaveId);
			System.out.println("com.ghgande.j2mod.modbus.debug set to: "
					+ System.getProperty("com.ghgande.j2mod.modbus.debug"));

			System.out
					.println("Hit enter to start and <s enter> to terminate the test.");
			inChar = System.in.read();
			if ((inChar == 's') || (inChar == 'S')) {
				System.out.println("Exiting");
				System.exit(0);
			}

			// 2. Setup serial parameters
			SerialParameters params = new SerialParameters();
			params.setPortName(portname);
			params.setBaudRate(9600);
			params.setDatabits(8);
			params.setParity("None");
			params.setStopbits(1);
			params.setEncoding("rtu");
			params.setEcho(false);
			
			if (Modbus.debug)
				System.out.println("Encoding [" + params.getEncoding() + "]");

			// 3. Create the master facade
			msm = new ModbusSerialMaster(params);
			msm.connect();

			do {
				if (msm.writeCoil(slaveId, 4, true) == true) {
					System.out.println("Set output 5 to true");
				} else {
					System.err.println("Error setting slave " + slaveId
							+ " output 5");
				}
				BitVector coils = msm.readCoils(slaveId, 0, 8);
				if (coils != null) {
					System.out.print("Coils:");
					for (int i = 0; i < coils.size(); i++) {
						System.out.print(" " + i + ": " + coils.getBit(i));
					}
					System.out.println();

					try {
						msm.writeMultipleCoils(slaveId, 0, coils);
					} catch (ModbusException ex) {
						System.out.println("Error writing coils: " + result);
					}
				} else {
					System.out.println("Outputs: null");
					msm.disconnect();
					System.exit(-1);
				}

				BitVector digInp = msm.readInputDiscretes(slaveId, 0, 8);

				if (digInp != null) {
					System.out.print("Digital Inputs:");
					for (int i = 0; i < digInp.size(); i++) {
						System.out.print(" " + i + ": " + digInp.getBit(i));
					}
					System.out.println();
					System.out.println("Inputs: "
							+ ModbusUtil.toHex(digInp.getBytes()));
				} else {
					System.out.println("Inputs: null");
					msm.disconnect();
					System.exit(-1);
				}

				InputRegister[] ai = null;
				for (int i = 1000; i < 1010; i++) {
					ai = msm.readInputRegisters(slaveId, i, 1);
					if (ai != null) {
						System.out.print("Tag " + i + ": ");
						for (int n = 0; n < ai.length; n++) {
							System.out.print(" " + ai[n].getValue());
						}
						System.out.println();
					} else {
						System.out.println("Tag: " + i + " null");
						msm.disconnect();
						System.exit(-1);
					}
				}

				Register[] regs = null;
				for (int i = 1000; i < 1005; i++) {
					regs = msm.readMultipleRegisters(slaveId, i, 1);
					if (regs != null) {
						System.out.print("RWRegisters " + i + " length: "
								+ regs.length);
						for (int n = 0; n < regs.length; n++) {
							System.out.print(" " + regs[n].getValue());
						}
						System.out.println();
					} else {
						System.out.println("RWRegisters " + i + ": null");
						msm.disconnect();
						System.exit(-1);
					}
				}
				regs = msm.readMultipleRegisters(slaveId, 0, 10);
				System.out.println("Registers: ");
				if (regs != null) {
					System.out.print("regs :");
					for (int n = 0; n < regs.length; n++) {
						System.out.print("  " + n + "= " + regs[n]);
					}
					System.out.println();
				} else {
					System.out.println("Registers: null");
					msm.disconnect();
					System.exit(-1);
				}
				while (System.in.available() > 0) {
					inChar = System.in.read();
					if ((inChar == 's') || (inChar == 'S')) {
						finished = true;
					}
				}
			} while (!finished);
		} catch (Exception e) {
			System.err.println("SerialFacadeTest driver: " + e);
			e.printStackTrace();
		}
		msm.disconnect();
	}
}
