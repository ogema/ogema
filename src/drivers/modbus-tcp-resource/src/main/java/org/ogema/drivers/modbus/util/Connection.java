/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.drivers.modbus.util;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

//import net.wimpi.modbus.ModbusException;
//import net.wimpi.modbus.ModbusIOException;
//import net.wimpi.modbus.ModbusSlaveException;
//import net.wimpi.modbus.io.ModbusTCPTransaction;
//import net.wimpi.modbus.msg.ReadCoilsRequest;
//import net.wimpi.modbus.msg.ReadCoilsResponse;
//import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
//import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
//import net.wimpi.modbus.msg.ReadInputRegistersRequest;
//import net.wimpi.modbus.msg.ReadInputRegistersResponse;
//import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
//import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
//import net.wimpi.modbus.msg.WriteCoilRequest;
//import net.wimpi.modbus.msg.WriteMultipleCoilsRequest;
//import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
//import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
//import net.wimpi.modbus.net.TCPMasterConnection;
//import net.wimpi.modbus.procimg.InputRegister;
//import net.wimpi.modbus.procimg.Register;
//import net.wimpi.modbus.util.BitVector;

import org.ogema.drivers.modbus.tasks.ModbusTask;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadCoilsRequest;
import com.ghgande.j2mod.modbus.msg.ReadCoilsResponse;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesResponse;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.msg.WriteCoilRequest;
import com.ghgande.j2mod.modbus.msg.WriteMultipleCoilsRequest;
import com.ghgande.j2mod.modbus.msg.WriteMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;

/**
 * 
 * Util class for handle modbus connection (example: reading,writing into an
 * input register).
 * 
 * @author brequardt
 */
public class Connection {

	private ModbusTCPTransaction transaction;
	final TCPMasterConnection con;

