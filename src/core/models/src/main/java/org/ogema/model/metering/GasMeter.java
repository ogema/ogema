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
