/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
