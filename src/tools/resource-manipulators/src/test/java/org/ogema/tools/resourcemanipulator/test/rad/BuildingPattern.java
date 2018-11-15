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
package org.ogema.tools.resourcemanipulator.test.rad;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.locations.Building;
import org.ogema.model.locations.BuildingPropertyUnit;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.OccupancySensor;
import org.ogema.model.sensors.PowerSensor;

/**
 * Pattern for the main building. Contains a reference to the 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class BuildingPattern extends ResourcePattern<Building> {
	/**
	 * The node representing the electricity connection of the building. Details
	 * are given by the node's sub-nodes.
	 */
	public final ElectricityConnection connection = model.electricityConnectionBox().connection();

	private final PowerSensor powerSensor = connection.powerSensor();

	//    /**
	//     * Current power consumption. Positive values are consumption, negative values
	//     * are net generation. Unit is Watt (as defined by {@link PowerResource#getUnit()}).
	//     */
	//    public final PowerResource totalPower = powerSensor.reading();
	//    
	//    /**
	//     * Forecast of the power consumption. Units and sign convention is the same as in {@link #totalPower}
	//     */
	//    public final ForecastSchedule powerForecast = totalPower.forecast();

	/**
	 * Hidden field that causes the framework to create the list of property units when
	 * a pattern of this type is being created. After invocation of {@link #init()} the
	 * sole property unit will be referenced by the {@link #propertyUnit} field.
	 */
	private final ResourceList<BuildingPropertyUnit> listOfPropertyUnits = model.buildingPropertyUnits();

	/**
	 * Reference to the property unit within the building. In SEMIAH, we can assume
	 * that all parts of the building belong to the same property unit, so this is
	 * a single variable, not a list. Its value will be set in the {@link #init()}
	 * method, since the OGEMA data model assumes a list of property units.
	 */
	private BuildingPropertyUnit propertyUnit;

	/**
	 * List of rooms in the single {@link #propertyUnit}. Will be set during call to
	 * {@link #init()}.
	 */
	private ResourceList<Room> rooms;

	/**
	 * Occupancy sensor for the building.
	 */
	private final OccupancySensor occupancySensor = model.occupancySensor();

	/**
	 * Resource indicating if someone is present in the building.
	 */
	public final BooleanResource occupancy = occupancySensor.reading();

	/**
	 * Expected future occupancy that can be used for device scheduling.
	 */
	public final AbsoluteSchedule futureOccupancy = occupancy.forecast();

	/**
	 * Constructor inherited from the OGEMA resource pattern mechanism. Do not
	 * change this - unexpected behavior may occur, otherwise.
	 */
	public BuildingPattern(Resource res) {
		super(res);
	}

	/**
	 * Finalizes initialization of this after it has been initially created by
	 * the OGEMA resource pattern tools.
	 */
	public void init() {
		propertyUnit = listOfPropertyUnits.add();
		rooms = propertyUnit.rooms().create();
		occupancy.setValue(false);
	}

	/**
	 * Adds a room to the building and references the property unit from the
	 * room model.
	 */
	public void addRoom(RoomPattern room) {
		rooms.add(room.model);
		room.model.location().create();
		room.model.location().device().setAsReference(propertyUnit);
	}

	//    public void setPowerSchedule(PowerSchedulePattern schedule) {
	//        powerSensor.settings().setAsReference(schedule.model);
	//    }
}
