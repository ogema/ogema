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
package org.ogema.tools.resourcemanipulator.test.rad;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.schedule.ForecastSchedule;
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
	public final ForecastSchedule futureOccupancy = occupancy.forecast();

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
