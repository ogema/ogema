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
 * Definition of the simple resources that contain only a single entry (in
 * contrast to their array-counterparts and schedules). For most Java
 * primitives, a corresponding resource holding a value of this type is defined
 * with a suitable name. For example, a
 * {@link org.ogema.core.model.simple.FloatResource} holds a float (note that
 * the {@link org.ogema.core.model.units.PhysicalUnitResource}s extend the float
 * resource for cases where the value represents a physical property). Two
 * notable exceptions exist:<br>
 * - the {@link org.ogema.core.model.simple.TimeResource} holds a long value. In
 * OGEMA, times shall always be given as ms since 1970 (UTC).
 */
package org.ogema.core.model.simple;

