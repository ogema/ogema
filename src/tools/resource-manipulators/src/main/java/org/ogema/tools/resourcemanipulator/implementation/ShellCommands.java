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
package org.ogema.tools.resourcemanipulator.implementation;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;
import org.ogema.tools.resourcemanipulator.configurations.ProgramEnforcer;
import org.ogema.tools.resourcemanipulator.configurations.ScheduleManagement;
import org.ogema.tools.resourcemanipulator.configurations.ScheduleSum;
import org.ogema.tools.resourcemanipulator.configurations.Sum;
import org.ogema.tools.resourcemanipulator.configurations.Threshold;
import org.ogema.tools.resourcemanipulator.implementation.controllers.Controller;
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;

@Properties({ @Property(name = "osgi.command.scope", value = "ogmmanipulate"), @Property(name = "osgi.command.function", value = {
		"listManipulators", "getControllers", "lastManipulationTime" }) })
@Descriptor("OGEMA administration commands")
@Component
@Service(ShellCommands.class)
public class ShellCommands {
	
	private final Map<String, ResourceManipulatorImpl> manipulators = new ConcurrentHashMap<>();
	
	public void manipulatorAdded(String appId, ResourceManipulatorImpl manipulator) {
		manipulators.put(appId, manipulator);
	}
	
	public void manipulatorRemoved(String appId) {
		manipulators.remove(appId);
	}
	
	@Descriptor("Show all manipulators by app id")
	public Collection<ResourceManipulatorImpl> listManipulators() {
		return Collections.unmodifiableCollection(manipulators.values());
	}
	
	@Descriptor("Show manipulator configurations")
	public List<?> getControllers(
			@Parameter(names= {"-a","--app"}, absentValue="") String app,
			@Parameter(names= {"-t", "--type"}, absentValue="") String type) {
		final List<Controller> controllers = new ArrayList<>();
		type = type.toLowerCase();
		for (Map.Entry<String, ResourceManipulatorImpl> entry : manipulators.entrySet()) {
			if (!entry.getKey().toLowerCase().startsWith(app))
				continue;
			final ResourceManipulatorImpl impl = entry.getValue();
			final Map<ResourceManipulatorModel, Controller> map = getControllers(impl);
			if (map != null) {
				final Class<? extends ManipulatorConfiguration> clzz;
				if (type.startsWith("prog"))
					clzz = ProgramEnforcer.class;
				else if (type.startsWith("schedulem"))
					clzz = ScheduleManagement.class;
				else if (type.startsWith("schedules"))
					clzz = ScheduleSum.class;
				else if (type.startsWith("sum"))
					clzz = Sum.class;
				else if (type.startsWith("thresh"))
					clzz = Threshold.class;
				else {
					if (!type.isEmpty()) 
						System.out.println("Unknown type " + type + ". Admissible types: " 
								+ ProgramEnforcer.class.getSimpleName() + ", "
								+ ScheduleSum.class.getSimpleName() + ", "
								+ ScheduleManagement.class.getSimpleName() + ", "
								+ Sum.class.getSimpleName() + ", "
								+ Threshold.class.getSimpleName());
					clzz = ManipulatorConfiguration.class;
				}
				for (Controller c : map.values()) {
					if (!clzz.isAssignableFrom(c.getType()))
						continue;
					controllers.add(c);
				}
			}
		}
		return controllers;
	}
	
	@Descriptor("Print the last manipulation time")
	public void lastManipulationTime(
		@Parameter(names= {"-a","--app"}, absentValue="") String app,
		@Parameter(names= {"-t", "--type"}, absentValue="") String type,
		@Parameter(names= {"-c", "--controller"}, absentValue = "") String controller) {
		@SuppressWarnings("unchecked")
		final List<Controller> controllers = (List<Controller>) getControllers(app, type);
		controller = controller.toLowerCase();
		for (Controller c : controllers) {
			if (!c.getConfigurationResource().getName().toLowerCase().startsWith(controller))
				continue;
			final Long exec = c.getLastExecutionTime();
			final Date date = exec != null ? new Date(exec) : null;
			System.out.println("Controller " + c.getConfigurationResource().getName() + ": " + date);
		}
	}
	
	private static Map<ResourceManipulatorModel, Controller> getControllers(final ResourceManipulatorImpl impl) {
		return AccessController.doPrivileged(new PrivilegedAction<Map<ResourceManipulatorModel, Controller>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Map<ResourceManipulatorModel, Controller> run() {
				try {
					final Field field = ResourceManipulatorImpl.class.getDeclaredField("controllerMap");
					field.setAccessible(true);
					return (Map<ResourceManipulatorModel, Controller>) field.get(impl);
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
					throw new RuntimeException(e);
				}
			}
		});

	}
		
	

}
