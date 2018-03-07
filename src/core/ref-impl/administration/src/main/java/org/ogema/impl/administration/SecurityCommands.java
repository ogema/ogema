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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.felix.service.command.Descriptor;
import org.ogema.accesscontrol.ResourcePermission;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.condpermadmin.BundleLocationCondition;
import org.osgi.service.condpermadmin.Condition;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import org.osgi.service.permissionadmin.PermissionInfo;

/**
 *
 * @author jlapp
 */
@Component(immediate = true, service = SecurityCommands.class, property = {
    "osgi.command.scope=ogm",
    "osgi.command.function=allow",
    "osgi.command.function=deny",
    "osgi.command.function=removePolicy",
    "osgi.command.function=bundlePermissions",
    "osgi.command.function=grantLocalPermissions",
    "osgi.command.function=checkLocalPermissions",
    "osgi.command.function=printLocalPermissions",})
@SuppressWarnings("unchecked")
public class SecurityCommands {

    ConditionalPermissionAdmin cpa;

    //ensure that ogema permission classes are available to commands
    private static final Class<?> RESOURCEPERMISSION = ResourcePermission.class;
    //private static final Class<?> OGEMAFILEPERMISSION = OgemaFilePermission.class;    

    @Reference
    void setCPA(ConditionalPermissionAdmin cpa) {
        this.cpa = cpa;
    }

    private final static AtomicInteger PERMISSION_COUNTER = new AtomicInteger(1);

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
        return addPermission(bundle, (Class<? extends Permission>) Class.forName(type), name, actions, true, -1);
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
        return addPermission(bundle, (Class<? extends Permission>) Class.forName(type), name, actions, false, 0);
    }

    public boolean addPermission(final Bundle bundle, final Class<? extends Permission> type, final String name, final String actions,
            final boolean allowOrDeny, int index) {
        final ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
        addPermission(bundle, type, name, actions, cpa, cpu, allowOrDeny, index);
        return cpu.commit();
    }

    public void addPermission(final Bundle bundle, final Class<? extends Permission> type, final String name, final String actions,
            final ConditionalPermissionAdmin cpAdmin, final ConditionalPermissionUpdate update, final boolean allowOrDeny, int index) {
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
            Permission lp = createPermission(lpi);
            systemPolicies:
            for (ConditionalPermissionInfo scpi : systemPermissions) {
                for (PermissionInfo spi : scpi.getPermissionInfos()) {
                    Permission sp = createPermission(spi);
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
                        if (lp.implies(createPermission(spi))) {
                            System.out.printf("  constrained effective permission: %s%n", spi);
                        }
                    }
                }
            }
        }
    }

    static Permission createPermission(PermissionInfo pi) throws ClassNotFoundException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String type = pi.getType();
        Class<?> cls = Class.forName(type);
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
            return Collections.EMPTY_LIST;
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
                    skip = isImplied(pi, systemPermissions);
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

    boolean isImplied(PermissionInfo pi, List<ConditionalPermissionInfo> systemPolicies) {
        try {
            Permission localPerm = createPermission(pi);
            for (ConditionalPermissionInfo scpi : systemPolicies) {
                for (PermissionInfo spi : scpi.getPermissionInfos()) {
                    Permission sp = createPermission(spi);
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
