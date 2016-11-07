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
package org.ogema.accesscontrol;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.ogema.core.administration.AdministrationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@Component(specVersion = "1.2")
public class Activator {

	private ServiceTracker<AdministrationManager, AdministrationManager> aManTracker;
	protected static volatile AdministrationManager admin;

	@Activate
	public void activate(final BundleContext context, Map<String, Object> config) throws Exception {
		
		ServiceTrackerCustomizer<AdministrationManager, AdministrationManager> adminMancust
				= new ServiceTrackerCustomizer<AdministrationManager, AdministrationManager>() {

			@Override
			public AdministrationManager addingService(ServiceReference<AdministrationManager> sr) {
				admin = context.getService(sr);
				aManTracker.close();
				aManTracker = null;
				return admin;
			}

			@Override
			public void modifiedService(ServiceReference<AdministrationManager> sr, AdministrationManager t) {
			}

			@Override
			public void removedService(ServiceReference<AdministrationManager> sr, AdministrationManager t) {
			}

		}; 

		aManTracker = new ServiceTracker<>(context, AdministrationManager.class, adminMancust);
		aManTracker.open();
	}

	@Deactivate
	public void deactivate(Map<String, Object> config) throws Exception {
		admin = null;
		if (aManTracker != null) {
			aManTracker.close();
			aManTracker = null;
		}
	}
}
