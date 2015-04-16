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
package org.ogema.model.time;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.locations.Location;
import org.ogema.model.prototypes.Data;
import org.ogema.model.ranges.TimeRange;
import org.ogema.model.stakeholders.LegalEntity;

/**
 * Entry into the calendar of a building resource, a person etc.
 */
public interface CalendarEntry extends Data {

	/**
	 * Time interval that the calendar entry covers.
	 */
	TimeRange time();

	/**
	 * Subject / title of the event.
	 */
	StringResource subject();

	/**
	 * Description of the event.
	 */
	StringResource description();

	/**
	 * Location of the event described by this calendar entry.
	 */
	Location location();

	/**
	 * Persons/Institutions invited to the event (if sent by email meaning To:)
	 */
	ResourceList<LegalEntity> invitees();

	/**
	 * Persons/Institutions informed about the event (if sent by email meaning
	 * CC:)
	 */
	ResourceList<LegalEntity> informed();

	/**
	 * Person/Institution generating the entry (if sent by email meaning From:)
	 */
	LegalEntity inviting();

	/**
	 * Progress (0: not started, 1.0:finished, -1: canceled), values in between are
	 * application-specific estimations. Progress shall not be
	 * determined by the time entered for the entry, but by real progress. So
	 * "finished" may be entered earlier or later than the scheduled end of the
	 * entry.
	 */
	FloatResource progress();
}
