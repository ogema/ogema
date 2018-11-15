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
 * Defininition of schedules, which are
 * {@link org.ogema.core.timeseries.TimeSeries TimeSeries}-valued resources.
 * Schedules are always sub-resources of a resource that holds a non-array
 * value; their entries are of the same type as that parent resources' ones.
 * OGEMA knows two types of schedules,
 * {@link org.ogema.core.model.schedule.DefinitionSchedule}s and
 * {@link org.ogema.core.model.schedule.ForecastSchedule}s. Both types are
 * technically identical. They differ in their interpretation. Forecasts are
 * guesses about the future, which may or may not come true. Definition
 * schedules are used for values that either had been true in the past (which
 * never changes) or, in case of future definition schedules, decribe a program
 * for the respective parent resource.
 */
package org.ogema.core.model.schedule;

