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
package org.ogema.model.metering;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.VolumeResource;

public interface GasMeter extends GenericMeter {

	/**
	 * Consumed gas.
	 * @return
	 */
	VolumeResource reading();
	
	/**
	 * Consumed energy.
	 * @return
	 */
	EnergyResource energy();
	
	/**
	 * Nr of pulses received.
	 * @return
	 */
	IntegerResource pulseCount();
	
	/**
	 * Conversion factor to calculate energy content of gas volume.
	 * In J per m^3
	 * @return
	 */
	FloatResource energyPerVolume();
	
	/**
	 * Conversion factor to calculate consumed gas volume in terms of received pulses.
	 * In m^3 per pulse.
	 * @return
	 */
	FloatResource volumePerPulse();
	
}
