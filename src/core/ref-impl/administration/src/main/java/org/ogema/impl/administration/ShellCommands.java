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

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.ResourcePermission;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdminLogger;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.administration.PatternCondition;
import org.ogema.core.administration.RegisteredAccessModeRequest;
import org.ogema.core.administration.RegisteredPatternListener;
import org.ogema.core.administration.RegisteredResourceDemand;
import org.ogema.core.administration.RegisteredResourceListener;
import org.ogema.core.administration.RegisteredStructureListener;
import org.ogema.core.administration.RegisteredTimer;
import org.ogema.core.administration.RegisteredValueListener;
import org.ogema.core.administration.UserAccount;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.LogLevel;
import org.ogema.core.logging.LogOutput;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.security.AppPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.permissionadmin.PermissionInfo;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.UserAdmin;

/**
 * 
 * @author jlapp
 */
@Component(specVersion = "1.2")
@Properties({ @Property(name = "osgi.command.scope", value = "ogm"), @Property(name = "osgi.command.function", value = {
		"apps", "clock", "loggers", "log", "dump_cache", "update", 
		"listUsers", "getUserProps", "setUserProp", "removeUserProp", "createUser", "deleteUser", 
		"listGroups", "createGroup", "addMember", "removeMember",
		"setNewPassword" }) })
@Service(ShellCommands.class)
@Descriptor("OGEMA administration commands")
public class ShellCommands {

	@Reference
	protected AdministrationManager admin;
	
	@Reference
	private PermissionManager permMan;
	
	@Reference
	private UserAdmin userAdmin;

	@Descriptor("list running OGEMA apps")
	public void apps(@Descriptor("show listeners registered by app") @Parameter(names = { "-l",
			"--listeners" }, presentValue = "true", absentValue = "false") boolean listeners) {
		apps(listeners, null);
	}

	protected String getListenerName(Object listener) {
		try {
			if (!Object.class.equals(listener.getClass().getMethod("toString").getDeclaringClass())) {
				return listener.toString();
			}
		} catch (NoSuchMethodException ex) {
			// nevermind
		}
		String name = listener.getClass().getCanonicalName();
		if (name == null) {
			name = "<unnamed class>";
		}
		return name;
	}

