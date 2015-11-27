package org.ogema.driver.zwavehl.models;

import org.ogema.core.model.Resource;
import org.ogema.model.sensors.GenericBinarySensor;
import org.ogema.model.sensors.StateOfChargeSensor;

public interface DoorOpeningSensorConfig extends Resource {

	StateOfChargeSensor battery();

	GenericBinarySensor doorOpen();

	GenericBinarySensor alarm();

}
