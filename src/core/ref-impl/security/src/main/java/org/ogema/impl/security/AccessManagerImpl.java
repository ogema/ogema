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

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.AppPermissionFilter;
import org.ogema.accesscontrol.Authenticator;
import org.ogema.accesscontrol.Constants;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.SessionAuth;
import org.ogema.accesscontrol.UserRightsProxy;
import org.ogema.accesscontrol.WebAccessPermission;
import org.ogema.applicationregistry.ApplicationRegistry;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.CredentialStore;
import org.ogema.core.application.AppID;
import org.ogema.core.security.AppPermission;
import org.ogema.staticpolicy.StaticPolicies;
import org.ogema.staticpolicy.StaticUser;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import org.osgi.service.permissionadmin.PermissionInfo;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;

/**
 * All mechanisms related to role based access management is included in this class. Users including their properties
 * (public) and credentials (private) are stored persistently by the UserAdmin service. Code accessing the user
 * credentials or properties needs UserAdminPermission. For more information please refer to OSGi Service Platform
 * Service Compendium Release 4.
 * 
 * There are two ways to check user based permissions. First users could access web resources of apps over the web
 * interface and initiate security sensitive accesses that are protected by the security manager in common osgi way.
 * Second a user could access to security sensitive resources (like OGEMA resources via REST interface) without any
 * application to be involved.
 * 
 * In the first way for each installed app a Role from type GROUP is created. Each user is added as a member of the
 * group of the app its resources should be accessible by this user. The check of a permission ends up in the check if
 * the user has the role with the same name as the app id string. This is the case if the user is a member of the GROUP
 * that represents the app.
 * 
 * In the second way the user get access to specific OGEMA resources over the rest interface. These rights are granted
 * by the administration interface of the ogema framework. The implementation is similar to the first way mentioned
 * above. For each resource defined by its location path a Role from type GROUP is created. The location path could be
 * an exact path to a resource or it could be wildcarded that means that the access is recursively granted. The user
 * that should get the access right to a resource is added to the GROUP as a member.
 * 
 */

/**
 * @author Zekeriya Mansuroglu
 *
 */
class AccessManagerImpl implements AccessManager, BundleListener {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	final Map<String, ServiceReference<Authenticator>> authenticators;
	private volatile Collection<String> globallyAdmissibleAuthenticatorIds = null;
	
	// static final String ADMIN_NAME = "master";
	// static final String GUEST_NAME = "guest";
	// private static final String REST_NAME = "rest";

	static final String OGEMA_ROLE_NAME = "ogemaRole";

	static final String OGEMA_NATURAL_USER = "naturalUser";
	static final String OGEMA_MACHINE_USER = "machineUser";
	static final String OGEMA_APPLICATION = "applicationGroup";
	static final String OGEMA_USER = "userGroup";
	static final String OGEMA_RESOURCES = "resourcesGroup";
	static final String PARENT_PROP = "parentName";

	UserAdmin usrAdmin;

	private BundleContext osgi;

	static final String ALLAPPS = "ALL APPS";
	static final String NOAPPS = "NO APPS";

	static final String ALLRESOURCES = "ALL RESOURCES";

	private ServiceTracker<UserRightsProxy, UserRightsProxy> urpTracker;
	
	private final ThreadLocal<String> callerName = new ThreadLocal<String>() {
        @Override protected String initialValue() {
            return SYSTEM_ID;
        }
    };

	final ConcurrentHashMap<String, UserRightsProxy> urpMap;
	final PermissionManager permMan;
	final ApplicationRegistry appReg;
	final CredentialStore cStore;

	private int userCount;

	private int meanLoginTime;

	static final String WEB_ACCESS_PERMISSION_CLASS_NAME = WebAccessPermission.class.getName();

	AccessManagerImpl(UserAdmin ua, BundleContext osgi, CredentialStore cs, PermissionManager pm,
			StaticPolicies stPol, Map<String, Object> config) {
		this.cStore = cs;
		this.usrAdmin = ua;
		this.osgi = osgi;
		this.urpMap = new ConcurrentHashMap<>();
		this.permMan = pm;
		this.appReg = permMan.getApplicationRegistry();
		this.authenticators = ((DefaultPermissionManager) pm).authenticators;

		HashSet<String> inited = new HashSet<>();

		createDefaultGroups();

		initURPTracker();

		createDefaultUsers(stPol, inited);

		postInitUsers(inited);
		configChanged(config);
	}

