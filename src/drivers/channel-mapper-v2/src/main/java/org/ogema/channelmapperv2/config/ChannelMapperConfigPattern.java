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
