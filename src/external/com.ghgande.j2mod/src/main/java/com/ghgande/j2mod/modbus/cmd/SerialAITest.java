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
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.net.SerialConnection;
import com.ghgande.j2mod.modbus.util.SerialParameters;

/**
 * Class that implements a simple commandline
 * tool for reading an analog input.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class SerialAITest {

  public static void main(String[] args) {

    SerialConnection con = null;
    ModbusSerialTransaction trans = null;
    ReadInputRegistersRequest req = null;
    ReadInputRegistersResponse res = null;

    String portname = null;
    int unitid = 0;
    int ref = 0;
    int count = 0;
    int repeat = 1;

    try {

      //1. Setup the parameters
      if (args.length < 4) {
        printUsage();
        System.exit(1);
      } else {
        try {
          portname = args[0];
          unitid = Integer.parseInt(args[1]);
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

      //2. Set slave identifier for master response parsing
      ModbusCoupler.getReference().setUnitID(unitid);

      System.out.println("com.ghgande.j2mod.modbus.debug set to: " +
                         System.getProperty("com.ghgande.j2mod.modbus.debug"));

      //3. Setup serial parameters
      SerialParameters params = new SerialParameters();
      params.setPortName(portname);
      params.setBaudRate(9600);
      params.setDatabits(8);
      params.setParity("None");
      params.setStopbits(1);
      params.setEncoding("ascii");
      params.setEcho(false);
      if (Modbus.debug) System.out.println("Encoding [" + params.getEncoding() + "]");

      //4. Open the connection
      con = new SerialConnection(params);
      con.open();

      //5. Prepare a request
      req = new ReadInputRegistersRequest(ref, count);
      req.setUnitID(unitid);
      req.setHeadless();
      if (Modbus.debug) System.out.println("Request: " + req.getHexMessage());

      //6. Prepare the transaction
      trans = new ModbusSerialTransaction(con);
      trans.setRequest(req);

      //7. Execute the transaction repeat times
      int k = 0;
      do {
        trans.execute();

        res = (ReadInputRegistersResponse) trans.getResponse();
        if (Modbus.debug)
          System.out.println("Response: " + res.getHexMessage());
        for (int n = 0; n < res.getWordCount(); n++) {
          System.out.println("Word " + n + "=" + res.getRegisterValue(n));
        }
        k++;
      } while (k < repeat);

      //8. Close the connection
      con.close();

    } catch (Exception ex) {
      ex.printStackTrace();
      // Close the connection
      con.close();
    }
  }//main

  private static void printUsage() {
    System.out.println(
        "java com.ghgande.j2mod.modbus.cmd.SerialAITest <portname [String]>  <Unit Address [int8]> <register [int16]> <wordcount [int16]> {<repeat [int]>}"
    );
  }//printUsage

}//class SerialAITest
