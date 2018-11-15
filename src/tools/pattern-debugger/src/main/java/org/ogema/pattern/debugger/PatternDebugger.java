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
package org.ogema.pattern.debugger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class PatternDebugger implements Application {

	private OgemaLogger logger;
	private ApplicationManager am;

	@Override
	public void start(ApplicationManager appManager) {
		this.am = appManager;
		this.logger = appManager.getLogger();

		logger.debug("{} started", getClass().getName());
		String webResourcePackage = "org.ogema.pattern.debugger";
		webResourcePackage = webResourcePackage.replace(".", "/");

		appManager.getWebAccessManager().registerWebResourcePath("index.html", webResourcePackage);
		appManager.getWebAccessManager().registerWebResourcePath("servlet", new PatternDebuggerServlet(am));

	}

	@Override
	public void stop(AppStopReason reason) {
		if (am != null) {
			am.getWebAccessManager().unregisterWebResourcePath("index.html");
			am.getWebAccessManager().unregisterWebResourcePath("servlet");
		}
		am = null;
		logger = null;
	}

}