	public Connection(InetSocketAddress host) {

		con = new TCPMasterConnection(host.getAddress());
		con.setPort(host.getPort());
		try {
			con.connect();
//			con.setTimeout(10000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void connect() throws Exception {

		if (!con.isConnected()) {

			con.connect();
			transaction = new ModbusTCPTransaction(con);

		}
	}

	public boolean isConnected() {
		return con.isConnected();
	}

	public void close() {

		if (con != null && con.isConnected())

			con.close();

	}

	public BitVector readCoils(ModbusTask channel) throws ModbusIOException,
			ModbusSlaveException, ModbusException, SocketTimeoutException,
			SocketException {
		return readCoils(channel.getStartAddress(), channel.getCount(),
				channel.getUnitId());
	}

	public BitVector readDiscreteInputs(ModbusTask channel)
			throws ModbusIOException, ModbusSlaveException, ModbusException,
			SocketTimeoutException, SocketException {
		return readDiscreteInputs(channel.getStartAddress(),
				channel.getCount(), channel.getUnitId());
	}

	public Register[] readHoldingRegisters(ModbusTask channel)
			throws ModbusIOException, ModbusSlaveException, ModbusException,
			SocketTimeoutException, SocketException {
		return readHoldingRegisters(channel.getStartAddress(),
				channel.getCount(), channel.getUnitId());
	}

	public InputRegister[] readInputRegisters(ModbusTask channel)
			throws ModbusIOException, ModbusSlaveException, ModbusException,
			SocketTimeoutException, SocketException {
		return readInputRegisters(channel.getStartAddress(),
				channel.getCount(), channel.getUnitId());
	}

	public void writeSingleCoil(ModbusTask channel, boolean state)
			throws ModbusIOException, ModbusSlaveException, ModbusException,
			SocketTimeoutException {

		WriteCoilRequest writeCoilRequest = new WriteCoilRequest();

		writeCoilRequest.setReference(channel.getStartAddress());
		writeCoilRequest.setCoil(state);
		writeCoilRequest.setUnitID(channel.getUnitId());
		transaction.setRequest(writeCoilRequest);
		transaction.execute();
	}

	public void writeMultipleCoils(ModbusTask channel, BitVector coils)
			throws ModbusIOException, ModbusSlaveException, ModbusException,
			SocketTimeoutException, SocketException {

		WriteMultipleCoilsRequest writeMultipleCoilsRequest = new WriteMultipleCoilsRequest();

		writeMultipleCoilsRequest.setReference(channel.getStartAddress());
		writeMultipleCoilsRequest.setCoils(coils);
		writeMultipleCoilsRequest.setUnitID(channel.getUnitId());
		transaction.setRequest(writeMultipleCoilsRequest);
		transaction.execute();
	}

	public void writeSingleRegister(ModbusTask channel, Register register)
			throws ModbusIOException, ModbusSlaveException, ModbusException,
			SocketTimeoutException, SocketException {

		WriteSingleRegisterRequest writeSingleRegisterRequest = new WriteSingleRegisterRequest();

		writeSingleRegisterRequest.setReference(channel.getStartAddress());
		writeSingleRegisterRequest.setRegister(register);
		writeSingleRegisterRequest.setUnitID(channel.getUnitId());
		transaction.setRequest(writeSingleRegisterRequest);
		transaction.execute();
	}

	public void writeMultipleRegisters(ModbusTask channel, Register[] registers)
			throws ModbusIOException, ModbusSlaveException, ModbusException,
			SocketTimeoutException, SocketException {

		WriteMultipleRegistersRequest writeMultipleRegistersRequest = new WriteMultipleRegistersRequest();

		writeMultipleRegistersRequest.setReference(channel.getStartAddress());
		writeMultipleRegistersRequest.setRegisters(registers);
		writeMultipleRegistersRequest.setUnitID(channel.getUnitId());
		transaction.setRequest(writeMultipleRegistersRequest);
		transaction.execute();
	}

	private BitVector readDiscreteInputs(int startAddress, int count, int unitID)
			throws ModbusIOException, ModbusSlaveException, ModbusException,
			SocketTimeoutException, SocketException {

		ReadInputDiscretesRequest readInputDiscretesRequest = new ReadInputDiscretesRequest();

		readInputDiscretesRequest.setReference(startAddress);
		readInputDiscretesRequest.setBitCount(count);
		readInputDiscretesRequest.setUnitID(unitID);
		transaction.setRequest(readInputDiscretesRequest);
		transaction.execute();
		BitVector bitvector = ((ReadInputDiscretesResponse) transaction
				.getResponse()).getDiscretes();
		bitvector.forceSize(count);
		return bitvector;
	}

	private InputRegister[] readInputRegisters(int startAddress, int count,
			int unitID) throws ModbusIOException, ModbusSlaveException,
			ModbusException, SocketTimeoutException, SocketException {

		ReadInputRegistersRequest readInputRegistersRequest = new ReadInputRegistersRequest();

		readInputRegistersRequest.setReference(startAddress);
		readInputRegistersRequest.setWordCount(count);
		readInputRegistersRequest.setUnitID(unitID);
		transaction.setRequest(readInputRegistersRequest);

		transaction.execute();

		InputRegister[] registers = ((ReadInputRegistersResponse) transaction
				.getResponse()).getRegisters();
		return registers;
	}

	private Register[] readHoldingRegisters(int startAddress, int count,
			int unitID) throws ModbusIOException, ModbusSlaveException,
			ModbusException, SocketTimeoutException, SocketException {

		ReadMultipleRegistersRequest readHoldingRegisterRequest = new ReadMultipleRegistersRequest();

		readHoldingRegisterRequest.setReference(startAddress);
		readHoldingRegisterRequest.setWordCount(count);
		readHoldingRegisterRequest.setUnitID(unitID);
		transaction.setRequest(readHoldingRegisterRequest);

		transaction.execute();

		return ((ReadMultipleRegistersResponse) transaction.getResponse())
				.getRegisters();
	}

	private BitVector readCoils(int startAddress, int count, int unitID)
			throws ModbusIOException, ModbusSlaveException, ModbusException,
			SocketTimeoutException, SocketException {

		ReadCoilsRequest readCoilsRequest = new ReadCoilsRequest();

		readCoilsRequest.setReference(startAddress);
		readCoilsRequest.setBitCount(count);
		readCoilsRequest.setUnitID(unitID);
		transaction.setRequest(readCoilsRequest);
		transaction.execute();
		BitVector bitvector = ((ReadCoilsResponse
				) transaction.getResponse())
				.getCoils();
		bitvector.forceSize(count);
		return bitvector;
	}

}
