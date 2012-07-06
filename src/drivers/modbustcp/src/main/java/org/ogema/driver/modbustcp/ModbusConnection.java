/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.driver.modbustcp;

import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.ModbusIOException;
import net.wimpi.modbus.ModbusSlaveException;
import net.wimpi.modbus.io.ModbusTransaction;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.msg.WriteMultipleCoilsRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.BitVector;

public abstract class ModbusConnection {

	private final ReadCoilsRequest readCoilsRequest;
	private final ReadInputDiscretesRequest readInputDiscretesRequest;
	private final WriteCoilRequest writeCoilRequest;
	private final WriteMultipleCoilsRequest writeMultipleCoilsRequest;
	private final ReadInputRegistersRequest readInputRegistersRequest;
	private final ReadMultipleRegistersRequest readHoldingRegisterRequest;
	private final WriteSingleRegisterRequest writeSingleRegisterRequest;
	private final WriteMultipleRegistersRequest writeMultipleRegistersRequest;

	private ModbusTransaction transaction;

	public abstract void connect() throws Exception;

	public abstract void disconnect();

	public ModbusConnection() {

		transaction = null;

		readCoilsRequest = new ReadCoilsRequest();
		readInputDiscretesRequest = new ReadInputDiscretesRequest();
		readInputRegistersRequest = new ReadInputRegistersRequest();
		readHoldingRegisterRequest = new ReadMultipleRegistersRequest();
		writeCoilRequest = new WriteCoilRequest();
		writeMultipleCoilsRequest = new WriteMultipleCoilsRequest();
		writeSingleRegisterRequest = new WriteSingleRegisterRequest();
		writeMultipleRegistersRequest = new WriteMultipleRegistersRequest();
	}

	public void setTransaction(ModbusTransaction transaction) {
		this.transaction = transaction;
	}

	protected synchronized BitVector readCoils(int startAddress, int count, int unitID) throws ModbusException {
		readCoilsRequest.setReference(startAddress);
		readCoilsRequest.setBitCount(count);
		readCoilsRequest.setUnitID(unitID);
		transaction.setRequest(readCoilsRequest);
		transaction.execute();
		BitVector bitvector = ((ReadCoilsResponse) transaction.getResponse()).getCoils();
		bitvector.forceSize(count);
		return bitvector;

	}

	public BitVector readCoils(ModbusChannel channel) throws ModbusException {
		return readCoils(channel.getStartAddress(), channel.getCount(), channel.getUnitId());
	}

	public BitVector readCoils(ModbusChannelGroup channelGroup) throws ModbusException {
		return readCoils(channelGroup.getStartAddress(), channelGroup.getCount(), channelGroup.getUnitId());
	}

	protected synchronized BitVector readDiscreteInputs(int startAddress, int count, int unitID) throws ModbusException {
		readInputDiscretesRequest.setReference(startAddress);
		readInputDiscretesRequest.setBitCount(count);
		readInputDiscretesRequest.setUnitID(unitID);
		transaction.setRequest(readInputDiscretesRequest);
		transaction.execute();
		BitVector bitvector = ((ReadInputDiscretesResponse) transaction.getResponse()).getDiscretes();
		bitvector.forceSize(count);
		return bitvector;
	}

	public BitVector readDiscreteInputs(ModbusChannel channel) throws ModbusException {
		return readDiscreteInputs(channel.getStartAddress(), channel.getCount(), channel.getUnitId());
	}

	public BitVector readDiscreteInputs(ModbusChannelGroup channelGroup) throws ModbusException {
		return readDiscreteInputs(channelGroup.getStartAddress(), channelGroup.getCount(), channelGroup.getUnitId());
	}

	public synchronized Register[] readHoldingRegisters(int startAddress, int count, int unitID) throws ModbusException {
		readHoldingRegisterRequest.setReference(startAddress);
		readHoldingRegisterRequest.setWordCount(count);
		readHoldingRegisterRequest.setUnitID(unitID);
		transaction.setRequest(readHoldingRegisterRequest);
		transaction.execute();
		return ((ReadMultipleRegistersResponse) transaction.getResponse()).getRegisters();
	}

	public Register[] readHoldingRegisters(ModbusChannel channel) throws ModbusException {
		return readHoldingRegisters(channel.getStartAddress(), channel.getCount(), channel.getUnitId());
	}

	public Register[] readHoldingRegisters(ModbusChannelGroup channelGroup) throws ModbusException {
		return readHoldingRegisters(channelGroup.getStartAddress(), channelGroup.getCount(), channelGroup.getUnitId());
	}

	/**
	 * Read InputRegisters
	 * 
	 * @param startAddress
	 * @param count
	 * @param unitID
	 * @return the InputRegister[]
	 * @throws ModbusIOException
	 * @throws ModbusSlaveException
	 * @throws ModbusException
	 */
	protected synchronized InputRegister[] readInputRegisters(int startAddress, int count, int unitID)
			throws ModbusIOException, ModbusSlaveException, ModbusException {
		readInputRegistersRequest.setReference(startAddress);
		readInputRegistersRequest.setWordCount(count);
		readInputRegistersRequest.setUnitID(unitID);
		transaction.setRequest(readInputRegistersRequest);
		transaction.execute();
		InputRegister[] registers = ((ReadInputRegistersResponse) transaction.getResponse()).getRegisters();
		return registers;
	}

	/**
	 * Read InputRegisters for a channel
	 * 
	 * @param channel
	 * @return the InputRegister
	 * @throws ModbusException
	 */
	public InputRegister[] readInputRegisters(ModbusChannel channel) throws ModbusException {
		return readInputRegisters(channel.getStartAddress(), channel.getCount(), channel.getUnitId());
	}

	/**
	 * Read InputRegisters for a channelGroup
	 * 
	 * @param channelGroup
	 * @return the InputRegister
	 * @throws ModbusException
	 */
	public InputRegister[] readInputRegisters(ModbusChannelGroup channelGroup) throws ModbusException {
		return readInputRegisters(channelGroup.getStartAddress(), channelGroup.getCount(), channelGroup.getUnitId());
	}

	public synchronized void writeSingleCoil(ModbusChannel channel, boolean state) throws ModbusException {
		writeCoilRequest.setReference(channel.getStartAddress());
		writeCoilRequest.setCoil(state);
		writeCoilRequest.setUnitID(channel.getUnitId());
		transaction.setRequest(writeCoilRequest);
		transaction.execute();
	}

	public synchronized void writeMultipleCoils(ModbusChannel channel, BitVector coils) throws ModbusException {
		writeMultipleCoilsRequest.setReference(channel.getStartAddress());
		writeMultipleCoilsRequest.setCoils(coils);
		writeMultipleCoilsRequest.setUnitID(channel.getUnitId());
		transaction.setRequest(writeMultipleCoilsRequest);
		transaction.execute();
	}

	public synchronized void writeSingleRegister(ModbusChannel channel, Register register) throws ModbusException {

		writeSingleRegisterRequest.setReference(channel.getStartAddress());
		writeSingleRegisterRequest.setRegister(register);
		writeSingleRegisterRequest.setUnitID(channel.getUnitId());
		transaction.setRequest(writeSingleRegisterRequest);
		transaction.execute();
	}

	public synchronized void writeMultipleRegisters(ModbusChannel channel, Register[] registers) throws ModbusException {
		writeMultipleRegistersRequest.setReference(channel.getStartAddress());
		writeMultipleRegistersRequest.setRegisters(registers);
		writeMultipleRegistersRequest.setUnitID(channel.getUnitId());
		transaction.setRequest(writeMultipleRegistersRequest);
		transaction.execute();
	}

}
