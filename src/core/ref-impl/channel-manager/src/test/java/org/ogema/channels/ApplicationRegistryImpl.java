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
package org.ogema.channels;

import static org.junit.Assert.*;

import java.util.List;

import org.ogema.applicationregistry.ApplicationListener;
import org.ogema.applicationregistry.ApplicationRegistry;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.AppID;
import org.osgi.framework.Bundle;

public class ApplicationRegistryImpl implements ApplicationRegistry {

	AppID appId;
	ApplicationListener listener;

	int registerAppListenerCount;
	int unregisterAppListenerCount;
	
	@Override
	public AppID getAppByBundle(Bundle b) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdminApplication getAppById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AdminApplication> getAllApps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppID getContextApp(Class<?> ignore) {
		return appId;
	}

	@Override
	public Bundle getContextBundle(Class<?> ignore) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerAppListener(ApplicationListener al) {
		registerAppListenerCount++;
		assertNull(listener);
		listener = al;
	}

	@Override
	public void unregisterAppListener(ApplicationListener al) {
		unregisterAppListenerCount++;
		assertEquals(listener, al);
		listener = null;
	}

}
