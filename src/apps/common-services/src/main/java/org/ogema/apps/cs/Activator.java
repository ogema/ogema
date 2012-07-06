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
package org.ogema.apps.cs;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.persistence.ResourceDB;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class Activator implements Application, BundleActivator {

	ResourceDB db;

	public void start(BundleContext bc) throws BundleException {
		db = (ResourceDB) bc.getService(bc.getServiceReference(ResourceDB.class.getName()));

		bc.registerService(Application.class, this, null);
	}

	public void stop(BundleContext context) {
	}

	@Override
	public void start(ApplicationManager appManager) {
		new CommonServlet(appManager.getWebAccessManager(), this.db, appManager.getResourceAccess());
	}

	@Override
	public void stop(AppStopReason reason) {
	}
}
