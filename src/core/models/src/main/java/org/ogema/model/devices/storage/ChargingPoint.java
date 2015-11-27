package org.ogema.model.devices.storage;

import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.PowerRange;
import org.ogema.model.smartgriddata.ElectricEnergyRequest;

public interface ChargingPoint extends PhysicalElement {

	/**
	 * Actor controlling the charging current.<br> 
	 */
	MultiSwitch setting();

	/**
	 * Electrical connection of the individual charging point.
	 */
	ElectricityConnection electricityConnection();

	/**
	 * Rated power of the charging point.
	 */
	PowerRange ratedPower();

	/**
	 * The battery currently connected to this charging point. Typically a 
	 * battery in an {@see org.ogema.model.devices.vehicles.ElectricVehicle ElectricVehicle}.
	 */
	ElectricityStorage battery();

	/**
	 * Energy request from the currently connected vehicle/battery.
	 */
	ElectricEnergyRequest energyRequest();

	/**
	 * Plug type according to IEC 62196-2
	 * <ul>
	 *  <li>0: mains socket outlet
	 *  <li>1: IEC 62196-2 Type 1 (SAE J1772/2009)
	 *  <li>2: IEC 62196-2 Type 2
	 *  <li>3: IEC 62196-2 Type 3
	 *  <li>9999: other
	 *  <li>10000+: custom types
	 * </ul> 
	 */
	IntegerResource plugType();

	/**
	 * Charging mode according to IEC 62196-1
	 * <ul>
	 *  <li>1: Mode 1 - slow charging from a household-type socket-outlet
	 *  <li>2: Mode 2 - slow charging from a household-type socket-outlet with an in-cable protection device
	 *  <li>3: Mode 3 - slow or fast charging using a specific EV socket-outlet with control and protection function installed
	 *  <li>4: Mode 4 - fast charging using an external charger; DC charging
	 *  <li>9999: other
	 *  <li>10000+: custom modes
	 * </ul>
	 */
	IntegerResource chargingMode();

}
