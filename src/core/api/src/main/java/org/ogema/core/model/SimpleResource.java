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
package org.ogema.core.model;

import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.SingleValueResource;

/**
 * This is a marker interface for simple resources. All interfaces defining resources that contain actual
 * values (except for schedules) have to define this interface.
 * @deprecated this marker interface was not defined in a useful manner. Use {@link ValueResource} and its specializations {@link ArrayResource}, {@link Schedule} and {@link SingleValueResource}, instead.
 * 
 */
@Deprecated
public interface SimpleResource extends Resource {

}
