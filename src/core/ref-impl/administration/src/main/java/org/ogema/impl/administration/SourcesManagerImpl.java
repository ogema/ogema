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
package org.ogema.impl.administration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.SourcesManagement;

@Component(immediate=true)
@Service(SourcesManagement.class)
public class SourcesManagerImpl implements SourcesManagement {
	
	@Reference(referenceInterface=ApplicationSource.class, bind="addSource", unbind="removeSource", 
			cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE, policy=ReferencePolicy.DYNAMIC)
	private ApplicationSource source;

	private final Map<String, ApplicationSource> appStores = new HashMap<String, ApplicationSource>();
	
	@Reference
	private PermissionManager pMan;
	
	protected synchronized void addSource(ApplicationSource source) {
		appStores.put(source.getName(), source);
	}
	
	protected synchronized void removeSource(ApplicationSource source) {
		appStores.remove(source.getName());
	}
	
	@Deactivate
	protected synchronized void deactivate(Map<String,Object> props) {
		appStores.clear();
	}
	
	@Override
	public synchronized ApplicationSource connectAppSource(String address) {
		if (!pMan.handleSecurity(new AdminPermission(AdminPermission.APP)))
			throw new SecurityException("Permission to connect to marketplace is denied: " + address);
		ApplicationSource src = appStores.get(address);
		if (src != null)
			src.connect();
		return src;
	}

	@Override
	public synchronized void disconnectAppSource(String address) {
		if (!pMan.handleSecurity(new AdminPermission(AdminPermission.APP)))
			throw new SecurityException("Permission to disconnect from marketplace is denied: " + address);
		ApplicationSource src = appStores.get(address);
		if (src != null)
			src.disconnect();
	}

	@Override
	public synchronized List<ApplicationSource> getConnectedAppSources() {
		return new ArrayList<ApplicationSource>(appStores.values());
	}


	@Override
	public synchronized ApplicationSource getDefaultAppStore() {
		return appStores.values().iterator().next();
	}
	
}
