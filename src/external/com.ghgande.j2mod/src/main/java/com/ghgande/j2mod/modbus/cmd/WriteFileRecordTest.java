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

import java.io.IOException;
import java.util.Arrays;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransport;
import com.ghgande.j2mod.modbus.msg.ExceptionResponse;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.WriteFileRecordRequest;
import com.ghgande.j2mod.modbus.msg.WriteFileRecordRequest.RecordRequest;
import com.ghgande.j2mod.modbus.msg.WriteFileRecordResponse;
import com.ghgande.j2mod.modbus.msg.WriteFileRecordResponse.RecordResponse;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;

/**
 * ReadFileRecordText -- Exercise the "READ FILE RECORD" Modbus
 * message.
 * 
 * @author Julie
 * @version 0.96
 */
public class WriteFileRecordTest {

	/**
	 * usage -- Print command line arguments and exit.
	 */
	private static void usage() {
		System.out.println(
				"Usage: WriteFileRecordTest connection unit file record value [value ...]");
		
		System.exit(1);
	}

	public static void main(String[] args) {
		ModbusTransport	transport = null;
		WriteFileRecordRequest request = null;
		WriteFileRecordResponse response = null;
		ModbusTransaction	trans = null;
		int			unit = 0;
		int			file = 0;
		int			record = 0;
		int			registers = 0;
		short		values[] = null;
		boolean		isSerial = false;

		/*
		 * Get the command line parameters.
		 */
		if (args.length < 6)
			usage();
		
		try {
			transport = ModbusMasterFactory.createModbusMaster(args[0]);
			if (transport instanceof ModbusSerialTransport) {
				((ModbusSerialTransport) transport).setReceiveTimeout(500);
				((ModbusSerialTransport) transport).setBaudRate(19200);
				isSerial = true;
				
				Thread.sleep(2000);
			}
			unit = Integer.parseInt(args[1]);
			file = Integer.parseInt(args[2]);
			record = Integer.parseInt(args[3]);
			
			if (args.length > 4) {
				registers = args.length - 4;
				values = new short[registers];
				
				for (int i = 0;i < registers;i++) {
					values[i] = Short.parseShort(args[i + 4]);
				}
			}
		} catch (NumberFormatException x) {
			System.err.println("Invalid parameter");
			usage();
		} catch (Exception ex) {
			ex.printStackTrace();
			usage();
			System.exit(1);
		}

		try {
			/*
			 * Setup the WRITE FILE RECORD request.
			 */
			request = new WriteFileRecordRequest();
			request.setUnitID(unit);
			if (isSerial)
				request.setHeadless(true);

			RecordRequest recordRequest = request.new RecordRequest(file,
					record, values);
			request.addRequest(recordRequest);

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
				System.err.println("Slave Exception: "
						+ x.getLocalizedMessage());
				System.exit(1);
			} catch (ModbusIOException x) {
				System.err.println("I/O Exception: " + x.getLocalizedMessage());
				System.exit(1);
			} catch (ModbusException x) {
				System.err.println("Modbus Exception: "
						+ x.getLocalizedMessage());
				System.exit(1);
			}

			ModbusResponse dummy = trans.getResponse();
			if (dummy == null) {
				System.err.println("No response for transaction ");
				System.exit(1);
			}
			if (dummy instanceof ExceptionResponse) {
				ExceptionResponse exception = (ExceptionResponse) dummy;

				System.err.println(exception);
			} else if (dummy instanceof WriteFileRecordResponse) {
				response = (WriteFileRecordResponse) dummy;

				if (Modbus.debug)
					System.out.println("Response: " + response.getHexMessage());

				int count = response.getRequestCount();
				for (int j = 0; j < count; j++) {
					RecordResponse data = response.getRecord(j);
					values = new short[data.getWordCount()];
					for (int k = 0; k < data.getWordCount(); k++)
						values[k] = data.getRegister(k).toShort();

					System.out.println("data[" + j + "] = "
							+ Arrays.toString(values));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (IOException e) {
					// Do nothing.
				}
			}
		}
		System.exit(0);
	}
}
