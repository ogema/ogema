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
