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
package org.ogema.model.connections;

import org.ogema.model.prototypes.PhysicalElement;

/**
 * In case that a circuit or a sub-part of a circuit shall not be modeled by creating
 * resources for all {@link CircuitConnection}s separately, this element can be used in a
 * connection to indicate a circuit with no further provided internal details. Still,
 * multiple connections can point to the same circuit, meaning that the devices on the
 * "other end" of the connections are connected to the circuit.
 * 
 */
public interface GenericCircuit extends PhysicalElement {
}
