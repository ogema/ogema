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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.UserRightsProxy;
import org.ogema.core.application.AppID;
import org.ogema.core.security.AppPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
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
 * @author mns
 * 
 */

public class AccessManagerImpl implements AccessManager, BundleListener {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	static final String ADMIN_NAME = "master";
	static final String GUEST_NAME = "guest";
	private static final String REST_NAME = "rest";

	private static final String ADMIN_DEFAULT_PASSWORD = "master";

	private static final String GUEST_DEFAULT_PASSWORD = "guest";

	private static final String REST_DEFAULT_PASSWORD = "rest";

	static final String PASSWORD_NAME = "password";
	static final String OGEMA_ROLE_NAME = "ogemaRole";

	static final String OGEMA_NATURAL_USER = "naturalUser";
	static final String OGEMA_MACHINE_USER = "machineUser";
	static final String OGEMA_APPLICATION = "applicationGroup";
	static final String OGEMA_RESOURCES = "resourcesGroup";

	UserAdmin usrAdmin;
	User admin, guest;
	// TODO Does it make sense to have these roles for better performance?
	Group allApps, noApps, allResources;

	private BundleContext osgi;

	static final String ALLAPPS = "ALL APPS";
	static final String NOAPPS = "NO APPS";

	static final String ALLRESOURCES = "ALL RESOURCES";

	private ServiceTracker<UserRightsProxy, UserRightsProxy> urpTracker;

	HashMap<String, UserRightsProxy> urpMap;
	PermissionManager permMan;

	public AccessManagerImpl() {
	}

