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
package org.ogema.impl.security;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.ChannelPermission;
import org.ogema.accesscontrol.ResourcePermission;
import org.ogema.core.application.AppID;
import org.ogema.core.security.AppPermission;
import org.ogema.core.security.AppPermissionType;
import org.osgi.framework.Bundle;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import org.osgi.service.permissionadmin.PermissionInfo;
import org.slf4j.Logger;

/**
 * @author Zekeriya Mansuroglu
 *
 */
public class AppPermissionImpl implements AppPermission {

	static final String BUNDLE_LOCATION_CONDITION_NAME = "org.osgi.service.condpermadmin.BundleLocationCondition";

	private AppID appID;
	ConditionalPermissionAdmin cpa;
	ArrayList<AppPermissionType> nTypes;
	ArrayList<AppPermissionType> pTypes;
	private HashMap<String, AppPermissionType> allTypes;
	/*
	 * List of the permissions that are already commited to the security manager
	 */
	ConcurrentHashMap<String, ConditionalPermissionInfo> granted;

	private String[] blcArgs;

	ConditionInfo blcInfo;

	Bundle bundle;

	private String location;

	static AppPermissionTypeImpl denyAll;

	private Logger log;

	AppPermissionImpl(ConditionalPermissionAdmin cpa, AppID man) {
		this.appID = man;
		this.cpa = cpa;
		this.pTypes = new ArrayList<>();
		this.nTypes = new ArrayList<>();
		this.allTypes = new HashMap<>();
		this.granted = new ConcurrentHashMap<>();
		this.blcArgs = new String[1];
		this.blcArgs[0] = man.getLocation();
		this.blcInfo = new ConditionInfo(BUNDLE_LOCATION_CONDITION_NAME, blcArgs);
		initLogger();
	}

