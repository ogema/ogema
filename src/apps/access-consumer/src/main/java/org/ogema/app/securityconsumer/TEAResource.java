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
package org.ogema.app.securityconsumer;

import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.units.ElectricCurrentResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.FrequencyResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.VoltageResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.metering.ElectricityMeter;

/**
 * Manage complex ElectricityMeter resource. Cache direct references to values.
 * 
 * @author pau
 *
 */
public class TEAResource {

	private ResourceManagement resourceManager;

	// top level resource
	ElectricityMeter meter;

	// ElectricityMeter.powerReading() and ElectricityMeter.connection().powerSensor().reading()
	PowerResource totalPower;

	// ElectricityMeter.connection().reactivePowerSensor().reading()
	PowerResource totalReactivePower;

	// ElectricityMeter.energyReading()
	EnergyResource totalEnergy;

	// ElectricityMeter.connection().voltageSensor().reading()
	VoltageResource totalVoltage;

	// ElectricityMeter.connection().currentSensor().reading()
	ElectricCurrentResource totalCurrent;

	// ElectricityMeter.connection().frequencySensor().reading()
	FrequencyResource frequency;

	// ElectricityMeter.connection().subPhaseConnections(0).currentSensor().reading()
	// Phase A current
	ElectricCurrentResource aCurrent;

	// ElectricityMeter.connection().subPhaseConnections(0).voltageSensor().reading()
	// Phase A voltage
	VoltageResource aVoltage;

	// ElectricityMeter.connection().subPhaseConnections(0).powerSensor().reading()
	// Phase A power
	PowerResource aPower;

	// ElectricityMeter.connection().subPhaseConnections(0).reactivePowerSensor().reading()
	// Phase A reactive power
	PowerResource aReactivePower;

	// Phase B current
	ElectricCurrentResource bCurrent;

	// Phase B voltage
	VoltageResource bVoltage;

	// Phase B power
	PowerResource bPower;

	// Phase B reactive power
	PowerResource bReactivePower;

	// Phase C current
	ElectricCurrentResource cCurrent;

	// Phase C voltage
	VoltageResource cVoltage;

	// Phase C power
	PowerResource cPower;

	// Phase C reactive power
	PowerResource cReactivePower;

	IntegerResource istwert;
	IntegerResource sollwert;

	TEAResource(ResourceManagement resourceManager, String name) {

		ElectricityConnection connection;
		ElectricityConnection phaseA;
		ElectricityConnection phaseB;
		ElectricityConnection phaseC;

		this.resourceManager = resourceManager;

		meter = resourceManager.createResource(name, ElectricityMeter.class);

		meter.addOptionalElement("powerReading");
		meter.addOptionalElement("energyReading");
		meter.addOptionalElement("connection");

		connection = meter.connection();

		createConnection(connection);
		connection.powerSensor().reading().setAsReference(meter.powerReading());
		connection.addOptionalElement("frequencySensor");
		connection.frequencySensor().addOptionalElement("reading");

		connection.addOptionalElement("subPhaseConnections");

		phaseA = connection.subPhaseConnections().add();
		phaseB = connection.subPhaseConnections().add();
		phaseC = connection.subPhaseConnections().add();

		createConnection(phaseA);
		createConnection(phaseB);
		createConnection(phaseC);

		totalEnergy = meter.energyReading();
		totalPower = connection.powerSensor().reading();
		totalReactivePower = connection.reactivePowerSensor().reading();
		totalVoltage = connection.voltageSensor().reading();
		totalCurrent = connection.currentSensor().reading();
		frequency = connection.frequencySensor().reading();

		aCurrent = phaseA.currentSensor().reading();
		aVoltage = phaseA.voltageSensor().reading();
		aPower = phaseA.powerSensor().reading();
		aReactivePower = phaseA.reactivePowerSensor().reading();

		bCurrent = phaseB.currentSensor().reading();
		bVoltage = phaseB.voltageSensor().reading();
		bPower = phaseB.powerSensor().reading();
		bReactivePower = phaseB.reactivePowerSensor().reading();

		cCurrent = phaseC.currentSensor().reading();
		cVoltage = phaseC.voltageSensor().reading();
		cPower = phaseC.powerSensor().reading();
		cReactivePower = phaseC.reactivePowerSensor().reading();
	}

	private void createConnection(ElectricityConnection connection) {
		connection.addOptionalElement("powerSensor");
		connection.powerSensor().addOptionalElement("reading");

		connection.addOptionalElement("reactivePowerSensor");
		connection.reactivePowerSensor().addOptionalElement("reading");

		connection.addOptionalElement("voltageSensor");
		connection.voltageSensor().addOptionalElement("reading");

		connection.addOptionalElement("currentSensor");
		connection.currentSensor().addOptionalElement("reading");

		connection.addOptionalElement("frequencySensor");
		connection.frequencySensor().addOptionalElement("reading");
	}

	void activate(boolean b) {
		meter.activate(b);
	}

	public void deactivate(boolean b) {
		meter.deactivate(b);
	}
}
