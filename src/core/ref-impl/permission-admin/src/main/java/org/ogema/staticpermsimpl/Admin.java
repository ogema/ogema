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
package org.ogema.staticpermsimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import org.slf4j.LoggerFactory;

/**
 * This is the activator class for the bundle which is responsible for the initial policy setting. Thats why this bundle
 * shouldn't use any external services.
 * 
 * @author mns
 * 
 */
public class Admin implements BundleActivator {

	/**
	 * Framework property ({@value} ) containing the location of the OGEMA security policies file, default =
	 * {@value #POLICY_DIR}/{@value #APP_POLICY_FILE}
	 */
	public static final String POLICY_LOCATION_PROPERTY = "org.ogema.security.policy";
	/**
	 * Framework property ({@value} ) containing the location of the OGEMA security roles file, default =
	 * {@value #POLICY_DIR}/{@value #USER_POLICY_FILE}
	 */
	public static final String ROLES_LOCATION_PROPERTY = "org.ogema.security.roles";

	public static final String APP_POLICY_FILE = "ogema.policy";
	public static final String USER_POLICY_FILE = "ogema.roles";
	public static final String POLICY_DIR = "config";
	ConditionalPermissionAdmin cpa;
	/*
	 * These system properties would normally granted dynamically by PermissionManager. Therefore interacts the
	 * PermissionManager with the system administrator over the web interface.
	 */
	private static final String[] DEFAULT_PINFO = {};

	private void parseUserPolicies(File f, ArrayList<String> staticpInfos) {
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {

			String line = reader.readLine();
			while (line != null) {
				line = line.trim();
				/*
				 * If the line is a comment or an empty line skip it.
				 */
				if (!line.startsWith("#") && !line.startsWith("//") && !line.equals("")) {
					line = line.replaceFirst("(.*userName)[ \t]*\"([a-zA-Z_0-9]*)\"",
							"$1 \"file:./ogema/users/$2/urp$2\"");
					line = line.replaceFirst("(.*)userName(.*)",
							"$1org.osgi.service.condpermadmin.BundleLocationCondition$2");
					staticpInfos.add(line);
				}
				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseAppPolicies(File f, ArrayList<String> staticpInfos) {
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {

			String line = reader.readLine();
			while (line != null) {
				line = line.trim();
				/*
				 * If the line is a comment skip it.
				 */
				if (!line.startsWith("#") && !line.startsWith("//") && !line.equals(""))
					staticpInfos.add(line);
				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start(BundleContext bc) throws BundleException {

		System.out.println("PermissionAdmin location: " + bc.getBundle().getLocation());

		ServiceReference<?> sRef = bc.getServiceReference(ConditionalPermissionAdmin.class);
		if (sRef != null) {
			cpa = (ConditionalPermissionAdmin) bc.getService(sRef);
			/*
			 * Check if the storage of the framework is to be cleaned
			 */
			File cleanStartMarker = bc.getDataFile("policiesInstalled");
			if (!cleanStartMarker.exists()) {
				System.out.println("Installing OGEMA security policies...");
				// if (clean != null && clean.equals(org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT))
				// {

				File policiesDir = new File(POLICY_DIR);
				String policyFilename = bc.getProperty(POLICY_LOCATION_PROPERTY);
				File policyFile = policyFilename == null ? new File(policiesDir, APP_POLICY_FILE) : new File(
						policyFilename);
				ArrayList<String> staticpInfos = new ArrayList<String>(16);

				/*
				 * Application static policies
				 */
				if (policyFile.exists()) {
					parseAppPolicies(policyFile, staticpInfos);
				}
				else {
					for (String s : DEFAULT_PINFO)
						staticpInfos.add(s);
					try {
						System.out.printf("OGEMA Policy file %s doesn't exist. Default policy will be used.\n",
								policyFile.getCanonicalPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				/*
				 * User static policies
				 */
				String rolesFilename = bc.getProperty(ROLES_LOCATION_PROPERTY);
				File rolesFile = rolesFilename == null ? new File(policiesDir, USER_POLICY_FILE) : new File(
						rolesFilename);
				if (rolesFile.exists()) {
					parseUserPolicies(rolesFile, staticpInfos);
				}
				installPolicies(staticpInfos, true);

				try {
					cleanStartMarker.createNewFile();
				} catch (IOException ex) {
					throw new BundleException("IOException", ex);
				}
			}
		}
		else {
			throw new BundleException("Bundle CPA-test can not start, There is no "
					+ "ConditinalPermissionAdmin service");
		}
	}

	public void installPolicies(List<String> pInfos, boolean reset) {
		// First get the permissions table
		ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();
		if (reset) {
			piList.clear();
		}

		for (String pInfo : pInfos) {
			// Create new permission info object each new entry
			// Multiple entries with same name are not permitted.
			ConditionalPermissionInfo cpi;
			try {
				cpi = cpa.newConditionalPermissionInfo(pInfo);
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).error(String.format("Error setting permission '%s'", pInfo), e);
				continue;
			}
			String name = cpi.getName();
			// Check if a permission info with the same name exists
			for (ConditionalPermissionInfo tmpcpi : piList) {
				// If a permission info exists in the table remove it before adding the new info with the same name
				if (tmpcpi.getName().equals(name)) {
					piList.remove(tmpcpi);
					break;
				}
			}
			piList.add(cpi);
		}
		cpu.commit();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}
}
