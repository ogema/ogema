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
