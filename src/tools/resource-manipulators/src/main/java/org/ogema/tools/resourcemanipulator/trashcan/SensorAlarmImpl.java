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

import java.util.List;
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;
import org.ogema.tools.resourcemanipulator.implementation.controllers.Controller;

/**
 * Implementation for a sensor alarm.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class SensorAlarmImpl extends ManipulatorBase<SensorAlarmPattern> {

	public SensorAlarmImpl() {
		super(SensorAlarmPattern.class);
	}

	@Override
	Controller createNewControllerInstance(SensorAlarmPattern pattern) {
		return new SensorAlarmController(pattern, appMan);
	}

	@Override
	public <T extends ManipulatorConfiguration> T createConfiguration(Class<T> type) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public <T extends ManipulatorConfiguration> List<T> getConfigurations(Class<T> type) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void deleteAllConfigurations() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void start() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
