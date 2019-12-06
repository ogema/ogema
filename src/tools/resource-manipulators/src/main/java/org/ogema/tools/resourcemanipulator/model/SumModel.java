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
package org.ogema.tools.resourcemanipulator.model;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.simple.BooleanResource;
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
    
    /**
     * Defines how to handle input resource updates. If false, the result will only
     * be rewritten when an input value changes. Otherwise (default) write
     * the result whenever an input value is updated, even if the update does not
     * change the value
     * 
     * @return update the the result whenever an input value is updated (default), or only on changed values?
     */
    BooleanResource callOnEveryUpdate();
    
    FloatArrayResource factors();
    
    FloatArrayResource offsets();
}
