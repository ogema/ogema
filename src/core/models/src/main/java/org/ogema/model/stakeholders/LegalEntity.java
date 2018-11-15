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
	 * 1: natural person 10..higher: institution 
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
