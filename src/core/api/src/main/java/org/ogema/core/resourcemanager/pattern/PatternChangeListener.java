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
package org.ogema.core.resourcemanager.pattern;

import java.util.List;

import org.ogema.core.resourcemanager.CompoundResourceEvent;

public interface PatternChangeListener<P extends ResourcePattern<?>> {
	
	/**
	 * 
	 * @param instance
	 * @param changes
	 */
	void patternChanged(P instance, List<CompoundResourceEvent<?>> changes);

}
