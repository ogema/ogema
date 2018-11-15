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
