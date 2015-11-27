package org.ogema.tools.rource.util.test;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.locations.Room;

public class TestPattern extends ResourcePattern<Thermostat> {

	public TestPattern(Resource match) {
		super(match);
	}

	ElectricityStorage battery = model.battery();

	FloatResource chargeState = battery.chargeSensor().reading();

	public ElectricityConnection conn = battery.electricityConnection();

	PowerResource power = conn.powerSensor().reading();

	@Existence(required = CreateMode.OPTIONAL)
	Room room = model.location().room();

	@Existence(required = CreateMode.OPTIONAL)
	IntegerResource type = room.type();

	public ElectricityStorage storage;

	public float testFloat = 0.2F;

}
