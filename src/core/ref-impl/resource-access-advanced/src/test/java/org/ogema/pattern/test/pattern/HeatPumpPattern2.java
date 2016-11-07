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
package org.ogema.pattern.test.pattern;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.devices.generators.HeatPump;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.sensors.StateOfChargeSensor;

public class HeatPumpPattern2 extends ResourcePattern<HeatPump> {

	public final ElectricityStorage battery = model.getSubResource("electricityStorage", ElectricityStorage.class);
	
	public final StateOfChargeSensor chargeSensor = battery.chargeSensor();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final FloatResource batteryStatus = chargeSensor.reading();
	
	
	public HeatPumpPattern2(Resource match) {
		super(match);
	}


}
