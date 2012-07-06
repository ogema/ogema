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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import net.wimpi.modbus.ModbusException;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.modbustcp.enums.EDatatype;
import org.ogema.driver.modbustcp.enums.EFunctionCode;
import org.ogema.driver.modbustcp.enums.EPrimaryTable;

public class ModbusChannel extends Channel {

	/** Contains values to define the access method of the channel */
	public static enum EAccess {
		READ, WRITE
	}

	/** A Parameter of the channel address */
	public static final int IGNORE_UNIT_ID = -1;

	/** A Parameter of the channel address */
	private final int UNITID = 0;

	/** A Parameter of the channel address */
	private final int PRIMARYTABLE = 1;

	/** A Parameter of the channel address */
	private final int ADDRESS = 2;

	/** A Parameter of the channel address */
	private final int DATATYPE = 3;

	/** Start address to read or write from */
	private int startAddress;

	/** Number of registers/coils to be read or written */
	private int count;

	/** Used to determine the register/coil count */
	private EDatatype datatype;

	/** Used to determine the appropriate transaction method */
	private EFunctionCode functionCode;

	/** Specifies whether the channel should be read or written */
	private EAccess accessFlag;

	/** */
	private EPrimaryTable primaryTable;

	private String channelAddress;

	private ChannelLocator locator;

	/**
	 * Flag for checking if the number of bytes which should be read are even or not. This is required for reading,
	 * because it's only possible to read whole registers(2 bytes each) and if the number of needed bytes is not even,
	 * the last byte of the last read register will be removed.
	 */
	private boolean isEvenNumber = false;

	/**
	 * Is needed when the target device is behind a gateway/bridge which connects Modbus TCP with Modbus+ or Modbus
	 * Serial. Note: Some devices requires the unitId even if they are in a Modbus TCP Network and have their own IP.
	 * "Like when a device ties its Ethernet and RS-485 ports together and broadcasts whatever shows up on one side,
	 * onto the other if the packet isn't for themselves, but isn't "just a bridge"."
	 */
	private int unitId;

	public ModbusChannel(String channelAddress) {

		// super();

		channelAddress = channelAddress.toLowerCase();
		String[] addressParams = decomposeAddress(channelAddress);
		if (addressParams != null && checkAddressParams(addressParams)) {
			this.channelAddress = channelAddress;
			setUnitId(addressParams[UNITID]);
			setPrimaryTable(addressParams[PRIMARYTABLE]);
			setStartAddress(addressParams[ADDRESS]);
			setDatatype(addressParams[DATATYPE]);
			setCount(addressParams[DATATYPE]);

		}
		else {
			throw new RuntimeException("Address initialization failed! Invalid parameters used: " + channelAddress
					+ "\n" + "Please use this syntax: MODBUSDATATYPE:REGISTERNUMBER:REGISTERDATATYP");
		}

	}

	@Override
	public void update(EAccess access) {
		setAccessFlag(access);
		setFunctionCode();
	}

	private String[] decomposeAddress(String channelAddress) {

		String[] param = new String[4];
		String[] addressParams = channelAddress.toLowerCase().split(":");
		if (addressParams.length == 3) {
			param[UNITID] = "";
			param[PRIMARYTABLE] = addressParams[0];
			param[ADDRESS] = addressParams[1];
			param[DATATYPE] = addressParams[2];
		}
		else if (addressParams.length == 4) {
			param[UNITID] = addressParams[0];
			param[PRIMARYTABLE] = addressParams[1];
			param[ADDRESS] = addressParams[2];
			param[DATATYPE] = addressParams[3];
		}
		else {
			return null;
		}
		return param;
	}

	private boolean checkAddressParams(String[] params) {
		boolean returnValue = false;
		if ((params[UNITID].matches("\\d+?") || params[UNITID].equals(""))
				&& EPrimaryTable.isValidValue(params[PRIMARYTABLE]) && params[ADDRESS].matches("\\d+?")
				&& EDatatype.isValidDatatype(params[DATATYPE])) {
			returnValue = true;
		}
		return returnValue;
	}

	private void setFunctionCode() {
		if (accessFlag.equals(EAccess.READ)) {
			setFunctionCodeForReading();
		}
		else {
			setFunctionCodeForWriting();
		}
	}

	/**
	 * Matches data type with function code
	 * 
	 * @throws Exception
	 */
	private void setFunctionCodeForReading() {

		switch (datatype) {
		case BOOLEAN:
			if (primaryTable.equals(EPrimaryTable.COILS)) {
				functionCode = EFunctionCode.FC_01_READ_COILS;
			}
			else if (primaryTable.equals(EPrimaryTable.DISCRETE_INPUTS)) {
				functionCode = EFunctionCode.FC_02_READ_DISCRETE_INPUTS;
			}
			else {
				invalidReadAddressParameterCombination();
			}
			break;
		case SHORT:
		case INT:
		case FLOAT:
		case DOUBLE:
		case LONG:
		case STRING:
		case BYTEARRAY:
			if (primaryTable.equals(EPrimaryTable.HOLDING_REGISTERS)) {
				functionCode = EFunctionCode.FC_03_READ_HOLDING_REGISTERS;
			}
			else if (primaryTable.equals(EPrimaryTable.INPUT_REGISTERS)) {
				functionCode = EFunctionCode.FC_04_READ_INPUT_REGISTERS;
			}

			else {
				invalidReadAddressParameterCombination();
			}
			break;
		default:
			throw new RuntimeException("read: Datatype " + datatype.toString() + " not supported yet!");
		}
	}

