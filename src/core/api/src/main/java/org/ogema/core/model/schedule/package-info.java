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

