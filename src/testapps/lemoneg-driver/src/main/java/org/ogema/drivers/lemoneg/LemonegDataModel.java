/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
package org.ogema.drivers.lemoneg;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.Resource;
import org.ogema.model.sensors.ElectricCurrentSensor;
import org.ogema.model.sensors.ElectricPowerSensor;
import org.ogema.model.sensors.ElectricVoltageSensor;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;

/**
 * Resource Model for the LEMONEG Power Measurement System
 * 
 * @author pau
 * 
 */
public interface LemonegDataModel extends Resource {

	@NonPersistent
	ElectricVoltageSensor voltage();

	// FloatResource voltage();

	@NonPersistent
	ElectricCurrentSensor current();

	// FloatResource current();

	@NonPersistent
	ElectricPowerSensor activePower();

	// FloatResource activePower();

	@NonPersistent
	IntegerResource phaseFrequency();

	@NonPersistent
	TimeResource timeStamp();
}