	private void initLogger() {
		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			public Void run() {
				log = org.slf4j.LoggerFactory.getLogger(getClass());
				return null;
			}
		});

	}

	public AppPermissionImpl(ConditionalPermissionAdmin cpa, String uri) {
		this.cpa = cpa;
		this.pTypes = new ArrayList<>();
		this.nTypes = new ArrayList<>();
		this.allTypes = new HashMap<>();
		this.granted = new ConcurrentHashMap<>();
		this.blcArgs = new String[1];
		this.blcArgs[0] = uri;
		this.blcInfo = new ConditionInfo(BUNDLE_LOCATION_CONDITION_NAME, blcArgs);
		this.location = uri;
		initLogger();
	}

	public AppPermissionImpl(ConditionalPermissionAdmin cpa) {
		this.cpa = cpa;
		this.pTypes = new ArrayList<>();
		this.nTypes = new ArrayList<>();
		this.allTypes = new HashMap<>();
		this.granted = new ConcurrentHashMap<>();
		this.blcArgs = new String[1];
		this.blcInfo = new ConditionInfo(BUNDLE_LOCATION_CONDITION_NAME, blcArgs);
		initLogger();
	}

	@Override
	public ArrayList<AppPermissionType> getTypes() {
		ArrayList<AppPermissionType> result = new ArrayList<>(nTypes.size() + pTypes.size());
		result.addAll(nTypes);
		result.addAll(pTypes);
		return result;
	}

	String getPermName(Enum<?> action) {
		String permName;
		if (action instanceof AppPermissionType.ResourceAction) {
			permName = ResourcePermission.class.getName();
		}
		else if (action instanceof AppPermissionType.AdminAction) {
			permName = AdminPermission.class.getName();
		}
		else if (action instanceof AppPermissionType.ChannelAction) {
			permName = ChannelPermission.class.getName();
		}
		else
			throw new IllegalArgumentException(action.toString());

		return permName;
	}

	@Override
	public AppPermissionType addException(Enum<?> action, String filter, ConditionInfo cond) {
		String permName = getPermName(action);
		AppPermissionTypeImpl at = createType();
		at.registerPermission(permName, filter, action.name(), cond);
		// at.setOwner(this);
		allTypes.put(at.name, at);
		at.mode = false;
		nTypes.add(at);
		return at;
	}

	AppPermissionTypeImpl createType() {
		AppPermissionTypeImpl at = new AppPermissionTypeImpl(this);
		String name = newTypeName(at.hashCode());
		at.name = name;
		return at;
	}

	String newTypeName(int code) {
		if (appID != null)
			return appID.getIDString() + System.currentTimeMillis() + code;
		if (bundle != null)
			return bundle.getLocation() + System.currentTimeMillis() + code;
		if (location != null)
			return location + System.currentTimeMillis() + code;
		return "default" + System.currentTimeMillis() + code;
	}

	@Override
	public AppPermissionType addException(Enum<?> action, String filter) {
		return addException(action, filter, null);
	}

	@Override
	public AppPermissionType addException(String permName, String[] args, ConditionInfo cond) {
		AppPermissionTypeImpl at = createType();
		String actions = null, filter = null;
		if (args != null) {
			if (args.length >= 1)
				filter = args[0];
			if (args.length >= 2)
				actions = args[1];
		}
		at.registerPermission(permName, filter, actions, cond);
		// at.setOwner(this);
		allTypes.put(at.name, at);
		at.mode = false;
		nTypes.add(at);
		return at;
	}

	@Override
	public List<AppPermissionType> getExceptions() {
		return nTypes;
	}

	@Override
	synchronized public void removeException(String name) {
		removeType(name);
	}

	@Override
	public AppPermissionType addPermission(String permName, String[] args, ConditionInfo cond) {
		AppPermissionTypeImpl at = createType();
		String actions = null, filter = null;
		if (args != null) {
			if (args.length >= 1)
				filter = args[0];
			if (args.length >= 2)
				actions = args[1];
		}
		at.registerPermission(permName, filter, actions, cond);
		// at.setOwner(this);
		allTypes.put(at.name, at);
		at.mode = true;
		pTypes.add(at);
		return at;
	}

	@Override
	public AppPermissionType addPermission(Enum<?> action, String filter, ConditionInfo cond) {
		String permName = getPermName(action);
		AppPermissionTypeImpl at = createType();
		at.registerPermission(permName, filter, action.name(), cond);
		allTypes.put(at.name, at);
		at.mode = true;
		pTypes.add(at);
		return at;
	}

	@Override
	public AppPermissionType addPermission(Enum<?> action, String filter) {
		return addPermission(action, filter, null);
	}

	@Override
	public void removePermission(String name) {
		removeType(name);
	}

	/*
	 * Add a policy to the AppPermission that is already commited to the security manager. Granted policies are added to
	 * the "granted" list, where the policies not yet granted are added to pTypes or nTypes dependent of the access
	 * mode.
	 */
	void add(ConditionalPermissionInfo pInfo) {
		granted.put(pInfo.getName(), pInfo);
	}

	/*
	 * If the permission to be removed was already granted, its to be removed from 'granted' map. Possibly the
	 * permission is added to this appPermission but it wasn't yet applied to the SecurityManager, in this case it is in
	 * one of the lists nTypes or pTypes.
	 */
	void removeType(String name) {
		AppPermissionType apt = allTypes.get(name);
		if (apt != null) {
			allTypes.remove(name);
			nTypes.remove(apt);
			pTypes.remove(apt);
		}

		removeGranted(name);
		granted.remove(name);
	}

	void completeTypes() {
		for (AppPermissionType apt : pTypes) {
			AppPermissionTypeImpl aptimpl = (AppPermissionTypeImpl) apt;
			aptimpl.postInit(this);
		}

		for (AppPermissionType apt : nTypes) {
			AppPermissionTypeImpl aptimpl = (AppPermissionTypeImpl) apt;
			aptimpl.postInit(this);
		}
	}

	boolean apply() {
		boolean exists = false;
		completeTypes();
		// First get the permissions table
		ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();

		// apply the permissions
		for (AppPermissionType apt : pTypes) {
			// Create new permission info object each new entry
			// Multiple entries with same name are not permitted.
			AppPermissionTypeImpl aptimpl = (AppPermissionTypeImpl) apt;
			String name = aptimpl.name;
			ConditionInfo conds[] = new ConditionInfo[aptimpl.conds.size()];
			PermissionInfo perms[] = new PermissionInfo[aptimpl.perms.size()];
			ConditionalPermissionInfo cpi = cpa.newConditionalPermissionInfo(name, aptimpl.conds.toArray(conds),
					aptimpl.perms.toArray(perms), ConditionalPermissionInfo.ALLOW);
			// Check if a permission info with the same name exists
			for (ConditionalPermissionInfo tmpcpi : piList) {
				// If a permission info exists in the table remove it before adding the new info
				if (PermissionManagerImpl.implies(tmpcpi, cpi)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				if (Configuration.DEBUG) {
					log.info(apt.getDeclarationString());
				}
				piList.add(cpi);
				aptimpl.pInfo = cpi;
			}
			else
				exists = false;
		}

		// apply the exceptions
		for (AppPermissionType apt : nTypes) {
			// Create new permission info object each new entry
			// Multiple entries with same name are not permitted.
			AppPermissionTypeImpl aptimpl = (AppPermissionTypeImpl) apt;
			String name = aptimpl.name;
			ConditionInfo conds[] = new ConditionInfo[aptimpl.conds.size()];
			PermissionInfo perms[] = new PermissionInfo[aptimpl.perms.size()];
			ConditionalPermissionInfo cpi = cpa.newConditionalPermissionInfo(name, aptimpl.conds.toArray(conds),
					aptimpl.perms.toArray(perms), ConditionalPermissionInfo.DENY);
			// Check if a permission info with the same name exists
			for (ConditionalPermissionInfo tmpcpi : piList) {
				// If a permission info exists in the table remove it before adding the new info with the same name
				if (PermissionManagerImpl.implies(tmpcpi, cpi)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				if (Configuration.DEBUG) {
					log.info(apt.getDeclarationString());
				}
				piList.add(cpi);
				aptimpl.pInfo = cpi;
			}
			else
				exists = false;
		}
		boolean result = cpu.commit();
		refresh();
		return result;
	}

	void refresh() {
		/*
		 * To avoid ConcurrentModificationException, You can't remove from list if you're browsing it with "for each"
		 * loop. We use Iterator instead.
		 */
		// transfer the permissions to the granteds
		for (Iterator<AppPermissionType> it = pTypes.iterator(); it.hasNext();) {
			AppPermissionType apt = it.next();
			AppPermissionTypeImpl aptimpl = (AppPermissionTypeImpl) apt;
			String name = aptimpl.name;
			it.remove();
			granted.put(name, apt.getDeclarationInfo());
		}

		// transfer the exceptions to the granteds
		for (Iterator<AppPermissionType> it = nTypes.iterator(); it.hasNext();) {
			AppPermissionType apt = it.next();
			AppPermissionTypeImpl aptimpl = (AppPermissionTypeImpl) apt;
			String name = aptimpl.name;
			it.remove();
			granted.put(name, apt.getDeclarationInfo());
		}
	}

	@Override
	public void removeAllPolicies() {
		// Quarantine the app
		// remove All Granted
		// First get the permissions table
		ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();

		Set<Entry<String, ConditionalPermissionInfo>> grantedsSet = granted.entrySet();
		for (Map.Entry<String, ConditionalPermissionInfo> entry : grantedsSet) {

			// Create new permission info object each new entry
			// Multiple entries with same name are not permitted.
			String name = entry.getKey();
			/*
			 * If the bundle location condition is wildcarded the entry sholdn't be removed, because it can affect other
			 * apps too.
			 */
			ConditionalPermissionInfo cpi = entry.getValue();
			ConditionInfo cinfos[] = cpi.getConditionInfos();
			boolean wildcard = true;
			for (ConditionInfo ci : cinfos) {
				if (ci.getType().equals(BUNDLE_LOCATION_CONDITION_NAME) && !ci.getArgs()[0].endsWith("*")) {
					wildcard = false;
				}
			}
			// If the location condition is not wildcarded check if a permission info with the same name exists
			if (!wildcard)
				for (ConditionalPermissionInfo tmpcpi : piList) {
					// If a permission info exists in the table remove it before adding the new info with the same name
					if (tmpcpi.getName().equals(name)) {
						if (Configuration.DEBUG)
							log.info("Removed Policy: " + tmpcpi.getEncoded());
						piList.remove(tmpcpi);
						break;
					}
				}
		}
		cpu.commit();
		// remove the policies not yet applied
		nTypes.clear();
		pTypes.clear();
		// remove the granted policies
		granted.clear();
		allTypes.clear();
	}

	@Override
	public Map<String, ConditionalPermissionInfo> getGrantedPerms() {
		return granted;
	}

	void removeGranted(String name) {
		// First get the permissions table
		ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();
		for (ConditionalPermissionInfo tmpcpi : piList) {
			// If a permission info exists in the table remove it
			if (tmpcpi.getName().equals(name)) {
				if (Configuration.DEBUG)
					log.info("Removed Policy: " + tmpcpi.getEncoded());
				piList.remove(tmpcpi);
				break;
			}
		}
		cpu.commit();
	}
}