	@SuppressWarnings("unchecked")
	// no generics in UserAdmin
	AccessManagerImpl(UserAdmin ua, BundleContext osgi, PermissionManager pm) {
		this.usrAdmin = ua;
		this.osgi = osgi;
		this.urpMap = new HashMap<>();
		this.permMan = pm;

		Dictionary<String, Object> dict = null;
		Role role = ua.getRole(ALLAPPS);
		if (role == null) {
			role = ua.createRole(ALLAPPS, Role.GROUP);
			dict = role.getProperties();
			dict.put(OGEMA_ROLE_NAME, OGEMA_APPLICATION);
		}
		this.allApps = (Group) role;

		role = ua.getRole(NOAPPS);
		if (role == null) {
			role = ua.createRole(NOAPPS, Role.GROUP);
			dict = role.getProperties();
			dict.put(OGEMA_ROLE_NAME, OGEMA_APPLICATION);
		}
		this.noApps = (Group) role;

		role = ua.getRole(ALLRESOURCES);
		if (role == null) {
			role = ua.createRole(ALLRESOURCES, Role.GROUP);
			dict = role.getProperties();
			dict.put(OGEMA_ROLE_NAME, OGEMA_RESOURCES);
		}
		this.allResources = (Group) role;

		initURPTracker();
		/*
		 * Create the statically configured users and their roles
		 */

		/*
		 * Create standard user admin
		 */
		if (createUser(ADMIN_NAME, true))
			setCredetials(ADMIN_NAME, PASSWORD_NAME, ADMIN_DEFAULT_PASSWORD);
		this.admin = getUser(ADMIN_NAME);
		// Set the admin user as a member of ALLAPPS
		allApps.addMember(this.admin);
		// Set the admin user as a member of ALLRESOURCES
		allResources.addMember(this.admin);

		/*
		 * Create standard user guest
		 */
		if (createUser(GUEST_NAME, true))
			setCredetials(GUEST_NAME, PASSWORD_NAME, GUEST_DEFAULT_PASSWORD);
		this.guest = getUser(GUEST_NAME);
		// Set the guest user as a member of NOAPPS
		noApps.addMember(this.guest);

		/*
		 * Create standard rest user
		 */
		if (createUser(REST_NAME, false))
			setCredetials(REST_NAME, PASSWORD_NAME, REST_DEFAULT_PASSWORD);

		if (Configuration.DEBUG) {
			try {
				Role[] roles = ua.getRoles(null);
				for (Role r : roles) {
					int type = r.getType();
					switch (type) {
					case Role.GROUP:
						logger.debug("Registered group: " + r.getName());
						break;
					case Role.USER:
						logger.debug("Registered user: " + r.getName());
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
				urpMap.remove(urp);
			}
		};

		urpTracker = new ServiceTracker<>(osgi, UserRightsProxy.class, cust);
		urpTracker.open();

	}

	@Override
	public UserRightsProxy getUrp(String usr) {
		return urpMap.get(usr);
	}

	static final String rolesFile = "ogema.roles";
	static final String rolesDir = "ogema";

	/*
	 * These user roles normally would dynamically granted by the framework administration. Therefore interacts the
	 * AccessManager with the system administrator over the web interface.
	 */
	private static final String SECURITY_URP_FILE = "urp";

	private static final String SECURITY_URP_SOURCE_DIR = "ogema";

	private static final String SECURITY_URP_DEST_DIR = "ogema/users";

	private static final String USER_URP_ID_NAME = "URPid";

	private static final String APPLICATION_ROLES_FILTER = "(" + OGEMA_ROLE_NAME + "=" + OGEMA_APPLICATION + ")";

	/**
	 * An user is created and and added to the user management. The user could represent an App or a natural person
	 * which has to be authenticated on web interface. This property is given by the value of {@link isNatural}.
	 * 
	 * The added user doesn't possess any app or resource permissions as default. They could be granted later on the
	 * administration interface.
	 * 
	 * @param user
	 *            name of the entity which is added to the system as user.
	 */
	@Override
	public boolean createUser(final String user, final boolean natural) {
		Role role = usrAdmin.getRole(user);
		if (role == null) {
			AccessController.doPrivileged(new PrivilegedAction<Bundle>() {
				public Bundle run() {
					Bundle result = installURPBundle(user);
					if (result != null) {
						Role r = usrAdmin.createRole(user, Role.USER);
						@SuppressWarnings("unchecked")
						Dictionary<String, Object> dict = r.getProperties();
						if (natural)
							dict.put(OGEMA_ROLE_NAME, OGEMA_NATURAL_USER);
						else
							dict.put(OGEMA_ROLE_NAME, OGEMA_MACHINE_USER);
						dict.put(USER_URP_ID_NAME, Long.toString(result.getBundleId()));
					}
					else
						logger.info("User couldn't be created.");
					return result;
				}
			});
			return true;
		}
		else if (Configuration.DEBUG) {
			logger.debug("User already exist: " + user);
		}
		return false;
	}

	private Bundle installURPBundle(String userName) {
		String destFileName = SECURITY_URP_FILE + userName;
		File source = new File(SECURITY_URP_SOURCE_DIR, SECURITY_URP_FILE);
		File dest = new File(SECURITY_URP_DEST_DIR + "/" + userName);

		/*
		 * If the user rights proxy template doesn't exist give up.
		 */
		if (!source.exists())
			return null;
		/*
		 * If the destination user proxy location dir not exists, create it.
		 */
		if (!dest.exists())
			dest.mkdirs();
		dest = new File(dest, destFileName);

		/*
		 * If an older version of the user rights proxy file exists, remove it before copying the new one.
		 */
		if (dest.exists())
			dest.delete();

		try {
			Files.copy(source.toPath(), dest.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bundle b = null;
		try {
			String relative = "file:./"
					+ new URI(new File("./").toURI().relativize(dest.toURI()).toString()).toString();
			b = this.osgi.installBundle(relative);
			logger.info(String.format("User rights proxy installed: %s", b.getLocation()));
			b.start();
		} catch (BundleException | URISyntaxException e) {
			if (b == null)
				logger.info("Installation of user rights proxy bundle failed.");
			else {
				logger.error("URP bundle failed to start.", e);
			}
		}
		return b;
	}

	@Override
	public void removeUser(String userName) {
		boolean b = usrAdmin.removeRole(userName);
		if (!b && Configuration.DEBUG)
			logger.debug("User couldn't be removed: " + userName);
	}

	@Override
	public void addPermission(String user, String roleName) {
		/*
		 * Get the group that represents the app and the user object that matches the given name
		 */
		Role role = usrAdmin.getRole(roleName);

		if (role == null)
			role = usrAdmin.createRole(roleName, Role.GROUP);
		final Role usrRole = usrAdmin.getRole(user);
		/*
		 * Check if the objects are valid
		 */
		if (usrRole == null || !(usrRole instanceof User))
			throw new IllegalArgumentException();
		final Role finalRole = role;
		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			public Void run() {
				/*
				 * Add the user as a member to the group
				 */
				((Group) finalRole).addMember(usrRole);
				/*
				 * In order to have an effect a possible membership of NOAPPS is to be removed.
				 */
				noApps.removeMember(usrRole);
				return null;
			}
		});

	}

	@Override
	public void setNewPassword(String user, final String newPassword) {
		Role role = usrAdmin.getRole(user);
		final User usr = (User) role;

		if (role == null) {
			throw new IllegalArgumentException("User doesn't exist: " + user);
		}
		else {
			// Set users default password
			Boolean hasCred = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					@SuppressWarnings("unchecked")
					Dictionary<String, Object> dict = usr.getCredentials();
					dict.put(PASSWORD_NAME, newPassword);
					return usr.hasCredential(PASSWORD_NAME, newPassword);
				}
			});

			if (hasCred) {
				if (Configuration.DEBUG)
					logger.debug("Set new password succeeded.");
			}
			else {
				if (Configuration.DEBUG)
					logger.debug("Set new password failed.");
			}
		}
	}

	@Override
	public User getUser(String userName) {
		User usr = (User) usrAdmin.getRole(userName);
		return usr;
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
	public List<String> getAppsPermitted(String user) {
		Vector<String> v = null;
		Role[] roles = null;
		/*
		 * Get the all groups that represent installed apps
		 */
		try {
			roles = usrAdmin.getRoles(APPLICATION_ROLES_FILTER);
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}

		if (roles == null)
			return null;

		Role usrRole = usrAdmin.getRole(user);
		if (usrRole == null || !(usrRole instanceof User))
			throw new IllegalArgumentException();
		Authorization auth = usrAdmin.getAuthorization((User) usrRole);

		for (Role r : roles) {
			String name = r.getName();
			if (auth.hasRole(name)) {
				if (v == null)
					v = new Vector<>();
				v.add(name);
			}
		}
		return v;
	}

	@Override
	public boolean isAppPermitted(User user, AppID app) {
		String appid = app.getIDString();
		// Is NO_APPS permitted?
		Authorization auth = usrAdmin.getAuthorization(user);
		if (auth.hasRole(NOAPPS))
			return false;
		// Is the permission granted explicitly for ALL_APPS
		if (auth.hasRole(ALLAPPS))
			return true;
		// Is the permission granted explicitly for this app
		if (auth.hasRole(appid))
			return true;
		else
			return false;
	}

	@Override
	public boolean isAppPermitted(String user, AppID app) {
		Role r = usrAdmin.getRole(user);
		if (r == null || !(r instanceof User))
			throw new RuntimeException("Unknown user " + user);
		String appid = app.getIDString();
		// Is NO_APPS permitted?
		Authorization auth = usrAdmin.getAuthorization((User) r);
		if (auth.hasRole(NOAPPS))
			return false;
		// Is the permission granted explicitly for ALL_APPS
		if (auth.hasRole(ALLAPPS))
			return true;
		// Is the permission granted explicitly for this app
		if (auth.hasRole(appid))
			return true;
		else
			return false;
	}

	@Override
	public boolean authenticate(String userName, final String password, boolean isnatural) {
		Role role = usrAdmin.getRole(userName);
		if (role == null)
			return false;
		final User admin = (User) role;
		Boolean hasCred = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
			public Boolean run() {
				return admin.hasCredential(PASSWORD_NAME, password);
			}
		});
		if (!hasCred)
			return false;
		boolean userProp = isNatural(userName);
		if ((isnatural && !userProp) || (!isnatural && userProp))
			return false;
		return true;
	}

	@Override
	public void removePermission(String user, AppID app) {
		/*
		 * Get the group that represents the app and the user object that matches the given name
		 */
		Role appRole = usrAdmin.getRole(app.getIDString());
		Role usrRole = usrAdmin.getRole(user);
		/*
		 * Check if the objects are valid
		 */
		if (appRole == null || !(appRole instanceof Group))
			throw new IllegalArgumentException();
		if (usrRole == null || !(usrRole instanceof User))
			throw new IllegalArgumentException();
		/*
		 * Remove the user as a member to the group
		 */
		((Group) appRole).removeMember(usrRole);

		/*
		 * If the removed user doesn't belong to any role, it is to be added to NOAPPS. TODO check it before
		 */
	}

	@Override
	public void setCredetials(String user, String credential, String value) {
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
	public boolean checkPermission(String userName, String roleName) {
		User user = (User) usrAdmin.getRole(userName);
		// Is NO_APPS permitted?
		Authorization auth = usrAdmin.getAuthorization(user);
		if (auth.hasRole(NOAPPS))
			return false;
		// Is the permission granted explicitly for ALL_APPS
		if (auth.hasRole(ALLAPPS))
			return true;
		// Is the permission granted explicitly for this app
		if (auth.hasRole(roleName))
			return true;
		else
			return false;
	}

	@Override
	public void registerApp(AppID id) {
		Role r = usrAdmin.createRole(id.getIDString(), Role.GROUP);
		if (r == null) {
			r = usrAdmin.getRole(id.getIDString());
		}
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> dict = r.getProperties();
		dict.put(OGEMA_ROLE_NAME, OGEMA_APPLICATION);
	}

	@Override
	public void unregisterApp(AppID appid) {
	}

	@Override
	public AppPermission getPolicies(String user) {
		AppPermission ap;
		UserRightsProxy urp = urpMap.get(user);
		AppID appid = permMan.getAdminManager().getAppByBundle(urp.getBundle());
		ap = permMan.getPolicies(appid);
		return ap;
	}

	@Override
	public void setProperty(String user, String propName, String propValue) {
		Role role = usrAdmin.getRole(user);
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> dict = role.getProperties();
		dict.put(propName, propValue);
	}

	@Override
	public String getProperty(String user, String propName) {
		Role role = usrAdmin.getRole(user);
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> dict = role.getProperties();
		return (String) dict.get(propName);
	}

	@Override
	public boolean isNatural(String user) {
		return getProperty(user, OGEMA_ROLE_NAME).equals(OGEMA_NATURAL_USER);
	}

	/*
	 * Surrogate implementation for unregisterApp. The UserAdmin role is only removed, if the bundle really uninstalled.
	 */
	@Override
	public void bundleChanged(BundleEvent event) {
		if (event.getType() != BundleEvent.UNINSTALLED)
			return;
		Bundle b = event.getBundle();
		AppID appid = permMan.getAdminManager().getAppByBundle(b);
		if (appid != null)
			usrAdmin.removeRole(appid.getIDString());
	}
}
