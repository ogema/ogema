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
package org.ogema.model.devices.connectiondevices;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.prototypes.Connection;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Generic automated circuit breaking element.
 */
public interface GenericCircuitBreaker extends PhysicalElement {
	/** The connection this breaker operates on. */
	Connection connection();

	/**
	 * Current at which circuit breaker cuts circuit. The unit depends on
	 * the commodity; commodity-specialized breakers like {@link ElectricityCircuitBreaker}
	 * override this with a suitable physical quantity.
	 */
	FloatResource cutOffCurrent();

	/**
	 * Leakage flow at which circuit breaker cuts circuit (only if circuit breaker is flow-based and if circuit breaker
	 * has a leakage detection functionality)<br>
	 * unit: depending on commodity
	 */
	FloatResource leakageOffCurrent();

	/**
	 * Time until breaker switches off if cutOffCurrent or leakageCurrent is detected
	 */
	TimeResource switchOffTime();

	/**
	 * Time to switch on again after limits for automated switch off are not exceeded anymore. May be infinity if
	 * circuit breaker is a fuse that does not recover automatically
	 */
	TimeResource timeToRecover();
}
