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
 * Most basic data models. Prototypes should not be used as actual resource types.
 * Instead, all non-prototype data models inherit from a prototypes (note that
 * this does not conflict with prototypes having non-prototype sub-resources). There
 * are three prototypes that are defined: <br>
 * {@link PhysicalElement} is the prototype
 * for anything that is, or could sensibly be, a physical object. Most notably this
 * includes devices and {@link Sensor}s.<br>
 * A {@link Connection}
 * connects two physical objects with a third physical object, usually a device
 * related to energy management, possibly operating on the connection. In the easiest
 * case they are just informal relations between objects, but connections with actual
 * relevant properties of themselves have also been defined. An example
 * is a {@link ThermalConnection} between the outside of a building and a heating circuit, 
 * where a heat pump is the device that operates on this connection by actively
 * transporting heat through it.<br>
 * {@link Data} is a generic prototype for any type of information that is not, 
 * or cannot sensibly be, a physical object and is not a connection.
 */
package org.ogema.model.prototypes;

