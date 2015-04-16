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
package org.ogema.driver.xbee.manager;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class NodeDescriptor {
	private byte firstByte; // 3Bits Logical type, 1Bit complex descriptor
	// available, 1Bit user descriptor available,
	// 3Bits reserved
	private boolean isCoordinator;
	private boolean isRouter;
	private boolean isEndDevice;
	private boolean hasComplexDescriptor;
	private boolean hasUserDescriptor;

	private byte secondByte; // 3Bits reserved, 5Bits Frequency band
	private boolean band868MHz;
	private boolean band900MHz;
	private boolean band2400MHz;

	private byte macCapabilityFlags;
	private boolean isAlternateCoordinator;
	private boolean isFullFunctionDevice;
	private boolean hasAcPower;
	private boolean hasRxOnWhenIdle;
	private boolean hasSecurityCapability;
	private boolean allocateShortAddress;

	private short manufacturerCode;
	private byte maximumBufferSize;
	private short maximumIncomingTransferSize;

	// Signifying the system server capabilities of this node
	// TODO does primaryTrustCenter only mean it's capable of being the primary
	// trust center?
	private short serverMask;
	private boolean primaryTrustCenter;
	private boolean backupTrustCenter;
	private boolean primaryBindingTableCache;
	private boolean backupBindingTableCache;
	private boolean primaryDiscoveryCache;
	private boolean backupDiscoveryCache;
	// private boolean networkManager;

	private short maximumOutgoingTransferSize;

	private byte descriptorCapabilityField;
	private boolean hasExtendedActiveEndpointListAvailable;
	private boolean hasExtendedSimpleDescriptorListAvailable;
	private byte[] rawNodeDescriptor;

	public NodeDescriptor() {
		this.rawNodeDescriptor = new byte[0];
	}

	public byte getFirstByte() {
		return firstByte;
	}

	public void setFirstByte(byte firstByte) {
		this.firstByte = firstByte;
		parseFirstByte();
	}

	private void parseFirstByte() {
		// Reset values
		isCoordinator = false;
		isRouter = false;
		isEndDevice = false;

		switch (firstByte & 0B00000111) {
		case (0B00000000):
			isCoordinator = true;
			break;
		case (0B00000001):
			isRouter = true;
			break;
		case (0B00000010):
			isEndDevice = true;
			break;
		}

		if ((firstByte & 0B00001000) == 0B00001000) {
			hasComplexDescriptor = true;
		}
		else {
			hasComplexDescriptor = false;
		}

		if ((firstByte & 0B00010000) == 0B00010000) {
			hasUserDescriptor = true;
		}
		else {
			hasUserDescriptor = false;
		}
	}

	public boolean isCoordinator() {
		return isCoordinator;
	}

	public boolean isRouter() {
		return isRouter;
	}

	public boolean isEndDevice() {
		return isEndDevice;
	}

	public boolean hasComplexDescriptor() {
		return hasComplexDescriptor;
	}

	public boolean hasUserDescriptor() {
		return hasUserDescriptor;
	}

	public byte getSecondByte() {
		return secondByte;
	}

	public void setSecondByte(byte secondByte) {
		this.secondByte = secondByte;
		parseSecondByte();
	}

	private void parseSecondByte() {
		// Reset values
		band868MHz = false;
		band900MHz = false;
		band2400MHz = false;

		switch (secondByte & 0B011010000) {
		case (0B00001000):
			band868MHz = true;
			break;
		case (0B00100000):
			band900MHz = true;
			break;
		case (0B01000000):
			band2400MHz = true;
			break;
		}
	}

	public boolean usesBand868MHz() {
		return band868MHz;
	}

	public boolean usesband900MHz() {
		return band900MHz;
	}

	public boolean usesband2400MHz() {
		return band2400MHz;
	}

	public byte getMacCapabilityFlags() {
		return macCapabilityFlags;
	}

	public void setMacCapabilityFlags(byte macCapabilityFlags) {
		this.macCapabilityFlags = macCapabilityFlags;
		parseMacCapabilityFlags();
	}

	private void parseMacCapabilityFlags() {
		if ((macCapabilityFlags & 0B00000001) == 0B00000001) {
			isAlternateCoordinator = true;
		}
		else {
			isAlternateCoordinator = false;
		}

		if ((macCapabilityFlags & 0B00000010) == 0B00000010) {
			isFullFunctionDevice = true;
		}
		else {
			isFullFunctionDevice = false;
		}

		if ((macCapabilityFlags & 0B00000100) == 0B00000100) {
			hasAcPower = true;
		}
		else {
			hasAcPower = false;
		}

		if ((macCapabilityFlags & 0B00001000) == 0B00001000) {
			hasRxOnWhenIdle = true;
		}
		else {
			hasRxOnWhenIdle = false;
		}

		if ((macCapabilityFlags & 0B01000000) == 0B01000000) {
			hasSecurityCapability = true;
		}
		else {
			hasSecurityCapability = false;
		}

		if ((macCapabilityFlags & 0B10000000) == 0B10000000) {
			allocateShortAddress = true;
		}
		else {
			allocateShortAddress = false;
		}
	}

	public boolean isAlternateCoordinator() {
		return isAlternateCoordinator;
	}

	public boolean isFullFunctionDevice() {
		return isFullFunctionDevice;
	}

	public boolean hasAcPower() {
		return hasAcPower;
	}

	public boolean hasRxOnWhenIdle() {
		return hasRxOnWhenIdle;
	}

	public boolean hasSecurityCapability() {
		return hasSecurityCapability;
	}

	public boolean needsAllocateShortAddress() {
		return allocateShortAddress;
	}

	public short getManufacturerCode() {
		return manufacturerCode;
	}

	public void setManufacturerCode(short manufacturerCode) {
		this.manufacturerCode = manufacturerCode;
	}

	public byte getMaximumBufferSize() {
		return maximumBufferSize;
	}

	public void setMaximumBufferSize(byte maximumBufferSize) {
		this.maximumBufferSize = maximumBufferSize;
	}

	public short getMaximumIncomingTransferSize() {
		return maximumIncomingTransferSize;
	}

	public void setMaximumIncomingTransferSize(short maximumIncomingTransferSize) {
		this.maximumIncomingTransferSize = maximumIncomingTransferSize;
	}

	public short getServerMask() {
		return serverMask;
	}

	public void setServerMask(short serverMask) {
		this.serverMask = serverMask;
		parseServerMask();
	}

	private void parseServerMask() {
		if ((serverMask & 0B0000000000000001) == 0B0000000000000001) {
			primaryTrustCenter = true;
		}
		else {
			primaryTrustCenter = false;
		}

		if ((serverMask & 0B0000000000000010) == 0B0000000000000010) {
			backupTrustCenter = true;
		}
		else {
			backupTrustCenter = false;
		}

		if ((serverMask & 0B0000000000000100) == 0B0000000000000100) {
			primaryBindingTableCache = true;
		}
		else {
			primaryBindingTableCache = false;
		}

		if ((serverMask & 0B0000000000001000) == 0B0000000000001000) {
			backupBindingTableCache = true;
		}
		else {
			backupBindingTableCache = false;
		}

		if ((serverMask & 0B0000000000010000) == 0B0000000000010000) {
			primaryDiscoveryCache = true;
		}
		else {
			primaryDiscoveryCache = false;
		}

		if ((serverMask & 0B0000000000100000) == 0B0000000000100000) {
			backupDiscoveryCache = true;
		}
		else {
			backupDiscoveryCache = false;
		}
	}

	// TODO can be or is?
	public boolean primaryTrustCenter() {
		return primaryTrustCenter;
	}

	// TODO can be or is?
	public boolean backupTrustCenter() {
		return backupTrustCenter;
	}

	// TODO can be or is?
	public boolean primaryBindingTableCache() {
		return primaryBindingTableCache;
	}

	// TODO can be or is?
	public boolean backupBindingTableCache() {
		return backupBindingTableCache;
	}

	// TODO can be or is?
	public boolean primaryDiscoveryCache() {
		return primaryDiscoveryCache;
	}

	// TODO can be or is?
	public boolean backupDiscoveryCache() {
		return backupDiscoveryCache;
	}

	public short getMaximumOutgoingTransferSize() {
		return maximumOutgoingTransferSize;
	}

	public void setMaximumOutgoingTransferSize(short maximumOutgoingTransferSize) {
		this.maximumOutgoingTransferSize = maximumOutgoingTransferSize;
	}

	public byte getDescriptorCapabilityField() {
		return descriptorCapabilityField;
	}

	public void setDescriptorCapabilityField(byte descriptorCapabilityField) {
		this.descriptorCapabilityField = descriptorCapabilityField;
		parseDescriptorCapabilityField();
	}

	private void parseDescriptorCapabilityField() {
		if ((descriptorCapabilityField & 0B00000001) == 0B00000001) {
			hasExtendedActiveEndpointListAvailable = true;
		}
		else {
			hasExtendedActiveEndpointListAvailable = false;
		}

		if ((descriptorCapabilityField & 0B00000010) == 0B00000010) {
			hasExtendedSimpleDescriptorListAvailable = true;
		}
		else {
			hasExtendedSimpleDescriptorListAvailable = false;
		}
	}

	public boolean hasExtendedActiveEndpointListAvailable() {
		return hasExtendedActiveEndpointListAvailable;
	}

	public boolean hasExtendedSimpleDescriptorListAvailable() {
		return hasExtendedSimpleDescriptorListAvailable;
	}

	public void setRawNodeDescriptor(byte[] rawNodeDescriptor) {
		this.rawNodeDescriptor = rawNodeDescriptor;
	}

	public void parseRawNodeDescriptor() {
		ByteBuffer byteBuffer = ByteBuffer.wrap(rawNodeDescriptor);
		try {
			setFirstByte(byteBuffer.get());
			setSecondByte(byteBuffer.get());
			setMacCapabilityFlags(byteBuffer.get());
			setManufacturerCode(Short.reverseBytes(byteBuffer.getShort()));
			setMaximumBufferSize(byteBuffer.get());
			setMaximumIncomingTransferSize(Short.reverseBytes(byteBuffer.getShort()));
			setServerMask(Short.reverseBytes(byteBuffer.getShort()));
			setMaximumOutgoingTransferSize(Short.reverseBytes(byteBuffer.getShort()));
			setDescriptorCapabilityField(byteBuffer.get());
		} catch (BufferUnderflowException e) {
			e.printStackTrace();
		}
	}

	public byte[] getRawNodeDescriptor() {
		return rawNodeDescriptor;
	}
}
