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
/**
 * Most basic data models. Prototypes should not be used as actual resource types.
 * Instead, all non-prototype data models inherit from a prototypes (note that
 * this does not conflict with prototypes having non-prototype sub-resources). There
 * are three prototypes that are defined: <br>
 * {@link PhysicalElement} is the prototype
 * for anything that is, or could sensibly be, a physical object. Most notably this
 * includes devices and {@link org.ogema.model.sensors.Sensor}s.<br>
 * A {@link Connection}
 * connects two physical objects with a third physical object, usually a device
 * related to energy management, possibly operating on the connection. In the easiest
 * case they are just informal relations between objects, but connections with actual
 * relevant properties of themselves have also been defined. An example
 * is a {@link org.ogema.model.connections.ThermalConnection} between the outside of a building and a heating circuit, 
 * where a heat pump is the device that operates on this connection by actively
 * transporting heat through it.<br>
 * {@link Data} is a generic prototype for any type of information that is not, 
 * or cannot sensibly be, a physical object and is not a connection.
 */
package org.ogema.model.prototypes;

