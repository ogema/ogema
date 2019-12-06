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
package org.ogema.impl.security;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class LoginFailureInspector {
	// used to block IPs when multiple logins failed ... preventing brute force attacks.
	// ordinary the blocking should be done in a Filter but unfortunately using 
	// osgi HttpService and not extended version of Pax Web to abstract from implementation details...
	// other possibility would be to use felix ExtHttpService but with that we would depend on
	// an implementation... for now simply implementing it here:
	// NOTE: do not store in session because this can be easily faked.
	private static Map<String, IpLoginFailureInfo> loginFailureMap = new ConcurrentHashMap<String, IpLoginFailureInfo>();
	private static final int MAX_LOGIN_TRIES = 3;
	private static final long LOGIN_BLOCK_TIME = 60 * 1000 * 10;
	
	public boolean isUserBlocked(String ip) {
		IpLoginFailureInfo ipLoginFailureInfo = loginFailureMap.get(ip);
		if (ipLoginFailureInfo == null) {
			return false;
		}

		if (ipLoginFailureInfo.tries < MAX_LOGIN_TRIES) {
			return false;
		}

		long blockedUntil = (Long) ipLoginFailureInfo.timestamp + LOGIN_BLOCK_TIME;
		if (blockedUntil < System.currentTimeMillis()) {
			loginFailureMap.remove(ip);
			return false;
		}

		return true;
	}

	public String getRemainingBlockTime(String ip) {
		Long blockedUntil = (Long) loginFailureMap.get(ip).getTimestamp() + LOGIN_BLOCK_TIME;

		String result = null;
		long timeLeftForBlock = ((blockedUntil - System.currentTimeMillis()) / 1000);
		if (timeLeftForBlock >= 60) {
			result = timeLeftForBlock / 60 + " minutes";
		}
		else if (timeLeftForBlock > 0) {
			result = timeLeftForBlock + " seconds";
		}
		else {
			// negative? looks like we're stucked while executing and block time is over now ...
			result = "0 seconds";
		}
		return result;
	}

	public void loginFailed(String remoteAddress) {
		IpLoginFailureInfo failureInfo = loginFailureMap.get(remoteAddress);
		if (failureInfo != null) {
			failureInfo.tries++;
			failureInfo.timestamp = System.currentTimeMillis(); // use time of last try for block time
		}
		else {
			failureInfo = new IpLoginFailureInfo(System.currentTimeMillis(), 1);
			loginFailureMap.put(remoteAddress, failureInfo);
		}
	}

	// FIXME this should maybe happen in an own thread within a specific cycle to prevent an overflow.
	// It may help to create an own app for this or simply start a timer task from here... for now
	// a cleanup will only be invoked from the login servlet if a user accesses the 
	// login page or try to login (doGet or doPost)
	public void cleanUp() {
		Iterator<Entry<String, IpLoginFailureInfo>> it = loginFailureMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, IpLoginFailureInfo> next = it.next();
			if (next.getValue().timestamp + LOGIN_BLOCK_TIME < System.currentTimeMillis()) {
				it.remove();
			}
		}
	}

	class IpLoginFailureInfo {
		private long timestamp;
		private int tries;

		public IpLoginFailureInfo(long timestamp, int tries) {
			this.timestamp = timestamp;
			this.tries = tries;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		public int getTries() {
			return tries;
		}

		public void setTries(int tries) {
			this.tries = tries;
		}
	}
}
