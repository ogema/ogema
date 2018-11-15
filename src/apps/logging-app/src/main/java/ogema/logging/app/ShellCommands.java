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
package ogema.logging.app;

import java.util.Collection;

import org.apache.felix.service.command.Descriptor;
import org.ogema.core.model.Resource;
import org.ogema.core.recordeddata.RecordedDataConfiguration;

class ShellCommands {
	
	private final LoggingApp app;
	
	ShellCommands(LoggingApp app) {
		this.app = app;
	}
	
	@Descriptor("Start logging all sensor values that are not configured for logging yet")
	public int logAllSensors() {
		final int cnt = app.logAllSensors();
		System.out.println("Started logging of " + cnt  + " new resources");
		return cnt;
	}

	@Descriptor("Start logging all actor setpoints that are not configured for logging yet")
	public int logAllActors() {
		final int cnt = app.logAllActors();
		System.out.println("Started logging of " + cnt  + " new resources");
		return cnt;
	}
	
	// TODO logging type
	@Descriptor("Activate logging for a specific resource")
	public boolean startLogging(
		@Descriptor("The resource path")
		final String path) {
		return app.activateLogging(path);
	}
	
	@Descriptor("Deactivate logging for a specific resource")
	public boolean stopLogging(
		@Descriptor("The resource path")
		final String path) {
		return app.stopLogging(path);
	}

	@Descriptor("Get all logged resources")
	public Collection<Resource> getLoggedResources() {
		return app.getLoggedResources();
	}
	
	@Descriptor("Check if logging is enabled for a specific resource")
	public boolean isLoggingActive(
			@Descriptor("The resource path")
			final String path) {
		return app.isLoggingActive(path);
	}
	
	@Descriptor("Return the logging configuration for a specific resource")
	public RecordedDataConfiguration getLoggingConfig(
			@Descriptor("The resource path")
			final String path) {
		return app.getLoggingConfiguration(path);
	}
}