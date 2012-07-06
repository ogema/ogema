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
package org.ogema.channelmapper.impl;

import org.ogema.core.channelmanager.ChannelConfiguration.Direction;

public class ChannelDescription {
	private String driverId = null;
	private String interfaceId = null;
	private String deviceAddress = null;
	private String parameters = null;
	private String channelAddress = null;
	private Long samplingPeriod = (long) 1000;
	private Double scalingFactor = 1.0;
	private Double valueOffset = 0.0;
	private Direction direction;

	public ChannelDescription() {

	}

	public ChannelDescription(String driverId, String interfaceId, String deviceAddress, String parameters,
			String channelAddress, Long samplingPeriod, Double scalingFactor, Double valueOffset) {
		this.driverId = driverId;
		this.interfaceId = interfaceId;
		this.deviceAddress = deviceAddress;
		this.parameters = parameters;
		this.channelAddress = channelAddress;
		this.samplingPeriod = samplingPeriod;
		this.scalingFactor = scalingFactor;
		this.valueOffset = valueOffset;
	}

	public String getDriverId() {
		return driverId;
	}

	public void setDriverId(String driverId) {
		this.driverId = driverId;
	}

	public String getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;
	}

	public String getDeviceAddress() {
		return deviceAddress;
	}

	public void setDeviceAddress(String deviceAddress) {
		this.deviceAddress = deviceAddress;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getChannelAddress() {
		return channelAddress;
	}

	public void setChannelAddress(String channelAddress) {
		this.channelAddress = channelAddress;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Long getSamplingPeriod() {
		return samplingPeriod;
	}

	public void setSamplingPeriod(Long samplingPeriod) {
		this.samplingPeriod = samplingPeriod;
	}

	public Double getScalingFactor() {
		return scalingFactor;
	}

	public void setScalingFactor(Double scalingFactor) {
		this.scalingFactor = scalingFactor;
	}

	public Double getValueOffset() {
		return valueOffset;
	}

	public void setValueOffset(Double valueOffset) {
		this.valueOffset = valueOffset;
	}

}
