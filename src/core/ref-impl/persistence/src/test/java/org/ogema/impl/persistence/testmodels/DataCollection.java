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
package org.ogema.impl.persistence.testmodels;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Configuration;

/**
 * Central data storage
 */
public interface DataCollection extends Configuration {
	ResourceList<StringResource> apps();

	ResourceList<StringResource> categoryList();

	ResourceList<User> users();

	ResourceList<StringResource> organizations();

	ResourceList<StringResource> appGroups();

	StringResource genericIconDirectory();
}
