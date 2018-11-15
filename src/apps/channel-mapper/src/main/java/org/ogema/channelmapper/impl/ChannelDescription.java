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
