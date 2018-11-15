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
package org.ogema.model.communication;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.model.prototypes.Data;

/**
 * Description of the current status of some communication channel.
 */
public interface CommunicationStatus extends Data {
	/**
	 * communication quality range: 0.0 to 1.0
	 */
	@NonPersistent
	FloatResource quality();

	/**
	 * communication does not work properly or not at all, reason unknown
	 */
	@NonPersistent
	BooleanResource communicationDisturbed();

	/** communication does not work due to some configuration */
	@NonPersistent
	BooleanResource communicationDisabled();
}
