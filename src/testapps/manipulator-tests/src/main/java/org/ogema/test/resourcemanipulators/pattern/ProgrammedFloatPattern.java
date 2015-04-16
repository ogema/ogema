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
package org.ogema.test.resourcemanipulators.pattern;

import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

/**
 * Creation and test pattern for a programmed FloatResource.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ProgrammedFloatPattern extends ResourcePattern<FloatResource> {

	public final FloatResource value = model;
	public final DefinitionSchedule program = value.program();

	/**
	 * Default constructor required by OGEMA. Do not change this.
	 */
	public ProgrammedFloatPattern(Resource res) {
		super(res);
	}
}
