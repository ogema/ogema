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
 * Variety of physically different connections between physical elements as well
 * as the definition of generic nodes between the connections. All connections
 * defined in this package inherit from the {@link CircuitConnection} prototype and
 * extend it by defining the type of connection (thermal, electric, fluid). Nodes
 * between the connections extend the base class {@link GenericCircuit} by defining
 * the type of circuit modeled by the resource. Topologically, a circuit is a 
 * point that can be the input or output of any number of connections. In practice,
 * the circuit can be a complete heating circuit or only a sub-set of it, depending
 * on the detail level that has been chosen to model connections and circuits.
 */
package org.ogema.model.connections;

