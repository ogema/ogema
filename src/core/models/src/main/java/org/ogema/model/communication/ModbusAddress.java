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
package org.ogema.model.communication;

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.communication.DeviceAddress;

/**
 * Modbus communication address 
 */
public interface ModbusAddress extends DeviceAddress{

	/**
	 * Formatted as a valid IP address 
	 */
	StringResource host();
	/**
	 * Port, i.e. a value between 0 and 65535 
	 */
	IntegerResource port();
	/**
	 * Modbus register id of start register 
	 */
	IntegerResource register();
	/**
	 * Modbus unitId 
	 */
	IntegerResource unitId();
	/**
	 * Number of registers
	 */
	IntegerResource count();
	/**
	 * Valid entries:
	 * <ul>
	 *   <li>BOOLEAN (1 register)
	 *   <li>SHORT (1 register)
	 *   <li>INT (1 register)
	 *   <li>FLOAT (1 register)
	 *   <li>DOUBLE (2 registers)
	 *   <li>LONG (2 registers)
	 *   <li>BYTEARRAY  (variable register count) 
	 *   <li>STRING (variable register count) 
	 * </ul>
	 */
	StringResource dataType();
	/**
	 * Valid entries:
	 * <ul>
	 *   <li>COILS (for boolean values, read &amp; write possible)
	 *   <li>DISCRETE_INPUTS (read only)
	 *   <li>INPUT_REGISTERS (read only)
	 *   <li>HOLDING_REGISTERS (read &amp; write possible) 
	 * </ul>
	 */
	StringResource registerType(); 
	/**
	 * Whereas bits inside a Modbus register are always BigEndian ordered,
	 * the order of the registers is not specified. By default, a 
	 * BigEndian order is assumed as well, which can be overridden 
	 * using this subresource.
	 */
	BooleanResource littleEndianRegisterOrder();
	
	
	
}
