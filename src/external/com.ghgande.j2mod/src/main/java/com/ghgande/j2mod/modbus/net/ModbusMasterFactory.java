package com.ghgande.j2mod.modbus.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import gnu.io.CommPort;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPort;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.ModbusRTUTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusUDPTransport;
import com.ghgande.j2mod.modbus.util.SerialParameters;

/**
 * Create a <tt>ModbusListener</tt> from an URI-like specifier.
 * 
 * @author Julie
 * 
 */
public class ModbusMasterFactory {
	public static ModbusTransport createModbusMaster(String address) {
		String parts[] = address.split(":");
		if (parts == null || parts.length < 2)
			throw new IllegalArgumentException("missing connection information");

		if (parts[0].toLowerCase().equals("device")) {
			/*
			 * Create a ModbusSerialListener with the default Modbus values of
			 * 19200 baud, no parity, using the specified device. If there is an
			 * additional part after the device name, it will be used as the
			 * Modbus unit number.
			 */
			SerialParameters parms = new SerialParameters();
			parms.setPortName(parts[1]);
			parms.setBaudRate(19200);
			parms.setDatabits(8);
			parms.setEcho(false);
			parms.setParity(SerialPort.PARITY_NONE);
			parms.setFlowControlIn(SerialPort.FLOWCONTROL_NONE);

			try {
				ModbusRTUTransport transport = new ModbusRTUTransport();
				CommPort port = new RXTXPort(parms.getPortName());

				transport.setCommPort(port);
				transport.setEcho(false);

				return transport;
			} catch (PortInUseException e) {
				return null;
			} catch (IOException e) {
				return null;
			}
		} else if (parts[0].toLowerCase().equals("tcp")) {
			/*
			 * Create a ModbusTCPListener with the default interface value. The
			 * second optional value is the TCP port number and the third
			 * optional value is the Modbus unit number.
			 */
			String hostName = parts[1];
			int port = Modbus.DEFAULT_PORT;

			if (parts.length > 2)
				port = Integer.parseInt(parts[2]);

			try {
				Socket socket = new Socket(hostName, port);
				if (Modbus.debug)
					System.err.println("connecting to " + socket);
				
				ModbusTCPTransport transport = new ModbusTCPTransport(socket);

				return transport;
			} catch (UnknownHostException x) {
				return null;
			} catch (IOException e) {
				return null;
			}
		} else if (parts[0].toLowerCase().equals("udp")) {
			/*
			 * Create a ModbusUDPListener with the default interface value. The
			 * second optional value is the TCP port number and the third
			 * optional value is the Modbus unit number.
			 */
			String hostName = parts[1];
			int port = Modbus.DEFAULT_PORT;

			if (parts.length > 2)
				port = Integer.parseInt(parts[2]);

			UDPMasterTerminal terminal;
			try {
				terminal = new UDPMasterTerminal(
						InetAddress.getByName(hostName));
				terminal.setRemotePort(port);
				terminal.activate();
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			ModbusUDPTransport transport = terminal.getModbusTransport();

			return transport;
		} else
			throw new IllegalArgumentException("unknown type " + parts[0]);
	}
}
