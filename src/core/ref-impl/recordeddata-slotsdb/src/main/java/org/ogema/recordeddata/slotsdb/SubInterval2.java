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
package org.ogema.recordeddata.slotsdb;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubInterval2 {

	private static Logger logger = LoggerFactory.getLogger(SubInterval2.class.getName());

	List<SampledValue> values = new ArrayList<SampledValue>();

	public void add(SampledValue value) {
		values.add(value);
	}

	public List<SampledValue> getValues() {
		return values;
	}

}
