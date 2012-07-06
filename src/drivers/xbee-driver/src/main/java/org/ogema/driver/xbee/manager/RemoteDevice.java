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
package org.ogema.driver.xbee.manager;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a remote endpoint in a ZigBee network.
 * 
 * @author puschas
 * 
 */
public class RemoteDevice {
	public enum InitStates {
		UNINITIALIZED, INITIALIZED
	}

	public enum DeviceStates {
		AWAITING_RESPONSE, IDLE
	}

	protected final Map<Byte, Endpoint> endpoints = new HashMap<Byte, Endpoint>();
	private NodeDescriptor nodeDescriptor;
	private ComplexDescriptor complexDescriptor;
	private UserDescriptor userDescriptor;
	private long address64Bit;
	private short address16Bit;
	private InitStates initState = InitStates.UNINITIALIZED;
	private DeviceStates deviceState = DeviceStates.IDLE;
	protected String deviceType = "ZigBee";
	protected String nodeIdentifier = null; // UserDescriptor value if available

	public RemoteDevice(long address64Bit, short address16Bit) {
		this.address64Bit = address64Bit;
		this.address16Bit = address16Bit;
	}

	public short getAddress16Bit() {
		return address16Bit;
	}

	public void setAddress16Bit(short address16Bit) {
		this.address16Bit = address16Bit;
	}

	public long getAddress64Bit() {
		return address64Bit;
	}

	public void setAddress64Bit(long address64Bit) {
		this.address64Bit = address64Bit;
	}

	public NodeDescriptor getNodeDescriptor() {
		return nodeDescriptor;
	}

	public void setNodeDescriptor(NodeDescriptor nodeDescriptor) {
		this.nodeDescriptor = nodeDescriptor;
	}

	public InitStates getInitState() {
		return initState;
	}

	public void setInitState(InitStates initState) {
		this.initState = initState;
	}

	public DeviceStates getDeviceState() {
		return deviceState;
	}

	public void setDeviceState(DeviceStates deviceState) {
		this.deviceState = deviceState;
	}

	public Map<Byte, Endpoint> getEndpoints() {
		return endpoints;
	}

	public ComplexDescriptor getComplexDescriptor() {
		return complexDescriptor;
	}

	public void setComplexDescriptor(ComplexDescriptor complexDescriptor) {
		this.complexDescriptor = complexDescriptor;
	}

	public void setUserDescriptor(UserDescriptor userDescriptor) {
		this.userDescriptor = userDescriptor;
		nodeIdentifier = new String(userDescriptor.getUserDescription());
		nodeIdentifier = nodeIdentifier.replaceAll("\\s+", ""); // Remove spaces because we want to use this for channel
		// names in the
		// high level driver
	}

	public UserDescriptor getUserDescriptor() {
		return userDescriptor;
	}

	public Endpoint getEndpoint(byte endpoint) {
		return endpoints.get(endpoint);
	}

	public String getDeviceType() {
		return deviceType;
	}

	public String getNodeIdentifier() {
		return nodeIdentifier;
	}

	public void postInit() {
		if (this.userDescriptor == null) {
			nodeIdentifier = "Unknown_" + Long.toHexString(address64Bit).toUpperCase();
			userDescriptor = new UserDescriptor();
			userDescriptor.setUserDescription(nodeIdentifier.toCharArray());
		}
	}
}