	void configChanged(final Map<String, Object> properties) {
		final Object authenticators = properties == null ? null : properties.get(ConfigurationConstants.OGEMA_AUTHENTICATORS_CONFIG);
		this.globallyAdmissibleAuthenticatorIds = 
				authenticators instanceof String ? Arrays.asList(((String) authenticators).split(ConfigurationConstants.AUTH_SEP)) : null;
	}
	
	private void postInitUsers(Set<String> initedUsers) {
		// For users that aren't statically created the URP has to be installed too
		try {
			Role[] roles = usrAdmin.getRoles(null);
			if (roles == null)
				return;
			for (Role r : roles) {
				int type = r.getType();
				switch (type) {
				case Role.GROUP:
					logger.debug("Registered group: " + r.getName());
					break;
				case Role.USER:
					String name = r.getName();
					logger.debug("Registered user: " + name);
					if (!initedUsers.contains(name)) {
						Bundle b;
						try {
							b = installURPBundle(name);
						} catch (BundleException e) { // ?
							continue;
						}
						initedUsers.add(name);
						logger.debug("UserRightsProxy bundle ID: " + b.getBundleId());
					}
					break;
				case Role.ROLE:
					logger.debug("Registered role: " + r.getName());
					break;
				default:
					throw new IllegalStateException();

				}

			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	private void createDefaultUsers(StaticPolicies stPol, Set<String> inited) {
		/*
		 * Create the statically configured users and their roles
		 */
		List<StaticUser> users = stPol.getUsers();
		if (users != null) // if users is null non-clean start is performed, so no new users are to be created.
			for (StaticUser user : users) {
				String name = user.getName();
				boolean natural = user.isNatural();
				createUser(name, name, natural);
			}
		// }
		/*
		 * Create standard user admin
		 */
		// if (!inited.contains(ADMIN_NAME))
		// if (createUser(ADMIN_NAME, ADMIN_NAME, true))
		// inited.add(ADMIN_NAME);
		//
		/*
		 * Create standard user guest
		 */
		// if (!inited.contains(GUEST_NAME))
		// if (createUser(GUEST_NAME, GUEST_NAME, true))
		// inited.add(GUEST_NAME);
		//
		/*
		 * Create standard rest user
		 */
		// if (!inited.contains(REST_NAME))
		// if (createUser(REST_NAME, REST_NAME, false))
		// inited.add(REST_NAME);
	}

	@SuppressWarnings("unchecked")
	private void createDefaultGroups() {

		Role role = usrAdmin.getRole(ALLAPPS);
		if (role == null) {
			role = usrAdmin.createRole(ALLAPPS, Role.GROUP);
			Dictionary<String, Object> dict = role.getProperties();
			dict.put(OGEMA_ROLE_NAME, OGEMA_APPLICATION);
		}

		role = usrAdmin.getRole(NOAPPS);
		if (role == null) {
			role = usrAdmin.createRole(NOAPPS, Role.GROUP);
			Dictionary<String, Object> dict = role.getProperties();
			dict.put(OGEMA_ROLE_NAME, OGEMA_APPLICATION);
		}

		role = usrAdmin.getRole(ALLRESOURCES);
		if (role == null) {
			role = usrAdmin.createRole(ALLRESOURCES, Role.GROUP);
			Dictionary<String, Object> dict = role.getProperties();
			dict.put(OGEMA_ROLE_NAME, OGEMA_RESOURCES);
		}
		
		role = usrAdmin.getRole(OGEMA_MACHINE_USER);
		if (role == null) {
			role = usrAdmin.createRole(OGEMA_MACHINE_USER, Role.GROUP);
			Dictionary<String, Object> dict = role.getProperties();
			dict.put(OGEMA_ROLE_NAME, OGEMA_USER);
		}
		
		role = usrAdmin.getRole(OGEMA_NATURAL_USER);
		if (role == null) {
			role = usrAdmin.createRole(OGEMA_NATURAL_USER, Role.GROUP);
			Dictionary<String, Object> dict = role.getProperties();
			dict.put(OGEMA_ROLE_NAME, OGEMA_USER);
		}
	}

	void close() {
		if (urpTracker != null)
			urpTracker.close();
		urpTracker = null;
		urpMap.clear();
	}

	private void initURPTracker() {
		ServiceTrackerCustomizer<UserRightsProxy, UserRightsProxy> cust = new ServiceTrackerCustomizer<UserRightsProxy, UserRightsProxy>() {

			@Override
			public UserRightsProxy addingService(ServiceReference<UserRightsProxy> sr) {
				UserRightsProxy urp = osgi.getService(sr);
				urpMap.put(urp.getUserName(), urp);
				return urp;
			}

			@Override
			public void modifiedService(ServiceReference<UserRightsProxy> sr, UserRightsProxy t) {
			}

			@Override
			public void removedService(ServiceReference<UserRightsProxy> sr, UserRightsProxy urp) {
				urpMap.remove(urp.getUserName());
			}
		};

		urpTracker = new ServiceTracker<>(osgi, UserRightsProxy.class, cust);
		urpTracker.open(true);

	}

	@Override
	public UserRightsProxy getUrp(String usr) {
		return urpMap.get(usr);
	}

	static final String rolesFile = "ogema.roles";
	static final String rolesDir = "ogema";

	private static final String USER_URP_ID_NAME = "URPid";

	/**
	 * An user is created and and added to the user management. The user could represent an App or a natural person
	 * which has to be authenticated on web interface. This property is given by the value of {@link isNatural}.
	 * 
	 * The added user doesn't possess any app or resource permissions as default. They could be granted later on the
	 * administration interface.
	 *
	 * @param user
	 *            name of the entity which is added to the system as user.
	 * @param natural
	 *            true if the user is a natural one or false if the user is a machine
	 * @return true, if a new user was created successfully or false if the user already exist or the creation failed.
	 */
	@Override
	public boolean createUser(final String user, final String pwd, final boolean natural) {
		final String pwd2 = pwd != null ? pwd : user;
		Bundle b = AccessController.doPrivileged(new PrivilegedAction<Bundle>() {
			public Bundle run() {
				Role role = usrAdmin.getRole(user);
				final boolean existed = role != null;
				if (role instanceof Group)
					throw new IllegalArgumentException("The role " + user + " already exists as a group");
				Bundle result = null;
				if (role == null) {
					role = usrAdmin.createRole(user, Role.USER);
					// add to credential store
					try {
						cStore.createUser(user, pwd2, null, null);
					} catch (IOException e) {
						logger.error("Credential store reported exception during user crreation. User is not crated.",
								e);
						usrAdmin.removeRole(user);
						return null;
					}

					final Role group;
					@SuppressWarnings("unchecked")
					Dictionary<String, Object> dict = role.getProperties();
					if (natural) {
						dict.put(OGEMA_ROLE_NAME, OGEMA_NATURAL_USER);
						group = usrAdmin.getRole(OGEMA_NATURAL_USER);
					}
					else {
						dict.put(OGEMA_ROLE_NAME, OGEMA_MACHINE_USER);
						group = usrAdmin.getRole(OGEMA_MACHINE_USER);
					}
					if (group instanceof Group)
						((Group) group).addMember(role);
				}
				if (role != null) {
					UserRightsProxy urp = urpMap.get(user);
					if (urp == null) {
						try {
							result = installURPBundle(user);
						} catch (BundleException e) {
							 // if the bundle creation failed we remove the user, but only if it did not exist previously
							if (!existed) {
								try {
									usrAdmin.removeRole(user);
								} catch (Exception ee) {
									logger.warn("Failed to remove user from user admin after unsuccessful user creation",ee);
								}
								try {
									cStore.removeUser(user);
								} catch (Exception ee) {
									logger.warn("Failed to remove user from credential store after unsuccessful user creation",ee);
								} 
							}
						}
					}
					else {
						result = FrameworkUtil.getBundle(urp.getClass());
					}
					if (result == null)
						return null;
					@SuppressWarnings("unchecked")
					Dictionary<String, Object> dict = role.getProperties();
					dict.put(USER_URP_ID_NAME, Long.toString(result.getBundleId()));
				}
				return result;
			}
		});
		if (b == null) {
			return false;
		}
		else {
			// measure how long take it to login an existing user. It cant be set as constant value, because it is
			// platform dependent.
			userCount++;
			long time = System.currentTimeMillis();
			cStore.login(user, pwd);
			time = System.currentTimeMillis() - time;
			meanLoginTime = (int) (((long) (meanLoginTime * userCount) + time) / (long) userCount);
//			logger.info(String.format("Login mean time %d", meanLoginTime));
			return true;
		}
	}

	/**
	 * @param userName
	 * @return
	 * 		never null
	 * @throws BundleException
	 * 		if the bundle could not be installed
	 */
	private Bundle installURPBundle(String userName) throws BundleException {
		Bundle b = null;
		try {
			b = this.osgi.installBundle("urp:" + userName);
			logger.info(String.format("User rights proxy installed: %s", b.getLocation()));
			b.start();
		} catch (BundleException e) {
			logger.error("BundleException.TYPE: {}", e.getType(), e);
			if (b == null) {
				logger.info("Installation of user rights proxy bundle failed."); 
				throw e;
			}
			else {
				logger.error("URP bundle failed to start.");
			}
		}
		return b;
	}

	@Override
	public void removeUser(String userName) {
		boolean b = usrAdmin.removeRole(userName);
		if (!b && Configuration.DEBUG)
			logger.debug("User couldn't be removed: " + userName);
		cStore.logout(userName);
		cStore.removeUser(userName);
	}

	@Override
	public void addPermission(String user, AppPermissionFilter props) {
		UserRightsProxy urp = urpMap.get(user);
		if (urp == null)
			throw new IllegalStateException(
					String.format("User rights proxy installation for the user %s not yet completed.", user));
		Bundle b = urp.getBundle();
		AppID appid = appReg.getAppByBundle(b);

		addPerm(appid, props);
	}
	
	@Override
	public boolean addPermission(String user, Permission permission) {
		return addPermissions(user, Collections.singletonList(permission));
	}
	
	@Override
	public boolean addPermissions(String user, List<Permission> permissions) {
		UserRightsProxy urp = urpMap.get(user);
		if (urp == null)
			throw new IllegalStateException(
					String.format("User rights proxy installation for the user %s not yet completed.", user));
		Bundle b = urp.getBundle();
		final ConditionalPermissionAdmin cpa = (ConditionalPermissionAdmin) permMan.getSystemPermissionAdmin();
		final ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		
		final List<ConditionalPermissionInfo> perms = cpu.getConditionalPermissionInfos();
		int id = getNextUserPermId(user, perms);
		for (Permission perm: permissions) {
			final String name = "user_" + user + id++;
	        perms.add(perms.size(),
	                cpa.newConditionalPermissionInfo(
	                        name,
	                        new ConditionInfo[]{
	                            new ConditionInfo("org.osgi.service.condpermadmin.BundleLocationCondition", new String[]{b.getLocation()})},
	                        new PermissionInfo[]{
	                            new PermissionInfo(perm.getClass().getName(), perm.getName(), perm.getActions())},
	                        "allow"));
		}
		return cpu.commit();
	}
	
	private static int getNextUserPermId(final String user, final List<ConditionalPermissionInfo> perms) {
		final String prefix=  "user_" + user;
		int next = 0;
		for (ConditionalPermissionInfo info : perms) {
			final String name = info.getName();
			if (name == null || !name.startsWith(prefix) || name.equals(prefix))
				continue;
			final String suffix = name.substring(prefix.length());
			try {
				final int cnt = Integer.parseInt(suffix);
				if (cnt >= next)
					next = cnt + 1;
			} catch (NumberFormatException e) {}
		}
		return next;
	}

	@Override
	public void addPermission(String user, List<AppPermissionFilter> props) {
		UserRightsProxy urp = urpMap.get(user);
		if (urp == null)
			throw new IllegalStateException(
					String.format("User rights proxy installation for the user %s not yet completed.", user));
		Bundle b = urp.getBundle();
		AppID appid = appReg.getAppByBundle(b);

		addPermList(appid, props);
	}

	private void addPermList(AppID appid, List<AppPermissionFilter> props) {
		String args[] = new String[2];
		args[1] = null;
		AppPermission ap = permMan.getPolicies(appid);

		for (AppPermissionFilter approps : props) {
			args[0] = approps.getFilterString();
			if (args[0] == null)
				args[0] = "*";
			{
				ap.addPermission(WEB_ACCESS_PERMISSION_CLASS_NAME, args, null);
			}
		}
		// To commit the made changes the following is needed.
		permMan.installPerms(ap);

	}

	private void addPerm(AppID appid, AppPermissionFilter props) {
		String args[] = new String[2];
		args[0] = props.getFilterString();
		if (args[0] == null)
			args[0] = "*";
		args[1] = null;

		AppPermission ap = permMan.getPolicies(appid);
		{
			ap.addPermission(WEB_ACCESS_PERMISSION_CLASS_NAME, args, null);
		}
		// To commit the made changes the following is needed.
		permMan.installPerms(ap);

	}

	@Override
	public void setNewPassword(String user, String oldPwd, String newPwd) {
		cStore.setGWPassword(user, oldPwd, newPwd);
	}

	@Override
	public Role getRole(String name) {
		Role r = usrAdmin.getRole(name);
		return r;
	}

	@Override
	public List<String> getAllUsers() {
		Vector<String> v = null;
		Role[] roles = null;
		try {
			roles = usrAdmin.getRoles(null);
		} catch (InvalidSyntaxException e) {
		}
		if (roles != null)
			v = new Vector<>();
		for (Role r : roles) {
			if (r.getType() == Role.USER)
				v.add(r.getName());
		}
		return v;
	}

	@Override
	public List<AppID> getAppsPermitted(String user) {
		Vector<AppID> v = new Vector<>();
		/*
		 * Get the all groups that represent installed apps
		 */
		List<AdminApplication> laa = appReg.getAllApps();

		for (AdminApplication aa : laa) {
			AppID id = aa.getID();
			if (isPermitted(user, id))
				v.add(id);
		}
		return v;
	}

	@Override
	public boolean isAppPermitted(User user, AppID app) {
		String username = user.getName();
		return isPermitted(username, app);
	}

	@Override
	public boolean isAppPermitted(String user, AppID app) {
		return isPermitted(user, app);
	}

	private boolean isPermitted(String username, AppID app) {
		if (!permMan.isSecure())
			return true;
		Bundle b = app.getBundle();
		WebAccessPermission wap = new WebAccessPermission(b.getSymbolicName(), username, null, b.getVersion());
		UserRightsProxy urp = urpMap.get(username);
		if (urp == null) {
			logger.error("Unknown user " + username);
			return false;
		}
		AccessControlContext acc = permMan.getBundleAccessControlContext(urp.getClass());
		return permMan.handleSecurity(wap, acc);
	}

	@Override
	public boolean authenticate(String usrName, final String pwd, boolean isnatural) {
		boolean result;
		long time = System.currentTimeMillis();
		boolean userProp = isNatural(usrName);
		if (permMan.isSecure() && (isnatural && !userProp) || (!isnatural && userProp)) {
			result = false;
		}
		else {
			result = cStore.login(usrName, pwd);
		}
		time = System.currentTimeMillis() - time;
		int toWait = meanLoginTime + meanLoginTime >> 4 - (int) time;
//		logger.info(String.format("Login wait time %d", toWait));
		if (toWait > 0)
			try {
				Thread.sleep(toWait);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		// wait until meanLoginTime is over
		return result;
	}

	@Override
	public void removePermission(String user, AppPermissionFilter props) {
		Bundle urpbundle = urpMap.get(user).getBundle();
		// FIXME can we remove the urpapp and pass the bundle directly?
		AppID urpapp = appReg.getAppByBundle(urpbundle);
		permMan.removePermission(urpapp.getBundle(), WEB_ACCESS_PERMISSION_CLASS_NAME, props.getFilterString(), null);
	}

	@Override
	public void setCredential(String user, String credential, String value) {
		User usr;
		Role role = usrAdmin.getRole(user);
		if (role == null) {
			throw new IllegalArgumentException();
		}
		usr = (User) role;
		// Set users credential
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> dict = usr.getCredentials();
		dict.put(credential, value);
		if (Configuration.DEBUG)
			if (usr.hasCredential(user, credential))
				logger.debug("User credential is set correctly");
	}

	@Override
	public void registerApp(AppID id) {
	}

	@Override
	public void unregisterApp(AppID appid) {
	}

	@Override
	public AppPermission getPolicies(String user) {
		AppPermission ap;
		UserRightsProxy urp = urpMap.get(user);
		AppID appid = appReg.getAppByBundle(urp.getBundle());
		ap = permMan.getPolicies(appid);
		return ap;
	}

	@Override
	public void setProperty(String user, String propName, String propValue) {
		Role role = usrAdmin.getRole(user);
		if (role == null)
			return;
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> dict = role.getProperties();
		dict.put(propName, propValue);
	}

	@Override
	public String getProperty(String user, String propName) {
		Role role = usrAdmin.getRole(user);
		if (role == null)
			return null;
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> dict = role.getProperties();
		return (String) dict.get(propName);
	}

	@Override
	public boolean isNatural(String user) {
		// may be null, if some external component creates users too
		String userType = getProperty(user, OGEMA_ROLE_NAME);
		return userType != null ? userType.equals(OGEMA_NATURAL_USER) : false;
	}

	/*
	 * Surrogate implementation for unregisterApp. The UserAdmin role is only removed, if the bundle really uninstalled.
	 */
	@Override
	public void bundleChanged(BundleEvent event) {
		if (event.getType() != BundleEvent.UNINSTALLED)
			return;
		Bundle b = event.getBundle();
		AppID appid = appReg.getAppByBundle(b);
		if (appid != null)
			usrAdmin.removeRole(appid.getIDString());
	}

	@Override
	public boolean isAllAppsPermitted(String user) {
		User usr = (User) usrAdmin.getRole(user);
		if (usr == null)
			return false;
		Authorization auth = usrAdmin.getAuthorization(usr);
		return auth.hasRole(ALLAPPS);
	}

	@Override
	public boolean isNoAppPermitted(String user) {
		User usr = (User) usrAdmin.getRole(user);
		if (usr == null)
			return false;
		Authorization auth = usrAdmin.getAuthorization(usr);
		return auth.hasRole(NOAPPS);
	}

	@Override
	public Group createGroup(String name) {
		Role role = usrAdmin.getRole(name);
		if (role == null) {
			role = usrAdmin.createRole(name, Role.GROUP);
		}
		return (Group) role;
	}

	@Override
	public void logout(String usrName) {
		cStore.logout(usrName);
	}

	@Override
	public List<String> getParents(String userName) {
		List<String> parents = new ArrayList<>();
		while (true) {
			userName = getProperty(userName, PARENT_PROP);
			if (userName == null || OWNER_NAME.equals(userName))
				break;
			parents.add(userName);	
		}
		return parents;
	}
	

	@Override
	public String getCurrentUser() {
		return callerName.get();
	}

	@Override
	public void setCurrentUser(String userName) {
		if(!checkSystemAdminPerm()){
			throw new SecurityException("Unauthorized to set current user.");
		}
		setUser(userName);
	}
	
	protected void setUser(String userName){
		callerName.set(userName);
	}

	@Override
	public void removeCurrentUser() {
		if(!checkSystemAdminPerm()){
			throw new SecurityException("Unauthorized to remove current user.");
		}
		resetUser();
	}
	protected void resetUser(){
		callerName.remove();
	}
	
	private boolean checkSystemAdminPerm(){
		AdminPermission perm = new AdminPermission("system");
		if(permMan.handleSecurity(perm)){
			return true;
		}
		return false;
	}
	
	@Override
	public String authenticate(HttpServletRequest req) {
		return authenticate(req, null);
	}
	
	@Override
	public String authenticate(HttpServletRequest req, boolean naturalUser) {
		return authenticate(req, Boolean.valueOf(naturalUser));
	}

	private String authenticate(HttpServletRequest req, Boolean isNatural) {
		long time = System.currentTimeMillis();
		// check if user/pw login is admissible for the user group (machine/natural)
		if (isNatural == null || 
				isAuthenticatorAdmitted(isNatural ? OGEMA_NATURAL_USER : OGEMA_MACHINE_USER, Authenticator.DEFAULT_USER_PW_ID)) {
			// the default pw-based login uses different parameters for machine users and natural users...
			final String user = (String) req.getParameter(Constants.OTUNAME);
			final String pw = req.getParameter(Constants.OTPNAME);
			if (user != null && pw != null	&& isAuthenticatorAdmitted(user, Authenticator.DEFAULT_USER_PW_ID)) {
				if (authenticate(user, pw, isNatural != null ? isNatural : isNatural(user)))
					return user;
				else
					return null;
			}
		}
		for (Map.Entry<String, ServiceReference<Authenticator>> entry : authenticators.entrySet()) {
			if (isNatural != null && 
					!isAuthenticatorAdmitted(isNatural ? OGEMA_NATURAL_USER : OGEMA_MACHINE_USER, entry.getKey()))
				continue;
			Authenticator auth = null;
			try {
				auth = osgi.getService(entry.getValue());
				final String usr = auth.authenticate(req);
				if (usr != null) {
					if (isAuthenticatorAdmitted(usr, entry.getKey())) {
						final Role r = usrAdmin.getRole(usr);
						if (!(r instanceof User)) {
							logger.warn("Invalid user authenticated by {}", entry.getKey());
							continue;
						}
						if (isNatural != null) {
							final Object prop = r.getProperties().get(OGEMA_ROLE_NAME);
							if (isNatural && !OGEMA_NATURAL_USER.equals(prop))
								return null;
							if (!isNatural && !OGEMA_MACHINE_USER.equals(prop))
								return null;
						}
						return usr;
					}
				}
			} catch (Exception e) {
				logger.warn("Exception using authenticator {}", entry.getKey());
			} finally {
				if (auth != null)
					osgi.ungetService(entry.getValue());
			}
		}
		time = System.currentTimeMillis() - time;
		int toWait = meanLoginTime + meanLoginTime >> 4 - (int) time;
		// wait until meanLoginTime is over
		if (toWait > 0)
			try {
				Thread.sleep(toWait);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		return null;
	}
	
	@Override
	public String getLoggedInUser(HttpServletRequest req) {
		final HttpSession session = req.getSession(false);
		if (session == null)
			return null;
		final SessionAuth sesAuth = (SessionAuth) session.getAttribute(Constants.AUTH_ATTRIBUTE_NAME);
		if (sesAuth == null)
			return null;
		return sesAuth.getName();
	}

	@Override
	public Collection<String> getSupportedAuthenticators(String user) {
		Objects.requireNonNull(user);
		final Role role = usrAdmin.getRole(user);
		if (role == null)
			return null;
		final Object auths = role.getProperties().get(ConfigurationConstants.OGEMA_AUTHENTICATORS_CONFIG);
		if (!(auths instanceof String)) {
			final Collection<String> globalIds = this.globallyAdmissibleAuthenticatorIds;
			return globalIds == null ? null : Collections.unmodifiableCollection(globalIds);
		}
		return Arrays.asList(((String) auths).split(ConfigurationConstants.AUTH_SEP));
	}
	
	Collection<String> getSupportedAuthenticatorsInfo(String user) {
		Collection<String> ids = getSupportedAuthenticators(user);
		if (ids != null)
			return Collections.unmodifiableCollection(ids);
		ids = new ArrayList<>(authenticators.keySet());
		ids.add(Authenticator.DEFAULT_USER_PW_ID);
		return Collections.unmodifiableCollection(ids);
	}
	
	private boolean isAuthenticatorAdmitted(final String user, final String authId) {
		final Collection<String> supported = getSupportedAuthenticators(user);
		return supported == null || supported.contains(authId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addSupportedAuthenticator(String user, String authenticatorId) {
		Objects.requireNonNull(user);
		Objects.requireNonNull(authenticatorId);
		final Role role = usrAdmin.getRole(user);
		if (role == null) {
			logger.warn("Invalid user or group {}", user);
			return;
		}
		synchronized (authenticators) {
			final Object auths = role.getProperties().get(ConfigurationConstants.OGEMA_AUTHENTICATORS_CONFIG);
			if (auths == null)
				return;
			if (!(auths instanceof String)) {
				logger.error("Invalid authenticatos property {}",auths);
				return;
			}
			if (Arrays.asList(((String) auths).split(ConfigurationConstants.AUTH_SEP)).contains(authenticatorId))
				return;
			final String authStr = ((String) auths).isEmpty() ? authenticatorId : ((String) auths) + ConfigurationConstants.AUTH_SEP + authenticatorId;
			role.getProperties().put(ConfigurationConstants.OGEMA_AUTHENTICATORS_CONFIG, authStr);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removeSupportedAuthenticator(String user, String authenticatorId) {
		Objects.requireNonNull(user);
		Objects.requireNonNull(authenticatorId);
		final Role role = usrAdmin.getRole(user);
		if (role == null) {
			logger.warn("Invalid user {}", user);
			return;
		}
		synchronized (authenticators) {
			final Object auths = role.getProperties().get(ConfigurationConstants.OGEMA_AUTHENTICATORS_CONFIG);
			if (auths != null && !(auths instanceof String)) {
				logger.error("Invalid authenticators property {}",auths);
				return;
			}
			final List<String> ids;
			if (auths != null)
				ids = new ArrayList<>(Arrays.asList(((String) auths).split(ConfigurationConstants.AUTH_SEP)));
			else {
				ids = new ArrayList<>(authenticators.keySet());
				ids.add(Authenticator.DEFAULT_USER_PW_ID);
			}
			ids.remove(authenticatorId);
			role.getProperties().put(ConfigurationConstants.OGEMA_AUTHENTICATORS_CONFIG, buildAuthenticatorsString(ids));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setSupportedAuthenticators(String user, Collection<String> authenticatorIds) {
		Objects.requireNonNull(user);
		final Role role = usrAdmin.getRole(user);
		if (role == null) {
			logger.warn("Invalid user {}", user);
			return;
		}
		synchronized (authenticators) {
			if (authenticatorIds == null)
				role.getProperties().remove(ConfigurationConstants.OGEMA_AUTHENTICATORS_CONFIG);
			else {
				role.getProperties().put(ConfigurationConstants.OGEMA_AUTHENTICATORS_CONFIG, buildAuthenticatorsString(authenticatorIds));
			}
		}
		
	}
	
	@Override
	public void addSupportedAuthenticator(String authenticatorId) {
		Objects.requireNonNull(authenticatorId);
		final Collection<String> ids = this.globallyAdmissibleAuthenticatorIds;
		if (ids == null || ids.contains(authenticatorId))
			return;
		final String authString = buildAuthenticatorsString(ids);
		final String newString = authString.isEmpty() ? authenticatorId : authString + ConfigurationConstants.AUTH_SEP + authenticatorId;
		// in principle, these three lines are superfluous, since the collection will be rebuilt in a properties changed callback anyway
		// ... but they ensure the change is applied instantly, and make it work even if config admin is not available (non-persistently)
		final List<String> newIds = new ArrayList<>(ids);
		newIds.add(authenticatorId);
		this.globallyAdmissibleAuthenticatorIds = newIds;
		setProperty(ConfigurationConstants.OGEMA_AUTHENTICATORS_CONFIG, newString);
	}
	
	Collection<String> getAllAuthenticators() {
		final List<String> keys = new ArrayList<>(authenticators.keySet());
		keys.add(Authenticator.DEFAULT_USER_PW_ID);
		return keys;
	}
	
	@Override
	public Collection<String> getSupportedAuthenticators() {
		final Collection<String> ids = this.globallyAdmissibleAuthenticatorIds;
		return ids == null ? null : Collections.unmodifiableCollection(ids);
	}
	
	Collection<String> getSupportedAuthenticatorsInfo() {
		Collection<String> ids = this.globallyAdmissibleAuthenticatorIds;
		if (ids != null)
			return Collections.unmodifiableCollection(ids);
		ids = new ArrayList<>(authenticators.keySet());
		ids.add(Authenticator.DEFAULT_USER_PW_ID);
		return Collections.unmodifiableCollection(ids);
	}
	
	@Override
	public void removeSupportedAuthenticator(String authenticatorId) {
		Objects.requireNonNull(authenticatorId);
		final Collection<String> ids = this.globallyAdmissibleAuthenticatorIds;
		if (ids != null && !ids.contains(authenticatorId))
			return;
		final List<String> newIds = ids == null ? new ArrayList<>(authenticators.keySet()) : new ArrayList<>(ids);
		newIds.remove(authenticatorId);
		this.globallyAdmissibleAuthenticatorIds = newIds;
		setProperty(ConfigurationConstants.OGEMA_AUTHENTICATORS_CONFIG, buildAuthenticatorsString(newIds));
	}
	
	@Override
	public void setSupportedAuthenticators(Collection<String> authenticators) {
		if (authenticators == null) {
			if (this.globallyAdmissibleAuthenticatorIds == null)
				return;
			this.globallyAdmissibleAuthenticatorIds = null;
			setProperty(ConfigurationConstants.OGEMA_AUTHENTICATORS_CONFIG, null);
			return;
		}
		this.globallyAdmissibleAuthenticatorIds = new ArrayList<>(authenticators);
		setProperty(ConfigurationConstants.OGEMA_AUTHENTICATORS_CONFIG, buildAuthenticatorsString(authenticators));
	}
	
	private static final String buildAuthenticatorsString(final Collection<String> ids) {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String id: ids) {
			if (!first)
				sb.append(ConfigurationConstants.AUTH_SEP);
			first = false;
			sb.append(id);
		}
		return sb.toString();
	}
	
	private final void setProperty(final String key, final String value) {
		Objects.requireNonNull(key);
		ServiceReference<ConfigurationAdmin> sr = null;
		try {
			sr = osgi.getServiceReference(ConfigurationAdmin.class);
		} catch (IllegalStateException | NullPointerException e) {}
		if (sr == null) {
			logger.warn("Configuration admin not found... cannot persist property {}={}",key,value);
			return;
		}
		final ConfigurationAdmin configAdmin = osgi.getService(sr);
		try {
			final org.osgi.service.cm.Configuration cfg = configAdmin.getConfiguration(ConfigurationConstants.CONFIGURATION_PID);
			Dictionary<String, Object> props = cfg.getProperties();
			if (props == null) {
				props = new Hashtable<>(4);
			}
			if (value != null)
				props.put(key, value);
			else
				props.remove(key);
			cfg.update(props);
		} catch (IOException e) { 
			logger.error("Failed to update properties",e);
		} finally {
			osgi.ungetService(sr);
		}
	}
	
}
