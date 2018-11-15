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

import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Data;

/** Phone number in a machine processable form */
public interface PhoneNumber extends Data {
	/** Country code without starting "00" or "++" */
	IntegerArrayResource countryCode();

	/**
	 * Area code / prefix to be used within the country, without starting "0" or "1" that have to be left out when
	 * dialing with country code
	 */
	IntegerArrayResource cityCode();

	/** Local code including possibly the internal code */
	IntegerArrayResource localCode();

	/** Internal code to dial from within a company phone network etc. */
	IntegerArrayResource internalCode();

	/** Optionally also a string representation for the phone number may be given */
	StringResource standardDisplay();
}
