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
