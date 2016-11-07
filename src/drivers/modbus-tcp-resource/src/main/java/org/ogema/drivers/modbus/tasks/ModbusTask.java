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
package org.ogema.drivers.modbus.tasks;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.drivers.modbus.ModbusPattern;
import org.ogema.drivers.modbus.enums.DataType;
import org.ogema.drivers.modbus.enums.FunctionCode;
import org.ogema.drivers.modbus.enums.RegisterType;

/**
 * 
 * Abstract class for generell modbus tasks (reading/writing into a modbus
 * device).
 *
 * @author brequardt
 */
public abstract class ModbusTask {

	protected ModbusPattern pattern;
	protected Resource resource;

	protected float factor = 1;

	protected float offset = 0;

	protected int unitId;

	protected int startAddress;

	protected InetSocketAddress host;

	protected int count;

	protected DataType dataType;

	protected FunctionCode functionCode;

	protected OgemaLogger logger;

	protected RegisterType registerType;

	public ModbusTask(ModbusPattern pattern, ValueResource resource,
			ApplicationManager appManager) throws Exception {
		this.resource = resource;
		factor = pattern.factor.getValue();
		offset = pattern.offset.getValue();
		unitId = pattern.unitId.getValue();
		startAddress = pattern.register.getValue();

		String hostName = pattern.host.getValue();
		InetAddress a = InetAddress.getByName(hostName);
		host = new InetSocketAddress(a, pattern.port.getValue());
		count = pattern.count.getValue();
		dataType = DataType.getEnumFromString(pattern.dataType.getValue());
		registerType = RegisterType.getEnumfromString(pattern.registerType
				.getValue());
		logger = appManager.getLogger();
	}

	public abstract void destroy();

	public ModbusPattern getPattern() {
		return pattern;
	}

	public void setPattern(ModbusPattern pattern) {
		this.pattern = pattern;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public float getFactor() {
		return factor;
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}

	public float getOffset() {
		return offset;
	}

	public void setOffset(float offset) {
		this.offset = offset;
	}

	public int getUnitId() {
		return unitId;
	}

	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}

	public int getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(int startAddress) {
		this.startAddress = startAddress;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public FunctionCode getFunctionCode() {
		return functionCode;
	}

	public void setFunctionCode(FunctionCode functionCode) {
		this.functionCode = functionCode;
	}

	public OgemaLogger getLogger() {
		return logger;
	}

	public void setLogger(OgemaLogger logger) {
		this.logger = logger;
	}

	public InetSocketAddress getHost() {
		return host;
	}

	public void setHost(InetSocketAddress host) {
		this.host = host;
	}

	@Override
	public String toString() {
		return "ModbusTask [resource=" + resource.getLocation() + ", factor="
				+ factor + ", offset=" + offset + ", unitId=" + unitId
				+ ", startAddress=" + startAddress + ", host=" + host
				+ ", count=" + count + ", dataType=" + dataType
				+ ", functionCode=" + functionCode + ", registerType="
				+ registerType + "]";
	}

}
