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
package org.ogema.channelmanager.testdriver;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class TestDriverActivator implements BundleActivator, Application {

	protected ChannelDriver createDriverInstance() {
		return new TestDrv("test-driver", "dummy driver for tests");
	}

	@Override
	public void start(BundleContext context) throws Exception {
		ChannelDriver driver = createDriverInstance();

		context.registerService(ChannelDriver.class.getName(), driver, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

	@Override
	public void start(ApplicationManager appManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop(AppStopReason reason) {
		// TODO Auto-generated method stub

	}

}