	// TODO select individual listener types: timers, pattern listeners, etc.
	@Descriptor("list running OGEMA apps")
	public void apps(
			@Descriptor("show listeners registered by app") @Parameter(names = { "-l",
					"--listeners" }, presentValue = "true", absentValue = "false") boolean listeners,
			@Descriptor("substring matched against application or bundle name") String pattern) {
		for (AdminApplication app : admin.getAllApps()) {
			String appName = app.getID().getApplication().getClass().getCanonicalName();
			String bundleName = app.getBundleRef().getSymbolicName();
			if (pattern != null) {
				if (!(appName.toUpperCase().contains(pattern.toUpperCase())
						|| (bundleName.toUpperCase().contains(pattern.toUpperCase())))) {
					continue;
				}
			}
			System.out.printf("%s (%s, %d)%n", appName, bundleName, app.getBundleRef().getBundleId());
			if (listeners) {
				if (!app.getTimers().isEmpty()) {
					System.out.printf("  timers:%n");
					for (RegisteredTimer t : app.getTimers()) {
						System.out.printf("    every %d ms:%n", t.getTimer().getTimingInterval());
						for (TimerListener tl : t.getListeners()) {
							System.out.printf("      %s%n", getListenerName(tl));
						}
					}
				}
				if (!app.getResourceDemands().isEmpty()) {
					System.out.printf("  type demands:%n");
					for (RegisteredResourceDemand rrd : app.getResourceDemands()) {
						System.out.printf("    %s: %s%n", rrd.getTypeDemanded(), getListenerName(rrd.getListener()));
					}
				}
				if (!app.getAccessModeRequests().isEmpty()) {
					System.out.printf("  access mode requests:%n");
					for (RegisteredAccessModeRequest ramr : app.getAccessModeRequests()) {
						System.out.printf("    %s: %s (%s): %b%n", ramr.getResource().getPath(),
								ramr.getRequiredAccessMode(), ramr.getPriority(), ramr.isFulfilled());
					}
				}
				if (!app.getResourceListeners().isEmpty() || !app.getValueListeners().isEmpty()) {
					System.out.printf("  change listeners:%n", app.getResourceListeners());
					for (RegisteredResourceListener rrl : app.getResourceListeners()) {
						System.out.printf("    %s: %s%n", rrl.getResource().getPath(),
								getListenerName(rrl.getListener()));
					}
					for (RegisteredValueListener rvl : app.getValueListeners()) {
						System.out.printf("    %s: %s (%s)%n", rvl.getResource().getPath(),
								getListenerName(rvl.getValueListener()),
								rvl.isCallOnEveryUpdate() ? "on update" : "on change");
					}
				}
				if (!app.getStructureListeners().isEmpty()) {
					System.out.printf("  structure listeners:%n");
					for (RegisteredStructureListener rsl : app.getStructureListeners()) {
						System.out.printf("    %s: %s%n", rsl.getResource().getPath(),
								getListenerName(rsl.getListener()));
					}
				}
				if (!app.getPatternListeners().isEmpty()) {
					System.out.printf("  pattern listeners:%n");
					for (RegisteredPatternListener rpl : app.getPatternListeners()) {
						System.out.printf("    %s: %s%n", rpl.getPatternDemandedModelType(),
								getListenerName(rpl.getListener()));
						if (!rpl.getCompletedPatterns().isEmpty()) {
							System.out.printf("    complete:%n");
							for (ResourcePattern<?> completedPattern : rpl.getCompletedPatterns()) {
								System.out.printf("      %s%n", completedPattern.model.getPath());
							}
						}
						if (!rpl.getIncompletePatterns().isEmpty()) {
							System.out.printf("    incomplete:%n");
							for (ResourcePattern<?> incompletePattern : rpl.getIncompletePatterns()) {
								System.out.printf("      %s%n", incompletePattern.model.getPath());
								for (PatternCondition cond : rpl.getConditions(incompletePattern)) {
									if (!cond.isSatisfied()) {
										StringBuilder state = new StringBuilder(cond.getPath()).append(" ");
										if (cond.exists()) {
											state.append("(exists) ");
										}
										else {
											if (!cond.isOptional()) {
												state.append("(missing) ");
											}
										}
										if (!cond.isActive()) {
											state.append("(inactive) ");
										}
										System.out.printf("        %s: %s%n", cond.getFieldName(), state);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Descriptor("Display or set framework clock settings")
	public void clock(
			@Descriptor("set the simulation factor (value>=0)") 
			@Parameter(names = { "-f", "--factor" }, absentValue = "-1.0") float factor,
			@Descriptor("set the current time (in ms since 1st January 1970)")
			@Parameter(names = { "-t", "--timestamp"}, absentValue = (Long.MAX_VALUE + "")) long timestamp) {
		FrameworkClock cl = admin.getFrameworkClock();
		if (factor >= 0 && timestamp !=  Long.MAX_VALUE) {
			cl.setSimulationTimeAndFactor(timestamp, factor);
		}
		else if (factor >= 0) {
			cl.setSimulationFactor(factor);
		}
		else if (timestamp != Long.MAX_VALUE) {
			cl.setSimulationTime(timestamp);
		}
		System.out.printf("%s%n%tc\tfactor=%f%n", cl.getName(), cl.getExecutionTime(), cl.getSimulationFactor());
	}

	@Descriptor("List loggers")
	public void loggers() {
		loggers("", "", "");
	}

	@Descriptor("List/configure loggers")
	public void loggers(
			@Descriptor("set log level for selected loggers") @Parameter(names = { "-l",
					"--level" }, absentValue = "") String level,
			@Descriptor("comma separated list of outputs (file, console or cache) for which to set the log level (default: all outputs)") @Parameter(names = {
					"-o", "--output" }, absentValue = "") String output,
			@Descriptor("select loggers by regex (case-insensitive subsequence match)") String match) {
		List<AdminLogger> loggers = admin.getAllLoggers();
		Collections.sort(loggers, new Comparator<AdminLogger>() {

			@Override
			public int compare(AdminLogger o1, AdminLogger o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
			}

		});
		Pattern p = null;
		List<LogOutput> outputs = Arrays.asList(LogOutput.values());
		if (!match.isEmpty()) {
			p = Pattern.compile(match, Pattern.CASE_INSENSITIVE);
		}
		if (!output.isEmpty()) {
			outputs = new ArrayList<>(4);
			for (String o : output.split(",")) {
				outputs.add(LogOutput.valueOf(o.toUpperCase()));
			}
		}
		for (AdminLogger l : loggers) {
			String loggerName = l.getName();
			if (p != null && !p.matcher(loggerName).find()) {
				continue;
			}
			if (!level.isEmpty()) {
				LogLevel ll = LogLevel.valueOf(level.toUpperCase());
				for (LogOutput o : outputs) {
					l.setMaximumLogLevel(o, ll);
				}
			}
			System.out.printf("  %s {%s=%s, %s=%s, %s=%s}%n", loggerName, LogOutput.CONSOLE,
					l.getMaximumLogLevel(LogOutput.CONSOLE), LogOutput.FILE, l.getMaximumLogLevel(LogOutput.FILE),
					LogOutput.CACHE, l.getMaximumLogLevel(LogOutput.CACHE));
		}
	}

	@Descriptor("Print recent log entries")
	public void log(
			@Descriptor("set log message limit (use negative value to start from end of cache (most recent message))") @Parameter(names = {
					"-l", "--limit" }, absentValue = "0") int limit,
			@Descriptor("print log messages to file instead of console, does not work with -l") @Parameter(names = {
					"-f", "--file" }, absentValue = "") String filename,
			@Descriptor("regex for filtering log messages (case insensitive substring match)") String pattern)
			throws IOException {
		Pattern p = pattern == null || pattern.isEmpty() ? null : Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		if (!filename.isEmpty()) {
			try (PrintStream out = new PrintStream(filename, "UTF-8")) {
				printCache(out, p, limit);
				System.out.printf("log written to %s%n", filename);
			}
		}
		else {
			printCache(System.out, p, limit);
		}
	}

	@Descriptor("Print recent log entries")
	public void log(
			@Descriptor("set log message limit (use negative value to start from end of cache (most recent message))") @Parameter(names = {
					"-l", "--limit" }, absentValue = "0") int limit,
			@Descriptor("print log messages to file instead of console, does not work with -l") @Parameter(names = {
					"-f", "--file" }, absentValue = "") String filename)
			throws IOException {
		log(0, "", "");
	}

	public void printCache(PrintStream out, Pattern filter, int limit) {
		List<String> cache = admin.getAllLoggers().get(0).getCache();
		List<String> results = new ArrayList<>();
		int todo = limit == 0 ? Integer.MAX_VALUE : Math.abs(limit);
		for (int i = 0; i < cache.size() && todo > 0; i++) {
			int idx = limit < 0 ? cache.size() - 1 - i : i;
			String s = cache.get(idx);
			if (filter != null && !filter.matcher(s).find()) {
				continue;
			}
			results.add(s);
			todo--;
		}
		if (limit < 0) {
			Collections.reverse(results);
		}
		for (String s : results) {
			out.print(s);
		}
	}

	@Descriptor("writes the current logger cache to disk")
	public void dump_cache() {
		boolean success = admin.getAllLoggers().get(0).saveCache();
		System.out.println(success ? "ok" : "failed");
	}

	@Descriptor("Create a new user")
	public void createUser(
			@Descriptor("Create a natural user (flag absent) or a machine user (flag present)?") 
			@Parameter(names = {"-m", "--machine" }, absentValue="false", presentValue="true")
			final boolean machineUser,
			@Descriptor("Create an admin user? Default: false.") 
			@Parameter(names = {"-a", "--admin" }, absentValue="false", presentValue="true")			
			final boolean isAdmin,
			@Descriptor("The new user id.") 
			String id) {
		Objects.requireNonNull(id);
		id = id.trim();
		if (id.length() > 25) {
			throw new IllegalArgumentException("User id too long. Max 25 characters.");
		}
		try {
			admin.getUser(id);
			System.out.println("Could not create user " + id + ": already exists.");
			return;
		} catch (RuntimeException expected) {}
		admin.createUserAccount(id, !machineUser);
		System.out.println("User created: " + id);
		if (isAdmin) {
			if (permMan.getAccessManager().isNatural(id)) {
				if (!id.equals("master")) { // FIXME?
					final String result = grantAdminRights(id);
					if (result != null) {
						System.out.println("Could no grant admin rights to user " + id + ": " + result);
					}
				} 
			} else {
				final ResourcePermission resPerm = new ResourcePermission("path=*", "*");
				permMan.getAccessManager().addPermission(id, resPerm);
			}
			
		}
	}
	
	@Descriptor("Delete a user")
	public void deleteUser(
			@Descriptor("The user id to be deleted.") 
			String id) {
		Objects.requireNonNull(id);
		id = id.trim();
		try {
			admin.getUser(id);
		} catch (RuntimeException expected) {
			System.out.println("Could not delete user " + id + ": does not exist.");
			return;
		}
		admin.removeUserAccount(id);
		System.out.println("User " + id + " deleted.");
	}
			
	
	@Descriptor("List known users")
	public void listUsers(
			@Parameter(names= {"-f", "--filter"}, absentValue = "")
			@Descriptor("A filter, such as \"ogemaRole=naturalUser\", \"ogemaRole=machineUser\" or \"\"!(ogemaRole=machineUser)\"\".")
			String filter) throws InvalidSyntaxException {
		filter = adjustFilter(filter);
		final Role[] roles0 = userAdmin.getRoles(filter);
		if (roles0 == null)
			return;
		final List<String> roles = new ArrayList<>(roles0.length);
		for (Role r : roles0) {
			roles.add(r.getName());
		}
		final AccessManager accMan = permMan.getAccessManager();
		System.out.println("Known users:");
		final StringBuilder sb=  new StringBuilder();
		for (UserAccount user: admin.getAllUsers()) {
			if (!roles.contains(user.getName()))
				continue;
			sb.append(" ").append(user.getName()).append(": ").append((accMan.isNatural(user.getName()) ? "natural" : "machine") + " user");
			if (checkUserAdmin(user.getName())) {
				sb.append(" (admin)");
			}
			sb.append('\n');
		}
		System.out.println(sb.toString());
	}
	
	@Descriptor("List groups")
	public List<Role> listGroups(
			@Parameter(names= {"-f", "--filter"}, absentValue = "")
			@Descriptor("A filter, for instance \"ogemaRole=userGroup\" or \"ogemaRole=applicationGroup\".")
			String filter) throws InvalidSyntaxException {
		filter = adjustFilter(filter);
		final Role[] roles = userAdmin.getRoles(filter);
		if (roles == null)
			return null;
		final List<Role> groups = new ArrayList<>();
		for (Role r : roles) {
			if (r.getType() == Role.GROUP)
				groups.add(r);
		}
		return groups;
	}
	
	@Descriptor("Create a new group")
	public Role createGroup(@Descriptor("Group name") String name) {
		if (userAdmin.getRole(name) != null) {
			System.out.println("Role " + name + " already exists.");
			return userAdmin.getRole(name);
		}
		return userAdmin.createRole(name, Role.GROUP);
	}
	
	@Descriptor("Add a member to a group")
	public boolean addMember(
			@Descriptor("Group name") String group,
			@Descriptor("User name or group name; the member to be added.") String user) {
		final Role g = userAdmin.getRole(group);
		if (g == null) {
			System.out.println("Group " + group + " does not exist.");
			return false;
		}
		if (g.getType() != Role.GROUP) {
			System.out.println("Role " + group + " is not of type GROUP.");
			return false;
		}
		final Role u = userAdmin.getRole(user);
		if (u == null) {
			System.out.println("User " + user + " does not exist.");
			return false;
		}
		return ((Group) g).addMember(u);
	}
	
	@Descriptor("Remove a member from a group")
	public boolean removeMember(
			@Descriptor("Group name") String group,
			@Descriptor("User name or group name; the member to be removed.") String user) {
		final Role g = userAdmin.getRole(group);
		if (g == null) {
			System.out.println("Group " + group + " does not exist.");
			return false;
		}
		if (g.getType() != Role.GROUP) {
			System.out.println("Role " + group + " is not of type GROUP.");
			return false;
		}
		final Role u = userAdmin.getRole(user);
		if (u == null) {
			System.out.println("User " + user + " does not exist.");
			return false;
		}
		return ((Group) g).removeMember(u);
	}
	
	
	@SuppressWarnings("unchecked")
	@Descriptor("Print user or group properties")
	public Dictionary<String, Object> getUserProps(@Descriptor("User or group id") String user) {
		user = user.trim();
		final Role r = userAdmin.getRole(user);
		if (r == null) {
			System.out.println("User " + user + " not found");
			return null;
		}
		return r.getProperties();
	}
	
	@SuppressWarnings("unchecked")
	@Descriptor("Set user or group properties")
	public Dictionary<String, Object> setUserProp(
			@Descriptor("User or group id") String user,
			@Descriptor("Property key") String key,
			@Descriptor("Property value") String value) {
		user = user.trim();
		final Role r = userAdmin.getRole(user);
		if (r == null) {
			System.out.println("User " + user + " not found");
			return null;
		}
		key = key.trim();
		value = value.trim();
		if (key.isEmpty() || value.isEmpty()) {
			System.out.println("Key or value is empty");
			return r.getProperties();
		}
		r.getProperties().put(key, value);
		return r.getProperties();
	}
	
	@SuppressWarnings("unchecked")
	@Descriptor("Remove user or group properties")
	public Dictionary<String, Object> removeUserProp(
			@Descriptor("User or group id") String user,
			@Descriptor("Property key") String key) {
		user = user.trim();
		final Role r = userAdmin.getRole(user);
		if (r == null) {
			System.out.println("User " + user + " not found");
			return null;
		}
		key = key.trim();
		if (key.isEmpty()) {
			System.out.println("Key is empty");
			return r.getProperties();
		}
		r.getProperties().remove(key);
		return r.getProperties();
	}
	
	@Descriptor("Set new password")
	public void setNewPassword(
			@Descriptor("The user id")
			String user,
			@Descriptor("The old password")
			String oldPassword,
			@Descriptor("The new password")
			String newPassword) {
		final UserAccount userAccount;
		try {
			userAccount = admin.getUser(user);
		} catch (RuntimeException e) {
			System.out.println("User " + user + " does not exist.");
			return;
		}
		try {
			userAccount.setNewPassword(oldPassword, newPassword);
			System.out.println("Password reset.");
		} catch (SecurityException e) {
			System.out.println("Old password does not match");
		}
	}
	
	@Descriptor("Updates an ogema app and enforces package refresh before it is started again.")
	public void update(@Descriptor("The bundle id of the app to be updated.") final long id) throws BundleException, InterruptedException {
		final Bundle tobeupdated = FrameworkUtil.getBundle(getClass()).getBundleContext().getBundle(id);
		if (tobeupdated == null) {
			System.out.println("Bundle with id " + id + " not found.");
			return;
		}
		final Bundle sysbundle = FrameworkUtil.getBundle(getClass()).getBundleContext().getBundle(0);
		final int state = tobeupdated.getState();
		final boolean active = state == Bundle.ACTIVE || state == Bundle.STARTING;
		if (active) 
			tobeupdated.stop();
		tobeupdated.update();

		FrameworkWiring fw = sysbundle.adapt(FrameworkWiring.class);
//				fw.refreshBundles(Arrays.asList(tobeupdated));
		final CountDownLatch latch = new CountDownLatch(1);
		// restart must wait until refresh is done, otherwise a bundle lock error may occur
		final Runnable startTask = new Runnable() {
			
			@Override
			public void run() {
				synchronized (this) {
					if (latch.getCount() == 0) // already executed
						return;
					latch.countDown();
				}
				try {
					tobeupdated.start();
					System.out.println("Bundle restarted.");
				} catch (BundleException e) {
					System.err.print("Bundle restart failed.");
					e.printStackTrace();
				}
			}
		};
		final Collection<Bundle> bundlesToBeRefreshed = fw.getDependencyClosure(fw.getRemovalPendingBundles());
		if (bundlesToBeRefreshed.isEmpty()) {
			System.out.println("No package refresh required");
			if (active)
				startTask.run();
			return;
		}
		System.out.println("Refreshing " + bundlesToBeRefreshed.size() + " bundles.");
		if (!active) {
			checkForFrameworkExtensionBundle(bundlesToBeRefreshed);
			fw.refreshBundles(bundlesToBeRefreshed);
			return;
		}
		fw.refreshBundles(bundlesToBeRefreshed, new FrameworkListener() {
			
			@Override
			public void frameworkEvent(FrameworkEvent event) {
				if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
					startTask.run();
				}
			}
		});
		// wait at most 30s for packages refresh, then try to restart
		if (!latch.await(30, TimeUnit.SECONDS)) {
			startTask.run();
		}
		
	}
	
	// we assume here that the updated bundle was not active, so in particular not the framework bundle itself
	private final static void checkForFrameworkExtensionBundle(final Collection<Bundle> bundlesToBeRefreshed) {
		for (Bundle b: bundlesToBeRefreshed) {
			if (b.getBundleId() == 0) {
				// this is a hack to overcome a problem with Felix and Knopflerfish framework bundles when a framework
				// extension bundle is updated... the framework bundle tries to restart the framework, but this fails for an unknown reason;
				// the same happens when using the felix:update command followed by refresh, so it is probably nothing we can 
				// really avoid
				final String symbName = b.getSymbolicName();
				if (symbName.contains("felix") || symbName.contains("knopflerfish")) { 
					System.out.println("A framework extension bundle has been updated; good bye!");
					// signals the wish to restart the framework to a Java-external launcher;
					// the same exit code is used by the OGEMA launcher and bnd launcher
					System.exit(-4); 
				}
			}
		}
		
	}
	
	/**
	 * Grants administrator rights to a given user. Only natural user are allowed to be administrators. Adds ALL APPS as
	 * a role and puts java.security.AllPermission into policies.
	 * 
	 * @param user
	 *            the name of the user as string
	 * @return null on success, an explanation otherwise
	 */
	private String grantAdminRights(String user) {
		// master is always admin // XXX
		if ("master".equals(user)) {
			return "Master user is always admin(?)";
		}
		final AccessManager accessManager = permMan.getAccessManager();
		// machine user should not be admin
		if (!accessManager.isNatural(user)) {
			return "Machine user cannot be admin.";
		}
		AppPermission appPermission = accessManager.getPolicies(user);
		appPermission.addPermission("java.security.AllPermission", null, null);
		permMan.installPerms(appPermission);
		return null;
	}
	
	/**
	 * Checks if the given user has administrator rights. master always returns true. Machine users always return false.
	 * Checks for ALL APPS role and java.security.AllPermission policy.
	 * 
	 * @param user
	 *            the name of the user as string
	 * @return true or false
	 */
	private boolean checkUserAdmin(String user) {

		if ("master".equals(user)) { // FIXME?
			return true;
		}
		final AccessManager accessManager = permMan.getAccessManager();
		// machine user is not allowed to be admin
		if (!accessManager.isNatural(user)) {
			return false;
		}

		String allPerms = "java.security.AllPermission";
		AppPermission appPermission = accessManager.getPolicies(user);
		Map<String, ConditionalPermissionInfo> grantedPerms = appPermission.getGrantedPerms();
		for (ConditionalPermissionInfo cpi : grantedPerms.values()) {
			PermissionInfo[] permInfo = cpi.getPermissionInfos();
			for (PermissionInfo pi : permInfo) {
				if (allPerms.equals(pi.getType())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static String adjustFilter(String filter) {
		filter = filter.trim();
		if (filter.isEmpty())
			return null;
		if (!filter.startsWith("("))
			return "(" + filter + ")";
		return filter;
	}
	
}
