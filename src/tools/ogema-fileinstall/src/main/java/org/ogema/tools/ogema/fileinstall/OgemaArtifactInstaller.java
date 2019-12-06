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
package org.ogema.tools.ogema.fileinstall;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * DS component adapting {@link OgemaFileInstall} to a Felix File Install
 * ArtifactInstaller. Will only be activated if File Install is available and
 * the OGEMA framework is running.
 * 
 * @author jlapp
 */
@Component(service = ArtifactInstaller.class, immediate = false)
public class OgemaArtifactInstaller extends OgemaFileInstall
		implements
			ArtifactInstaller,
			Application {

	private ServiceRegistration<Application> appreg;
	private final CountDownLatch appStartLatch = new CountDownLatch(1);

	@Activate
	protected void activate(BundleContext context) {
		appreg = context.registerService(Application.class, this, null);
		try {
			appStartLatch.await(60, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			// nevermind
		}
	}

	@Deactivate
	protected void deactivate(BundleContext context) {
		if (appreg != null) {
			appreg.unregister();
		}
	}

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();
		appStartLatch.countDown();
		logger.debug("{} started", getClass().getName());
	}

	@Override
	public void stop(AppStopReason reason) {
		logger.debug("{} stopped", getClass().getName());
	}

}
