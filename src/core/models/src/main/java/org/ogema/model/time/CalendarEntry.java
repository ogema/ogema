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
