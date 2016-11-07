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
package org.ogema.persistence.test;

import static java.lang.Math.PI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.impl.persistence.DBResourceIO;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator, Application {
	private ServiceRegistration<Application> me;

	public void start(BundleContext bc) throws IOException {
		me = bc.registerService(Application.class, this, null);
	}

	public void stop(BundleContext bc) throws IOException {
		me.unregister();
	}

	@Override
	public void start(ApplicationManager appManager) {
		populateDB(appManager);
	}

	@Override
	public void stop(AppStopReason reason) {
	}

	static final String text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
			+ "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,"
			+ " sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum."
			+ " Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
			+ " Lorem ipsum dolor sit amet, consetetur sadipscing elitr,"
			+ " sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, "
			+ "sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum."
			+ " Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
	StringResource strRes;
	static final String strResName = "TestString";

	static String[] strArr = { text, text, text, text, text, text, text, text, text, text, text, text, text, text, text,
			text, text, text, text, text, text, text, text, text, text, text, text, text, text, text, text, text, text,
			text, text, text, text, text, text };
	StringArrayResource strArrRes;
	static final String strArrResName = "TestStringArray";

	BooleanResource boolRes;
	static final String boolResName = "TestBooleanResource";

	FloatResource floatRes;
	private float floatValue = (float) PI;
	static final String floatResName = "TestFloatResource";

	void populateDB(ApplicationManager appMngr) {
		ResourceAccess ra = appMngr.getResourceAccess();

		// check if its the first run
		File f = new File(appMngr.getDataFile("") + "/.lock");
		if (f.exists()) {
			if (!checkResources(ra))
				DBResourceIO.copyFiles();
		}
		else {
			try {
				FileOutputStream fos = new FileOutputStream(f);
				fos.getFD().sync();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			init(appMngr.getResourceManagement());
		}

		while (true) {
			strRes.setValue(text);
			strArrRes.setValues(strArr);
			boolRes.setValue(true);
			floatRes.setValue(floatValue);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

	private void init(ResourceManagement resMngr) {
		strRes = resMngr.createResource(strResName, StringResource.class);
		strArrRes = resMngr.createResource(strArrResName, StringArrayResource.class);
		boolRes = resMngr.createResource(boolResName, BooleanResource.class);
		floatRes = resMngr.createResource(floatResName, FloatResource.class);
	}

	private boolean checkResources(ResourceAccess ra) {
		// Check String resource
		strRes = ra.getResource(strResName);
		if (strRes == null || !strRes.getValue().equals(text)) {
			return false;
		}

		// Check string array
		strArrRes = ra.getResource(strArrResName);
		if (strArrRes == null)
			return false;
		String[] strArr2 = strArrRes.getValues();
		int len = strArr.length;
		int count = 0;
		while (len > count) {
			if (!strArr[count].equals(strArr2[count]))
				return false;
			count++;
		}

		// check boolean resource
		boolRes = ra.getResource(boolResName);
		if (boolRes == null || !boolRes.getValue())
			return false;

		// check float resource
		floatRes = ra.getResource(floatResName);
		if (floatRes == null || floatRes.getValue() != floatValue)
			return false;
		return true;
	}

}
