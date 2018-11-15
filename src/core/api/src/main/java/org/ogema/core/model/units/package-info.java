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
 * Definition of {@link org.ogema.core.model.simple.FloatResource}
 * specializations that represent a physical property. All the specializations
 * implement the interface
 * {@link org.ogema.core.model.units.PhysicalUnitResource} in addition to the
 * FloatResource. A specialization exists for every commonly-used property,
 * which also defines the unit the property is measured. The currently-supported
 * units are defined in {@link org.ogema.core.model.units.PhysicalUnit}. <br>
 *
 * OGEMA tries to provide a useful list of properties, but can never be complete
 * in this respect. If no suitable physical property is available, plain
 * {@link org.ogema.core.model.simple.FloatResource}s can be used. The unit used
 * must then be explicitly defined in the definition of the data model.
 */
package org.ogema.core.model.units;

