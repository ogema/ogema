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
