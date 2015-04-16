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
/**
 * Metering devices and information. All meter models derive from the {@link GenericMeter},
 * which defines a common structure but no metering registers by itself (they depend on
 * the actual type of meter). All meters refer to metering a connection. If a physical
 * meter delivers real-time information about the measured quantities (e.g. power), the
 * meter driver is advised to put this information into the connection's respective
 * sensor readings in addition to the meter's register's resource.
 */
package org.ogema.model.metering;

