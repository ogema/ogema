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
package org.ogema.driver.knxdriver.gui;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.driver.knxdriver.KNXdriverI;

@Service(Application.class)
@Component(specVersion = "1.2", immediate = true)
public class KnxJsGuiApp implements Application {

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	private KNXdriverI knxDriver;
	private ApplicationManager appManager;

	@Override
	public void start(ApplicationManager appManager) {
		this.appManager = appManager;
		appManager.getWebAccessManager().registerWebResource("/ogema/knx", "org/ogema/knx/gui");
		appManager.getWebAccessManager().registerWebResource("/apps/ogema/knx", new KnxServlet(appManager, knxDriver));
	}

	@Override
	public void stop(AppStopReason reason) {
		appManager.getWebAccessManager().unregisterWebResource("/ogema/knx");
		appManager.getWebAccessManager().unregisterWebResource("/apps/ogema/knx");
	}
}
