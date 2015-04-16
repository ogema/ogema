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
package org.ogema.drivers.arduino;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareDescriptor.HardwareType;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.SerialHardwareDescriptor;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.drivers.arduino.data.ClimateData;

/**
 * This class manages the driver data for an open interface
 * 
 * @author pau
 * 
 */
public class ClimateDataImpl implements ClimateData {
	public enum FrameState {
		DATA_RECEIVED, START_DELIMITER_PARSED, ID_PARSED, RTEMP_PARSED, RH_PARSED, STEMP_PARSED, SIGNAL_LEVEL_PARSED, VOLTAGE_PARSED, TIMESTAMP_PARSED
	}

	private Thread timerThread;
	Calendar currentTime;
	private final int WAITING_TIME = 120000; // 2 minutes
	private static final boolean debug = true;
	FrameState frameState = FrameState.DATA_RECEIVED;
	Map<Integer, JSONObject> deviceData;
	Map<Integer, Long> deviceDataTimestamp;
	int stateReadCounter = 0;
	private final String interfaceId;
	private SerialConnection con;
	// private ModbusSerialTransaction transaction;
	// private final List<Device> devices;
	private final SerialParameters params;
	private final String parameterString;
	private Thread inputReaderThread;
	protected final InputStream is;

	// TODO JSON format
	ByteBuffer buffer = ByteBuffer.allocate(256);

	private volatile JSONObject json;

	@Override
	public JSONObject getCurrentData(int id) {
		synchronized (inputReaderThread) {
			return deviceData.get(id);
		}
	}

