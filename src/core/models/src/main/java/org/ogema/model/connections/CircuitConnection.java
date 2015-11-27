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

import org.ogema.model.prototypes.Connection;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Defines a connection between two physical objects. The actual type of the
 * connection is defined by the model type inheriting from this prototype. In
 * case of sensor values relating to some kind of flow (energy transfer, actual
 * flow of a liquid, ...) positive values of the flow refer to the direction
 * "input" to "output". <br>
 * In cases where a device operates on a connection or constitutes a connection
 * in its own right (e.g. a heat pump) the a connection element is part of the
 * device's data model as a sub-resource (no inheritance). Input and output fields
 * of the connection may be empty, if their meaning is obvious (or unknown and 
 * irrelevant). For example, many residential electric devices will have an 
 * {@link ElectricityConnection} that only contains sensor readings but no 
 * information about the topology of the electric grid they are connected to. <br>
 * <br>
 * How to handle general topologies: A generic circuit consisting of an arbitrary number of 
 * nodes and connections between nodes (arbitrary number of connections per node, 
 * one or two nodes per connection) can be modeled by using {@link GenericCircuit} elements 
 * for both the input and output fields of the connection; a GenericCircuit 
 * element is best thought of as a circuit node then. 
 */
public interface CircuitConnection extends Connection {

	/**
	 * "input" Element connected. This can be thought of as a node of a larger circuit,
	 * or a model for a complete circuit, or subcircuit, whose internal structure is not modeled. 
	 */
	@Override
	GenericCircuit input();

	/**
	 * "output" Element connected.
	 */
	PhysicalElement output();
}
