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
package org.ogema.exam;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.ExceptionListener;

/**
 * Can be used to transfer AssertionErrors in an application thread
 * to the test thread, so that the test fails in case of such 
 * errors.<br>
 * 
 * Usage: create an instance, register it at the beginning of the test (or the doBefore method) 
 * via {@see ApplicationManager#addExceptionListener(ExceptionListener)},
 * and call {@link #checkForExceptionsInOtherThreads(ApplicationManager)} at the end of the 
 * test. 
 *  
 * @author cnoelle
 */
public class ExceptionHandler implements ExceptionListener {

	private volatile AssertionError exception = null;
	
	@Override
	public void exceptionOccured(Throwable exception) {
		exception.printStackTrace();
		if (this.exception == null && exception instanceof AssertionError)  // keep the first exception, dismiss further ones
			this.exception = (AssertionError) exception;
	}
	
	public void reset() {
		this.exception = null;
	}

	public void checkForExceptionsInOtherThreads() throws AssertionError {
		if (exception != null) {
			System.out.println("Exception occured in other thread");
			throw exception;
		}
	}
	
}