	public ClimateDataImpl(String iface, String parameter) {

		deviceData = new HashMap<Integer, JSONObject>();
		deviceDataTimestamp = new HashMap<Integer, Long>();

		interfaceId = iface;

		parameterString = parameter;
		params = parseParameterString(parameter);
		params.setPortName(getPortName(iface)); // TODO liefert null
		if (debug)
			System.out.println(params.getPortName());
		// params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
		con = new SerialConnection(params);
		try {
			con.open();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		is = con.getInputStream();

		timerThread = new Thread(new Runnable() {
			volatile boolean running = true;
			private long currentTime = 0;
			private final int MAX_TIMEOUT = 60000 * 5;

			public void run() {
				while (running) {
					try {
						Thread.sleep(WAITING_TIME);
					} catch (Exception e) {
						e.printStackTrace();
					}
					ArrayList<Integer> toDel = new ArrayList<Integer>(); // TODO expensive and bad, need sth better
					currentTime = Calendar.getInstance().getTimeInMillis();
					for (Map.Entry<Integer, JSONObject> entry : deviceData.entrySet()) {
						if (currentTime - deviceDataTimestamp.get(entry.getKey()) > MAX_TIMEOUT) {
							System.out.println("currentTime: "
									+ (currentTime - deviceDataTimestamp.get(entry.getKey())) + " > " + MAX_TIMEOUT);
							toDel.add(entry.getKey());
						}
					}
					for (Integer key : toDel) {
						deviceDataTimestamp.remove(key);
						deviceData.remove(key);
					}
				}
			}
		});
		timerThread.start();

		inputReaderThread = new Thread(new Runnable() {
			private byte val = 0;
			private volatile boolean isComment = false;
			volatile boolean running = true;

			public void run() {
				while (running) {
					try {
						val = (byte) is.read();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (debug)
						System.out.println(val + " = " + (char) val);

					if ('#' == val) { // The next bytes are part of a comment
						isComment = true;
						continue;
					}
					if (isComment) { // Ignore the comment
						if ('@' == val) { // @ => End of comment
							isComment = false;
							frameState = FrameState.START_DELIMITER_PARSED;
						}
						continue;
					}

					switch (frameState) {
					case DATA_RECEIVED: // Parse start delimiter
						if ('@' == val) { // @
							frameState = FrameState.START_DELIMITER_PARSED;
						}
						break;
					case START_DELIMITER_PARSED: // Parse ID
						if (9 == val) { // \t
							buffer.put(val);
							++stateReadCounter;
							frameState = FrameState.ID_PARSED;
						}
						else if (val > 47 && val < 58) {
							buffer.put(val);
							++stateReadCounter;
						}
						break;
					case ID_PARSED: // Parse RTEMP
						if (9 == val) { // \t
							buffer.put(val);
							++stateReadCounter;
							frameState = FrameState.RTEMP_PARSED;
						}
						else {
							buffer.put(val);
							++stateReadCounter;
						}
						break;
					case RTEMP_PARSED: // Parse RH
						if (9 == val) { // \t
							buffer.put(val);
							++stateReadCounter;
							frameState = FrameState.RH_PARSED;
						}
						else {
							buffer.put(val);
							++stateReadCounter;
						}
						break;
					case RH_PARSED: // Parse STEMP
						if (9 == val) { // \t
							buffer.put(val);
							++stateReadCounter;
							frameState = FrameState.STEMP_PARSED;
						}
						else {
							buffer.put(val);
							++stateReadCounter;
						}
						break;
					case STEMP_PARSED: // Parse SIGNAL_LEVEL
						if (9 == val) { // \t
							buffer.put(val);
							++stateReadCounter;
							frameState = FrameState.SIGNAL_LEVEL_PARSED;
						}
						else {
							buffer.put(val);
							++stateReadCounter;
						}
						break;
					case SIGNAL_LEVEL_PARSED: // Parse VOLTAGE
						if (9 == val) { // \t
							buffer.put(val);
							++stateReadCounter;
							frameState = FrameState.VOLTAGE_PARSED;
						}
						else {
							buffer.put(val);
							++stateReadCounter;
						}
						break;
					case VOLTAGE_PARSED: // Parse TIMESTAMP
						if (9 == val) { // \t
							buffer.put(val);
							++stateReadCounter;
							frameState = FrameState.TIMESTAMP_PARSED;
						}
						else {
							buffer.put(val);
							++stateReadCounter;
						}
						break;
					case TIMESTAMP_PARSED: // Parse MESSAGE_ID
						if (13 == val || 10 == val) { // \n
							frameState = FrameState.DATA_RECEIVED;

							byte[] tempArr = new byte[stateReadCounter];
							buffer.position(0);
							buffer.get(tempArr, 0, stateReadCounter);
							if (debug) {
								System.out.println("Data:");
								for (byte b : tempArr) {
									System.out.print((char) b);
								}
								System.out.println("\n");
							}
							buffer.clear();
							stateReadCounter = 0;

							String dataString = new String(tempArr, Charset.forName("UTF-8"));
							String[] dataArray = dataString.split("\t");
							synchronized (this) {
								json = new JSONObject();
								try {
									json.put("ID", dataArray[0]);
									json.put("RTemp", dataArray[1]);
									json.put("RH", dataArray[2]);
									json.put("STemp", dataArray[3]);
									json.put("Signal level", dataArray[4]);
									json.put("Voltage", dataArray[5]);
									json.put("Timestamp", dataArray[6]);
									json.put("Message ID", dataArray[7]);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								deviceData.put(Integer.parseInt(dataArray[0]), json);
								deviceDataTimestamp.put(Integer.parseInt(dataArray[0]), Calendar.getInstance()
										.getTimeInMillis());
							}
							if (debug)
								System.out.println("\nPrint JSON:\n" + json.toString() + "\n");
						}
						else {
							buffer.put(val);
							++stateReadCounter;
						}
						break;
					}

					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		inputReaderThread.start();

		// transaction = new ModbusSerialTransaction(con);
	}

	private String getPortName(String iface) {
		String portName = null;
		HardwareManager hw = Activator.getHardwareManager();
		HardwareDescriptor desc = hw.getDescriptor(iface);

		if (desc.getHardwareType() == HardwareType.USB) {
			portName = ((UsbHardwareDescriptor) desc).getPortName();
		}
		else if (desc.getHardwareType() == HardwareType.SERIAL) {
			portName = ((SerialHardwareDescriptor) desc).getPortName();
		}
		return portName;
	}

	public String getParameterString() {
		return parameterString;
	}

	/**
	 * Parse the parameter string for the modbus connection
	 * 
	 * @param parameter
	 * @return
	 */
	public SerialParameters parseParameterString(String parameter) {
		// baudRate:databits:parity:stopbits:flowcontrol:echo:timeoutMs
		SerialParameters params = new SerialParameters();
		if (parameter != null) {
			String[] splitted = parameter.split(":");
			params.setBaudRate(Integer.parseInt(splitted[0], 10));
			params.setDatabits(Integer.parseInt(splitted[1], 10));
			params.setParity(splitted[2]);
			params.setStopbits(splitted[3]);
			params.setFlowControlIn(splitted[4]);
			params.setFlowControlOut(splitted[5]);
			params.setEcho(Integer.parseInt(splitted[6], 10) != 0);
			params.setReceiveTimeout(Integer.parseInt(splitted[7], 10));
		}
		return params;
	}

	static public String createParameterString(SerialParameters params) {
		String result;

		result = params.getBaudRateString();
		result += ":" + params.getDatabitsString();
		result += ":" + params.getParityString();
		result += ":" + params.getStopbitsString();
		result += ":" + params.getFlowControlInString();
		result += ":" + params.getFlowControlOutString();
		result += ":" + (params.isEcho() ? '1' : '0');
		result += ":" + params.getReceiveTimeout();

		return result;
	}

	// private int parseFlowcontrol(String input) throws Exception {
	// switch (input) {
	// case "NO":
	// return SerialPort.FLOWCONTROL_NONE;
	// case "RTSCTS":
	// return SerialPort.FLOWCONTROL_RTSCTS_IN |
	// SerialPort.FLOWCONTROL_RTSCTS_OUT;
	// case "XONXOFF":
	// return SerialPort.FLOWCONTROL_XONXOFF_IN |
	// SerialPort.FLOWCONTROL_XONXOFF_OUT;
	// default:
	// throw new Exception("Unknown Flowcontrol: " + input);
	// }
	// }
	//
	// private int parseParity(String input) throws Exception {
	// switch (input) {
	// case "N":
	// return SerialPort.PARITY_NONE;
	// case "E":
	// return SerialPort.PARITY_EVEN;
	// case "O":
	// return SerialPort.PARITY_ODD;
	// case "M":
	// return SerialPort.PARITY_MARK;
	// case "S":
	// return SerialPort.PARITY_SPACE;
	// default:
	// throw new Exception("Unknown Parity: " + input);
	// }
	// }

	public String getInterfaceId() {
		return interfaceId;
	}

	// public List<Device> getDevices() {
	// return devices;
	// }
	//
	// public List<DeviceLocator> getDeviceLocators() {
	// List<DeviceLocator> result = new ArrayList<DeviceLocator>(devices.size());
	//
	// for (Device dev : devices) {
	// result.add(dev.getDeviceLocator());
	// }
	//
	// return result;
	// }
	//
	// public Device findDevice(DeviceLocator device) {
	// for (Device dev : devices) {
	// if (dev.getDeviceLocator().equals(device))
	// return dev;
	// }
	//
	// return null;
	// }
	//
	// public void addDevice(Device dev) {
	// devices.add(dev);
	// }
	//
	// public void removeDevice(Device dev) {
	// devices.remove(dev);
	// }
	//
	// public boolean hasDevices() {
	// return !devices.isEmpty();
	// }

	// synchronized public ModbusResponse executeTransaction(ModbusRequest request) throws Exception {
	//
	// // if each device should have its own parameter settings,
	// // these are modified here before each transaction
	// // - params.equals(device.params) <-- TODO: add function parameter
	// // - modify params
	// // - con.setConnectionParameters() re-reads the params (has internal
	// // reference to it)
	//
	// transaction.setRequest(request);
	// transaction.execute();
	// return transaction.getResponse();
	// }

	synchronized public void close() {
		// transaction.setSerialConnection(null);
		con.close();
		con = null;
		// transaction = null;
	}

}
