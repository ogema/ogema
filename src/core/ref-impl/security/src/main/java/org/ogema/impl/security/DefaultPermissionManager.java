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
package org.ogema.impl.security;

import java.io.PrintStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferencePolicyOption;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.AppDomainCombiner;
import org.ogema.accesscontrol.Authenticator;
import org.ogema.accesscontrol.ChannelPermission;
import org.ogema.accesscontrol.HttpConfigManagement;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.ResourceAccessRights;
import org.ogema.accesscontrol.ResourcePermission;
import org.ogema.accesscontrol.UserRightsProxy;
import org.ogema.accesscontrol.Util;
import org.ogema.accesscontrol.WebAccessPermission;
import org.ogema.applicationregistry.ApplicationRegistry;
import org.ogema.core.administration.CredentialStore;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.model.Resource;
import org.ogema.core.security.AppPermission;
import org.ogema.core.security.WebAccessManager;
import org.ogema.resourcetree.TreeElement;
import org.ogema.staticpolicy.StaticPolicies;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import org.osgi.service.http.HttpService;
import org.osgi.service.permissionadmin.PermissionInfo;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.service.useradmin.UserAdmin;
import org.slf4j.Logger;

/**
 * @author Zekeriya Mansuroglu
 *
 */
@References({
	@Reference(
			referenceInterface=Authenticator.class,
			policy=ReferencePolicy.DYNAMIC,
			cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
			name="authenticator",
			bind="addAuthenticator",
			unbind="removeAuthenticator"
	),
	@Reference(
			referenceInterface=HttpConfigManagement.class,
			policy=ReferencePolicy.DYNAMIC,
			policyOption=ReferencePolicyOption.GREEDY,
			cardinality=ReferenceCardinality.OPTIONAL_UNARY,
			name="headerManagement",
			bind="setHeaderManagement",
			unbind="unsetHeaderManagement"
	)
})
@Component(
		immediate = true,
		configurationPid=ConfigurationConstants.CONFIGURATION_PID,
		policy=ConfigurationPolicy.OPTIONAL
)
@Service(PermissionManager.class)
public class DefaultPermissionManager implements PermissionManager {

	@Reference
	HttpService http;

	@Reference
	UserAdmin ua;

	@Reference
	ApplicationRegistry appreg;

	@Reference
	CredentialStore cStore;

	@Reference
	StaticPolicies staticPolicies;
	
	private final AtomicReference<HttpConfigManagement> headerManagement 
			= new AtomicReference<HttpConfigManagement>(null);
	

	protected void setHeaderManagement(HttpConfigManagement headerManagement) {
		this.headerManagement.set(headerManagement);
	}

	protected void unsetHeaderManagement(HttpConfigManagement headerManagement) {
		this.headerManagement.compareAndSet(headerManagement, null);
	}
	
	BundleContext bc;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	private static final String BUNDLE_LOCATION_CONDITION_NAME = "org.osgi.service.condpermadmin.BundleLocationCondition";

	public final ThreadLocal<AccessControlContext> currentAccessControlContext = new ThreadLocal<>();

	volatile AccessManagerImpl accessMan;
	volatile SecurityManager security;
	volatile SecurityManager initial;
	HashSet<String> permNames;

	ConditionalPermissionAdmin cpa;
	WebAccessManager webAccess;

	private AppDomainCombiner domainCombiner;

	AppPermissionImpl defaultPolicies;
	private ShellCommands sc;

	private final HashMap<Application, AccessControlContext> accs = new HashMap<>();
	final Map<String, ServiceReference<Authenticator>> authenticators = new ConcurrentHashMap<>(4);

	@Override
	public WebAccessManager getWebAccess() {
		return webAccess;
	}

	@Override
	public WebAccessManager getWebAccess(AppID app) {
		// XXX
		return ((ApplicationWebAccessFactory) webAccess).createApplicationWebAccessManager(app);
	}

	@Override
	public boolean closeWebAccess(AppID app) {
		return ((ApplicationWebAccessFactory) webAccess).closeWebAccess(app);
	}
	
