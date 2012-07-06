/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.experimental;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.core.logging.OgemaLogger;

/**
 * @author Jan Lapp, Fraunhofer IWES
 */
public class OsgiTestBase extends OsgiAppTestBase {

	protected ApplicationManager appMan;
	protected ResourceAccess resAcc;
	protected ResourceManagement resMan;
	protected ResourcePatternAccess advAcc;
	protected OgemaLogger logger;

	public OsgiTestBase() {
		super(false);
	}

	@Override
	public void doStart(ApplicationManager appMan) {
		super.doStart(appMan);
		this.appMan = appMan;
		logger = appMan.getLogger();
		resAcc = appMan.getResourceAccess();
		resMan = appMan.getResourceManagement();
		advAcc = appMan.getResourcePatternAccess();
	}

}
