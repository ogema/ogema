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
package org.ogema.pattern.test.pattern;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.TemperatureSensor;

/**
 * Same as the {@link RoomPattern}, but requires exclusive write access
 * and also requires a 2nd field to exist.
 * @author Timo Fischer, Fraunhofer IWES
 */
public class RoomPatternGreedy extends ResourcePattern<Room> {

	@Access(mode = AccessMode.EXCLUSIVE)
	public StringResource name = model.name();

	public TemperatureSensor temperatureSensor = model.temperatureSensor();

	public RoomPatternGreedy(Resource match) {
		super(match);
	}

}
