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
package org.ogema.resourcemanager.impl.test;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.exam.OsgiAppTestBase;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.osgi.framework.Constants;

/**
 *
 * @author jlapp
 */
public class OsgiTestBase extends OsgiAppTestBase {

	protected ResourceAccess resAcc;
	protected ResourceManagement resMan;

	static int counter = 0;

	public OsgiTestBase() {
		super(false);
	}
	
	@ProbeBuilder
	public TestProbeBuilder buildCustomProbe(TestProbeBuilder builder) {
		builder.setHeader(Constants.EXPORT_PACKAGE, "org.ogema.resourcemanager.impl.test.types");
		return builder;
	}
	
	@Override
	public void doStart(ApplicationManager appMan) {
		super.doStart(appMan);
		resAcc = appMan.getResourceAccess();
		resMan = appMan.getResourceManagement();
	}
	
	@Override
	public void doStop() {
		resAcc = null;
		resMan = null;
	}

}