	private void setFunctionCodeForWriting() {
		switch (datatype) {
		case BOOLEAN:
			if (primaryTable.equals(EPrimaryTable.COILS)) {
				functionCode = EFunctionCode.FC_05_WRITE_SINGLE_COIL;
			}
			else {
				invalidWriteAddressParameterCombination();
			}
			break;
		case SHORT:
			if (primaryTable.equals(EPrimaryTable.HOLDING_REGISTERS)) {
				functionCode = EFunctionCode.FC_06_WRITE_SINGLE_REGISTER;
			}
			else {
				invalidWriteAddressParameterCombination();
			}
			break;
		case INT:
		case FLOAT:
		case DOUBLE:
		case LONG:
		case BYTEARRAY:
			if (primaryTable.equals(EPrimaryTable.HOLDING_REGISTERS)) {
				functionCode = EFunctionCode.FC_16_WRITE_MULTIPLE_REGISTERS;
			}

			else {
				invalidWriteAddressParameterCombination();
			}

			break;
		default:
			throw new RuntimeException("write: Datatype " + datatype.toString() + " not supported yet!");
		}
	}

	private void invalidWriteAddressParameterCombination() {
		throw new RuntimeException("Invalid channel address parameter combination for writing. \n Datatype: "
				+ datatype.toString().toUpperCase() + " PrimaryTable: " + primaryTable.toString().toUpperCase());
	}

	private void invalidReadAddressParameterCombination() {
		throw new RuntimeException("Invalid channel address parameter combination for reading. \n Datatype: "
				+ datatype.toString().toUpperCase() + " PrimaryTable: " + primaryTable.toString().toUpperCase());
	}

	private void setStartAddress(String startAddress) {
		this.startAddress = Integer.parseInt(startAddress);
	}

	private void setDatatype(String datatype) {
		this.datatype = EDatatype.getEnumFromString(datatype);
	}

	private void setUnitId(String unitId) {
		if (unitId.equals("")) {
			this.unitId = IGNORE_UNIT_ID;
		}
		else {
			this.unitId = Integer.parseInt(unitId);
		}
	}

	private void setPrimaryTable(String primaryTable) {
		this.primaryTable = EPrimaryTable.getEnumfromString(primaryTable);
	}

	public EPrimaryTable getPrimaryTable() {
		return primaryTable;
	}

	private void setCount(String addressParamDatatyp) {
		if (datatype.equals(EDatatype.BYTEARRAY) || datatype.equals(EDatatype.STRING)) {
			// TODO check syntax first? bytearray[n]

			// special handling of the BYTEARRAY datatyp
			String[] datatypParts = addressParamDatatyp.split("\\[|\\]"); // split string either at [ or ]
			if (datatypParts.length == 2) {

				// calculate the correct number of required registers
				int number = Integer.parseInt(datatypParts[1]);

				if (number % 2 == 0) {
					count = number / 2;
					isEvenNumber = true;
				}

				else {
					count = (number / 2) + 1;
					isEvenNumber = false;
				}
			}
		}
		else {
			// all other datatyps
			count = datatype.getRegisterCount();
		}
	}

	public boolean isEvenNumber() {
		return isEvenNumber;
	}

	private void setAccessFlag(EAccess accessFlag) {
		this.accessFlag = accessFlag;
	}

	public void setChannelLocator(ChannelLocator locator) {
		this.locator = locator;
	}

	public int getStartAddress() {
		return startAddress;
	}

	public int getCount() {
		return count;
	}

	@Override
	public EDatatype getDatatype() {
		return datatype;
	}

	public EFunctionCode getFunctionCode() {
		return functionCode;
	}

	public EAccess getAccessFlag() {
		return accessFlag;
	}

	public int getUnitId() {
		return unitId;
	}

	public String getChannelAddress() {
		return channelAddress;
	}

	@Override
	public String toString() {
		return "channeladdress: " + unitId + ":" + primaryTable.toString() + ":" + startAddress + ":"
				+ datatype.toString();
	}

	@Override
	public SampledValue readValue(Connection connection) throws IOException {

		try {

			Value readValue = connection.readChannel(this);

			SampledValue result = new SampledValue(readValue, new Timestamp(new Date().getTime()).getTime(),
					Quality.GOOD);

			return result;

		}

		catch (ModbusException e) {
			e.printStackTrace();
		}
		return new SampledValue(null, new Timestamp(new Date().getTime()).getTime(), Quality.BAD);
	}

	@Override
	public void writeValue(Connection connection, Value value) throws IOException {

		try {
			connection.writeChannel(this, value);
		} catch (ModbusException e) {

			e.printStackTrace();
		} catch (RuntimeException e) {

			e.printStackTrace();
		}

	}

	@Override
	public ChannelLocator getChannelLocator() {
		return locator;
	}

}
