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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.ResourcePermission;
import org.ogema.applicationregistry.ApplicationRegistry;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.AppID;
import org.ogema.core.model.Resource;
import org.ogema.core.security.AppPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.condpermadmin.BundleLocationCondition;
import org.osgi.service.condpermadmin.Condition;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import org.osgi.service.permissionadmin.PermissionInfo;

class ShellCommands {
	
	private final PermissionManager permman;
	private final ConditionalPermissionAdmin cpa;
	private final ApplicationRegistry appReg;
	private final ServiceRegistration<ShellCommands> sr;
	private final BundleContext ctx;

	static final String policies_HelpString = "Lists policies as ConditionalPermissionInfo string representation."
			+ "\n\t-all print the whole list of the of the system policies."
			+ "\n\t-app print the list of the policies that match ogema applications. A policy is printed out each app that matches."
			+ "\n\t-help print this message.";
	
	ShellCommands(PermissionManager permMananger, BundleContext context) {
		this.ctx = context;
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "security");
		props.put("osgi.command.function", new String[] {
				"allow",
				"addResourcePermission",
				"deny",
				"removePolicy",
				"bundlePermissions",
				"userPermissions",
				"grantLocalPermissions",
				"checkLocalPermissions",
				"printLocalPermissions",
				"policies",
				"addAuthenticator",
				"removeAuthenticator",
				"getAuthenticators",
				"getSupportedAuthenticators",
				"clearUserAuthenticators"
		});
		this.permman = permMananger;
		this.appReg = permMananger.getApplicationRegistry();
		this.cpa = (ConditionalPermissionAdmin) permMananger.getSystemPermissionAdmin();
		sr = context.registerService(ShellCommands.class, this, props);
	}
	
	public void close() {
		try {
			sr.unregister();
		} catch (Exception e) { /* ignore */ }
	}

	@Descriptor(policies_HelpString)
	public void policies(String arg) {
		switch (arg) {
		case "-all":
			permman.printPolicies(System.out);
			break;
		case "-app":
			execute(System.out, System.err);
			break;
		default:
			System.out.println("Wrong parameter! Usage:");
			System.out.println(policies_HelpString);
			break;
		}
	}

	public String getName() {
		return "listPolicies";
	}

	public String getUsage() {
		return "listPolicies";
	}

	public String help() {
		return "List all policies in system.";
	}

	public void execute(PrintStream out, PrintStream err) {
		AppPermission ap = null;
		List<AdminApplication> apps = appReg.getAllApps();
		for (AdminApplication entry : apps) {
			AppID aidi = entry.getID();
			if (aidi == null)
				out.println("Internal error: Registered AppID is null.");
			ap = permman.getPolicies(aidi);
			out.println("_____________________________________________________________");
			out.println("App location: " + aidi.getLocation());
			out.println("App ID String: " + aidi.getIDString());
			out.println("List of granted permissions: ");
			Map<String, ConditionalPermissionInfo> granted = ap.getGrantedPerms();
			Set<Entry<String, ConditionalPermissionInfo>> tlrs = granted.entrySet();
			for (Map.Entry<String, ConditionalPermissionInfo> entry1 : tlrs) {
				out.println(entry1.getValue());
				// logger.info(entry.getKey());
			}

		}
	}
	
	private final static AtomicInteger PERMISSION_COUNTER = new AtomicInteger(1);

	@Descriptor("Add a resource permission for a user or an app.")
	public boolean addResourcePermission(
			@Parameter(names= {"-u", "--user"}, absentValue="")
			@Descriptor("User id, if the permission is to be added for a user.")
			String user,
			@Parameter(names= {"-b", "--bundle"}, absentValue="")
			@Descriptor("Bundle id or location, if the permission is to be added for a bundle.")
			String bundle,
			@Parameter(names= {"-t", "--type"}, absentValue="")
			@Descriptor("Fully qualified resource type")
			String type,
			@Parameter(names= {"-p", "--path"}, absentValue="")
			@Descriptor("Resource path")
			String path,
			@Parameter(names = {"-a", "--actions"}, absentValue="")
			@Descriptor("Comma-separated actions, such as \"read\" or \"read,write,create,delete,addsub\". If absent, the permission will apply to all actions.")
			String actions,
//			@Parameter(names = {"-c", "--count"}, absentValue="100")
//			@Descriptor("Number of resources that can be created (if the action implies create permission).")
//			int count,
			@Parameter(names= {"-d", "--deny"}, absentValue="false", presentValue="true")
			@Descriptor("Shall the permission be denied? If not set, it will be granted.")
			boolean deny) {
		if (!user.isEmpty() && !bundle.isEmpty()) {
			System.out.println("Select either a user or a bundle, but not both.");
			return false;
		}
		if (user.isEmpty() && bundle.isEmpty()) {
			System.out.println("Select either a user or a bundle.");
			return false;
		}
		if (type.isEmpty() && path.isEmpty())  {
			System.out.println("Specify resource type and/or path");
			return false;
		}
		if (actions.isEmpty())
			actions = "*";
		Bundle b = null;
		if (!user.isEmpty()) {
			b = ctx.getBundle("urp:" + user);
			if (b == null) {
				System.out.println("User " + user + " not found");
				return false;
			}
		} else {
			try {
				final long id = Long.parseLong(bundle);
				b = ctx.getBundle(id);
			} catch (NumberFormatException e) {
				b = ctx.getBundle(bundle);
			}
			if (b == null) {
				System.out.println("Bundle " + bundle + " not found");
				return false;
			}
		}
		
		final Class<? extends Resource> typeLoaded = !type.isEmpty() ? loadClass(ctx,type, Resource.class) : null;
		if (!type.isEmpty() && typeLoaded == null) {
			System.out.println("Resource type " + type + " not found");
			return false;
		}
		if (path.isEmpty())
			path = null;
		else if (path.startsWith("/")) // or the other way round?
			path = path.substring(1);
		final StringBuilder sb= new StringBuilder();
		if (!type.isEmpty()) {
			sb.append("type=").append(type);
			if (path != null)
				sb.append(',');
		}
		if (path != null)
			sb.append("path=").append(path);
		final ResourcePermission perm = new ResourcePermission(sb.toString(), actions);
		return addPermission(b, perm, !deny, -1);
	}
	
	@Descriptor("List all system permissions applying for a user.")
	public List<ConditionalPermissionInfo> userPermissions(@Descriptor("The user id") String user) {
		final Bundle b = ctx.getBundle("urp:" + user);
		if (b == null) {
			System.out.println("User " + user + " not found");
			return null;
		}
		return bundlePermissions(b);
	}
	
    @Descriptor("give a permission to a bundle")
    public boolean allow(
            final Bundle bundle,
            @Descriptor("permission type")
            final Class<? extends Permission> type,
            @Descriptor("permission filter")
            final String name,
            @Descriptor("permission actions")
            final String actions) {
        return addPermission(bundle, type, name, actions, true, -1);
    }

	public boolean allow(
            final Bundle bundle,
            @Descriptor("permission type") String type,
            @Descriptor("permission filter")
            final String name,
            @Descriptor("permission actions")
            final String actions) throws ClassNotFoundException {
        return addPermission(bundle, loadClass(ctx, type, Permission.class), name, actions, true, -1);
    }

    @Descriptor("deny a permission to a bundle")
    public boolean deny(final Bundle bundle,
            @Descriptor("permission type")
            final Class<? extends Permission> type,
            @Descriptor("permission filter")
            final String name,
            @Descriptor("permission actions")
            final String actions) {
        return addPermission(bundle, type, name, actions, false, 0);
    }

	@Descriptor("deny a permission to a bundle")
    public boolean deny(final Bundle bundle,
            @Descriptor("permission type") String type,
            @Descriptor("permission filter")
            final String name,
            @Descriptor("permission actions")
            final String actions) throws ClassNotFoundException {
        return addPermission(bundle, loadClass(ctx, type, Permission.class), name, actions, false, 0);
    }

    public boolean addPermission(final Bundle bundle, final Class<? extends Permission> type, final String name, final String actions,
            final boolean allowOrDeny, int index) {
    	if (type == null)
    		return false;
        final ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
        addPermission(bundle, type, name, actions, cpa, cpu, allowOrDeny, index);
        return cpu.commit();
    }

    public void addPermission(final Bundle bundle, final Class<? extends Permission> type, final String name, final String actions,
            final ConditionalPermissionAdmin cpAdmin, final ConditionalPermissionUpdate update, final boolean allowOrDeny, int index) {
    	if (type == null)
    		return;
        if (index == -1) {
            index = update.getConditionalPermissionInfos().size();
        }
        update.getConditionalPermissionInfos().add(index,
                cpAdmin.newConditionalPermissionInfo(
                        "testCond" + PERMISSION_COUNTER.getAndIncrement(),
                        new ConditionInfo[]{
                            new ConditionInfo("org.osgi.service.condpermadmin.BundleLocationCondition", new String[]{bundle.getLocation()})},
                        new PermissionInfo[]{
                            new PermissionInfo(type.getName(), name, actions)},
                        allowOrDeny ? "allow" : "deny"));
    }
    
    public boolean addPermission(final Bundle bundle, final Permission perm, final boolean allowOrDeny, int index) {
    	if (perm == null)
    		return false;
        final ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
        addPermission(bundle, perm, cpa, cpu, allowOrDeny, index);
        return cpu.commit();
    }

    public void addPermission(final Bundle bundle, final Permission perm, final ConditionalPermissionAdmin cpAdmin, 
    		final ConditionalPermissionUpdate update, final boolean allowOrDeny, int index) {
    	if (perm == null)
    		return;
        if (index == -1) {
            index = update.getConditionalPermissionInfos().size();
        }
        update.getConditionalPermissionInfos().add(index,
                cpAdmin.newConditionalPermissionInfo(
                        "testCond" + PERMISSION_COUNTER.getAndIncrement(),
                        new ConditionInfo[]{
                            new ConditionInfo("org.osgi.service.condpermadmin.BundleLocationCondition", new String[]{bundle.getLocation()})},
                        new PermissionInfo[]{
                            new PermissionInfo(perm.getClass().getName(), perm.getName(), perm.getActions())},
                        allowOrDeny ? "allow" : "deny"));
    }

    @Descriptor("removes a policy by name")
    public boolean removePolicy(String name) {
        ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
        for (ConditionalPermissionInfo cpi : cpu.getConditionalPermissionInfos()) {
            if (cpi.getName().equals(name)) {
                boolean removed = cpu.getConditionalPermissionInfos().remove(cpi);
                if (removed) {
                    return cpu.commit();
                }
                return removed;
            }
        }
        return false;
    }

    @Descriptor("list all system permissions applying to a bundle")
    public List<ConditionalPermissionInfo> bundlePermissions(Bundle b) {
        final ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
        List<ConditionalPermissionInfo> l = new ArrayList<>();
        for (ConditionalPermissionInfo cpi : cpu.getConditionalPermissionInfos()) {
            ConditionInfo[] cis = cpi.getConditionInfos();
            for (ConditionInfo ci : cis) {
                if (ci.getType().equals("org.osgi.service.condpermadmin.BundleLocationCondition")) {
                    Condition blc = BundleLocationCondition.getCondition(b, ci);
                    if (blc.isSatisfied()) {
                        l.add(cpi);
                    }
                }
            }
            if (cis.length == 0) {
                l.add(cpi);
            }

        }
        return l;
    }

    @Descriptor("print a bundle's permissions.perm file.")
    public void printLocalPermissions(Bundle b) throws IOException {
        URL u = b.getResource("OSGI-INF/permissions.perm");
        if (u == null) {
            System.err.printf("bundle %s does not contain a permissions file.%n", b);
            return;
        }
        try (
                InputStream is = u.openStream();
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader r = new BufferedReader(isr)) {
            String line;
            while ((line = r.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    @Descriptor("prints which local permissions of a bundle are allowed, not allowed or denied.")
    public void checkLocalPermissions(Bundle b) throws Exception {
        List<ConditionalPermissionInfo> systemPermissions = bundlePermissions(b);
        for (PermissionInfo lpi : readLocalPermissions(b)) {
            boolean granted = false;
            Permission lp = createPermission(ctx, lpi);
            systemPolicies:
            for (ConditionalPermissionInfo scpi : systemPermissions) {
                for (PermissionInfo spi : scpi.getPermissionInfos()) {
                    Permission sp = createPermission(ctx, spi);
                    if (scpi.getAccessDecision().equals("allow")) {
                        if (sp.implies(lp)) {
                            granted = true;
                            System.out.printf("allowed: %s%n", lp);
                            System.out.printf("  implied by '%s': %s%n", scpi.getName(), spi);
                            break systemPolicies;
                        }
                    } else {
                        if (lp.implies(sp)) { //XXX probably wrong, also ignores policy order
                            System.out.printf("denied: %s%n", lp);
                            System.out.printf("  denied by '%s': %s%n", scpi.getName(), spi);
                            break systemPolicies;
                        }
                    }
                }
            }
            if (!granted) {
                System.out.printf("not allowed: %s%n", lp);
                for (ConditionalPermissionInfo scpi : systemPermissions) {
                    if (scpi.getAccessDecision().equals("deny")) {
                        continue;
                    }
                    for (PermissionInfo spi : scpi.getPermissionInfos()) {
                        if (lp.implies(createPermission(ctx, spi))) {
                            System.out.printf("  constrained effective permission: %s%n", spi);
                        }
                    }
                }
            }
        }
    }

    static Permission createPermission(BundleContext ctx, PermissionInfo pi) throws ClassNotFoundException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String type = pi.getType();
        Class<?> cls = loadClass(ctx, type, Permission.class);
        Constructor<?> constr = cls.getConstructor(new Class<?>[]{String.class, String.class});
        String name = pi.getName();
        String actions = pi.getActions();
        Permission perm = (Permission) constr.newInstance(name, actions);
        return perm;
    }

    static List<PermissionInfo> readLocalPermissions(Bundle b) throws IOException {
        URL u = b.getResource("OSGI-INF/permissions.perm");
        if (u == null) {
            System.out.printf("bundle %s does not contain a permissions file.%n", b);
            return Collections.emptyList();
        }
        List<PermissionInfo> l = new ArrayList<>();
        try (
                InputStream is = u.openStream();
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader r = new BufferedReader(isr)) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                PermissionInfo pi = new PermissionInfo(line);
                l.add(pi);
            }
            return l;
        }
    }

    @Descriptor("add all permissions listed in the bundle's permissions.perm file,"
            + " so the effective permissions will be the local permissions + implied permissions.")
    public boolean grantLocalPermissions(Bundle b) throws IOException {
        boolean addAll = false;
        URL u = b.getResource("OSGI-INF/permissions.perm");
        if (u == null) {
            System.out.printf("bundle %s does not contain a permissions file.%n", b);
            return false;
        }
        //List<ConditionalPermissionInfo> activePermissions = bundlePermissions(b);
        ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
        ConditionInfo[] bundleLocationInfo = new ConditionInfo[]{
            new ConditionInfo("org.osgi.service.condpermadmin.BundleLocationCondition", new String[]{b.getLocation()})};
        try (
                InputStream is = u.openStream();
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader r = new BufferedReader(isr)) {
            List<ConditionalPermissionInfo> systemPermissions = bundlePermissions(b);
            for (PermissionInfo pi : readLocalPermissions(b)) {
                boolean skip = false;
                if (!addAll) {
                    skip = isImplied(ctx, pi, systemPermissions);
                    if (skip) {
                        System.out.printf("skipping %s - already implied%n", pi);
                    }
                }
                if (!skip) {
                    ConditionalPermissionInfo cpi = cpa.newConditionalPermissionInfo(null,
                            bundleLocationInfo, new PermissionInfo[]{pi}, "allow");
                    cpu.getConditionalPermissionInfos().add(cpi);
                    System.out.printf("allow %s%n", pi);
                }
            }
            /*
            String line;
            while ((line = r.readLine())!=null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                PermissionInfo pi = new PermissionInfo(line);
                ConditionalPermissionInfo cpi = cpa.newConditionalPermissionInfo(null, 
                        bundleLocationInfo, new PermissionInfo[]{pi}, "allow");
                cpu.getConditionalPermissionInfos().add(cpi);
                System.out.printf("allow %s%n", pi);
            }
             */
            return cpu.commit();
        }
    }
    
    @Descriptor("Get the list of all installed authenticators, including disabled ones.")
    public Collection<String> getAuthenticators() {
    	final AccessManagerImpl accMan = (AccessManagerImpl) permman.getAccessManager();
   		return accMan.getAllAuthenticators();
    }
    
    @Descriptor("Get the supported authenticators.")
    public Collection<String> getSupportedAuthenticators(
    		@Parameter(names= {"-u", "--user"}, absentValue="")
    		@Descriptor("User id. If this is not set, all globally admissible authenticator ids are returned.")
    		final String user) {
    	final AccessManagerImpl accMan = (AccessManagerImpl) permman.getAccessManager();
    	if (user.isEmpty())
    		return accMan.getSupportedAuthenticatorsInfo();
    	else
    		return accMan.getSupportedAuthenticatorsInfo(user);
    }
    
    @Descriptor("Add a supported authenticator id.")
    public void addAuthenticator(
    		@Parameter(names= {"-u", "--user"}, absentValue="")
    		@Descriptor("User id. If this is not set, the authenticator will be added to the list of global authenticator ids.")
    		String user,
    		@Descriptor("Id of the authenticator service")
    		String authenticatorId) {
    	authenticatorId = authenticatorId.trim();
    	user = user.trim();
    	if (authenticatorId.isEmpty())
    		return;
    	if (user.isEmpty())
    		permman.getAccessManager().addSupportedAuthenticator(authenticatorId);
    	else
    		permman.getAccessManager().addSupportedAuthenticator(user, authenticatorId);
    }
    
    @Descriptor("Remove a supported authenticator id.")
    public void removeAuthenticator(
    		@Parameter(names= {"-u", "--user"}, absentValue="")
    		@Descriptor("User id. If this is not set, the authenticator will be added to the list of global authenticator ids.")
    		String user,
    		@Descriptor("Id of the authenticator service")
    		String authenticatorId) {
    	authenticatorId = authenticatorId.trim();
    	user = user.trim();
    	if (authenticatorId.isEmpty())
    		return;
    	if (user.isEmpty())
    		permman.getAccessManager().removeSupportedAuthenticator(authenticatorId);
    	else
    		permman.getAccessManager().removeSupportedAuthenticator(user, authenticatorId);
    }
   
    @Descriptor("Reset the supported authenticators to the framework default settings.")
    public void clearUserAuthenticators(
    		@Descriptor("User id.")
    		String user) {
    	user = user.trim();
    	permman.getAccessManager().setSupportedAuthenticators(user, null);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T> loadClass(final BundleContext ctx, final String className, final Class<T> type) {
    	Objects.requireNonNull(type);
    	try {
	 	    final Class<?> clzz = Class.forName(className);
		    if (type.isAssignableFrom(clzz))
			    return (Class<? extends T>) clzz;
	    } catch (ClassNotFoundException expected) {}
	    for (Bundle b : ctx.getBundles()) {
		    final ClassLoader loader = b.adapt(BundleWiring.class).getClassLoader();
		    if (loader == null)
		    	continue;
		    try {
			    final Class<?> clzz = loader.loadClass(className);
			    if (type.isAssignableFrom(clzz))
				    return (Class<? extends T>) clzz;
		    } catch (ClassNotFoundException expected) {}
	    } 
	    System.out.println("Class " + className + " not found.");
	    return null;
    }


    static boolean isImplied(BundleContext ctx, PermissionInfo pi, List<ConditionalPermissionInfo> systemPolicies) {
        try {
            Permission localPerm = createPermission(ctx, pi);
            for (ConditionalPermissionInfo scpi : systemPolicies) {
                for (PermissionInfo spi : scpi.getPermissionInfos()) {
                    Permission sp = createPermission(ctx, spi);
                    if (sp.implies(localPerm)) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.printf("could not test local permission %s: %s%n", pi, ex.getMessage());
        }
        return false;
    }
    
}