	@Activate
	public synchronized void activate(BundleContext bc, Map<String, Object> config) throws BundleException {
		this.bc = bc;
		// Get reference to ConditionalPermissionAdmin
		ServiceReference<?> sRef = bc.getServiceReference(ConditionalPermissionAdmin.class.getName());
		if (sRef != null) {
			cpa = (ConditionalPermissionAdmin) bc.getService(sRef);
		}
		else {
			throw new BundleException(
					"ConditinalPermissionAdmin service is not available. OGEMA Security is disabled.");
		}

		try {
			registerURPHandler(bc);
		} catch (Exception e) {
			throw new BundleException("URPHandler registration failed.", e);
		}

		this.accessMan = new AccessManagerImpl(ua, bc, cStore, this, staticPolicies, config);
		bc.addBundleListener(this.accessMan); // BundleListener is needed to
												// manage storage area each app.
		this.webAccess = new ApplicationWebAccessFactory(this, http, ua, headerManagement, config);

		security = System.getSecurityManager();
		// Create and set custom SecurityManager
		if (security != null) {
			try {
				if (!registerSecurity().await(10, TimeUnit.SECONDS)) {
					throw new TimeoutException(); // should not happen
				}
			} catch (InterruptedException | TimeoutException e) {
				this.webAccess = null;
				this.sc = null;
				this.accessMan = null;
				this.bc = null;
				this.cpa = null;
				this.security = null;
				if (e instanceof InterruptedException)
					Thread.currentThread().interrupt();
				return;
			}
		}
		this.domainCombiner = new AppDomainCombiner();
		sc = new ShellCommands(this, bc);
	}
	
	@org.apache.felix.scr.annotations.Modified
	protected void modified(Map<String, Object> config) {
		final AccessManagerImpl accMan = this.accessMan;
		if (accMan == null) // should not really happen
			logger.warn("Received a property changed callback for AccessManager, but it is not available");
		else
			accMan.configChanged(config);
		final WebAccessManager webAccess = this.webAccess;
		if (webAccess != null)
			((ApplicationWebAccessFactory) webAccess).configChanged(config);
	}
	

