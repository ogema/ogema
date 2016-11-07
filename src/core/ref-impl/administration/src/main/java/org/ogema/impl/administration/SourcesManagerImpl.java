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
		appStores.put(source.getAddress(), source);
	}
	
	protected synchronized void removeSource(ApplicationSource source) {
		appStores.remove(source.getAddress());
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
