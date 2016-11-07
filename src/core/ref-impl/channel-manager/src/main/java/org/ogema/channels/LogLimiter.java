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
package org.ogema.channels;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;

public class LogLimiter {

	static long LOG_SUPPRESSION_INTERVAL = 500; /* ms */

	private static final Timer timer = new Timer(true);

	/** timestamp of last printed log message */
	private long timestamp;
	
	/** number of suppressed log messages */
	private TimerTaskImpl timeout;
	
	/** the logger used to print the count */
	private Logger logger;

	LogLimiter(Logger logger) {
		this.logger = logger;
	} 
	

	boolean check() {
		
		long current = System.currentTimeMillis();
		
		// check if timeout has expired
		if (current - timestamp >= LOG_SUPPRESSION_INTERVAL) {
			timestamp = 0;
			timeout = null;
		}
 
		// if no timestamp is set, set timestamp and return true
		if (timestamp == 0) {
			timestamp = current;
			return true;
		} else {
			if (timeout == null) {
				timeout = new TimerTaskImpl();
				timer.schedule(timeout, LOG_SUPPRESSION_INTERVAL);
			}
			timeout.messageSuppressed();
			return false;
		}
	}
	
	private class TimerTaskImpl extends TimerTask {

		private long suppressedMessagesCount;
		
		synchronized void messageSuppressed() {
			suppressedMessagesCount++;
		}

		@Override
		synchronized public void run() {
			logger.warn("suppressed {} messages during {} ms.", suppressedMessagesCount, LOG_SUPPRESSION_INTERVAL);
		}
	}
}
