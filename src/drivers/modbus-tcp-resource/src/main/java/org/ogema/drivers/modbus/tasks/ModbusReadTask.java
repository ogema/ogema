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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccessException;
import org.ogema.drivers.modbus.ModbusPattern;
import org.ogema.drivers.modbus.enums.FunctionCode;
import org.ogema.drivers.modbus.util.Connection;
import org.ogema.drivers.modbus.util.ModbusDriverUtil;

/**
 * Task for reading from a modbus device.
 * 
 * @author brequardt
 *
 */
public class ModbusReadTask extends ModbusTask {

	private static long defaultPollingInterval = 2000;
	private long pollingInterval;
	// private Timer timerTask;
	private final ModbusDriverUtil util;
	private final Connection connection;
	private final ScheduledExecutorService executor;
	private final Runnable task;
	private final ScheduledFuture<?> taskResult;
	private final boolean useLittleEndian;

	public ModbusReadTask(ModbusPattern pattern, final ValueResource resource,
			ApplicationManager appManager,
			Map<InetSocketAddress, ScheduledExecutorService> executors,
			Map<InetSocketAddress, Connection> connections) throws Exception {
		super(pattern, resource, appManager);
		this.pollingInterval = pattern.model.pollingConfiguration()
				.pollingInterval().getValue();
		if (pattern.useLittleEndian.isActive())
			useLittleEndian = pattern.useLittleEndian.getValue();
		else
			useLittleEndian = false; // defualt
		ScheduledExecutorService exec;

		synchronized (executors) {
			exec = executors.get(host); // all tasks for a single host use the
										// same thread

			if (exec == null) {
				exec = Executors.newSingleThreadScheduledExecutor();
				executors.put(host, exec);
			}
		}

		if (pollingInterval <= 0) {
			pollingInterval = defaultPollingInterval;
			logger.warn("polling interval not set using default for resource: "
					+ resource.getLocation());
		}

		if (!resource.requestAccessMode(AccessMode.EXCLUSIVE,
				AccessPriority.PRIO_HIGHEST)) {
			throw new ResourceAccessException(
					"Unable to get requested access mode for resource!");
		}
		util = new ModbusDriverUtil();

		functionCode = FunctionCode.getFunctionCodeFromRegisterType(true,
				registerType, dataType);

		Connection auxConnection;
		synchronized (connections) {
			auxConnection = connections.get(host);

			if (auxConnection == null) {

				auxConnection = new Connection(host);
				connections.put(host, auxConnection);
				try {
					auxConnection.connect();
				} catch (Exception e) {
				}
			}
		}
		this.connection = auxConnection;

		executor = exec;
		task = new Runnable() {

			@Override
			public void run() {
				try {

					connection.connect();
					Value response = readChannel();
					writeToResource(response);
					
				} catch (Exception e) {

					logger.warn("Error while trying to read. Resource: "
							+ resource.getLocation() + " Exception: "
							+ e.getMessage() + "; closing connection");

					connection.close();
				}

			}
		};
		taskResult = executor.scheduleAtFixedRate(task, 100, pollingInterval,
				TimeUnit.MILLISECONDS);

		logger.info("start modbus read task " + this.toString());

	}

	@Override
	public void destroy() {
		taskResult.cancel(false);
		resource.requestAccessMode(AccessMode.SHARED,
				AccessPriority.PRIO_LOWEST); // release resource
		// do not destroy executor... it may be used by other threads still
	}

	private void writeToResource(Value response) {
		if (resource instanceof BooleanResource) {
			((BooleanResource) resource).setValue(response.getBooleanValue());
		} else if (resource instanceof FloatResource) {
			float f = Float.valueOf(response.getFloatValue()).floatValue()
					* factor + offset;
			((FloatResource) resource).setValue(f);
		} else if (resource instanceof IntegerResource) {
			Integer val = (int) (Integer.valueOf(response.getIntegerValue())
					.intValue() * factor + offset);
			((IntegerResource) resource).setValue(val);
		} else if (resource instanceof TimeResource) {
			Long val = (long) (Long.valueOf(response.getLongValue())
					.longValue() * factor + offset);
			((TimeResource) resource).setValue(val);
		} else if (resource instanceof StringResource) {
			String val = response.getStringValue();
			((StringResource) resource).setValue(val);
		} else if (resource instanceof ByteArrayResource) {
			byte[] val = response.getByteArrayValue();
			((ByteArrayResource) resource).setValues(val);
		} else {
			logger.warn("Modbus Task for resource of type "
					+ resource.getClass().getSimpleName() + " not "
					+ "implemented yet!");
			destroy();
		}
		resource.activate(false);
	}

	public Value readChannel() throws Exception {

		Value value = null;

		switch (functionCode) {
		case FC_01_READ_COILS:
			value = util.getBitVectorsValue(connection.readCoils(this));
			break;
		case FC_02_READ_DISCRETE_INPUTS:
			value = util
					.getBitVectorsValue(connection.readDiscreteInputs(this));
			break;
		case FC_03_READ_HOLDING_REGISTERS:
			value = util.getRegistersValue(
					connection.readHoldingRegisters(this), this,
					useLittleEndian);
			break;
		case FC_04_READ_INPUT_REGISTERS:
			value = util.getRegistersValue(connection.readInputRegisters(this),
					this, useLittleEndian);
			break;
		default:
			throw new RuntimeException("FunctionCode " + functionCode
					+ " not supported yet");
		}
		return value;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

}
