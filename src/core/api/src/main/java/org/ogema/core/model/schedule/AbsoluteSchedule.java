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
package org.ogema.core.model.schedule;

/**
 * A {@link Schedule} that represents an absolute time series, i.e. the timestamps
 * of the entries are interpreted as milliseconds since 1 January 1970.
 */
@SuppressWarnings("deprecation")
public interface AbsoluteSchedule extends DefinitionSchedule, ForecastSchedule {

}
