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

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.net.ModbusSerialListener;
import com.ghgande.j2mod.modbus.procimg.*;
import com.ghgande.j2mod.modbus.util.SerialParameters;

/**
 * Class implementing a simple Modbus slave. A simple process image is available
 * to test functionality and behavior of the implementation.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author Julie Haugh
 * Added ability to specify the number of coils, discreates, input and
 * holding registers.
 */
public class SerialSlaveTest {

	public static void main(String[] args) {

		ModbusSerialListener listener = null;
		SimpleProcessImage spi = new SimpleProcessImage();
		String portname = null;
		boolean	hasUnit = false;
		int unit = 2;
		int		coils = 2;
		int		discretes = 4;
		boolean	hasInputs = false;
		int		inputs = 1;
		boolean	hasHoldings = false;
		int		holdings = 1;
		int		arg = 0;
		
		for (arg = 0;arg < args.length;arg++) {
			if (args[arg].equals("--port") || args[arg].equals("-p"))
				portname = args[++arg];
			else if (args[arg].equals("--unit") || args[arg].equals("-u")) {
				unit = Integer.parseInt(args[++arg]);
				hasUnit = true;
			} else if (args[arg].equals("--coils") || args[arg].equals("-c")) {
				coils = Integer.parseInt(args[++arg]);
			} else if (args[arg].equals("--discretes") || args[arg].equals("-d")) {
				discretes = Integer.parseInt(args[++arg]);
			} else if (args[arg].equals("--inputs") || args[arg].equals("-i")) {
				inputs = Integer.parseInt(args[++arg]);
				hasInputs = true;
			} else if (args[arg].equals("--holdings") || args[arg].equals("-h")) {
				holdings = Integer.parseInt(args[++arg]);
				hasHoldings = true;
			} else
				break;
		}
		
		if (arg < args.length && portname == null)
			portname = args[arg++];
		
		if (arg < args.length && ! hasUnit)
			unit = Integer.parseInt(args[arg++]);

		if (Modbus.debug)
			System.out.println("j2mod ModbusSerial Slave");

		try {

			/*
			 * Prepare a process image.
			 * 
			 * The file records from the TCP and UDP test harnesses are
			 * not included.  They can be added if there is a need to
			 * test READ FILE RECORD and WRITE FILE RECORD with a Modbus/RTU
			 * device.
			 */
			spi = new SimpleProcessImage();
			
			for (int i = 0;i < coils;i++)
				spi.addDigitalOut(new SimpleDigitalOut(i % 2 == 0));

			for (int i = 0;i < discretes;i++)
				spi.addDigitalIn(new SimpleDigitalIn(i % 2 == 0));
			
			if (hasHoldings) {
				System.out.println("Adding " + holdings + " holding registers");
				
				for (int i = 0;i < holdings;i++)
					spi.addRegister(new SimpleRegister(i));
			} else
				spi.addRegister(new SimpleRegister(251));
			
			if (hasInputs) {
				System.out.println("Adding " + inputs + " input registers");
				
				for (int i = 0;i < inputs;i++)
					spi.addInputRegister(new SimpleInputRegister(i));
			} else			
				spi.addInputRegister(new SimpleInputRegister(45));

			// 2. Create the coupler and set the slave identity
			ModbusCoupler.getReference().setProcessImage(spi);
			ModbusCoupler.getReference().setMaster(false);
			ModbusCoupler.getReference().setUnitID(unit);

			// 3. Set up serial parameters
			SerialParameters params = new SerialParameters();
			
			params.setPortName(portname);
			params.setBaudRate(19200);
			params.setDatabits(8);
			params.setParity("None");
			params.setStopbits(1);
			params.setEncoding("rtu");
			params.setEcho(false);
			if (Modbus.debug)
				System.out.println("Encoding [" + params.getEncoding() + "]");

			// 4. Set up serial listener
			listener = new ModbusSerialListener(params);
			listener.setListening(true);
			
			// 5. Start the listener thread.
			new Thread(listener).start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
