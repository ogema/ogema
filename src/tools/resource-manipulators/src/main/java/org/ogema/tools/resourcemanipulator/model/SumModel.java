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
package org.ogema.tools.resourcemanipulator.model;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.TimeResource;

/**
 * Data model for configuring a sum modifier rule.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface SumModel extends ResourceManipulatorModel {
	/**
	 * Input values. If an input value is not active (or non-existing), the input
	 * is considered as zero.
	 */
	ResourceList<SingleValueResource> inputs();

	/**
	 * Seed resource for the output value. Real output is referenced at .program(). // FIXME what does this mean?
	 */
	Resource resultBase();

	/**
	 * Delay time between detected changes in the inputs and evaluation of the results.
	 */
	TimeResource delay();

	/**
	 * Flag defining how to handle empty sums.
	 */
	BooleanResource deactivateEmptySum();
}
