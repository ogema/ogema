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
package org.ogema.impl.security;

import java.io.PrintStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.List;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.AppDomainCombiner;
import org.ogema.accesscontrol.ChannelPermission;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.ResourceAccessRights;
import org.ogema.accesscontrol.ResourcePermission;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.model.Resource;
import org.ogema.core.security.AppPermission;
import org.ogema.core.security.WebAccessManager;
import org.ogema.resourcetree.TreeElement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import org.osgi.service.http.HttpService;
import org.osgi.service.useradmin.UserAdmin;
import org.slf4j.Logger;

@Component
public class PermissionManagerImpl implements PermissionManager {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	private static final String BUNDLE_LOCATION_CONDITION_NAME = "org.osgi.service.condpermadmin.BundleLocationCondition";
	AccessManagerImpl accessMan;
	SecurityManager security;
	HashSet<String> permNames;

	ConditionalPermissionAdmin cpa;
	private WebAccessManagerImpl webAccess;

	@Reference
	HttpService http;

	@Reference
	UserAdmin ua;

	@Reference
	AdministrationManager admin;
	private AppDomainCombiner domainCombiner;

	private ServiceRegistration<PermissionManager> sreg;

	@Override
	public WebAccessManager getWebAccess() {
		return webAccess;
	}

	public PermissionManagerImpl() {
		security = System.getSecurityManager();
		this.domainCombiner = new AppDomainCombiner();
	}

	@Activate
	protected void activate(BundleContext bc) throws BundleException {
		// check if the security is activated
		String security = System.getProperty("org.ogema.security", "on");
		if (security.equals("on")) {
			// Get reference to ConditionalPermissionAdmin
			ServiceReference<?> sRef = bc.getServiceReference(ConditionalPermissionAdmin.class.getName());
			if (sRef != null) {
				cpa = (ConditionalPermissionAdmin) bc.getService(sRef);
			}
			else {
				throw new BundleException(
						"ConditinalPermissionAdmin service is not available. OGEMA Security is disabled.");
			}

			this.accessMan = new AccessManagerImpl(ua, bc, this);
			bc.addBundleListener(this.accessMan);

			this.webAccess = new WebAccessManagerImpl(this, admin);
			// Register service for the security calls
			sreg = bc.registerService(PermissionManager.class, this, null);
			new ShellCommands(this, bc);
		}
		else {
			sreg = bc.registerService(PermissionManager.class, new DummySecurityManager(http, admin), null);
		}
	}

	@Deactivate
	protected void deactivate(BundleContext context) throws Exception {
		if (sreg != null) {
			sreg.unregister();
		}
	}

	@Override
	public AccessManager getAccessManager() {
		return accessMan;
	}

	@Override
	public void installPerms(AppPermission pInfos) {
		AppPermissionImpl pimpl = (AppPermissionImpl) pInfos;
		pimpl.apply();
	}

	@Override
	public AppPermission getPolicies(final AppID app) {
		if (!handleSecurity(new AdminPermission(AdminPermission.APP))) {
			return null;
		}
		/*
		 * Scan permission table and and check for each ConditionalPermissionInfo if it match the location of app.
		 */
		AppPermission ap = AccessController.doPrivileged(new PrivilegedAction<AppPermission>() {
			public AppPermission run() {
				AppPermissionImpl result = new AppPermissionImpl(cpa, app);
				result.bundle = app.getBundle();
				boolean match = false;
				boolean blcIsSet = false;
				String appLoc = app.getBundle().getLocation();
				// First get the permissions table
				ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
				List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();

				for (ConditionalPermissionInfo pInfo : piList) {
					// Get the condition infos to check which of them match the given app location ...
					ConditionInfo cia[] = pInfo.getConditionInfos();
					if (cia.length == 0) // if no conditions are set, its a default permission
					{
						result.add(pInfo);
					}
					else {
						for (ConditionInfo tmpci : cia) {
							match = false;
							blcIsSet = false;
							// ... and check for BundleLocationCondition
							if (tmpci.getType().equals(BUNDLE_LOCATION_CONDITION_NAME)) {
								blcIsSet = true;
								String args[] = tmpci.getArgs();
								int length = args.length;
								if (length == 0)
									continue;
								String loc = args[0];
								// Is the location info wildcarded?
								int index = loc.indexOf('*');
								if (index != -1)
									loc = loc.substring(0, index);
								if (appLoc.startsWith(loc)) {
									match = true;
									// Is the condition negated?
									if (length >= 2 && args[1] != null && args[1].equals("!"))
										match = !match;
								}
								break;
							}
						}
						if (match || !blcIsSet) {
							result.add(pInfo);
						}
					}
				}
				return result;
			}
		});
		return ap;
	}

	@Override
	public boolean handleSecurity(Permission perm) {
		Application app = null;
		AppID id = admin.getContextApp(this.getClass());
		if (id != null)
			app = id.getApplication();
		if (security != null) {
			AccessControlContext acc = null;
			try {
				if (app != null) {
					acc = getACC(app);
					security.checkPermission(perm, acc);
				}
				else {
					security.checkPermission(perm);
				}
			} catch (SecurityException e) {
				logger.info(e.getMessage());
				return false;
			}
		}
		return true;
	}

