package org.ogema.test.resourcetype2.export;

import org.ogema.core.model.Resource;
import org.ogema.model.ranges.EnergyRange;
import org.ogema.model.sensors.Sensor;

public interface CustomType2 extends Resource {
	
	Sensor sensor();
	EnergyRange energy();
	
}
