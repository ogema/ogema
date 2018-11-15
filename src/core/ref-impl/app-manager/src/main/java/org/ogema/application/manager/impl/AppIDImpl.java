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
package org.ogema.application.manager.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.Constants;
import org.ogema.accesscontrol.RedirectionURLHandler;
import org.ogema.accesscontrol.SessionAuth;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

/**
 * @author Zekeriya Mansuroglu
 *
 */
public class AppIDImpl implements AppID {

	/*
	 * Bundle reference of the owner application
	 */
	Bundle bundle;
	/*
	 * Owner application reference
	 */
	Application app;
	/*
	 * Id string
	 */
	String id;

	String group, user, version;
	
	volatile boolean active;

	@Override
	public String getIDString() {
		return id;
	}

	UnsupportedOperationException uoe = new UnsupportedOperationException(
			"group, user and version are not yet supported.");

	static AppIDImpl getNewID(Application app) {
		AppIDImpl result = null;
		ClassLoader cl = app.getClass().getClassLoader();
		if (cl instanceof BundleReference) {
			result = new AppIDImpl();
			BundleReference ref = (BundleReference) cl;
			result.bundle = ref.getBundle();
			result.app = app;
			result.id = createID(ref.getBundle(), app);
		}
		return result;
	}

	static final AtomicInteger instanceCounter = new AtomicInteger();

	
	private static String createID(Bundle bundle, Application app) {
		String loc = bundle.getLocation();
		int begin = loc.lastIndexOf('/') + 1;
		int end = loc.lastIndexOf('.');
		if (begin > end)
			end = loc.length();
//		String appName = app.getClass().getSimpleName();
		return String.format("[%d]_%s@%d", instanceCounter.incrementAndGet(), app.getClass().getName(), bundle.getBundleId(),
				bundle.getLocation());
		//.substring(begin, end));// loc.substring(begin, end) + "_" + bundle.getBundleId();
		//return String.format("%s@%d/%s", appName, bundle.getBundleId(), loc.substring(begin, end));
	}

	static AppIDImpl getNewID(Bundle b, Application app) {
		AppIDImpl result = null;
		result = new AppIDImpl();
		result.bundle = b;
		result.app = app;
		result.id = createID(b, app);
		return result;
	}
	
	void close() {
		this.active = false;
	}

	@Override
	public String getLocation() {
		return AccessController.doPrivileged(new PrivilegedAction<String>() {
			public String run() {
				return bundle.getLocation();
			}
		});
	}

	@Override
	public Bundle getBundle() {
		return bundle;
	}

	@Override
	public Application getApplication() {
		return app;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof AppID))
			return false;
		AppID id = (AppID) o;
		if (id.getIDString().equals(this.getIDString()))
			return true;
		return false;
	}

	@Override
	public String toString() {
		//return "AppIDImpl{" + "bundle=" + bundle + ", app=" + app + '}';
		return id;
	}

	@Override
	public String getOwnerGroup() {
		throw uoe;
	}

	@Override
	public String getOwnerUser() {
		throw uoe;
	}

	@Override
	public String getVersion() {
		throw uoe;
	}
	@Override
	public URL getOneTimePasswordInjector(String name, HttpSession ses) {
		// Get the corresponding session authorization
		// String otp = registerOTP(owner, sesid);
		SessionAuth auth = (SessionAuth) ses.getAttribute(Constants.AUTH_ATTRIBUTE_NAME);
		if (auth == null)
			return null;

		String otp = auth.registerAppOtp(this);
		if (otp == null)
			return null;

		URL url = null;
		try {
			url = new URL("ogema", getIDString(), 0, name, new RedirectionURLHandler(this, name, otp));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return url;
	}
	
	@Override
	public boolean isActive() {
		return active;
	}
}
