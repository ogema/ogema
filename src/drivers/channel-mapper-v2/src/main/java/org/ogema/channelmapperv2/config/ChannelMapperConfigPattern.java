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
package org.ogema.channelmapperv2.config;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

public class ChannelMapperConfigPattern extends ResourcePattern<ChannelMapperConfiguration> {

	public ChannelMapperConfigPattern(Resource match) {
		super(match);
	}
	
	@ChangeListener(structureListener=true,valueListener=false) // must be a reference
	public final SingleValueResource target = model.target();
	
	public final PersistentChannelLocator channelLocator = model.channelLocator();
	
	@ChangeListener(structureListener=false,valueListener=true)
	public final StringResource driverId = channelLocator.driverId();
	@ChangeListener(structureListener=false,valueListener=true)
	public final StringResource interfaceId = channelLocator.interfaceId();
	@ChangeListener(structureListener=false,valueListener=true)
	public final StringResource deviceAddress = channelLocator.deviceAddress();
	@ChangeListener(structureListener=false,valueListener=true)
	public final StringResource parameters = channelLocator.parameters();
	@ChangeListener(structureListener=false,valueListener=true)
	public final StringResource channelAddress = channelLocator.channelAddress();
	
	@ChangeListener(structureListener=false,valueListener=true)
	public final StringResource direction = model.direction();
	
	@ChangeListener(structureListener=false,valueListener=true)
	@Existence(required=CreateMode.OPTIONAL)
	public final TimeResource samplingInterval = model.samplingInterval();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final FloatResource scalingFactor =  model.scalingFactor();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final FloatResource valueOffset = model.valueOffset();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final StringResource description = model.description();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final BooleanResource registrationSuccessful = model.registrationSuccessful();

}
