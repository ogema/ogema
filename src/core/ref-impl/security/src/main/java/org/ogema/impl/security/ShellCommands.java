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

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.felix.service.command.Descriptor;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.AppID;
import org.ogema.core.security.AppPermission;
import org.osgi.framework.BundleContext;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;

public class ShellCommands {
	private PermissionManager permman;

	static final String policies_HelpString = "Lists policies as ConditionalPermissionInfo string representation."
			+ "\n\t-all print the whole list of the of the system policies."
			+ "\n\t-app print the list of the policies that match ogema applications. A policy is printed out each app that matches."
			+ "\n\t-help print this message.";

	public ShellCommands(PermissionManager permMananger, BundleContext context) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "security");
		props.put("osgi.command.function", new String[] { "policies" });
		this.permman = permMananger;
		context.registerService(this.getClass().getName(), this, props);
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
		List<AdminApplication> apps = permman.getAdminManager().getAllApps();
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
}
