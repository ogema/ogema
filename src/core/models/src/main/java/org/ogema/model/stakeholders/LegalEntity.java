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
package org.ogema.model.stakeholders;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Data;

/** Generic legal entity, including a person */
public interface LegalEntity extends Data {
	//	GeographicAddress address();

	//	/** Contact information besides geographic address */
	//	PersonalCommunicationAddressing contactInfo();

	/**
	 * Type of legal entity<br>
	 * 1: natural person 10..higher: institution (see {@link org.ogema.core.model.stakeholders.Institution#type
	 * Institution.type})
	 */
	IntegerResource type();

	/**
	 * Name of user for identification purposes; if the user has an account on the OGEMA gateway the name should be the
	 * same as the login user name
	 */
	StringResource userName();

	/** Language(s) that may be used with legal entity */
	ResourceList<Language> knownLanguages();

	/** Standard language to be used when communicating with legal entity */
	Language standardLanguage();

}
