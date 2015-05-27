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
package org.ogema.apps.swtch.listeners;

import java.util.LinkedList;
import java.util.List;

import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.apps.swtch.patterns.ThermostatPattern;

public class ThermostatPatternListener implements PatternListener<ThermostatPattern> {

	private final List<ThermostatPattern> list;

	public ThermostatPatternListener() {
		list = new LinkedList<>();
	}

	@Override
	public void patternAvailable(ThermostatPattern pattern) {
		// System.out.println("ThermostatPattern available: " + pattern.model.getLocation());
		list.add(pattern);
	}

	@Override
	public void patternUnavailable(ThermostatPattern pattern) {
		// System.out.println("ThermostatPattern unavailable: " + pattern.model.getLocation());
		try {
			list.remove(pattern);
		} catch (Exception e) {
			System.out.println("Could not remove pattern " + pattern.model.getLocation() + ": " + e);
		}
	}

	public List<ThermostatPattern> getPatterns() {
		return new LinkedList<ThermostatPattern>(list); // avoid concurrency problems and manipulation from outside by copying
	}

}