	@Deactivate
	protected synchronized void deactivate(BundleContext context) throws Exception {
		if (this.webAccess != null)
			((ApplicationWebAccessFactory) webAccess).close();
		if (sc != null)
			sc.close();
		if (context != null && accessMan != null) {
			context.removeBundleListener(this.accessMan);
		}
		if (accessMan != null)
			accessMan.close();
		unregisterURPHandler(context);
		// currentAccessControlContext // are we sure instances are cleaned up?
//		this.webAccess = null;
		this.sc = null;
//		this.accessMan = null; // avoid returning null in #getAccessManager
		this.bc = null;
		this.cpa = null;
		this.security = null;
		try {
			System.setSecurityManager(initial);
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
		this.initial = null;
	}
	
	// this construction is a workaround to a NoClassDefFoundError in Felix component activator
	// if the OgemaSecurityManager class (which is an optional dependency) is not available, and
	// one tries to register it in the activate method
	private final CountDownLatch registerSecurity() {
		final CountDownLatch l = new CountDownLatch(1);
		try {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						initial = System.getSecurityManager();
						security = new org.ogema.base.security.OgemaSecurityManager();
						System.setSecurityManager(security);
						logger.debug("OGEMA security manager installed: {}", security);
					} finally {
						l.countDown();
					}
				}
			}).start();
		} catch (NoClassDefFoundError e) {
			// OgemaSecurityManager dependency is optional
			l.countDown();
			logger.warn("No OGEMA security manager found; OGEMA file permissions will not be available");
		}
		return l;
	}

	protected void addAuthenticator(final ServiceReference<Authenticator> authenticator) {
		final Object id = authenticator.getProperty(Authenticator.AUTHENTICATOR_ID);
		if (!(id instanceof String)) {
			logger.warn("Authenticator service without authenticator.id in bundle {}", authenticator.getBundle());
			return;
		}
		if (Authenticator.DEFAULT_USER_PW_ID.equals(id))
			throw new IllegalArgumentException("Illegal authenticator id " + id + " in bundle " + authenticator.getBundle());
		final ServiceReference<Authenticator> old = authenticators.put((String) id, authenticator);
		if (old != null) {
			logger.warn("Duplicate authenticator id: {}: {}, {}",id, old, authenticator);
		}
	}
	
	protected void removeAuthenticator(final ServiceReference<Authenticator> authenticator) {
		final Object id = authenticator.getProperty(Authenticator.AUTHENTICATOR_ID);
		if (!(id instanceof String)) {
			return;
		}
		if (authenticator.equals(authenticators.get(id)))
			authenticators.remove(id);
	}

	private ServiceRegistration<URLStreamHandlerService> urpHandlerRegistratrion;

	public void registerURPHandler(BundleContext bc) throws Exception {
		Dictionary<String, Object> urlHandlerProperties = new Hashtable<>();
		urlHandlerProperties.put(URLConstants.URL_HANDLER_PROTOCOL, UrpUrlHandler.PROTOCOL);
		urpHandlerRegistratrion = bc.registerService(URLStreamHandlerService.class, new UrpUrlHandler(),
				urlHandlerProperties);

	}

	public void unregisterURPHandler(BundleContext bc) throws Exception {
		if (urpHandlerRegistratrion != null) {
			urpHandlerRegistratrion.unregister();
		}

	}

	@Override
	public AccessManager getAccessManager() {
		return accessMan;
	}

	@Override
	public boolean installPerms(AppPermission pInfos) {
		AppPermissionImpl pimpl = (AppPermissionImpl) pInfos;
		return pimpl.apply();
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
					// Get the condition infos to check which of them match the
					// given app location ...
					ConditionInfo cia[] = pInfo.getConditionInfos();
					if (cia.length == 0) // if no conditions are set, its a
											// default permission
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
								match = checkBCMatch(args, appLoc);
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
		if (security != null) {
			Application app = null;
			AppID id = appreg.getContextApp(this.getClass());
			if (id != null)
				app = id.getApplication();
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

	@Override
	public boolean handleSecurity(Permission perm, AccessControlContext acc) {
		if (security != null) {
			try {
				if (acc == null)
					security.checkPermission(perm);
				else
					security.checkPermission(perm, acc);
			} catch (SecurityException e) {
				return false;
			} catch (Exception e) { // for malformed permissions
				logger.warn("Unexpected exception in security check",e);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean handleSecurity(String user, Permission perm) {

		// check app perms
		if (!handleSecurity(perm))
			return false;

		// check user perms
		if (security != null) {
			AccessControlContext acc = null;
			try {
				acc = getACC(user);
				security.checkPermission(perm, acc);
			} catch (SecurityException e) {
				logger.info(e.getMessage());
				return false;
			} catch (Exception e) { // for malformed permissions
				logger.warn("Unexpected exception in security check",e);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkWebAccess(AppID accessor, AppID access) {
		if (!isSecure())
			return true;
		Bundle b = access.getBundle();
		WebAccessPermission wap = new WebAccessPermission(b.getSymbolicName(), null, null, null);
		Application app = accessor.getApplication();
		AccessControlContext acc = getACC(app);
		return handleSecurity(wap, acc);
	}

	@Override
	public boolean checkCreateResource(final Application app, Class<? extends Resource> type, String name, int count) {
		// Get the AccessControlContex of the involved app
		AccessControlContext acc = getACC(app);
		final ResourcePermission perm = new ResourcePermission(name, type, 0);
		return handleSecurity(perm, acc);
	}

	private AccessControlContext getACC(final Application app) {
		AccessControlContext acc = AccessController.doPrivileged(new PrivilegedAction<AccessControlContext>() {
			public AccessControlContext run() {
				if (app != null) {
					AccessControlContext result = accs.get(app);
					if (result == null) {
						ProtectionDomain[] pda = new ProtectionDomain[1];
						pda[0] = app.getClass().getProtectionDomain();
						result = new AccessControlContext(new AccessControlContext(pda), domainCombiner);
						accs.put(app, result);
					}
					return result;
				}
				else {
					return null;
				}
			}
		});
		return acc;
	}

	private AccessControlContext getACC(final String user) {
		AccessControlContext acc = AccessController.doPrivileged(new PrivilegedAction<AccessControlContext>() {
			public AccessControlContext run() {
				ProtectionDomain[] pda;
				List<String> parents = accessMan.getParents(user);
				parents.add(user);
				List<UserRightsProxy> urps = new ArrayList<>();
				for (String parent : parents) {
					urps.add(accessMan.getUrp(parent));
				}
				pda = new ProtectionDomain[urps.size()];
				for (int i = 0; i < urps.size(); i++) {
					pda[i] = urps.get(i).getClass().getProtectionDomain();
				}
				return new AccessControlContext(new AccessControlContext(pda), domainCombiner);
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

	// check permManager... return result for TreeElement if OK, else return
	// null.
	@Override
	public ResourceAccessRights getAccessRights(Application app, TreeElement el) {
		return getAccessRights(app, el, null);
	}
	
	@Override
	public ResourceAccessRights getAccessRights(Application app, TreeElement el, String user) {
		// Get the AccessControlContex of the involved app
		// If no user app is involved in this case the caller system app must
		// have set the AccessControlContext to be
		// checked.
		AccessControlContext acc;
		if ((acc = currentAccessControlContext.get()) == null) {
			acc = getACC(app);
		}
		return new ResourceAccessRights(acc, el, this, user);
	}

	@Override
	public AppPermissionImpl setDefaultPolicies() {

		defaultPolicies = new AppPermissionImpl(cpa);
		/*
		 * Scan permission table
		 */
		boolean match = false;
		// First get the permissions table
		ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();

		for (ConditionalPermissionInfo pInfo : piList) {
			// Get the condition infos if its a BundleLocationCondition, in this
			// case its not a default policy
			ConditionInfo cia[] = pInfo.getConditionInfos();
			if (cia.length != 0)
				for (ConditionInfo tmpci : cia) {
					if (tmpci.getType().equals(BUNDLE_LOCATION_CONDITION_NAME)) {
						match = true;
						break;
					}
				}
			if (!match) {
				ConditionalPermissionInfo pInfoAccess = cpa.newConditionalPermissionInfo(pInfo.getName(),
						pInfo.getConditionInfos(), pInfo.getPermissionInfos(), pInfo.getAccessDecision());
				defaultPolicies.add(pInfoAccess);

			}
			match = false;
		}
		return defaultPolicies;
	}

	@Override
	public Object getSystemPermissionAdmin() {
		return cpa;
	}

	@Override
	public AppPermission createAppPermission(String uri) {
		AppPermissionImpl api = null;
		if (uri == "defaultPolicy") {
			api = getDefaultPolicies();
		}
		else {
			api = new AppPermissionImpl(cpa, uri);
			api.granted = (ConcurrentHashMap<String, ConditionalPermissionInfo>) getGrantedPerms(uri);
		}
		return api;
	}

	@Override
	public AppPermissionImpl getDefaultPolicies() {
		if (this.defaultPolicies != null)
			return defaultPolicies;

		defaultPolicies = setDefaultPolicies();
		return defaultPolicies;
	}

	@Override
	public boolean isDefaultPolicy(String permtype, String permname, String actions) {
		AppPermissionImpl ap = getDefaultPolicies();
		ConcurrentHashMap<String, ConditionalPermissionInfo> granteds = ap.granted;

		Set<Entry<String, ConditionalPermissionInfo>> grantedsSet = granteds.entrySet();
		for (Map.Entry<String, ConditionalPermissionInfo> entry : grantedsSet) {
			// Create new permission info object each new entry
			// Multiple entries with same name are not permitted.
			ConditionalPermissionInfo cpi = entry.getValue();
			PermissionInfo perms[] = cpi.getPermissionInfos();
			for (PermissionInfo pi : perms) {
				if (pi.getType().equals(permtype)) {
					String name = pi.getName();
					if (permname != null && (name == null || name.equals(permname))) {
						String acts = pi.getActions();
						if (actions != null && acts != null) {
							String[] tmpActions = acts.split(",");
							String[] actionsArr = actions.split(",");
							if (Util.containsAll(tmpActions, actionsArr))
								return true;
						}
					}
				}
			}
		}
		return false;
	}

	static boolean implies(ConditionalPermissionInfo implier, ConditionalPermissionInfo implied) {
		/*
		 * Check Access decision
		 */
		if (!implier.getAccessDecision().equals(implied.getAccessDecision()))
			return false;
		/*
		 * Check the PermisssionInfos
		 */
		Object rpinfos[] = implier.getPermissionInfos();
		Object dpinfos[] = implied.getPermissionInfos();
		boolean success = Util.containsAll(rpinfos, dpinfos);
		if (!success)
			return false;
		/*
		 * Check the ConditionInfos
		 */
		rpinfos = implier.getConditionInfos();
		dpinfos = implied.getConditionInfos();
		if (dpinfos.length != rpinfos.length)
			return false;
		success = Util.containsAll(rpinfos, dpinfos);
		return success;
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
		ChannelPermission perm = new ChannelPermission(busID, devAddr, chParams, action);
		AppID id = appreg.getContextApp(this.getClass());
		AccessControlContext acc = null;
		if (id != null) {
			Application app = id.getApplication();
			acc = getACC(app);
		}
		return handleSecurity(perm, acc);
	}

	@Override
	public boolean checkDeleteChannel(ChannelConfiguration configuration, DeviceLocator deviceLocator) {
		String busID;
		String devAddr;
		String chParams;

		busID = deviceLocator.getInterfaceName();
		devAddr = deviceLocator.getDeviceAddress();
		chParams = configuration.getChannelLocator().getChannelAddress();

		ChannelPermission perm = new ChannelPermission(busID, devAddr, chParams, ChannelPermission._DELETE);
		AppID id = appreg.getContextApp(this.getClass());
		AccessControlContext acc = null;
		if (id != null) {
			Application app = appreg.getContextApp(this.getClass()).getApplication();
			acc = getACC(app);
		}
		return handleSecurity(perm, acc);
	}

	@Override
	public AccessControlContext getBundleAccessControlContext(final Class<?> cls) {
		AccessControlContext acc = AccessController.doPrivileged(new PrivilegedAction<AccessControlContext>() {
			public AccessControlContext run() {
				if (cls != null) {
					ProtectionDomain[] pda = new ProtectionDomain[1];
					pda[0] = cls.getProtectionDomain();
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
	public boolean removePermission(final Bundle b, final String permissionName, final String filterString,
			final String actions) {
		if (!handleSecurity(new AdminPermission(AdminPermission.APP))) {
			throw new SecurityException("Access denied! org.ogema.accesscontrol.AdminPermission(APP) is required.");
		}
		/*
		 * Scan permission table and and check for each ConditionalPermissionInfo if it match the location of the app.
		 */
		Boolean ap = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
			public Boolean run() {
				ArrayList<ConditionalPermissionInfo> changedPInfos = null;
				boolean result = false;
				boolean match = false;
				boolean isDefaultPol = false;
				String appLoc = "";
				if (b == null) {
					isDefaultPol = true;
				}

				if (b != null) {
					appLoc = b.getLocation();
				}
				// First get the permissions table
				ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
				List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();
				for (Iterator<ConditionalPermissionInfo> it = piList.iterator(); it.hasNext();) {
					ConditionalPermissionInfo cpInfo = it.next();
					// for (ConditionalPermissionInfo cpInfo : piList) {
					// Get the condition infos to check which of them match the
					// given app location ...
					ConditionInfo cia[] = cpInfo.getConditionInfos();
					if (cia.length == 0 && !isDefaultPol) // if no conditions
															// are set, its a
					// default permission
					{
						// in this case the permission couldn't be removed
						// rather a negative permission is to be added.
						match = false;
						continue;
					}
					else {
						match = true;
					}
					if (!isDefaultPol) {
						for (ConditionInfo tmpci : cia) {
							match = false;
							// ... and check for BundleLocationCondition
							if (tmpci.getType().equals(BUNDLE_LOCATION_CONDITION_NAME)) {
								String args[] = tmpci.getArgs();
								int length = args.length;
								if (length == 0)
									continue; // should never happen
								match = checkBCMatch(args, appLoc);
								break;
							}
						}
					}
					if (match) {
						// Check if the permission match
						PermissionInfo[] pinfos = cpInfo.getPermissionInfos();
						int length = pinfos.length;
						int index = -1;

						for (PermissionInfo pinfo : pinfos) {
							index++;
							String permType = pinfo.getType();
							if (!permType.equals(permissionName))
								continue;
							if (compareStr(filterString, pinfo.getName()) == 0
									&& compareStr(actions, pinfo.getActions()) == 0) {
								logger.debug("Remove policy " + cpInfo.getEncoded());
								it.remove();

								if (length > 1) {
									PermissionInfo[] tmpPinfos = new PermissionInfo[length - 1];
									int index2 = 0, addidx = 0;
									for (PermissionInfo tmppinfo : pinfos) {
										if (index != index2) {
											tmpPinfos[addidx++] = tmppinfo;
										}
										index2++;
									}
									ConditionalPermissionInfo tmpCpi = cpa.newConditionalPermissionInfo(
											cpInfo.getName(), cia, tmpPinfos, cpInfo.getAccessDecision());
									if (changedPInfos == null)
										changedPInfos = new ArrayList<>();
									changedPInfos.add(tmpCpi);
								}
								result = true;
							}
							else if (compareStr(filterString, pinfo.getName()) == 0 // only actions are to be reduced
									&& compareStr(actions, pinfo.getActions()) != 0) {
								PermissionInfo[] tmpPinfos = new PermissionInfo[1];
								tmpPinfos[0] = new PermissionInfo(permissionName, filterString, actions);
								ConditionalPermissionInfo tmpCpi = cpa.newConditionalPermissionInfo(
										"deny_" + System.currentTimeMillis(), cia, tmpPinfos, cpInfo.DENY);
								if (changedPInfos == null)
									changedPInfos = new ArrayList<>();
								changedPInfos.add(tmpCpi);
							}
						}
						match = false;
					}
				}

				if (changedPInfos != null && !changedPInfos.isEmpty()) {
					piList.addAll(changedPInfos);
				}
				cpu.commit();
				return result;
			}

		});
		return ap;

	}

	private boolean checkBCMatch(String[] args, String appLoc) {
		boolean match = false;
		boolean conditionNegated = false;
		String loc = args[0];
		int length = args.length;
		if (length >= 2 && args[1] != null && args[1].equals("!"))
			conditionNegated = true;
		// Is the location info wildcarded?
		int index = loc.indexOf('*');
		if (index != -1) {
			loc = loc.substring(0, index);
			// Is the condition negated?
			if ((appLoc.startsWith(loc)) && !conditionNegated) {
				// in this case the permission couldn't be removed
				// rather a negative permission is to be added.
				match = true;
			}
		}
		else {
			if ((appLoc.equals(loc)) && !conditionNegated)
				match = true;
		}
		return match;
	}

	protected int compareStr(String str1, String str2) {
		if (str1 == null && str2 == null) // both null ->match
			return 0;
		if (str1 == null || str2 == null) // not both null but one of them ->
											// not match
			return -1;
		if (str1.equals(str2))
			return 0;
		else
			return -1;
	}

	@Override
	public void setAccessContext(AccessControlContext acc) {
		currentAccessControlContext.set(acc);
	}

	@Override
	public void resetAccessContext() {
		currentAccessControlContext.set(null);
	}

	@Override
	public Map<String, ConditionalPermissionInfo> getGrantedPerms(String b) {
		/*
		 * Scan permission table and and check for each ConditionalPermissionInfo if it match the location of app.
		 */
		ConcurrentHashMap<String, ConditionalPermissionInfo> result = new ConcurrentHashMap<String, ConditionalPermissionInfo>();
		if (b == "defaultPolicy") {
			AppPermissionImpl ap = getDefaultPolicies();
			result = ap.granted;
			return result;
		}
		boolean match = false;
		boolean blcIsSet = false;
		String appLoc = b;// b.getLocation();
		// First get the permissions table
		ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();

		for (ConditionalPermissionInfo pInfo : piList) {
			// Get the condition infos to check which of them match the given
			// app location ...
			ConditionInfo cia[] = pInfo.getConditionInfos();
			if (cia.length == 0) // if no conditions are set, its a default
									// permission
			{
				result.put(pInfo.getName(), pInfo);
			}
			else {
				for (ConditionInfo tmpci : cia) {
					if (b == "all") {
						result.put(pInfo.getName(), pInfo);
						continue;
					}

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
					result.put(pInfo.getName(), pInfo);
				}
			}
		}
		return result;
	}

	@Override
	public boolean isSecure() {
		return (security != null);
	}

	@Override
	public ApplicationRegistry getApplicationRegistry() {
		return appreg;
	}

	@Override
	public boolean removePermissionManual(final ConditionalPermissionUpdate cpu, final Bundle b,
			final String permissionName, final String filterString, final String actions) {

		if (!handleSecurity(new AdminPermission(AdminPermission.APP))) {
			throw new SecurityException("Access denied! org.ogema.accesscontrol.AdminPermission(APP) is required.");
		}
		/*
		 * Scan permission table and and check for each ConditionalPermissionInfo if it match the location of the app.
		 */
		Boolean ap = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
			public Boolean run() {
				ArrayList<ConditionalPermissionInfo> changedPInfos = null;
				boolean result = false;
				boolean match = false;
				boolean isDefaultPol = false;
				String appLoc = "";
				if (b == null) {
					isDefaultPol = true;
				}

				if (b != null) {
					appLoc = b.getLocation();
				}
				// First get the permissions table

				List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();

				for (Iterator<ConditionalPermissionInfo> it = piList.iterator(); it.hasNext();) {
					ConditionalPermissionInfo cpInfo = it.next();
					// for (ConditionalPermissionInfo cpInfo : piList) {
					// Get the condition infos to check which of them match the
					// given app location ...
					ConditionInfo cia[] = cpInfo.getConditionInfos();
					if (cia.length == 0 && !isDefaultPol) // if no conditions
															// are set, its a
					// default permission
					{
						// in this case the permission couldn't be removed
						// rather a negative permission is to be added.
						match = false;
						continue;
					}
					else {
						match = true;
					}
					if (!isDefaultPol) {
						for (ConditionInfo tmpci : cia) {
							match = false;
							// ... and check for BundleLocationCondition
							if (tmpci.getType().equals(BUNDLE_LOCATION_CONDITION_NAME)) {
								String args[] = tmpci.getArgs();
								int length = args.length;
								if (length == 0)
									continue; // should never happen
								match = checkBCMatch(args, appLoc);
								break;
							}
						}
					}
					if (match) {
						// Check if the permission match
						PermissionInfo[] pinfos = cpInfo.getPermissionInfos();
						int length = pinfos.length;
						int index = -1;

						for (PermissionInfo pinfo : pinfos) {
							index++;
							String permType = pinfo.getType();
							if (!permType.equals(permissionName))
								continue;
							if (compareStr(filterString, pinfo.getName()) == 0
									&& compareStr(actions, pinfo.getActions()) == 0) {
								logger.debug("Remove policy " + cpInfo.getEncoded());
								it.remove();

								if (length > 1) {
									PermissionInfo[] tmpPinfos = new PermissionInfo[length - 1];
									int index2 = 0, addidx = 0;
									for (PermissionInfo tmppinfo : pinfos) {
										if (index != index2) {
											tmpPinfos[addidx++] = tmppinfo;
										}
										index2++;
									}
									ConditionalPermissionInfo tmpCpi = cpa.newConditionalPermissionInfo(
											cpInfo.getName(), cia, tmpPinfos, cpInfo.getAccessDecision());
									if (changedPInfos == null)
										changedPInfos = new ArrayList<>();
									changedPInfos.add(tmpCpi);
								}
								result = true;
							}
							else if (compareStr(filterString, pinfo.getName()) == 0 // only actions are to be reduced
									&& compareStr(actions, pinfo.getActions()) != 0) {
								PermissionInfo[] tmpPinfos = new PermissionInfo[1];
								tmpPinfos[0] = new PermissionInfo(permissionName, filterString, actions);
								ConditionalPermissionInfo tmpCpi = cpa.newConditionalPermissionInfo(
										"deny_" + System.currentTimeMillis(), cia, tmpPinfos, cpInfo.DENY);
								if (changedPInfos == null)
									changedPInfos = new ArrayList<>();
								changedPInfos.add(tmpCpi);
							}

						}
						match = false;
					}
				}

				if (changedPInfos != null && !changedPInfos.isEmpty()) {
					piList.addAll(changedPInfos);
				}

				return result;
			}

		});
		return ap;

	}
}
