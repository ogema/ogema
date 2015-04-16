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
