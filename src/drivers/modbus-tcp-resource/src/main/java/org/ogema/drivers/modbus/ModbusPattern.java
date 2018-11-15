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
package org.ogema.drivers.modbus;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.communication.ModbusAddress;
import org.ogema.model.communication.ModbusCommunicationInformation;
/**
 * 
 * 
 * Modbus pattern class for OGEMA resource (ModbusCommunicationInformation).
 * @author brequardt
 */
public class ModbusPattern extends
		ResourcePattern<ModbusCommunicationInformation> {

	public ModbusPattern(Resource match) {
		super(match);
	}

	@Existence(required = CreateMode.MUST_EXIST)
	public ModbusAddress address = model.comAddress();
	@Existence(required = CreateMode.MUST_EXIST)
	public BooleanResource readable = address.readable();
	@Existence(required = CreateMode.MUST_EXIST)
	public BooleanResource writeable = address.writeable();
	@Existence(required = CreateMode.MUST_EXIST)
	public StringResource host = address.host();
	public FloatResource offset = model.offset();
	@Existence(required = CreateMode.OPTIONAL) 
	public FloatResource factor = model.factor();
	@Existence(required = CreateMode.MUST_EXIST)
	public IntegerResource port = address.port();
	@Existence(required = CreateMode.MUST_EXIST)
	public IntegerResource register = address.register();
	@Existence(required = CreateMode.MUST_EXIST)
	public IntegerResource unitId = address.unitId();
	@Existence(required = CreateMode.MUST_EXIST)
	public IntegerResource count = address.count();
	@Existence(required = CreateMode.MUST_EXIST)
	public StringResource dataType = address.dataType();
	@Existence(required = CreateMode.MUST_EXIST)
	public StringResource registerType = address.registerType();
	// optional to ensure compatibility with existing apps
	@Existence(required = CreateMode.OPTIONAL) 
	public BooleanResource useLittleEndian = address.littleEndianRegisterOrder();

	@Override
	public int hashCode() {
//		return model.hashCode();
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((count == null) ? 0 : count.hashCode());
		result = prime * result
				+ ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((factor == null) ? 0 : factor.hashCode());
		result = prime * result
				+ ((registerType == null) ? 0 : registerType.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result
				+ ((readable == null) ? 0 : readable.hashCode());
		result = prime * result
				+ ((register == null) ? 0 : register.hashCode());
		result = prime * result + ((unitId == null) ? 0 : unitId.hashCode());
		result = prime * result
				+ ((writeable == null) ? 0 : writeable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModbusPattern other = (ModbusPattern) obj;
//		return model.equalsLocation(other.model);
		
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (count == null) {
			if (other.count != null)
				return false;
		} else if (!count.equals(other.count))
			return false;
		if (dataType == null) {
			if (other.dataType != null)
				return false;
		} else if (!dataType.equals(other.dataType))
			return false;
		if (factor == null) {
			if (other.factor != null)
				return false;
		} else if (!factor.equals(other.factor))
			return false;
		if (registerType == null) {
			if (other.registerType != null)
				return false;
		} else if (!registerType.equals(other.registerType))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (offset == null) {
			if (other.offset != null)
				return false;
		} else if (!offset.equals(other.offset))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		if (readable == null) {
			if (other.readable != null)
				return false;
		} else if (!readable.equals(other.readable))
			return false;
		if (register == null) {
			if (other.register != null)
				return false;
		} else if (!register.equals(other.register))
			return false;
		if (unitId == null) {
			if (other.unitId != null)
				return false;
		} else if (!unitId.equals(other.unitId))
			return false;
		if (writeable == null) {
			if (other.writeable != null)
				return false;
		} else if (!writeable.equals(other.writeable))
			return false;
		return true;
	}

}