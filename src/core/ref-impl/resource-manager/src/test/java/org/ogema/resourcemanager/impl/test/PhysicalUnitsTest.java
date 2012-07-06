/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.resourcemanager.impl.test;

import java.util.Random;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.AngleResource;
import org.ogema.core.model.units.AreaResource;
import org.ogema.core.model.units.BrightnessResource;
import org.ogema.core.model.units.ConcentrationResource;
import org.ogema.core.model.units.ElectricCurrentResource;
import org.ogema.core.model.units.ElectricResistanceResource;
import org.ogema.core.model.units.EnergyPerAreaResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.FlowResource;
import org.ogema.core.model.units.FrequencyResource;
import org.ogema.core.model.units.LengthResource;
import org.ogema.core.model.units.LuminousFluxResource;
import org.ogema.core.model.units.MassResource;
import org.ogema.core.model.units.PhysicalUnit;
import org.ogema.core.model.units.PhysicalUnitResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.model.units.ThermalEnergyCapacityResource;
import org.ogema.core.model.units.VelocityResource;
import org.ogema.core.model.units.VoltageResource;
import org.ogema.core.model.units.VolumeResource;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@ExamReactorStrategy(PerClass.class)
public class PhysicalUnitsTest extends OsgiTestBase {

	final static Random random = new Random();

	public void createSetReadAndDeleteUnitResource(Class<? extends PhysicalUnitResource> type) {

		final PhysicalUnitResource resource = resMan.createResource(newResourceName(), type);
		assertNotNull(resource);
		assertTrue(resource.exists());
		assertTrue(resource.getResourceType().isAssignableFrom(type));
		assertTrue(type.isAssignableFrom(resource.getResourceType()));

		final float value = random.nextFloat();
		resource.setValue(value);
		assertEquals(value, resource.getValue(), 1.e-8);

		final PhysicalUnit unit = resource.getUnit();
		assertNotNull(unit);

		resource.delete();
		assertFalse(resource.exists());
	}

	public void addAsDecorator(Class<? extends PhysicalUnitResource> type) {
		final PhysicalUnitResource resource = resMan.createResource(newResourceName(), type);
		final PhysicalUnitResource decorator = resource.addDecorator("decorator", type);
		assertTrue(decorator.exists());
		assertFalse(decorator.isActive());

		final float value = random.nextFloat();
		decorator.setValue(value);
		assertEquals(value, decorator.getValue(), 1.e-8);

		decorator.activate(false);
		assertTrue(decorator.isActive());
	}

	@Test
	@SuppressWarnings( { "unchecked", "rawtypes" })
	public void testAllTypes() {
		final Class[] classes = { AngleResource.class, AreaResource.class, BrightnessResource.class,
				ConcentrationResource.class, ElectricCurrentResource.class, ElectricResistanceResource.class,
				EnergyPerAreaResource.class, EnergyResource.class, FlowResource.class, FrequencyResource.class,
				LengthResource.class, LuminousFluxResource.class, MassResource.class, PhysicalUnitResource.class,
				PowerResource.class, TemperatureResource.class, ThermalEnergyCapacityResource.class,
				VelocityResource.class, VoltageResource.class, VolumeResource.class };
		for (Class<? extends PhysicalUnitResource> clazz : classes) {
			createSetReadAndDeleteUnitResource(clazz);
			addAsDecorator(clazz);
		}
	}

	@Test
	public void createdResourcesAreOfCorrectUnitType() {
		TemperatureSensor tempSens = resMan.createResource(newResourceName(), TemperatureSensor.class);
		FloatResource mmxFloat = tempSens.reading();
		mmxFloat.create();
		PhysicalUnit unit = tempSens.reading().getUnit();
		Assert.assertEquals(PhysicalUnit.KELVIN, unit);
		assertTrue(mmxFloat instanceof TemperatureResource);
		assertTrue(tempSens.reading() instanceof TemperatureResource);

		FloatResource setpoint = tempSens.settings().setpoint();
		assertTrue(setpoint instanceof TemperatureResource);
	}
}
