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

import java.nio.ByteBuffer;

import org.ogema.driver.xbee.Constants;

public class SimpleDescriptor {
	private byte endpointId;
	private short applicationProfileId;
	private short applicationDeviceId;
	private byte applicationDeviceVersion; // 4 Bits + 4 Bits reserved
	private byte inputClusterCount;
	private byte[] inputClusterList;
	private byte outputClusterCount;
	private byte[] outputClusterList;
	private byte[] rawSimpleDescriptor;

	public byte getEndpointId() {
		return endpointId;
	}

	public void setEndpointId(byte endpointId) {
		this.endpointId = endpointId;
	}

	public short getApplicationProfileId() {
		return applicationProfileId;
	}

	public void setApplicationProfileId(short applicationProfileId) {
		this.applicationProfileId = applicationProfileId;
	}

	public short getApplicationDeviceId() {
		return applicationDeviceId;
	}

	public void setApplicationDeviceId(short applicationDeviceId) {
		this.applicationDeviceId = applicationDeviceId;
	}

	public byte getApplicationDeviceVersion() {
		return applicationDeviceVersion;
	}

	public void setApplicationDeviceVersion(byte applicationDeviceVersion) {
		this.applicationDeviceVersion = applicationDeviceVersion;
	}

	public byte getInputClusterCount() {
		return inputClusterCount;
	}

	public void setInputClusterCount(byte inputClusterCount) {
		this.inputClusterCount = inputClusterCount;
	}

	public byte[] getInputClusterList() {
		return inputClusterList;
	}

	public void setInputClusterList(byte[] inputClusterList) {
		this.inputClusterList = inputClusterList;
	}

	public byte getOutputClusterCount() {
		return outputClusterCount;
	}

	public void setOutputClusterCount(byte outputClusterCount) {
		this.outputClusterCount = outputClusterCount;
	}

	public byte[] getOutputClusterList() {
		return outputClusterList;
	}

	public void setOutputClusterList(byte[] outputClusterList) {
		this.outputClusterList = outputClusterList;
	}

	public void parseRawSimpleDescriptor() {
		ByteBuffer byteBuffer = ByteBuffer.wrap(rawSimpleDescriptor);
		/* int length = */byteBuffer.get()/* & 0xff */; // Length of the Simple Descriptor in bytes
		setEndpointId(byteBuffer.get());
		setApplicationProfileId(Short.reverseBytes(byteBuffer.getShort()));
		setApplicationDeviceId(Short.reverseBytes(byteBuffer.getShort()));
		setApplicationDeviceVersion(byteBuffer.get());
		setInputClusterCount(byteBuffer.get());
		int inputClusterCount = getInputClusterCount() & 0xff; // amount
		// of
		// short
		// values
		inputClusterCount *= 2; // 2 byte per cluster
		byte[] inputClusterList = new byte[inputClusterCount];
		byteBuffer.get(inputClusterList, 0, inputClusterCount);
		Constants.reverseByteArray(inputClusterList);
		setInputClusterList(inputClusterList);
		setOutputClusterCount(byteBuffer.get());
		int outputClusterCount = getOutputClusterCount() & 0xff; // amount
		// of
		// short
		// values
		outputClusterCount *= 2; // 2 byte per cluster
		byte[] outputClusterList = new byte[outputClusterCount];
		byteBuffer.get(outputClusterList, 0, outputClusterCount);
		Constants.reverseByteArray(outputClusterList);
		setOutputClusterList(outputClusterList);
	}

	public void setRawSimpleDescriptor(byte[] rawSimpleDescriptor) {
		this.rawSimpleDescriptor = rawSimpleDescriptor;
	}

	public byte[] getRawSimpleDescriptor() {
		return rawSimpleDescriptor;
	}
}
