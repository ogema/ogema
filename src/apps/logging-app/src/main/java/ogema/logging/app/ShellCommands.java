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