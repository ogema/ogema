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
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
package org.ogema.app.fileaccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.slf4j.Logger;

public class Activator implements BundleActivator, Application {
	private static final byte[] bytes = "Das ist eine Zeile in einem Textfile, das nur dazu da ist, als Testobjekt für die Einschränkung beim Filesystemzugriff zu dienen."
			.getBytes();
	public static boolean bundleIsRunning = true;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("ogema.testapp.fileaccess");

	public void start(BundleContext bc) throws IOException {
		bc.registerService(Application.class, this, null);
		// check which osgi implementation is there
		String osgiimplName = bc.getBundle(0).getSymbolicName();
		logger.info("determined OSGi implementation is: " + osgiimplName);
		// check if the access to default osgi cache directory is restricted, in case of overwriting the storage
		// location with org.osgi.framework.storage
		defaultStorageLocationAccessTest(bc);
		// restricted file access test
		restrictedfileaccesstest(bc);
	}

	private void defaultStorageLocationAccessTest(BundleContext bc) {
		File f = bc.getDataFile("defaultfile");
		String path = f.getAbsolutePath();
		String storagearea = System.getProperty("org.osgi.framework.storage");
		if (storagearea == null) {
			logger.info("Storage area defaults to " + path);
			logger.info("The test will be aborted.");
		}
		else {
			logger.info("Storage area is overwritten to " + path);
			File f1 = new File("./felix-cache/bundle" + bc.getBundle().getBundleId() + "/data", "defaultfile");
			try {
				FileOutputStream fos1 = new FileOutputStream(f1);
			} catch (Throwable e) {
				if (!(e instanceof SecurityException)) {
					logger.info("Test failed, SecurityException is expected.");
					e.printStackTrace();
				}
				else
					logger.info("As expected access to default implementation storage is protected, when org.osgi.framework.storage is set to an other location.");
			}
		}
		try {
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(48);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void restrictedfileaccesstest(BundleContext bc) {
		File f = bc.getDataFile("myData.txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		int linelen = bytes.length;
		int counter = 0;
		while (true) {
			try {
				fos.write(bytes);
				counter += linelen;
				if (counter > 1024 * 1024 * 100 + 1024)
					break;
			} catch (Throwable e) {
				break;
			}

		}
		if (counter > 1024 * 1024 * 100)
			logger.info("No restriction for file system access detected.");
		else
			logger.info("Restricted file sytem access detected, written bytes: " + counter);
	}

	public void stop(BundleContext bc) throws IOException {
		bundleIsRunning = false;
	}

	@Override
	public void start(ApplicationManager appManager) {
		File file = appManager.getDataFile("resources.graph");
		try {
			PrintWriter writer = new PrintWriter(file, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void stop(AppStopReason reason) {
	}

}
