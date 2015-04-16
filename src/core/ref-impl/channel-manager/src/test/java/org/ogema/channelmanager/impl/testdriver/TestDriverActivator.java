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
package org.ogema.channelmanager.impl.testdriver;

import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class TestDriverActivator implements BundleActivator {

	protected ChannelDriver createDriverInstance() {
		return new TestDriver("test-driver", "dummy driver for tests");
	}

	@Override
	public void start(BundleContext context) throws Exception {
		ChannelDriver driver = createDriverInstance();

		context.registerService(ChannelDriver.class.getName(), driver, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
