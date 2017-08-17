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
package org.ogema.rest.importer;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

/**
 * Imports resources from files at (clean) system start. The file content
 * must conform to the OGEMA REST specification. Both XML and JSON content
 * is allowed, the file ending (.xml or .json) is used for type identification.
 * <br>
 * The import directory is determined by the system property "org.ogema.rest.input.dir",
 * default is RUNDIR/config/restInit. // FIXME security settings do not allow a configurable path?
 * 
 * @author cnoelle
 */
@Component(specVersion = "1.2")
@Service(Application.class)
public class Importer implements Application {

	protected static final String DEFAULT_DIR = "config/restInit";
	protected static final String IN_DIR_PROP = "org.ogema.rest.input.dir";

	private ApplicationManager appman;
	private OgemaLogger logger;

	//	@Reference
	//	private AdministrationManager adminMan;

	@Override
	public void start(ApplicationManager appManager) {
		appman = appManager;
		logger = appman.getLogger();
		logger.info("Importer app starting");
		(new InitialImport(appManager)).startImport();
		//		String appId = appManager.getAppID().getIDString();
		//		AdminApplication app = adminMan.getAppById(appId);
		//		app.remove();  // TODO doesn't do anything at the moment
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					FrameworkUtil.getBundle(Importer.class).uninstall(); // app uninstalls itself after initial import
				} catch (BundleException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void stop(AppStopReason reason) {
		if (logger != null)
			logger.info("Uninstalling Importer application");
		appman = null;
		logger =  null;
	}

}
