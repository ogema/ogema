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
package org.ogema.core.resourcemanager.pattern;

/** 
 * Definition of an object that listens to pattern demands.
 * @param <PATTERN>
 */
public interface PatternListener<PATTERN extends ResourcePattern<?>> {

	/** 
	 * Availability callback issued by the framework whenever a new match for
	 * the requested pattern is found
	 * @param pattern the newly-found match for the pattern.
	 */
	void patternAvailable(PATTERN pattern);

	/** 
	 * Unavailability callback issued by the framework whenever a reported
	 * pattern match became invalid for some reason.
	 * @param pattern the pattern match that became unavailable.
	 */
	void patternUnavailable(PATTERN pattern);
}
