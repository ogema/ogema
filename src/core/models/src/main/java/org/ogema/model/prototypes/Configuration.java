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
package org.ogema.model.prototypes;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;

/**
 * Prototype for configuration resources. Applications that persistently store
 * configuration information should do so in custom data models that extend this
 * base type. This allows viewer applications to appropriately filter resources
 * based on their type.
 */
public interface Configuration extends Resource {
	/**
	 * Human-readable name.
	 */
	StringResource name();

}
