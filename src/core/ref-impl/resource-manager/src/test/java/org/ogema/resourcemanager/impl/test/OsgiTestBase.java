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
