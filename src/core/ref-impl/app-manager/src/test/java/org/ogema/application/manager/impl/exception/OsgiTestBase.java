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
package org.ogema.application.manager.impl.exception;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.exam.OsgiAppTestBase;

public class OsgiTestBase extends OsgiAppTestBase {

	protected ResourceAccess resAcc;
	protected ResourceManagement resMan;
	protected ApplicationManager appMan;

	@Override
	public void doStart(ApplicationManager appMan) {
		super.doStart(appMan);
		this.appMan = appMan;
		resAcc = appMan.getResourceAccess();
		resMan = appMan.getResourceManagement();
	}

}
