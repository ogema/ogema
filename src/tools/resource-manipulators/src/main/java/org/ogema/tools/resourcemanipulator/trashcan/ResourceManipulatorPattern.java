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
package org.ogema.tools.resourcemanipulator.trashcan;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;

/**
 * Base class for the manipulator patterns.
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ResourceManipulatorPattern<CONFIGURATION extends ResourceManipulatorModel> extends
		ResourcePattern<CONFIGURATION> {

	// should not be changeable
	@Access(mode=AccessMode.EXCLUSIVE, required=true)
	public final StringResource application = model.application();

	public ResourceManipulatorPattern(Resource res) {
		super(res);
	}

}
