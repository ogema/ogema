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
package org.ogema.tools.resourcemanipulator.test.rad;

import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.connections.ThermalConnection;
import org.ogema.model.devices.generators.ElectricHeater;
import org.ogema.model.locations.Location;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.ElectricPowerSensor;

/**
 * Creation pattern for an electric heating system installed in the building.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ElectricHeaterPattern extends ResourcePattern<ElectricHeater> {

	private final Location location = model.location();

	@Existence(required = CreateMode.OPTIONAL)
	// will be referenced after creation.
	private final PhysicalElement room = location.device();

	@Existence(required = CreateMode.OPTIONAL)
	// will be referenced to ZigBee trigger after creation.
	public final OnOffSwitch onOffSwitch = model.onOffSwitch();

	//    @Existence(required = CreateMode.OPTIONAL)
	//    public final BooleanResource stateControl = onOffSwitch.stateControl();
	//
	//    @Existence(required = CreateMode.OPTIONAL)
	//    public final BooleanResource stateFeedback = onOffSwitch.stateFeedback();

	//    private final MultiSwitch controlSwitch = model.setting();
	//    private final FloatResource stateControl = controlSwitch.stateControl();
	//    private final DefinitionSchedule program = stateControl.program();
	//    private final FloatResource stateFB = controlSwitch.stateFeedback();
	private final ThermalConnection thermalConnection = model.thermalConnection();

	@Existence(required = CreateMode.OPTIONAL)
	// will be referenced after creation.
	private final PhysicalElement heatedRoom = thermalConnection.output();

	private final ElectricityConnection electricityConnection = model.electricityConnection();
	private final ElectricPowerSensor electricPowerSensor = electricityConnection.powerSensor();
	private final PowerResource curElectricPower = electricPowerSensor.reading();
	private final PowerResource minElectricPower = electricPowerSensor.ratedValues().lowerLimit();
	private final PowerResource maxElectricPower = electricPowerSensor.ratedValues().upperLimit();
	private final AbsoluteSchedule electricPowerForecast = curElectricPower.forecast();

	public ElectricHeaterPattern(Resource res) {
		super(res);
	}

	//    public void init(Room targetRoom) {
	//        room.setAsReference(targetRoom);
	//        heatedRoom.setAsReference(targetRoom);
	//        temperatureSensor.setAsReference(targetRoom.temperatureSensor());
	//        
	////        stateControl.setValue(0.f);
	////        stateFB.setValue(0.f);
	//                
	////        curThermalPower.setValue(0.f);
	////        minThermalPower.setValue(0.f);
	////        maxThermalPower.setValue(10000.f);
	//        curElectricPower.setValue(0.f);
	//        minElectricPower.setValue(0.f);
	//        maxElectricPower.setValue(10000.f);
	//    }
	public void init() {
		//        final RecordedDataConfiguration heatingLogConfig = new RecordedDataConfiguration();
		//        heatingLogConfig.setStorageType(RecordedDataConfiguration.StorageType.ON_VALUE_CHANGED);
		//        stateFeedback.getHistoricalData().setConfiguration(heatingLogConfig);
	}
}
