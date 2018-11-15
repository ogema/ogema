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
package org.ogema.model.smartgriddata;

import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.model.prototypes.Data;

/** 
 * Generic price. Use more specialized price models wherever possible.
 */
public interface Price extends Data {
	/**
	 * current price value<br>
	 * Note: In case of a variable commodity price that is provided as a schedule ahead of time the price schedule
	 * should be added as a {@link AbsoluteSchedule} resource as a decorator.<br>
	 * unit: depending on commodity per currency
	 */
	FloatResource price();

	/** ISO 4217 */
	StringResource currency();

	/**
	 * Price type:<br>
	 * 1: commodity market price<br>
	 * 2: wholesale buying price<br>
	 * 3: general retail price<br>
	 * 4: specific customer price for billing<br>
	 * 5: tariff stage of a multi-rate retail tariff. Note that the time interval or other conditions defining
	 * when the tariff stage actually applies can be defined by a decorator or by a future extension of the model. At
	 * the time being the tariff stage should be defined by the name of the price resource.<br>
	 * 10: grid fees<br>
	 * 9999: other; greater 10.000: custom values
	 */
	IntegerResource scope();

	/** If true the price contains taxes, if false taxes have be added */
	BooleanResource containsTaxes();

	/** If true the price contains grid fees, if false grid fees have to be added */
	BooleanResource containsGridFees();
}
