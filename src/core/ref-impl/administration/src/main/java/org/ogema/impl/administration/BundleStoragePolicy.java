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
/**
 * 
 */
package org.ogema.impl.administration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.ogema.accesscontrol.BundleStorage;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;

public class BundleStoragePolicy extends BundleTracker<Bundle> implements BundleStorage {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("ogema.administration");

	private String storagearea;

	boolean restrictFSAccess;

	private String osgiimplName;

	private BundleContext osgi;

	private int minStartLevel;

	private boolean ready;

	/*
	 * Size of storage each app in kB
	 */
	private String storageSize;

	static final String[] states = { "UNINSTALLED", "INSTALLED", "RESOLVED", "STARTING", "STOPPING", "ACTIVE" };
	static final String[] types = { "INSTALLED", "STARTED", "STOPPED", "UPDATED", "UNINSTALLED", "RESOLVED",
			"UNRESOLVED", "Starting", "STOPPING", "LAZY_ACTIVATION", "null" };

	private static final String KNOPFLERFISH_FW_SYMBOLIC_NAME = "org.knopflerfish.framework";

	private static final String EQUINOX_FW_SYMBOLIC_NAME = "org.eclipse.osgi";

	private static final String APACHE_FELIX_FW_SYMBOLIC_NAME = "org.apache.felix.framework";

	private static final String DEFAULT_STORAGE_SIZE = "100";

	private static final int MINIMUM_STORAGE_SIZE = 10;

	public BundleStoragePolicy(BundleContext context, int mask, BundleTrackerCustomizer<Bundle> cust) {
		super(context, mask, cust);
		osgi = context;
		initAppStorage();
	}

	public Bundle addingBundle(Bundle bundle, BundleEvent event) {
		log("adding tracking", bundle, event);
		if (event == null) {
			logger.debug("State change is not triggered by an event.");
			return null;
		}
		// Check if URP, than skip it if the case
		String name = bundle.getSymbolicName();
		boolean isURP = name == null ? false : name.equals("org.ogema.ref-impl.user-rights-proxy");
		BundleStartLevel level = bundle.adapt(BundleStartLevel.class);
		if (event.getType() == BundleEvent.UNINSTALLED) {
			log("Seems to be a bundle with to low start level", bundle, event);
			return null;
		}
		if (event.getType() == BundleEvent.UPDATED) {
			log("Bundle update ", bundle, event);
			return null;
		}
		int sl = level.getStartLevel();
		if (sl >= minStartLevel && !isURP) {
			if (event.getType() == BundleEvent.INSTALLED)
				constructStorageArea(bundle);
			return bundle;
		}
		else
			return null;
	}

	public void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {
		log("remove tracking ", bundle, event);
	}

	public void modifiedBundle(Bundle bundle, BundleEvent event, Bundle object) {
		log("modified tracking", bundle, event);
		if (event.getType() == BundleEvent.UNINSTALLED) {
			destructStorageArea(bundle);
			remove(bundle);
		}
	}

	void constructStorageArea(Bundle b) {
		// prepare storage area
		long id = b.getBundleId();
		runSystemProgram("sh", "./construct.storage.sh", getBundleDataDirectory((int) id), id + "", storageSize);
		ready = true;
	}

	void destructStorageArea(Bundle b) {
		// prepare storage area
		long id = b.getBundleId();
		runSystemProgram("sh", "./destruct.storage.sh", getBundleDataDirectory((int) id), id + "");
		ready = false;
	}

	private String getBundleDataDirectory(int id) {
		String result = null;
		switch (osgiimplName) {
		case "org.apache.felix.framework":
			result = storagearea + "bundle" + id + "/data";
			break;
		case "org.knopflerfish.framework":
			result = storagearea + "bs/" + id;
			break;
		default:
			break;
		}
		return result;
	}

	void runSystemProgram(String... command) {
		try {
			ProcessBuilder exec = new ProcessBuilder(command);
			exec = exec.redirectErrorStream(true);
			Process p = exec.start();
			InputStream is = p.getInputStream();

			String line;

			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			while ((line = in.readLine()) != null) {
				logger.debug("Execution output: " + line);
			}
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private void log(String action, Bundle bundle, BundleEvent event) {
		String symbolicName = bundle.getSymbolicName();
		int state = bundle.getState();
		String statestr = getIndexedString(bundle, states, state);
		int type = event == null ? 1024 : event.getType();
		String typestr = getIndexedString(bundle, types, type);
		logger.debug(action + symbolicName + ", state: " + statestr + ", event.type: " + typestr);
	}

	private String getIndexedString(Bundle bundle, String[] strings, int mask) {
		int count = 0;
		while (mask > 1) {
			mask >>= 1;
			count++;
		}
		return strings[count];
	}

	private void initAppStorage() {
		// Check if the restriction of file system access desired
		String restriction = System.getProperty("org.ogema.installation.restrictstorage");
		if (restriction != null && restriction.equals("true"))
			restrictFSAccess = true;
		else
			restrictFSAccess = false;

		if (restrictFSAccess) {
			String osname = System.getProperty("os.name");
			logger.debug("Determined oprating system is: " + osname);
			if (osname.toLowerCase().indexOf("linux") < 0) {
				restrictFSAccess = false; // currently only linux based systems are supported to restrict file system
				// access.
				logger.debug("Unsupported operating system, file access restriction disabled.");
			}
		}

		// If restriction is desired determine which storage location the OSGi framework uses.
		if (restrictFSAccess) {
			osgiimplName = osgi.getBundle(0).getSymbolicName();
			logger.debug("Determined OSGi implementation is: " + osgiimplName);
			this.storagearea = System.getProperty("org.osgi.framework.storage");
			switch (osgiimplName) {
			case APACHE_FELIX_FW_SYMBOLIC_NAME:
				if (this.storagearea == null)
					this.storagearea = "./felix-cache/";
				break;
			case KNOPFLERFISH_FW_SYMBOLIC_NAME:
				if (this.storagearea == null)
					this.storagearea = "./fwdir/bs/";
				break;
			case EQUINOX_FW_SYMBOLIC_NAME:
				if (this.storagearea == null)
					this.storagearea = "./fwdir/bs/";
				break;
			default:
				restrictFSAccess = false;
				break;
			}
		}

		if (restrictFSAccess) {
			// Determine the minimum start level of bundles that should be tracked
			String fromlevel = System.getProperty("org.ogema.installation.storage.fromlevel");
			try {
				this.minStartLevel = Integer.valueOf(fromlevel);
			} catch (NumberFormatException e) {
				this.minStartLevel = 1;
			}
			String size = System.getProperty("org.ogema.installation.storage.size");
			this.storageSize = size;
			try {
				int kB = Integer.valueOf(size);
				if (kB < MINIMUM_STORAGE_SIZE)
					size = "10";
			} catch (NumberFormatException e) {
				this.storageSize = DEFAULT_STORAGE_SIZE;
			}
		}
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public int getFree() {
		return 0;
	}
}
