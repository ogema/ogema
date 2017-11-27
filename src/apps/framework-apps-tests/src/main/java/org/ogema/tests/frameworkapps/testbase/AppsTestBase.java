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
package org.ogema.tests.frameworkapps.testbase;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.exam.OsgiAppTestBase;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;

public class AppsTestBase extends OsgiAppTestBase {

	/**
	 * Map<bundle symbolic name, maven artifact>
	 */
	protected final static Map<String, MavenArtifactProvisionOption> FRAMEWORK_APPS;
	private final static MavenArtifactProvisionOption[] DEPENDENCIES = new MavenArtifactProvisionOption[] {
			CoreOptions.mavenBundle("commons-fileupload","commons-fileupload").version("1.3.3"),
			CoreOptions.mavenBundle("org.ogema.tools","grafana-base").version(ogemaVersion)
	};
	
	static {
		final Map<String, MavenArtifactProvisionOption> map = new HashMap<>();
		addApp(map, "org.ogema.ref-impl", "framework-administration");
		addApp(map, "org.ogema.ref-impl", "framework-gui");
		addApp(map, "org.ogema.ref-impl", "security-gui");
		addApp(map, "org.ogema.apps", "device-configurator");
		addApp(map, "org.ogema.apps", "grafana-logging");
		addApp(map, "org.ogema.apps", "graph-generator");
		addApp(map, "org.ogema.apps", "logging-app");
		addApp(map, "org.ogema.tools", "pattern-debugger");
		FRAMEWORK_APPS = Collections.unmodifiableMap(map);
	}
	
	// note: this is not the most general way to deduce the bundle symbolic name from group id and artifact id, 
	// but it works for all apps considered here
	private static final void addApp(final Map<String, MavenArtifactProvisionOption> map, final String groupId, final String artifactId) {
		map.put(groupId + "." + artifactId, CoreOptions.mavenBundle(groupId,artifactId).version(ogemaVersion));
	}
	
	public AppsTestBase() {
		super();
	}
	
	public AppsTestBase(boolean includeTestBundle) {
		super(includeTestBundle);
	}

	@Override
	public Option[] frameworkBundles() {
		final Option[] superBundles = super.frameworkBundles();
		final Option[] allBundles = new Option[superBundles.length+FRAMEWORK_APPS.size()+ DEPENDENCIES.length];
		System.arraycopy(superBundles, 0, allBundles, 0, superBundles.length);
		System.arraycopy(FRAMEWORK_APPS.values().toArray(), 0, allBundles, superBundles.length, FRAMEWORK_APPS.size());
		System.arraycopy(DEPENDENCIES, 0, allBundles, superBundles.length+FRAMEWORK_APPS.size(), DEPENDENCIES.length);
		return allBundles;
	}
	
	protected final static AdminApplication getAppWithRetry(final String symbolicName, final AdministrationManager admin, final long timeoutMs) throws InterruptedException {
		int cnt = 0;
		final int sleepTime = 100;
		do {
			final AdminApplication app = getApp(symbolicName, admin.getAllApps());
			if (app != null)
				return app;
			Thread.sleep(sleepTime);
			
		} while (cnt++ < timeoutMs/sleepTime);
		return null;
	}

	protected final static AdminApplication getApp(final String symbolicName, final List<AdminApplication> allApps) {
		for (AdminApplication app: allApps) {
			if (symbolicName.equals(app.getBundleRef().getSymbolicName()))
				return app; 
		}
		return null;
	}

}
