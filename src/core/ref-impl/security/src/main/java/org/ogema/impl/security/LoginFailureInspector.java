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
package org.ogema.impl.security;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ogema.impl.security.LoginFailureInspector.IpLoginFailureInfo;

public class LoginFailureInspector {
	// used to block IPs when multiple logins failed ... preventing brute force attacks.
	// ordinary the blocking should be done in a Filter but unfortunately using 
	// osgi HttpService and not extended version of Pax Web to abstract from implementation details...
	// other possibility would be to use felix ExtHttpService but with that we would depend on
	// an implementation... for now simply implementing it here:
	// NOTE: do not store in session because this can be easily faked.
	private static Map<String, IpLoginFailureInfo> loginFailureMap = new HashMap<String, IpLoginFailureInfo>();
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