	boolean handleSecurity(Permission perm, AccessControlContext acc) {
		if (security != null) {
			try {
				if (acc == null)
					security.checkPermission(perm);
				else
					security.checkPermission(perm, acc);
			} catch (SecurityException e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkCreateResource(final Application app, Class<? extends Resource> type, String name, int count) {
		// Get the AccessControlContex of the involved app
		AccessControlContext acc = getACC(app);
		final ResourcePermission perm = new ResourcePermission(name, type, 0);
		try {
			acc.checkPermission(perm);
		} catch (SecurityException e) {
			return false;
		}
		return true;
	}

	private AccessControlContext getACC(final Application app) {
		AccessControlContext acc = AccessController.doPrivileged(new PrivilegedAction<AccessControlContext>() {
			public AccessControlContext run() {
				if (app != null) {
					ProtectionDomain[] pda = new ProtectionDomain[1];
					pda[0] = app.getClass().getProtectionDomain();
					return new AccessControlContext(new AccessControlContext(pda), domainCombiner);
				}
				else {
					return null;
				}
			}
		});
		return acc;
	}

	@Override
	public boolean checkDeleteResource(Application app, TreeElement te) {
		// Get the AccessControlContex of the involved app
		AccessControlContext acc = getACC(app);

		ResourcePermission perm = new ResourcePermission(ResourcePermission.DELETE, te, 0);
		// Check create permission
		return handleSecurity(perm, acc);
	}

	// check permManager... return result for TreeElement if OK, else return null.
	@Override
	public ResourceAccessRights getAccessRights(Application app, TreeElement el) {
		int accessMask = 0;

		// Get the AccessControlContex of the involved app
		AccessControlContext acc = getACC(app);

		ResourcePermission perm = new ResourcePermission(ResourcePermission.READ, el, 0);
		// Check read permission
		if (handleSecurity(perm, acc))
			accessMask |= ResourcePermission._READ;

		perm = new ResourcePermission(ResourcePermission.WRITE, el, 0);
		// Check write permission
		if (handleSecurity(perm, acc))
			accessMask |= ResourcePermission._WRITE;

		perm = new ResourcePermission(ResourcePermission.DELETE, el, 0);
		// Check delete permission
		if (handleSecurity(perm, acc))
			accessMask |= ResourcePermission._DELETE;

		perm = new ResourcePermission(ResourcePermission.ADDSUB, el, 0);
		// Check add sub resource permission
		if (handleSecurity(perm, acc))
			accessMask |= ResourcePermission._ADDSUB;

		perm = new ResourcePermission(ResourcePermission.ACTIVITY, el, 0);
		// Check add sub resource permission
		if (handleSecurity(perm, acc))
			accessMask |= ResourcePermission._ACTIVITY;
		return new ResourceAccessRights(accessMask);
	}

	@Override
	public void setDefaultPolicies(AppPermission ap) {
		/*
		 * At this moment a newly installed app doesn't get any permission as default. This method could be used to
		 * define a set of permissions that are applied automatically to all newly installed applications.
		 */
		// AppPermissionImpl apimpl = (AppPermissionImpl) ap;
		// apimpl.addDefaults();
	}

	@Override
	public Object getSystemPermissionAdmin() {
		return cpa;
	}

	@Override
	public AppPermission createAppPermission(String uri) {
		return new AppPermissionImpl(cpa, uri);
	}

	@Override
	public AdministrationManager getAdminManager() {
		return admin;
	}

	@Override
	public AppPermission getDefaultPolicies() {
		AppPermissionImpl result = new AppPermissionImpl(cpa);
		/*
		 * Scan permission table
		 */
		boolean match = false;
		// First get the permissions table
		ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();

		for (ConditionalPermissionInfo pInfo : piList) {
			// Get the condition infos if its a BundleLocationCondition, in this case its not a default policy
			ConditionInfo cia[] = pInfo.getConditionInfos();
			if (cia.length != 0)
				for (ConditionInfo tmpci : cia) {
					if (tmpci.getType().equals(BUNDLE_LOCATION_CONDITION_NAME)) {
						match = true;
						break;
					}
				}
			if (!match) {
				result.add(pInfo);
			}
			match = false;
		}
		return result;
	}

	@Override
	public void printPolicies(PrintStream os) {
		/*
		 * Scan permission table
		 */
		// First get the permissions table
		ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();

		for (ConditionalPermissionInfo pInfo : piList) {
			os.println(pInfo.getEncoded());
		}
	}

	@Override
	public boolean checkAddChannel(ChannelConfiguration configuration, DeviceLocator deviceLocator) {
		int action = 0;
		String busID;
		String devAddr;
		String chParams;

		busID = deviceLocator.getInterfaceName();
		devAddr = deviceLocator.getDeviceAddress();
		chParams = configuration.getChannelLocator().getChannelAddress();
		Direction dir = configuration.getDirection();
		switch (dir) {
		case DIRECTION_INPUT:
			action = ChannelPermission._READ;
			break;
		case DIRECTION_OUTPUT:
		case DIRECTION_INOUT:
			action = ChannelPermission._WRITE;
			break;
		default:
			action = ChannelPermission._ALLACTIONS;
			break;
		}

		Application app = admin.getContextApp(this.getClass()).getApplication();
		AccessControlContext acc = getACC(app);
		ChannelPermission perm = new ChannelPermission(busID, devAddr, chParams, action);
		try {
			acc.checkPermission(perm);
		} catch (SecurityException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean checkDeleteChannel(ChannelConfiguration configuration, DeviceLocator deviceLocator) {
		String busID;
		String devAddr;
		String chParams;

		busID = deviceLocator.getInterfaceName();
		devAddr = deviceLocator.getDeviceAddress();
		chParams = configuration.getChannelLocator().getChannelAddress();

		Application app = admin.getContextApp(this.getClass()).getApplication();
		AccessControlContext acc = getACC(app);
		ChannelPermission perm = new ChannelPermission(busID, devAddr, chParams, ChannelPermission._DELETE);
		try {
			acc.checkPermission(perm);
		} catch (SecurityException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
