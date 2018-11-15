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
package org.ogema.accesscontrol;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.ogema.core.application.AppID;
import org.osgi.service.useradmin.Authorization;
import org.slf4j.Logger;

public class SessionAuth implements HttpSessionBindingListener {

	private final Authorization auth;
	private final HttpSession ses;

	// TODO housekeeping
	private final Map<String, String> otpList;

		/**
		 * Returns a live view of the otp list
		 * @return
		 * 	a concurrent map
		 */
	public Map<String, String> getOtpList() {
		return otpList;
	}

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	// User usr;
	private final AccessManager accMngr;

	// public User getUsr() {
	// return usr;
	// }

	private static final Random rnd;

	public SessionAuth(Authorization auth, AccessManager accessManager, HttpSession ses) {
		this.auth = auth;
		this.ses = ses;
		// this.usr = user;
		this.otpList = new ConcurrentHashMap<>();
		this.accMngr = accessManager;
	}

	@Override
	public void valueBound(HttpSessionBindingEvent arg0) {
		// String id = ses.getId();
		// logger.info("Session binding event: bound " + id);
	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent arg0) {
		String id = ses.getId();
		logger.info("Session binding event: unbound {}", id);
		accMngr.logout(auth.getName());
	}

	public String registerAppOtp(AppID app) {
		/*
		 * Register each app and one time password. This is to be verified in RestAccess servlet.
		 */
		String appId = app.getIDString();
		String otp = otpList.get(appId);

		if (otp == null) {
			otp = Long.toHexString(rnd.nextLong());
			otpList.put(appId, otp);
		}
		return otp;
	}

	static {
		rnd = new SecureRandom();
	}

	public String getName() {
		return auth.getName();
	}
}
