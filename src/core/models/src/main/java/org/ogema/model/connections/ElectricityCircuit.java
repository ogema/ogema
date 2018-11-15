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

import org.ogema.core.model.simple.IntegerResource;

/** 
 * Information on an electricity circuit. This circuit may be AC or DC. 
 */
public interface ElectricityCircuit extends GenericCircuit {
	/**
	 * Type of circuit:<br>
	 * 0 : DC<br>
	 * 1 : AC, multiple phases<br>
	 * 2 : AC, single phase circuit<br>
	 * 3 : sub phase of a multi-phase circuit
	 */
	public IntegerResource type();
	
	/**
	 * 0: not specified
	 * 1: L1
	 * 2: L2
	 * 3: L3
	 */
	public IntegerResource phase();
}
