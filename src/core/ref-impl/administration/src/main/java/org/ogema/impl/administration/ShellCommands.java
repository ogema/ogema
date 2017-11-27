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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ogema.impl.administration;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.permissionadmin.PermissionInfo;

/**
 * 
 * @author jlapp
 */
@Component(specVersion = "1.2", immediate = true)
@Properties({ @Property(name = "osgi.command.scope", value = "ogm"), @Property(name = "osgi.command.function", value = {
		"apps", "clock", "loggers", "log", "dump_cache", "update", "listUsers", "createUser", "setNewPassword" }) })
@Service(ShellCommands.class)
@Descriptor("OGEMA administration commands")
public class ShellCommands {

	@Reference
	protected AdministrationManager admin;
	
	@Reference
	private PermissionManager permMan;

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

	@Descriptor("Display framework clock settings")
	public void clock(@Descriptor("set the simulation factor (value>=0)") @Parameter(names = { "-f",
			"--factor" }, absentValue = "-1.0") float factor) {
		FrameworkClock cl = admin.getFrameworkClock();
		if (factor >= 0) {
			cl.setSimulationFactor(factor);
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
			if (!id.equals("master")) {
				final String result = grantAdminRights(id);
				if (result != null) {
					System.out.println("Could no grant admin rights to user " + id + ": " + result);
				}
			}
		}
	}
	
	@Descriptor("List known users")
	public void listUsers() {
		final AccessManager accMan = permMan.getAccessManager();
		System.out.println("Known users:");
		final StringBuilder sb=  new StringBuilder();
		for (UserAccount user: admin.getAllUsers()) {
			sb.append(" ").append(user.getName()).append(": ").append((accMan.isNatural(user.getName()) ? "natural" : "machine") + " user");
			if (checkUserAdmin(user.getName())) {
				sb.append(" (admin)");
			}
			sb.append('\n');
		}
		System.out.println(sb.toString());
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
		// master is always admin
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

		if ("master".equals(user)) {
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
	
}
