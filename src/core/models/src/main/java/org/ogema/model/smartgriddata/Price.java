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
