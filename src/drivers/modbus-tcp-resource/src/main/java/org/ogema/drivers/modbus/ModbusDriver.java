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

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.drivers.modbus.tasks.ModbusReadTask;
import org.ogema.drivers.modbus.tasks.ModbusTask;
import org.ogema.drivers.modbus.tasks.ModbusWriteTask;
import org.ogema.drivers.modbus.util.Connection;

/**
 * Modbus Driver implementation that listens to
 *  ModbusCommunicationInformation resources added as decorator to a
 * SingleValueResource or ByteArrayResource. Depending on if the
 * value should be read or written from the device the driver will read a value
 * from the specified URL and write it to the decorated resource or it will
 * write the value of the decorated resource to the specified URL.
 * 
 * @author brequardt
 */
@Component(specVersion = "1.2", immediate = true)
@Service({ Application.class, ModbusDriver.class })
public class ModbusDriver implements Application {

	/** Debug parameters*/
	/** set to null if all connections shall be used*/
	//private static String debugUseThisPathOnly = "Battery/electricityConnection/powerSensor/deviceSettings/modBusControlMode/modbusCommunication";
	//private static String debugUseThisPathOnly = "Battery/electricityConnection/powerSensor/deviceSettings/setpoint/modbusCommunication";
	private static String debugUseThisPathOnly = null;
	public static final boolean debugMode = false;
	/**minimal time for resending the same value that was sent before*/
	public static final long minimalUpdateInterval = 3000;
	
	private ApplicationManager appManager;
	private OgemaLogger logger;
	private ResourcePatternAccess patAcc;
	private final ConfigurationListener listener = new ConfigurationListener();
	private final Map<ModbusPattern, ModbusTask> tasks = new HashMap<>();
	/*
	 * Map<IP address, executor> -> just one thread per IP address is created,
	 * for reading tasks synchronize on the map itself
	 */
	private final Map<InetSocketAddress, ScheduledExecutorService> executors = new HashMap<InetSocketAddress, ScheduledExecutorService>();
	/*
	 * Map<IP address, connection> -> just one connection per IP address is
	 * created, for reading tasks; synchronize on map itself
	 */
	private final Map<InetSocketAddress, Connection> connections = new HashMap<InetSocketAddress, Connection>();

	@Override
	public void start(ApplicationManager appManager) {
		// Store references to the application manager and common services for
		// future use.
		this.logger = appManager.getLogger();
		this.patAcc = appManager.getResourcePatternAccess();
		this.appManager = appManager;
		patAcc.addPatternDemand(ModbusPattern.class, listener,
				AccessPriority.PRIO_DEVICEGROUPMAN);
	}

	@Override
	public void stop(AppStopReason reason) {
		patAcc.removePatternDemand(ModbusPattern.class, listener);
		for (ModbusTask task : tasks.values()) {
			task.destroy();
		}
		for (ScheduledExecutorService exec : executors.values()) {
			gracefulShutdown(exec);
		}
		for (Connection conn : connections.values()) {
			conn.close();
		}
		connections.clear();
		tasks.clear(); // relevant if app is stopped and restarted
		executors.clear();
	}

	class ConfigurationListener implements PatternListener<ModbusPattern> {

		@Override
		public void patternAvailable(ModbusPattern pattern) {
			if((debugUseThisPathOnly != null) && (!debugUseThisPathOnly.equals(pattern.model.getPath()))) {
				System.out.println("Skipping Modbus pattern due to debugUseThisPathOnly:"+pattern.model.getPath());
				return;
			}
			if (tasks.containsKey(pattern)) {
				// logger.info("modbus task already exist for resource "+pattern.model.getParent());
				return;
			}

			Resource parent = pattern.model.getParent();
			if (parent == null) {
				logger.warn("Modbus Communication Info found but no parent set -> ignoring this communication info: "
						+ pattern.model.getLocation());
			} else if (!(parent instanceof ValueResource)) {
				logger.warn("Modbus Communication Info found but parent is of invalid type ("
						+ parent.getClass().getName()
						+ ") -> ignoring this communication info: "
						+ pattern.model.getLocation());
			} else {
				try {

					logger.info("create new modbus task for resource "
							+ parent.getLocation());

					createModbusTask(pattern, (ValueResource) parent);
				} catch (Exception e) {
					logger.error("Error while initializing connection for "
							+ pattern.model.getParent().getLocation()
							+ " - message: " + e.getMessage(),e);
				}
			}
		}

		private void createModbusTask(ModbusPattern pattern,
				ValueResource parent) throws Exception {
			boolean writeable = pattern.address.writeable().getValue();
			boolean readable = pattern.address.readable().getValue();
			if (writeable && readable || !writeable && !readable) {
				throw new Exception(
						"Invalid Modbus communication info - writeable XOR readable must be true... "
								+ "ignoring Modbus config for resource "
								+ parent.getLocation());
			}

			if (writeable) {
				tasks.put(pattern, new ModbusWriteTask(pattern, parent,
						appManager, executors));
			} else {
				tasks.put(pattern, new ModbusReadTask(pattern, parent,
						appManager, executors, connections));
			}
		}

		@Override
		public void patternUnavailable(ModbusPattern pattern) {
			ModbusTask t = tasks.get(pattern);
			if (t != null) {
				t.destroy();
				tasks.remove(pattern);

				InetSocketAddress host = t.getHost();
				boolean executorStillInUse = false;
				boolean connectionStillInUse = false;
				for (ModbusTask task : tasks.values()) {
					if (task.getHost().equals(host)) {
						executorStillInUse = true;
						if (task instanceof ModbusReadTask) {
							connectionStillInUse = true;
						}
						break;
					}
				}

				if (!executorStillInUse) {
					synchronized (executors) {
						ScheduledExecutorService exec = executors.remove(host);
						if (exec != null)
							gracefulShutdown(exec);
					}
				}
				if (!connectionStillInUse && t instanceof ModbusReadTask) {
					synchronized (connections) {
						Connection conn = connections.remove(host);
						if (conn != null)
							conn.close();
					}
				}
			} else {
				// logger.warn("cant remove pattern for resource "+pattern.model.getParent().getPath()+" pattern not found.");
			}
		}
	}

	public int getConnectionsCount() {
		return tasks.size();
	}

	private void gracefulShutdown(ExecutorService exec) {
		try {
			exec.shutdown();
			exec.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		} finally {
			if (!exec.isTerminated())
				exec.shutdownNow();
		}
	}

}