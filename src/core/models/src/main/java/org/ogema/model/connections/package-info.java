/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
 * Variety of physically different connections between physical elements as well
 * as the definition of generic nodes between the connections. All connections
 * defined in this package inherit from the {@link Connection} prototype and
 * extend it by defining the type of connection (thermal, electric, fluid). Nodes
 * between the connections extend the base class {@link GenericCircuit} by defining
 * the type of circuit modeled by the resource. Topologically, a circuit is a 
 * point that can be the input or output of any number of connections. In practice,
 * the circuit can be a complete heating circuit or only a sub-set of it, depending
 * on the detail level that has been chosen to model connections and circuits.
 */
package org.ogema.model.connections;

