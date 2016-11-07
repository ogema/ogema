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

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.drivers.modbus.ModbusDriver;
import org.ogema.drivers.modbus.ModbusPattern;
import org.ogema.drivers.modbus.enums.FunctionCode;
import org.ogema.drivers.modbus.util.Connection;
import org.ogema.drivers.modbus.util.ModbusDriverUtil;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

/**
 * 
 * Class for modbus writing tasks (write into a modbus device).
 * 
 * @author brequardt
 */
public class ModbusWriteTask extends ModbusTask implements
		ResourceValueListener<ValueResource> {

	ModbusDriverUtil util;
	private volatile Value v;
	private Connection con;
	private final boolean useLittleEndian;
	private final ScheduledExecutorService executor;
	private final ApplicationManager appManager;

	public ModbusWriteTask(ModbusPattern pattern, ValueResource resource,
			ApplicationManager appManager,
			Map<InetSocketAddress, ScheduledExecutorService> executors) throws Exception {
		super(pattern, resource, appManager);
		if (pattern.useLittleEndian.isActive())
			useLittleEndian = pattern.useLittleEndian.getValue();
		else
			useLittleEndian = false;
		resource.addValueListener(this, true);
		util = new ModbusDriverUtil();
		functionCode = FunctionCode.getFunctionCodeFromRegisterType(false,
				registerType, dataType, count);

		this.con = new Connection(host);
		this.appManager = appManager;

		ScheduledExecutorService exec;
		synchronized (executors) {
			exec = executors.get(host); // all tasks for a single host use the
										// same thread
			if (exec == null) {
				exec = Executors.newSingleThreadScheduledExecutor();
				executors.put(host, exec);
			}
		}

		this.executor = exec;
		
		logger.info("started modbus write task ("+functionCode+")" + this.toString());
		
		resourceChanged(resource);
	}

	@Override
	public void destroy() {
		resource.removeValueListener(this);
		con.close();
	}

	long lastCallTime = -1;
	String lastValue = "xxxx";
	@Override
	public void resourceChanged(ValueResource resource) {

System.out.println("Modbus: resourceChanged:"+resource.getLocation());
		if (resource instanceof BooleanResource) {

			boolean b = ((BooleanResource) resource).getValue();
			if (factor < 0) {
				b = !b;
			}
			v = new BooleanValue(b);

		} else if (resource instanceof FloatResource) {

			v = new FloatValue((((FloatResource) resource).getValue() * factor)
					+ offset);

		} else if (resource instanceof IntegerResource) {

			v = new FloatValue(
					(((IntegerResource) resource).getValue() * factor) + offset);

		} else if (resource instanceof TimeResource) {

			v = new LongValue(
					(long) ((((TimeResource) resource).getValue() * factor) + offset));

		} else if (resource instanceof StringResource) {
			v = new StringValue(((StringResource) resource).getValue());
		} else if (resource instanceof ByteArrayResource) {
			v = new ByteArrayValue(((ByteArrayResource) resource).getValues());
		} else {
			logger.warn("Modbus Task for resource of type "
					+ resource.getClass().getSimpleName() + " not "
					+ "implemented yet!");
			destroy();
			con.close();
		}

		long now = appManager.getFrameworkTime();
		if(((now - lastCallTime) < ModbusDriver.minimalUpdateInterval)&&(lastValue.equals(v.getStringValue()))) {
			return;
		}
		lastCallTime = now;
		lastValue = v.getStringValue();
		Runnable r = new Runnable() {

			@Override
			public void run() {
				if (v != null) {

					try {
						con.connect();
						logger.info("Writing value:"+v.getFloatValue()+" to:"+getStartAddress());
						writeChannel(v, con);
						logger.info("Success:Writing value:"+v.getFloatValue()+" to:"+getStartAddress());
					} catch (Exception e) {
						if(ModbusDriver.debugMode) {
							logger.error("Write task to "+getHost()+"-"+getStartAddress()+" failed... " + e.getMessage(),
									e);
						} else {
							logger.error("Write task to "+getHost()+"-"+getStartAddress()+" failed... " + e.getMessage());
						}
					} finally {
						con.close();
					}
				}

			}
		};
		executor.submit(r);

	}

	public void writeChannel(Value value, Connection con)
			throws ModbusIOException, ModbusSlaveException, ModbusException,
			SocketTimeoutException, SocketException {

		switch (functionCode) {
		case FC_05_WRITE_SINGLE_COIL:

			if (value instanceof BooleanValue) {

				con.writeSingleCoil(this, value.getBooleanValue());
			} else {

				con.writeSingleCoil(this, value.getIntegerValue() > 0 ? true
						: false);

			}

			break;
		case FC_15_WRITE_MULITPLE_COILS:
			con.writeMultipleCoils(this, util.getBitVectorFromByteArray(value));
			break;
		case FC_06_WRITE_SINGLE_REGISTER:
			con.writeSingleRegister(this,
					new SimpleRegister(value.getIntegerValue()));
			break;
		case FC_16_WRITE_MULTIPLE_REGISTERS:
			con.writeMultipleRegisters(this, util.valueToRegisters(value,
					dataType, count, useLittleEndian));
			break;
		default:
			throw new RuntimeException("FunctionCode "
					+ functionCode.toString() + " not supported yet");
		}
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	

}
