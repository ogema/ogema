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
package org.ogema.resourcemanager.impl.model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.array.ArrayResource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.AngleResource;
import org.ogema.core.model.units.AreaResource;
import org.ogema.core.model.units.BrightnessResource;
import org.ogema.core.model.units.ColourResource;
import org.ogema.core.model.units.ConcentrationResource;
import org.ogema.core.model.units.ElectricChargeResource;
import org.ogema.core.model.units.ElectricCurrentResource;
import org.ogema.core.model.units.ElectricResistanceResource;
import org.ogema.core.model.units.EnergyPerAreaResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.FlowResource;
import org.ogema.core.model.units.FrequencyResource;
import org.ogema.core.model.units.LengthResource;
import org.ogema.core.model.units.LuminousFluxResource;
import org.ogema.core.model.units.MassResource;
import org.ogema.core.model.units.PercentageResource;
import org.ogema.core.model.units.PhysicalUnitResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.model.units.ThermalEnergyCapacityResource;
import org.ogema.core.model.units.VelocityResource;
import org.ogema.core.model.units.VoltageResource;
import org.ogema.core.model.units.VolumeResource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.resourcemanager.impl.ConnectedResource;
import org.ogema.resourcemanager.impl.DynamicProxyResource;
import org.ogema.resourcemanager.impl.ResourceDBManager;
import org.ogema.resourcemanager.impl.ValueResourceBase;
import org.ogema.resourcemanager.impl.model.array.DefaultBooleanArrayResource;
import org.ogema.resourcemanager.impl.model.array.DefaultByteArrayResource;
import org.ogema.resourcemanager.impl.model.array.DefaultFloatArrayResource;
import org.ogema.resourcemanager.impl.model.array.DefaultIntegerArrayResource;
import org.ogema.resourcemanager.impl.model.array.DefaultStringArrayResource;
import org.ogema.resourcemanager.impl.model.array.DefaultTimeArrayResource;
import org.ogema.resourcemanager.impl.model.schedule.DefaultSchedule;
import org.ogema.resourcemanager.impl.model.schedule.HistoricalSchedule;
import org.ogema.resourcemanager.impl.model.simple.DefaultBooleanResource;
import org.ogema.resourcemanager.impl.model.simple.DefaultFloatResource;
import org.ogema.resourcemanager.impl.model.simple.DefaultIntegerResource;
import org.ogema.resourcemanager.impl.model.simple.DefaultOpaqueResource;
import org.ogema.resourcemanager.impl.model.simple.DefaultStringResource;
import org.ogema.resourcemanager.impl.model.simple.DefaultTimeResource;
import org.ogema.resourcemanager.impl.model.simple.SingleValueResourceBase;
import org.ogema.resourcemanager.impl.model.units.DefaultAngleResource;
import org.ogema.resourcemanager.impl.model.units.DefaultAreaResource;
import org.ogema.resourcemanager.impl.model.units.DefaultBrightnessResource;
import org.ogema.resourcemanager.impl.model.units.DefaultColourResource;
import org.ogema.resourcemanager.impl.model.units.DefaultConcentrationResource;
import org.ogema.resourcemanager.impl.model.units.DefaultElectricChargeResource;
import org.ogema.resourcemanager.impl.model.units.DefaultElectricCurrentResource;
import org.ogema.resourcemanager.impl.model.units.DefaultElectricResistanceResource;
import org.ogema.resourcemanager.impl.model.units.DefaultEnergyPerAreaResource;
import org.ogema.resourcemanager.impl.model.units.DefaultEnergyResource;
import org.ogema.resourcemanager.impl.model.units.DefaultFlowResource;
import org.ogema.resourcemanager.impl.model.units.DefaultFrequencyResource;
import org.ogema.resourcemanager.impl.model.units.DefaultLengthResource;
import org.ogema.resourcemanager.impl.model.units.DefaultLuminousFluxResource;
import org.ogema.resourcemanager.impl.model.units.DefaultMassResource;
import org.ogema.resourcemanager.impl.model.units.DefaultPhysicalUnitResource;
import org.ogema.resourcemanager.impl.model.units.DefaultPowerResource;
import org.ogema.resourcemanager.impl.model.units.DefaultTemperatureResource;
import org.ogema.resourcemanager.impl.model.units.DefaultThermalEnergyCapacityResource;
import org.ogema.resourcemanager.impl.model.units.DefaultVelocityResource;
import org.ogema.resourcemanager.impl.model.units.DefaultVoltageResource;
import org.ogema.resourcemanager.impl.model.units.DefaultVolumeResource;
import org.ogema.resourcemanager.impl.model.units.DefaultPercentageResource;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;
import org.ogema.resourcetree.TreeElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Factory class for creating the resource objects passed to the applications. Returns the explicit implementations
 * where they are provided by this implementation (e.g. for simple resources) and creates suitable proxies for the
 * others.
 *
 * @author Jan Lapp, Fraunhofer IWES
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ResourceFactory {

	/**
	 * Boolean framework/system property ({@value} ) used to enable dynamic byte code generation when creating resource
	 * implementations. Default is to use Java dynamic proxies.
	 */
	public static final String USEBYTECODEGENERATION = "ogema.resources.useByteCodeGeneration";
	final static boolean BYTECODEGENERATION;

	static {
		boolean byteCodeGen = Boolean.getBoolean(USEBYTECODEGENERATION);
		Bundle b = FrameworkUtil.getBundle(ApplicationManager.class);
		if (b != null) {
			final BundleContext ctx = b.getBundleContext();
			String v = ctx != null ? ctx.getProperty(USEBYTECODEGENERATION) : null;
			if (v != null) {
				byteCodeGen = Boolean.valueOf(v);
			}
			else {
				byteCodeGen = Boolean.valueOf(System.getProperty(USEBYTECODEGENERATION, "true"));
			}
		}
		BYTECODEGENERATION = byteCodeGen;
		org.slf4j.LoggerFactory.getLogger(ApplicationResourceManager.class).info("use byte code generation: {}",
				BYTECODEGENERATION);
	}

	private final ApplicationManager m_appMan;
	private final ApplicationResourceManager m_resMan;
	private final ResourceDBManager m_dbMan;

	public ResourceFactory(ApplicationManager appMan, ApplicationResourceManager resMan, ResourceDBManager dbMan) {
		m_appMan = appMan;
		m_resMan = resMan;
		m_dbMan = dbMan;
	}

	/*
	 * Creates a resource object of the suitable type.
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public final <T extends Resource, ResourceBase> T createResource(Class<? extends Resource> resType,
			VirtualTreeElement el, String path) {

		final Resource result;
		if (el.isComplexArray()) {
			result = new DefaultResourceList<>(el, path, m_resMan);
			return (T) result;
		}

		if (ValueResource.class.isAssignableFrom(resType)) {
			// Resource types with units
			if (PhysicalUnitResource.class.isAssignableFrom(resType)) {
				if (resType.equals(AngleResource.class)) {
					result = new DefaultAngleResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(AreaResource.class)) {
					result = new DefaultAreaResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(BrightnessResource.class)) {
					result = new DefaultBrightnessResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(ConcentrationResource.class)) {
					result = new DefaultConcentrationResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(ElectricCurrentResource.class)) {
					result = new DefaultElectricCurrentResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(ElectricChargeResource.class)) {
					result = new DefaultElectricChargeResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(ElectricResistanceResource.class)) {
					result = new DefaultElectricResistanceResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(EnergyPerAreaResource.class)) {
					result = new DefaultEnergyPerAreaResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(EnergyResource.class)) {
					result = new DefaultEnergyResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(FlowResource.class)) {
					result = new DefaultFlowResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(FrequencyResource.class)) {
					result = new DefaultFrequencyResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(LengthResource.class)) {
					result = new DefaultLengthResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(LuminousFluxResource.class)) {
					result = new DefaultLuminousFluxResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(MassResource.class)) {
					result = new DefaultMassResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(PhysicalUnitResource.class)) {
					result = new DefaultPhysicalUnitResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(PowerResource.class)) {
					result = new DefaultPowerResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(TemperatureResource.class)) {
					result = new DefaultTemperatureResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(ThermalEnergyCapacityResource.class)) {
					result = new DefaultThermalEnergyCapacityResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(VelocityResource.class)) {
					result = new DefaultVelocityResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(VoltageResource.class)) {
					result = new DefaultVoltageResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(VolumeResource.class)) {
					result = new DefaultVolumeResource(el, resType, path, m_resMan);
				}
				else if (resType.equals(PercentageResource.class)) {
					result = new DefaultPercentageResource(el, resType, path, m_resMan);
				}
				else {
					throw new UnsupportedOperationException(
							"Cannot create a resource object for SimpleResource of type " + resType.getCanonicalName()
									+ ": Case is not implemented.");
				}
				return (T) result;
			}

			// simple resources and array resources.
			if (SingleValueResource.class.isAssignableFrom(resType)) {
				if (resType.equals(BooleanResource.class)) {
					result = new DefaultBooleanResource(el, path, m_resMan);
				}
				else if (resType.equals(FloatResource.class)) {
					result = new DefaultFloatResource(el, path, m_resMan);
				}
				else if (resType.equals(IntegerResource.class)) {
					result = new DefaultIntegerResource(el, path, m_resMan);
				}
				else if (resType.equals(StringResource.class)) {
					result = new DefaultStringResource(el, path, m_resMan);
				}
				else if (resType.equals(TimeResource.class)) {
					result = new DefaultTimeResource(el, path, m_resMan);
				}
				else if (resType.equals(SingleValueResource.class)) {
					return (T) new SingleValueResourceBase(el, path, m_resMan);
				}
				else {
					throw new UnsupportedOperationException(
							"Cannot create a resource object for SingleValueResource of type "
									+ resType.getCanonicalName() + ": Case is not implemented.");
				}
				return (T) result;
			}

			if (ArrayResource.class.isAssignableFrom(resType)) {
				if (resType.equals(BooleanArrayResource.class)) {
					result = new DefaultBooleanArrayResource(el, path, m_resMan);
				}
				else if (resType.equals(ByteArrayResource.class)) {
					result = new DefaultByteArrayResource(el, path, m_resMan);
				}
				else if (resType.equals(FloatArrayResource.class)) {
					result = new DefaultFloatArrayResource(el, path, m_resMan);
				}
				else if (resType.equals(IntegerArrayResource.class)) {
					result = new DefaultIntegerArrayResource(el, path, m_resMan);
				}
				else if (resType.equals(StringArrayResource.class)) {
					result = new DefaultStringArrayResource(el, path, m_resMan);
				}
				else if (resType.equals(TimeArrayResource.class)) {
					result = new DefaultTimeArrayResource(el, path, m_resMan);
				}
				else if (resType.equals(ColourResource.class)) {
					result = new DefaultColourResource(el, path, m_resMan);
				}
				else if (resType.equals(org.ogema.core.model.simple.OpaqueResource.class)) {
					result = new DefaultOpaqueResource(el, path, m_resMan);
				}
				else {
					throw new UnsupportedOperationException("Cannot create a resource object for ArrayResource of type "
							+ resType.getCanonicalName() + ": Case is not implemented.");
				}
				return (T) result;
			}

			// schedules
			if (Schedule.class.isAssignableFrom(resType)) {
				if (el.getParent() == null) {
					throw new IllegalStateException("schedules as top level resources are not allowed.");
				}
				TreeElement locEl = el;
				while (locEl.isReference()) {
					locEl = locEl.getReference();
				}
				String loc = locEl.getName();

//				ScheduleTreeElement scheduleElement = m_dbMan.getScheduleElement(el);
				if (loc.equals(HistoricalSchedule.PATH_IDENTIFIER))
					return (T) new HistoricalSchedule(el, path, m_resMan, m_appMan, m_dbMan);
				else
					return (T) new DefaultSchedule(el, path, m_resMan, m_appMan, m_dbMan);
			}

			// a plain ValueResource, should probably only occur as virtual resource...
			return (T) new ValueResourceBase(el, path, m_resMan);
		}

		// default case.
		return mkProxy(el, path);
	}

	/*
	 * true, if implementation type is generated by the framework, false otherwise
	 */
	private static boolean isComplexType(Class<? extends Resource> type) {
		return !(ValueResource.class.isAssignableFrom(type) || ResourceList.class.isAssignableFrom(type));
	}

	@SuppressWarnings("unchecked")
	protected <T extends Resource> T mkProxy(VirtualTreeElement el, String path) {
		if (BYTECODEGENERATION) {
			return (T) ResourceFactoryASM.INSTANCE.makeResource(el, path, m_resMan);
		}
		else {
			InvocationHandler handler = new DynamicProxyResource(el, path, m_resMan);
			Class<?>[] interfaces = new Class<?>[] { el.getType(), ConnectedResource.class };
			return (T) Proxy.newProxyInstance(getClass().getClassLoader(), interfaces, handler);
		}
	}

	public boolean hasResourceType(Class<? extends Resource> type) {
		if (!BYTECODEGENERATION) // TODO
			return true;
		if (!isComplexType(type))
			return true;
		try {
			ResourceFactoryASM.INSTANCE.getImplementation(type);
			return true;
		} catch (InvalidResourceTypeException e) {
			return false;
		}
	}

}
